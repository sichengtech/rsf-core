package com.hc360.rsf.rpc.protocol.codec.mina;
import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.hc360.rsf.common.Constants;
import com.hc360.rsf.common.URL;
import com.hc360.rsf.common.io.Bytes;
import com.hc360.rsf.common.io.UnsafeByteArrayInputStream;
import com.hc360.rsf.common.io.UnsafeByteArrayOutputStream;
import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.HandlerDelegate;
import com.hc360.rsf.rpc.protocol.codec.Codec;
import com.hc360.rsf.remoting.transport.mina.MinaChannel;

/**
 * Mina的编码工厂
 * 
 * @author zhaolei 2012-5-31
 */
public final class MinaCodecAdapter implements ProtocolCodecFactory {

    private static final String   BUFFER_KEY          = MinaCodecAdapter.class.getName() + ".BUFFER";

    private final ProtocolEncoder encoder            = new InternalEncoder();

    private final ProtocolDecoder decoder            = new InternalDecoder();

    private final Codec           codec;

    private final int            bufferSize;
    
    /**
     * 构造方法
     * 
     * @param codec
     * @param url
     * @param handler
     */
    public MinaCodecAdapter(Codec codec, URL url, HandlerDelegate handler){
        this.codec = codec;
        int b = url.getPositiveParameter(Constants.BUFFER_KEY, Constants.DEFAULT_BUFFER_SIZE);
        this.bufferSize = b >= Constants.MIN_BUFFER_SIZE && b <= Constants.MAX_BUFFER_SIZE ? b : Constants.DEFAULT_BUFFER_SIZE;
    }
    
    /**
     * 构造方法
     * 
     * @param upstreamCodec
     * @param downstreamCodec
     * @param url
     * @param handler
     */
    public MinaCodecAdapter(Codec upstreamCodec, Codec downstreamCodec, URL url, HandlerDelegate handler){
        this.codec = upstreamCodec;
        int b = url.getPositiveParameter(Constants.BUFFER_KEY, Constants.DEFAULT_BUFFER_SIZE);
        this.bufferSize = b >= Constants.MIN_BUFFER_SIZE && b <= Constants.MAX_BUFFER_SIZE ? b : Constants.DEFAULT_BUFFER_SIZE;
    }

    public ProtocolEncoder getEncoder(IoSession iosession)throws Exception{
        return encoder;
    }

    public ProtocolDecoder getDecoder(IoSession iosession)throws Exception{
        return decoder;
    }

    /**
     * 内部编码器
     * 
     * @author zhaolei 2011-6-1
     */
    private class InternalEncoder implements ProtocolEncoder {
        public void dispose(IoSession session) throws Exception { }
        public void encode(IoSession session, Object msg, ProtocolEncoderOutput out) throws Exception {
        	//这个Byte数组是重点
            UnsafeByteArrayOutputStream os = new UnsafeByteArrayOutputStream(1024); 
            //使用Mina 的session创建一个MinaChannel
            MinaChannel channel = MinaChannel.getOrAddChannel(session);
            try {
            	codec.encode(channel, os, msg);
            } finally {
                MinaChannel.removeChannelIfDisconnectd(session);
            }
            //被序列化的数据,协议头+数据体,都在os对象中
            out.write(IoBuffer.wrap(os.toByteArray()));
            out.flush();
        }
    }

    /**
     * 内部解码器
     * 
     * @author zhaolei 2011-6-1
     */
    private class InternalDecoder implements ProtocolDecoder {

        public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
            int readable = in.limit();
            if (readable <= 0) return;

            int off, limit;
            byte[] buf;
            // load buffer from context.
            Object[] tmp = (Object[]) session.getAttribute(BUFFER_KEY);
            if (tmp == null) {
                buf = new byte[bufferSize];//8192
                off = limit = 0;
            } else {
                buf = (byte[]) tmp[0];
                off = (Integer) tmp[1];
                limit = (Integer) tmp[2];
            }

            Channel channel = MinaChannel.getOrAddChannel(session);
            boolean remaining = true;
            Object msg;
            //这个Byte数组是重点
            //要反序列化的数据,都在bis对象中
            UnsafeByteArrayInputStream bis;
            try {
                do {
                    // read data into buffer.
                    int read = Math.min(readable, buf.length - limit);
                    in.get(buf, limit, read);//从输入流中读数据,最多一次可读8192字节
                    limit += read;
                    readable -= read;
                    bis = new UnsafeByteArrayInputStream(buf, off, limit - off); // 不需要关闭
                    // decode object.
                    do {
                        try {
                            msg = codec.decode(channel, bis);
                        } catch (IOException e) {
                            remaining = false;
                            throw e;
                        }
                        if (msg == Codec.NEED_MORE_INPUT) {
                            if (off == 0) {
                                if (readable > 0) {
                                    buf = Bytes.copyOf(buf, buf.length << 1);//现有长度*2
                                }
                            } else {
                                int len = limit - off;
                                System.arraycopy(buf, off, buf, 0, len);
                                off = 0;
                                limit = len;
                            }
                            break;
                        } else {
                            int pos = bis.position();
                            if (pos == off) {
                                remaining = false;
                                throw new IOException("Decode without read data.");
                            }
                            if (msg != null) {
                                out.write(msg);
                            }
                            off = pos;
                        }
                    } while (bis.available() > 0);
                } while (readable > 0);
            } finally {
                if (remaining) {
                    int len = limit - off;
                    if (len < buf.length / 2) {
                        System.arraycopy(buf, off, buf, 0, len);
                        off = 0;
                        limit = len;
                    }
                    session.setAttribute(BUFFER_KEY, new Object[] { buf, off, limit });
                } else {
                    session.removeAttribute(BUFFER_KEY);
                }
                MinaChannel.removeChannelIfDisconnectd(session);
            }
        }

        public void dispose(IoSession session) throws Exception { }

        public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception { }
    }
}
