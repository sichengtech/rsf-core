package com.hc360.rsf.rpc.protocol.codec;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hc360.rsf.common.Constants;
import com.hc360.rsf.common.Version;
import com.hc360.rsf.common.serialize.ObjectInput;
import com.hc360.rsf.common.serialize.ObjectOutput;
import com.hc360.rsf.common.utils.ReflectUtils;
import com.hc360.rsf.common.utils.StringUtils;
import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.SerializableException;
import com.hc360.rsf.rpc.RpcException;
import com.hc360.rsf.rpc.RpcInvocation;
import com.hc360.rsf.rpc.RpcResult;
import com.hc360.rsf.rpc.suppport.RpcUtils;

/**
 * Rsf codec.
 * 
 * rsf协议--包体实现类 Java版包体
 * 把对象RpcInvocation/RpcResult用Java序列化方式传输，
 * 真实的业务数据用Java序列化方式传输
 * 
 * 原类名：RsfCodec
 * 
 */
public class JavaCodec extends ExchangeCodec {
	private static Logger logger = LoggerFactory.getLogger(JavaCodec.class);

    public static final String      NAME                    = "rsf";

    private static final byte       RESPONSE_WITH_EXCEPTION = 0;//响应异常

    private static final byte       RESPONSE_VALUE          = 1;//响应值

    private static final byte       RESPONSE_NULL_VALUE     = 2;//响应空值 

    private static final Object[]   EMPTY_OBJECT_ARRAY      = new Object[0];

    private static final Class<?>[] EMPTY_CLASS_ARRAY       = new Class<?>[0];
    
   
    /**
     * 编码
     * 客户端向服务端发送数据
     * 把RpcInvocation对象写出
     * 
     * 通过ObjectOutput out把Object data写出
     * Channel channel没什么大用
     */
    @Override
    protected void encodeRequestData(Channel channel, ObjectOutput out, Object data) throws IOException {
        RpcInvocation inv = (RpcInvocation) data;

        out.writeUTF(inv.getAttachment(Constants.RSF_VERSION_KEY, Version.getVersion()));//rsf的版本号
        out.writeUTF(inv.getAttachment(Constants.PATH_KEY));//接口名
        out.writeUTF(inv.getAttachment(Constants.VERSION_KEY));
        out.writeUTF(inv.getMethodName());//方法名
        out.writeUTF(ReflectUtils.getDesc(inv.getParameterTypes()));//参数类型,转为String,再传递
        Object[] args = inv.getArguments();//实参
        if (args != null)
        for (int i = 0; i < args.length; i++){
        	/**
        	 * 从RpcInvocation inv中取出实参(调用远程接口时传进来的实参),
        	 * 并一个一个的通过ObjectOutput out写出
        	 */
        	try{
        		out.writeObject(args[i]);
	       	}catch(Exception e){
	   			//throw new RpcException(RpcException.SERIALIZATION_EXCEPTION,"序列化异常,请检查被传输对象和对象的成员是否实现了Serializable接口：",e);
	   			throw new SerializableException("序列化异常,请检查被传输对象和对象的成员是否实现了Serializable接口：",e);
	   		}
        }
    	try{
    		 out.writeObject(inv.getAttachments());//附件
       	}catch(Exception e){
   			//throw new RpcException(RpcException.SERIALIZATION_EXCEPTION,"序列化异常,请检查被传输对象和对象的成员是否实现了Serializable接口：",e);
   			throw new SerializableException("序列化异常,请检查被传输对象和对象的成员是否实现了Serializable接口：",e);
   		}
    }

