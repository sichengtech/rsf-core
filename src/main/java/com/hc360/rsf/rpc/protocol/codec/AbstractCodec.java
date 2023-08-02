package com.hc360.rsf.rpc.protocol.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hc360.rsf.common.serialize.Serialization;
import com.hc360.rsf.common.serialize.support.hessian.Hessian2Serialization;
import com.hc360.rsf.common.utils.StringUtils;
import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.exchange.support.Request;
import com.hc360.rsf.remoting.exchange.support.Response;

/**
 * <br>
 * AbstractCodec所有编码工具类的基类<br>
 * 本类以及其子类主要用途是：<br>
 * 发送数据前要编码()<br>
 * 接收到数据后要解码()<br>
 * <br>
 */
public abstract class AbstractCodec implements Codec {
	private static Logger logger = LoggerFactory.getLogger(AbstractCodec.class);

    /**
     * 取得序列化工具
     * 
     * 方法为什么要传入一个channel?
     * 因为channel中有URL对象,URL对象中的参数表示使用哪一个序列化的实现,
     * 如果没有这个参数,则使用默认值
     * 
     * 现在结构变了，channel中没有这个信息，这里使用默认值 
     * 
     * @param channel
     * @return
     */
    protected Serialization getSerialization(Channel channel) {
    	Serialization serialization =null;
    	
		//TODO 未实现
    	//serialization =读用户的xml配置文件，指明的使用哪个序列化方式。
    	//默认使用Hessian序列化方式 
    	if(serialization==null){
    		serialization = new Hessian2Serialization();
    	}
    	
        //Serialization serialization = ExtensionLoader.getExtensionLoader(Serialization.class).getExtension(channel.getUrl().getParameter(Constants.SERIALIZATION_KEY, Constants.DEFAULT_REMOTING_SERIALIZATION));
    	//Serialization serialization = new DubboSerialization();
    	//Serialization serialization = new Hessian2Serialization();
    	//Serialization serialization = new JavaSerialization();
        return serialization;
    }

	// S端发生异常时，也要响应C端,不然C端只能等待超时了。
	// S端发生的异常有两类
	// 第一类是业务异常，第二类是RSF本身异常，如线程池耗尽等等。
	// 本段代码，就是针对：第二类RSF本身异常 的处理，通知C端 S端发生异常了，不要再等待了。
    public static void errorProce_ClientNotWait(Object message,Channel channel,Throwable e){
    	errorProce_ClientNotWait(message,channel,e,Response.SERVER_ERROR);
    }
    public static void errorProce_ClientNotWait(Object message,Channel channel,Throwable e,byte rsfErrorType){
		if (message instanceof Request) {
			Request res = (Request) message;
			
			//如果是双向通信，则返回处理结果
			if( ! res.isTwoWay()){
				logger.error("服务端RSF发生异常,由于是异步调用，不需要通知客户端, cause: " + e.getMessage(), e);
				return;
			}
			try {
				logger.error("服务端RSF发生异常,通知客户端不再等待, cause: " + e.getMessage(), e);
                Response r = new Response(res.getId(), res.getVersion());
                r.setData(null);// 当有异常发生时,data值不会被传递
                r.setStatus(rsfErrorType);
                //r.setErrorMsg("服务端RSF发生异常,通知客户端不再等待, cause: " + StringUtils.toString(e));
                if(rsfErrorType==Response.DATA_TOO_LENGTH){
                	//如果是Response.DATA_TOO_LENGTH
                	//只返回简要的异常信息
                	r.setErrorMsg(e.getMessage());
                }else{
                	//返回全面的异常信息
                	r.setErrorMsg(StringUtils.toString(e));
                }
                channel.send(r);
                return;
            } catch (Exception e1) {
                logger.error("服务端RSF发生异常,通知客户端不再等待时再次发生异常, cause: " + e1.getMessage(), e1);
            }
		}else if(message instanceof Response){
			try {
				logger.error("服务端RSF发生异常,通知客户端不再等待, cause: " + e.getMessage(), e);
				Response r =(Response)message;
				r.setData(null);// 当有异常发生时,data值不会被传递
	            r.setStatus(rsfErrorType);
	            r.setErrorMsg(StringUtils.toString(e));
	            channel.send(r);
	            return;
			} catch (Exception e1) {
                logger.error("服务端RSF发生异常,通知客户端不再等待时再次发生异常, cause: " + e1.getMessage(), e1);
            }
		}else{
			logger.error("服务端RSF发生异常, cause: " + e.getMessage(), e);
		}
    }
}
