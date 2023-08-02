package com.hc360.rsf.rpc.protocol.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.hc360.rsf.common.serialize.ObjectInput;
import com.hc360.rsf.common.serialize.ObjectOutput;
import com.hc360.rsf.common.serialize.Serialization;
import com.hc360.rsf.common.utils.StringUtils;
import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.SerializableException;

/**
 * TransportCodec
 * 
 *无协议
 *粗粒度传输，把Request/Response对象整个序列化传输。
 *
 */
public class TransportCodec extends AbstractCodec {

    /**
     * 编码  使用OutputStream写出message
     * 
     * @param channel  channel参数没有用
     * @param output
     * @param message
     * @throws IOException
     * @see com.hc360.rsf.rpc.protocol.codec.Codec#encode(com.hc360.rsf.remoting.Channel, java.io.OutputStream, java.lang.Object)
     */
    public void encode(Channel channel, OutputStream output, Object message) throws IOException {
    	/**
    	 * 从AbstractCodec类中取出取得序列化工具
    	 */
    	Serialization serialization=getSerialization(channel);
    	
    	//URL参数没有使用
    	ObjectOutput objectOutput = serialization.serialize(null, output);
        
        //使用objectOutput写出message
        //channel参数没有用
        encodeData(channel, objectOutput, message);
        objectOutput.flushBuffer();
    }
    /**
     * 解码
     * 
     * @param channel channel参数没有用
     * @param input
     * @return
     * @throws IOException
     * @see com.hc360.rsf.rpc.protocol.codec.Codec#decode(com.hc360.rsf.remoting.Channel, java.io.InputStream)
     */
    public Object decode(Channel channel, InputStream input) throws IOException {
        return decodeData(channel, getSerialization(channel).deserialize(null, input));
    }
    
    

    protected void encodeData(Channel channel, ObjectOutput output, Object message) throws IOException {
        encodeData(output, message);
    }

    protected Object decodeData(Channel channel, ObjectInput input) throws IOException {
        return decodeData(input);
    }

    protected void encodeData(ObjectOutput output, Object message) throws IOException {
    	try{
    		output.writeObject(message);
    	}catch(IOException e){
			throw new SerializableException("序列化异常,请检查被传输对象和对象的成员是否实现了Serializable接口："+StringUtils.toString(e));
		}
    }

    protected Object decodeData(ObjectInput input) throws IOException {
        try {
            return input.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("ClassNotFoundException: " + StringUtils.toString(e));
        }
    }
}
