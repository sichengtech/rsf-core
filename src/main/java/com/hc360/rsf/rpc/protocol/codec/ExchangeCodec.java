package com.hc360.rsf.rpc.protocol.codec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hc360.rsf.common.Constants;
import com.hc360.rsf.common.io.Bytes;
import com.hc360.rsf.common.io.StreamUtils;
import com.hc360.rsf.common.io.UnsafeByteArrayInputStream;
import com.hc360.rsf.common.io.UnsafeByteArrayOutputStream;
import com.hc360.rsf.common.serialize.ObjectInput;
import com.hc360.rsf.common.serialize.ObjectOutput;
import com.hc360.rsf.common.serialize.Serialization;
import com.hc360.rsf.common.serialize.support.dubbo.DubboSerialization;
import com.hc360.rsf.common.serialize.support.hessian.Hessian2Serialization;
import com.hc360.rsf.common.serialize.support.java.JavaSerialization;
import com.hc360.rsf.common.utils.AuthHelperProxy;
import com.hc360.rsf.common.utils.IOUtils;
import com.hc360.rsf.common.utils.StringUtils;
import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.EncryptException;
import com.hc360.rsf.remoting.SerializableException;
import com.hc360.rsf.remoting.exchange.support.Request;
import com.hc360.rsf.remoting.exchange.support.Response;
import com.hc360.rsf.remoting.exchange.support.DefaultFuture;

/**
 * rsf协议-包头实现类--平台无关协议
 * 实现了16字节的rsf协议头，与具体语言平台无关。
 * 把Request/Response对象用协议头传输。
 * 
 */
public class ExchangeCodec extends TelnetCodec {

    private static final Logger     logger             = LoggerFactory.getLogger(ExchangeCodec.class);
    // 协议头总长度
    protected static final int      HEADER_LENGTH      = 16;
    // 协议头，魔法数据头 
    protected static final short    MAGIC              = (short) 0xdabb; //二进制 11011010 10111011
    // 协议头--高位
    protected static final byte     MAGIC_HIGH         = Bytes.short2bytes(MAGIC)[0]; 
    // 协议头--低位
    protected static final byte     MAGIC_LOW          = Bytes.short2bytes(MAGIC)[1];
    // request/response标志
    protected static final byte     FLAG_REQUEST       = (byte) 0x80;//128 二进制 10000000
    // 双向通信标志
    protected static final byte     FLAG_TWOWAY        = (byte) 0x40;//64   二进制01000000
    // 心跳事件标志
    protected static final byte     FLAG_EVENT		= (byte) 0x20;//32       二进制00100000
    // 握手标志  
    protected static final byte     FLAG_SHAKEHANDS		= (byte) 0x10;//16       二进制00010000
    // 加密标志 
    protected static final byte     FLAG_SECURITY		= (byte) 0x8;//8       二进制00001000
    // 支持序列化方式（8种）
    protected static final int      SERIALIZATION_MASK = 0x7;//7          二进制 00000111

    /**
     * RSF协议最多支持8种序列化方式
     * 把支持的多种序列化方式，都放入Map中，key是serialization.getContentTypeId()的返回值 
     * 
     * 由客户端决定使用哪种序列化方式，一个请求从客户端到达服务端后，rsf协议头中记录了使用了哪一种序列化方式，
     * 服务端也使用同一种序列化方式进行  反序列化。
     * 
     */
    private static Map<Byte, Serialization> ID_SERIALIZATION_MAP   = new HashMap<Byte, Serialization>();
    static {
    	Serialization serialization_Dubbo = new DubboSerialization();
    	Serialization serialization_Hessian2 = new Hessian2Serialization();
		Serialization serialization_Java = new JavaSerialization();
		Serialization[] arr=new Serialization[]{serialization_Dubbo,serialization_Hessian2,serialization_Java};
        for (Serialization serialization : arr) {
            byte idByte = serialization.getContentTypeId();
            if (ID_SERIALIZATION_MAP.containsKey(idByte)) {
                logger.error("Serialization extension " + serialization.getClass().getName()
                             + " has duplicate id to Serialization extension "
                             + ID_SERIALIZATION_MAP.get(idByte).getClass().getName()
                             + ", ignore this Serialization extension");
                continue;
            }
            ID_SERIALIZATION_MAP.put(idByte, serialization);
        }
    }

