/**
 * RsfInvoker_2.java   2012-4-28
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.rpc.protocol;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hc360.rsf.common.URL;
import com.hc360.rsf.rpc.Invoker;
import com.hc360.rsf.rpc.RpcInvocation;
import com.hc360.rsf.rpc.RpcResult;

/**
 * Invoker接口的实现
 * 
 * 作用：在服务端使用，服务端“调用真实业务方法”
 * 
 * @author zhaolei 2012-4-28
 */
public class RsfInvokerServer<T> implements Invoker<T> {
	private static Logger logger = LoggerFactory.getLogger(RsfInvokerServer.class);
	private final T proxy;

	private final Class<T> type;

	private final URL url;

	public RsfInvokerServer(T proxy, Class<T> type, URL url) {
		if (proxy == null) {
			throw new IllegalArgumentException("proxy == null");
		}
		if (type == null) {
			throw new IllegalArgumentException("interface == null");
		}
		if (!type.isInstance(proxy)) {
			throw new IllegalArgumentException(proxy.getClass().getName() + " not implement interface "
					+ type);
		}
		this.proxy = proxy;
		this.type = type;
		this.url = url;
	}

	/**
	 * function description
	 * 
	 * @param invocation
	 * @return
	 * @see com.hc360.rsf.rpc.Invoker#invoke(com.hc360.rsf.rpc.RpcInvocation)
	 */
	public RpcResult invoke(RpcInvocation invocation) {
		String methodName = null;
		try {
			long t1=System.nanoTime();
			methodName = invocation.getMethodName();
			Class<?>[] parameterTypes = invocation.getParameterTypes();
			Object[] arguments = invocation.getArguments();
			Method method = proxy.getClass().getMethod(methodName, parameterTypes);
			method.setAccessible(true);
			Object rs = method.invoke(proxy, arguments);
			long t2=System.nanoTime();
			RpcResult result=new RpcResult(rs);
			result.setTime((int)(t2-t1));//记录服务端业务执行时间,单位纳秒
			return result;
		}catch(InvocationTargetException e){
			//是一种包装由调用方法或构造方法所抛出异常的经过检查的异常。 
			logger.error("服务端发生异常1："+proxy.getClass().getName()+" "+(methodName==null?"":methodName)+"() 执行时发生异常",e);
			return new RpcResult(e.getCause()==null?e:e.getCause());
		}catch (Exception e) {
			logger.error("服务端发生异常2："+proxy.getClass().getName()+" "+(methodName==null?"":methodName)+"() 执行时发生异常",e);
			return new RpcResult(e);
		}
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
		return type;
	}
}