    /**
     * 解码
     * 服务端接收到客户端发来的数据
     * 还原出RpcInvocation对象
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Object decodeRequestData(Channel channel, ObjectInput in) throws IOException {
        RpcInvocation inv = new RpcInvocation();

        inv.setAttachment(Constants.RSF_VERSION_KEY, in.readUTF());//rsf的版本号
        inv.setAttachment(Constants.PATH_KEY, in.readUTF());//接口名
        inv.setAttachment(Constants.VERSION_KEY, in.readUTF());

        inv.setMethodName(in.readUTF());//方法名
        try {
            Object[] args;//实参
            Class<?>[] pts;//型参
            String desc = in.readUTF();//方法型参的描述
            if (desc.length() == 0) {
                pts = EMPTY_CLASS_ARRAY;
                args = EMPTY_OBJECT_ARRAY;
            } else {
                pts = ReflectUtils.desc2classArray(desc);
                args = new Object[pts.length];
                for (int i = 0; i < args.length; i++){
                    //try{
                        args[i] = in.readObject(pts[i]);
                    //}catch (Exception e) {
                    //    e.printStackTrace();
                    //}
                }
            }
            inv.setParameterTypes(pts);
            
            Map<String, String> map = (Map<String, String>) in.readObject(Map.class);//附件
            if (map != null && map.size() > 0) {
                Map<String, String> attachment = inv.getAttachments();
                if (attachment == null) {
                    attachment = new HashMap<String, String>();
                }
                attachment.putAll(map);
                inv.setAttachments(attachment);
            }
            
            inv.setArguments(args);
            
        } catch (ClassNotFoundException e) {
            throw new IOException(StringUtils.toString("Read invocation data failed.", e));
        }
        return inv;
    }

    /**
     * 编码
     * 服务端向客户端发送数据（业务处理的结果）
     * 把RpcResult对象写出
     * 
     * 如果有异常对象,也把异常对象序列化
     * 
     */
    @Override
    protected void encodeResponseData(Channel channel, ObjectOutput out, Object data) throws IOException {
    	RpcResult result = (RpcResult) data;

        Throwable th = result.getException();
        if (th == null) {
            Object ret = result.getValue();
            if (ret == null) {
                out.writeByte(RESPONSE_NULL_VALUE);
            } else {
                out.writeByte(RESPONSE_VALUE);
            	try{
            		 out.writeObject(ret);
            	}catch(Exception e){
        			//throw new RpcException(RpcException.SERIALIZATION_EXCEPTION,"序列化异常,请检查被传输对象和对象的成员是否实现了Serializable接口：",e);
        			throw new SerializableException("序列化异常,请检查被传输对象和对象的成员是否实现了Serializable接口：",e);
        		}
            }
        } else {
            out.writeByte(RESPONSE_WITH_EXCEPTION);
        	try{
        		out.writeObject(th);
	       	}catch(Exception e){
	   			//throw new RpcException(RpcException.SERIALIZATION_EXCEPTION,"序列化异常,请检查被传输对象和对象的成员是否实现了Serializable接口：",e);
	   			throw new SerializableException("序列化异常,请检查被传输对象和对象的成员是否实现了Serializable接口：",e);
	   		}
        }
        //rsf 1.1.0版本加入的,加再了包体的最后端
        //服务端业务执行耗时
        out.writeInt(result.getTime());
    }
    
    /**
     * 解码
     * 客户端接收到服务端响应回来的数据（业务处理的结果）
     * 还原出RpcResult对象
     * 
     */
    @Override
    protected Object decodeResponseData(Channel channel, ObjectInput in, Object request) throws IOException {
    	RpcInvocation invocation = (RpcInvocation) request;
        RpcResult result = new RpcResult();
      
        byte flag = in.readByte();
        switch (flag) {
            case RESPONSE_NULL_VALUE:
                break;
            case RESPONSE_VALUE:
                try {
                    Type[] returnType = RpcUtils.getReturnTypes(invocation);
                    result.setValue(returnType == null || returnType.length == 0 ? in.readObject() : 
                        (returnType.length == 1 ? in.readObject((Class<?>)returnType[0]) 
                                : in.readObject((Class<?>)returnType[0], returnType[1])));
                } catch (ClassNotFoundException e) {
                    throw new IOException(StringUtils.toString("Read response data failed.", e));
                }
                break;
            case RESPONSE_WITH_EXCEPTION:
                try {
                    Object obj = in.readObject();
                    if (obj instanceof Throwable == false) throw new IOException("Response data error, expect Throwable, but get " + obj);
                    result.setException((Throwable) obj);
                } catch (ClassNotFoundException e) {
                    throw new IOException(StringUtils.toString("Read response data failed.", e));
                }
                break;
            default:
                throw new IOException("Unknown result flag, expect '0' '1' '2', get " + flag);
        }
        //RSF1.1.0版本加入的以下内容,加再了包体的最后端
        //由于RSF 1.1.0以下版本，没有记录服务端业务执行时间
        //所以高版本与 1.1.0以下版本通信时，取这个值会报错。
        try{
            int time=in.readInt();//服务端业务执行耗时
            result.setTime(time);
        }catch(Exception e){
        	result.setTime(-1);
        	logger.debug("服务端RSF的jar包版本低于1.1.0,没有记录服务端业务执行时间值.");
        }
        return result;
    }
}
