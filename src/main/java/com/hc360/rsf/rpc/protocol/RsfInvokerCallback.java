/**
 * RsfInvoker_1.java   2012-4-27
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.rpc.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hc360.rsf.common.URL;
import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.RemotingException;
import com.hc360.rsf.remoting.exchange.support.Response;
import com.hc360.rsf.rpc.Invoker;
import com.hc360.rsf.rpc.RpcInvocation;
import com.hc360.rsf.rpc.RpcResult;

/**
 * Invoker接口的实现
 * 
 * 作用：在服务端使用，向客户端推送消息时，使用的Invoker
 * 
 * 
 * @author zhaolei 2012-4-27
 */
public class RsfInvokerCallback<T> implements Invoker<T> {
	private static Logger logger = LoggerFactory.getLogger(RsfInvokerCallback.class);
	private final Class<T> serviceType;
	private final URL url;
	private final Channel channel;

	public RsfInvokerCallback(Class<T> serviceType, URL url,Channel channel) {
		this.serviceType = serviceType;
		this.url = url;
		this.channel=channel;
	}

	/**
	 * 在服务端使用，向客户端推送消息
	 * 
	 * @param invocation
	 * @return
	 * @see com.hc360.rsf.rpc.Invoker#invoke(com.hc360.rsf.rpc.RpcInvocation)
	 */
	public RpcResult invoke(RpcInvocation invocation) {
		Object rs = null;
		try {
			rs = channel.request(invocation);
		} catch (RemotingException e1) {
			String msg="调用"+serviceType.getName()+" "+invocation.getMethodName()+"()时异常";
			logger.error(msg,e1);
			throw new RuntimeException(msg,e1);
		}
		if (rs instanceof Response) {
			//在进入这里之前,已处理服务端超时异常,服务端业务异常
			//如果有以上异常,就会在前一步抛出异常,不会进入这里
			Response response = (Response) rs;
			return (RpcResult)response.getData();
		}
		return new RpcResult(rs);
	}

	/**
	 * function description
	 * 
	 * @return
	 * @see com.hc360.rsf.rpc.Invoker#getUrl()
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * function description
	 * 
	 * @return
	 * @see com.hc360.rsf.rpc.Invoker#getInterface()
	 */
	public Class<T> getInterface() {
		return serviceType;
	}
}