    public Short getMagicCode() {
        return MAGIC;
    }

    /**
     * 编码 encode
     */
    public void encode(Channel channel, OutputStream os, Object msg) throws IOException {
        if (msg instanceof Request) {
        	//请求
        	//使用OutputStream os把 equest req写出
        	//channel参数没大用
            encodeRequest(channel, os, (Request) msg);
        } else if (msg instanceof Response) {
        	//应答
            encodeResponse(channel, os, (Response) msg);
        } else {
        	//走父类的方法
            super.encode(channel, os, msg);
        }
    }

   
    /**
     * 解码
     * 
     * @param channel Channel
     * @param is  是UnsafeByteArrayInputStream类型
     * @return 结果
     * @throws IOException
     * @see com.hc360.rsf.rpc.protocol.codec.TelnetCodec#decode(com.hc360.rsf.remoting.Channel, java.io.InputStream)
     */
    public Object decode(Channel channel, InputStream is) throws IOException {
    	int readable = is.available();
        byte[] header = new byte[Math.min(readable, HEADER_LENGTH)];
        is.read(header);
        return decode(channel, is, readable, header);
    }
    /**
     * 解码--重载
     */
    protected Object decode(Channel channel, InputStream is, int readable, byte[] header) throws IOException {
        // check magic number. 
    	// readable是可读入的字节数
    	
    	//判断是不是数据的后续部分
        if (readable > 0 && header[0] != MAGIC_HIGH 
                || readable > 1 && header[1] != MAGIC_LOW) {
            int length = header.length;
            if (header.length < readable) {
                header = Bytes.copyOf(header, readable);
                is.read(header, length, readable - length);
            }
            for (int i = 1; i < header.length - 1; i ++) {
                if (header[i] == MAGIC_HIGH && header[i + 1] == MAGIC_LOW) {
                    UnsafeByteArrayInputStream bis = ((UnsafeByteArrayInputStream) is);
                    bis.position(bis.position() - header.length + i);
                    header = Bytes.copyOf(header, i);
                    break;
                }
            }
            return super.decode(channel, is, readable, header);
        }
        // check length.
        if (readable < HEADER_LENGTH) {
            return NEED_MORE_INPUT;//须要更多的数据输入
        }

        // get data length.
        //通过分析协议头,取出数据包体长度
        int len = Bytes.bytes2int(header, 12);
        
        int all_length = len + HEADER_LENGTH;
        if( readable < all_length ) {
            return NEED_MORE_INPUT;//须要更多的数据输入
        }

        // limit input stream.
        if( readable != all_length ){
            is = StreamUtils.limitedInputStream(is, len);
        }

        //从数据头中取出 反序列化的方式
        byte flag = header[2];
        byte proto = (byte)( flag & SERIALIZATION_MASK );
        Serialization s = getSerializationById(proto);
        if (s == null) {
            s = getSerialization(channel);
        }
        //////////////////////
        // 解密(有加密标志) 
        //////////////////////
        if (( flag & FLAG_SECURITY ) != 0){
        	 //从输入流取出Byte数组
        	UnsafeByteArrayOutputStream  baos=new UnsafeByteArrayOutputStream (len);
        	IOUtils.write(is, baos);
        	byte[] data=baos.toByteArray();
        	
        	
        	String session_key=(String)channel.getAttribute(Constants.SESSION_KEY);
        	if(session_key==null){
        		String msg="加密通信- session_key=null";
        		logger.error(msg);
        		throw new EncryptException(msg);
        	}
        	try {
        		//解密
        		long t1=System.currentTimeMillis();
        		data=AuthHelperProxy.sessionDecrypt(session_key, data);
				long t2=System.currentTimeMillis();
				if( ( flag & FLAG_REQUEST ) == 0 ) {
					logger.info("加密通信-  对Response解密，耗时："+(t2-t1)+"ms.channel:"+channel);
				}else{
					logger.info("加密通信-  对Request解密，耗时："+(t2-t1)+"ms.channel:"+channel);
				}
				
				is=new UnsafeByteArrayInputStream(data);
			} catch (Exception e) {
				String msg="加密通信-解密时发生异常.channel:"+channel;
        		logger.error(msg,e);
				throw new EncryptException(msg,e);
			}
        }
        //////////////////////
    	// 反序列化     数据包体//
        //////////////////////
        ObjectInput in = s.deserialize(null, is);
        
        //根据请求头判断是Request还是Response
        if( ( flag & FLAG_REQUEST ) == 0 ) {
        	
            //////////////////////
        	// 对Response解码         //
            //////////////////////
        	long id = Bytes.bytes2long(header, 4);// get Response id.
            Response res = new Response(id);
            // 数据长度
            res.setSize(len);
            // 心跳事件标志
            if (( flag & FLAG_EVENT ) != 0){
                res.setEvent(Response.HEARTBEAT_EVENT);
            }
            // 握手标志
            if (( flag & FLAG_SHAKEHANDS ) != 0){
            	res.setShakehands(true);
            }
            // 加密标志 
            if (( flag & FLAG_SECURITY ) != 0){
            	res.setSecurity(true);
            }
            // get status.
            byte status = header[3];
            res.setStatus(status);
            if( status == Response.OK ) {
                try {
                    Object data;
                    if (res.isHeartbeat() || res.isEvent() || res.isShakehands()) {
                        data = decodeEventData(channel, in);
                    } else {
                        data = decodeResponseData(channel, in, getRequestData(id));
                    }
                    res.setData(data);
                } catch (Throwable t) {
                    res.setStatus(Response.CLIENT_ERROR);
                    res.setErrorMsg(StringUtils.toString(t));
                }
            } else {
                res.setErrorMsg(in.readUTF());
            }
            return res;
        } else {
            //////////////////////
        	// 对Request解码         //
            //////////////////////
        	long id = Bytes.bytes2long(header, 4);// get request id.
            Request req = new Request(id);  
            req.setSize(len);
            //req.setVersion("2.0.0");
            
            // 双向通信标志
            if(( flag & FLAG_TWOWAY ) != 0){
            	req.setTwoWay(true);
            }else{
            	req.setTwoWay(false);
            }
            // 心跳事件标志
            if (( flag & FLAG_EVENT ) != 0 ){
                req.setEvent(Request.HEARTBEAT_EVENT);
            }
            // 握手标志  
            if (( flag & FLAG_SHAKEHANDS ) != 0 ){
            	req.setShakehands(true);
            }
            // 加密标志 
            if (( flag & FLAG_SECURITY ) != 0 ){
            	req.setSecurity(true);
            }
            
//            try{
//                //检查有没有超载
//            	//int len2 = Bytes.bytes2int(header, 12);
//                checkPayload(channel, len);
//        	}catch(Exception e){
//            	// S端发生异常时，也要响应C端,不然C端只能等待超时了。
//    			// S端发生的异常有两类
//    			// 第一类是业务异常，第二类是RSF本身异常，如线程池耗尽等等。
//    			// 本段代码，就是针对：第二类RSF本身异常 的处理，通知C端 S端发生异常了，不要再等待了。
//    			AbstractCodec.errorProce_ClientNotWait(req, channel, e,Response.DATA_TOO_LENGTH);
//    			//注意这里，后面的逻辑会处理is Broken 
//    			req.setBroken(true);
//        	}
            
            try {
                Object data;
                if (req.isHeartbeat() || req.isEvent() || req.isShakehands()) {
                    data = decodeEventData(channel, in);
                } else {
                	//取出RpcInvcation,因为是请求,对象一定是RpcInvcation类型
                    data = decodeRequestData(channel, in);
                }
                req.setData(data);
            } catch (Throwable t) {
            	//注意这里，后面的逻辑会处理is Broken 
                req.setBroken(true);
                req.setData(t);
            }
            return req;
        }
    }

    protected Object getRequestData(long id) {
        DefaultFuture<?> future = DefaultFuture.getFuture(id);
        if (future == null){
            return null;
        }
        Request req = future.getRequest();
        if (req == null){
            return null;
        }
        return req.getData();
    }

    /**
     * 该方法，被本类中的 encode（编码）方法调用
     * 使用OutputStream os把Request req写出
     * channel参数没大用
     * 
     * @param channel
     * @param os 是UnsafeByteArrayOutputStream类型
     * @param req
     * @throws IOException
     */
    protected void encodeRequest(Channel channel, OutputStream os, Request req) throws IOException {
    	//选用一个序列化的实现
        Serialization serialization = getSerialization(channel);
        
        // 16字节协议头 ，长度为16字节的空byte数组
        byte[] header = new byte[HEADER_LENGTH];

        ////////////////////////
        //第1-2字节，共2个字节   //
        ////////////////////////
        // MAGIC是魔法数字头，占两个字节
        // MAGIC是魔法数字头的内容是 (short) 0xdabb;  十进制值是：-9541 ,二进制值是： 11011010 10111011
        Bytes.short2bytes(MAGIC, header);

        ////////////////////////
        //第3字节，共1个字节        //
        ////////////////////////
        
        // FLAG_REQUEST十六进制值是：(byte)0x80,二进制值是：10000000.
        // serialization.getContentTypeId()序列化方式值是：00000001,不同的序列实现值不同,最多占4bit可表示16种实现.
        // 按位 或运算后二进制值是： 10000001
        header[2] = (byte) (FLAG_REQUEST | serialization.getContentTypeId());//request/response标志+序列化实现方法
        if (req.isTwoWay()) header[2] |= FLAG_TWOWAY; //64 二进制01000000    双向标志
        if (req.isEvent()) header[2] |= FLAG_EVENT; //32 二进制00100000    心跳事件标志
        // 走到这里  header[2]的二进制值是 11100010
        //System.out.println(Byte.toString(header[2]));
        
        if (req.isShakehands()) header[2] |=FLAG_SHAKEHANDS;//16 二进制00010000  握手标志  
        if (req.isSecurity()) header[2] |=FLAG_SECURITY;//8      二进制00001000  加密标志 
        // 走到这里  header[2]的二进制值是 11111010
        
        ////////////////////////
        //第4字节，共1个字节        //
        ////////////////////////
        // header[3] 在请求时，这里是空的，是00000000
        
        ////////////////////////
        //第5-12字节，共8个字节//
        ////////////////////////
        // 设置请求 id.
        Bytes.long2bytes(req.getId(), header, 4);

        // encode request data.
        UnsafeByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(1024);
        
    	//把bos (OutputStream)转换为ObjectOutput
    	//URL参数没有用
        //是new GenericObjectOutput(out),用于序列化对象
        ObjectOutput out = serialization.serialize(null, bos);
        
        //处理序列化异常
        try{
	        if (req.isEvent()  || req.isShakehands()) {
	        	//encodeEventData方法本质是out.writeObject(data),没有实质内容
	        	//在子类DubboCodec的encodeEventData方法中才有实际功能的实现
	            encodeEventData(channel, out, req.getData());
	        } else {
	        	//这里本质是out.writeObject(data),没有实质内容
	        	//在子类JavaCodec中才有实际功能的实现
	            encodeRequestData(channel, out, req.getData());
	        }
        }catch(SerializableException e){
        	//客户端发生了序列化异常,所以通知客户端异常了别再等待了。
        	//构造Response对象,返回给客户端，打断客户端的阻塞，让客户端抛出异常
        	Response response = new Response(req.getId(), req.getVersion());
            response.setData(e);// 把异常放入，供客户端使用
            response.setStatus(Response.CLIENT_SERIALIZABLE_ERROR);
            //response.setErrorMsg(StringUtils.toString(e));
            //打断客户端的阻塞，让客户端抛出异常
            DefaultFuture.received(channel, response);
        }
        out.flushBuffer();//必要
        bos.flush();
        bos.close();
        
        //被序列化后的数据
        byte[] data = bos.toByteArray();
        
        // 把上面的data[] 加密
        if (req.isSecurity()) {
        	String session_key=(String)channel.getAttribute(Constants.SESSION_KEY);
        	if(session_key==null){
        		String msg="加密通信- session_key=null";
        		logger.error(msg);
        		throw new EncryptException(msg);
        	}
        	try {
        		//加密
        		long t1=System.currentTimeMillis();
        		if(data!=null){
        			data=AuthHelperProxy.sessionEncrypt(session_key, data);
        		}
    			long t2=System.currentTimeMillis();
				logger.info("加密通信-  对Request加密，耗时："+(t2-t1)+"ms.channel:"+channel);
			} catch (Exception e) {
				String msg="加密通信-加密时发生异常.channel:"+channel;
        		logger.error(msg,e);
				throw new EncryptException(msg,e);
			}
        }
        
        ////////////////////////
        //第13-16字节，共4个字节//
        ////////////////////////
        //设置数据包体的长度
        Bytes.int2bytes(data.length, header, 12);

        // write
        os.write(header); // write header.
        os.write(data); // write data.
    }
    /**
     *  该方法，被本类中的 encode（编码）方法调用
     * 使用OutputStream os把Response res写出
     * channel参数没大用
     */
    protected void encodeResponse(Channel channel, OutputStream os, Response res) throws IOException {
        try {
        	//选用一个序列化的实现
            Serialization serialization = getSerialization(channel);
            // 16字节协议头 ，长度为16字节的空byte数组
            byte[] header = new byte[HEADER_LENGTH];
            
            ////////////////////////
            //第1-2字节，共2个字节   //
            ////////////////////////
            // MAGIC是魔法数字头，占两个字节
            // MAGIC是魔法数字头的内容是 (short) 0xdabb;  十进制值是：-9541 ,二进制值是： 11011010 10111011
            Bytes.short2bytes(MAGIC, header);

            ////////////////////////
            //第3字节，共1个字节        //
            ////////////////////////
            
            //request/response标志为0，所以不用设置
            // FLAG_REQUEST十六进制值是：(byte)0x80,二进制值是：00000000.
            // serialization.getContentTypeId()序列化方式值是：00000001,不同的序列实现值不同,最多占5bit可表示8种实现.
            // 按位 或运算后二进制值是： 00000001
            header[2] = serialization.getContentTypeId();
            //双向标志为0，所以不用设置
            if (res.isHeartbeat()) header[2] |= FLAG_EVENT; //32 二进制00100000    心跳事件标志
            if (res.isShakehands()) header[2] |=FLAG_SHAKEHANDS;//16 二进制00010000  握手标志  
            if (res.isSecurity()) header[2] |=FLAG_SECURITY;//8      二进制00001000  加密标志 
            
            ////////////////////////
            //第4字节，共1个字节        //
            ////////////////////////
            // set response status.
            byte status = res.getStatus();
            header[3] = status;
            
            ////////////////////////
            //第5-12字节，共8个字节//
            ////////////////////////
            // 设置请求 id.
            Bytes.long2bytes(res.getId(), header, 4);
    
            // encode request data.
            UnsafeByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(1024);
            ObjectOutput out = serialization.serialize(null, bos);
            // encode response data or error message.
            if (status == Response.OK) {
                if (res.isHeartbeat() || res.isShakehands()) {
                    //encodeHeartbeatData(channel, out, res.getData());
                	encodeEventData(out,res.getData());
                } else {
                    encodeResponseData(channel, out, res.getData());
                }
            }else {
            	//注意：异常信息是在这里输出的
            	out.writeUTF(res.getErrorMsg());
            }
            out.flushBuffer();
            bos.flush();
            bos.close();
    
            //被序列化后的数据
            byte[] data = bos.toByteArray();
            
            // 把上面的data[] 加密
            if (res.isSecurity()) {
            	String session_key=(String)channel.getAttribute(Constants.SESSION_KEY);
            	if(session_key==null){
            		String msg="加密通信- session_key=null";
            		logger.error(msg);
            		throw new EncryptException(msg);
            	}
            	try {
            		//加密
    				long t1=System.currentTimeMillis();
    				if(data!=null){
    					data=AuthHelperProxy.sessionEncrypt(session_key, data);
    				}
    				long t2=System.currentTimeMillis();
    				logger.info("加密通信-  对Response加密，耗时："+(t2-t1)+"ms.channel:"+channel);
    			} catch (Exception e) {
    				String msg="加密通信-加密时发生异常.channel:"+channel;
            		logger.error(msg,e);
    				throw new EncryptException(msg,e);
    			}
            }
            
            ////////////////////////
            //第13-16字节，共4个字节//
            ////////////////////////
            //设置数据包体的长度
            Bytes.int2bytes(data.length, header, 12);
            // write
            os.write(header); // write header.
            os.write(data); // write data.
            
        } catch (Throwable t) {
            // 发送失败信息给Consumer,否则Consumer只能等超时了
            if (! res.isEvent() && res.getStatus() != Response.BAD_RESPONSE) {
            	// S端发生异常时，也要响应C端,不然C端只能等待超时了。
    			// S端发生的异常有两类
    			// 第一类是业务异常，第二类是RSF本身异常，如线程池耗尽等等。
    			// 本段代码，就是针对：第二类RSF本身异常 的处理，通知C端 S端发生异常了，不要再等待了。
    			AbstractCodec.errorProce_ClientNotWait(res, channel, t);
            }
            
            // 重新抛出收到的异常
            if (t instanceof IOException) {
                throw (IOException) t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else  {
                throw new RuntimeException(t.getMessage(), t);
            }
        }
    }
    
    private static final Serialization getSerializationById(Byte id) {
        return ID_SERIALIZATION_MAP.get(id);
    }
    
    @Override
    protected Object decodeData(ObjectInput in) throws IOException {
        return decodeRequestData(in);
    }

    @Deprecated
    protected Object decodeHeartbeatData(ObjectInput in) throws IOException {
        try {
            return in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(StringUtils.toString("Read object failed.", e));
        }
    }

    protected Object decodeRequestData(ObjectInput in) throws IOException {
        try {
            return in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(StringUtils.toString("Read object failed.", e));
        }
    }

    protected Object decodeResponseData(ObjectInput in) throws IOException {
        try {
            return in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(StringUtils.toString("Read object failed.", e));
        }
    }
    
    @Override
    protected void encodeData(ObjectOutput out, Object data) throws IOException {
        encodeRequestData(out, data);
    }
    
    private void encodeEventData(ObjectOutput out, Object data) throws IOException {
    	try{
    		out.writeObject(data);
    	}catch(Exception e){
			throw new SerializableException("序列化异常,请检查被传输对象和对象的成员是否实现了Serializable接口：",e);
		}
    }
    
//    @Deprecated
//    protected void encodeHeartbeatData(ObjectOutput out, Object data) throws IOException {
//        encodeEventData(out, data);
//    }

    protected void encodeRequestData(ObjectOutput out, Object data) throws IOException {
    	try{
    		 out.writeObject(data);
    	}catch(Exception e){
			throw new SerializableException("序列化异常,请检查被传输对象和对象的成员是否实现了Serializable接口：",e);
		}
    }

    protected void encodeResponseData(ObjectOutput out, Object data) throws IOException {
    	try{
    		 out.writeObject(data);
    	}catch(Exception e){
			throw new SerializableException("序列化异常,请检查被传输对象和对象的成员是否实现了Serializable接口：",e);
		}
    }
    
    @Override
    protected Object decodeData(Channel channel, ObjectInput in) throws IOException {
        return decodeRequestData(channel ,in);
    }
    
    private Object decodeEventData(Channel channel, ObjectInput in) throws IOException {
        try {
            return in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(StringUtils.toString("Read object failed.", e));
        }
    }

//    @Deprecated
//    protected Object decodeHeartbeatData(Channel channel, ObjectInput in) throws IOException {
//        try {
//            return in.readObject();
//        } catch (ClassNotFoundException e) {
//            throw new IOException(StringUtils.toString("Read object failed.", e));
//        }
//    }

    protected Object decodeRequestData(Channel channel, ObjectInput in) throws IOException {
        return decodeRequestData(in);
    }

    protected Object decodeResponseData(Channel channel, ObjectInput in) throws IOException {
        return decodeResponseData(in);
    }

    protected Object decodeResponseData(Channel channel, ObjectInput in, Object requestData) throws IOException {
        return decodeResponseData(channel, in);
    }
    
    @Override
    protected void encodeData(Channel channel, ObjectOutput out, Object data) throws IOException {
        encodeRequestData(channel, out, data);
    }

    private void encodeEventData(Channel channel, ObjectOutput out, Object data) throws IOException {
        encodeEventData(out, data);
    }
//    @Deprecated
//    protected void encodeHeartbeatData(Channel channel, ObjectOutput out, Object data) throws IOException {
//        encodeHeartbeatData(out, data);
//    }

    protected void encodeRequestData(Channel channel, ObjectOutput out, Object data) throws IOException {
        encodeRequestData(out, data);
    }

    protected void encodeResponseData(Channel channel, ObjectOutput out, Object data) throws IOException {
        encodeResponseData(out, data);
    }

}
