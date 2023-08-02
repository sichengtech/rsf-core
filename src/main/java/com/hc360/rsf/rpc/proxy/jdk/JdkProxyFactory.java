/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.rpc.proxy.jdk;

import java.lang.reflect.Proxy;

import com.hc360.rsf.common.URL;
import com.hc360.rsf.common.utils.ReflectUtils;
import com.hc360.rsf.rpc.EchoService;
import com.hc360.rsf.rpc.Invoker;
import com.hc360.rsf.rpc.RpcException;
import com.hc360.rsf.rpc.protocol.RsfInvokerServer;
import com.hc360.rsf.rpc.proxy.ProxyFactory;

/**
 * JdkProxyFactory
 */
public class JdkProxyFactory implements ProxyFactory {
	
	/**
	 * 客户端的Invoke--只有在客户端才被使用
	 * 把客户端的请求 转换为 网络通信发向服务器端
	 */
	public <T> T getProxy(Invoker<T> invoker) throws RpcException {
        Class<?>[] interfaces = null;
        //从invoker的URL中取得接口的名称
        URL url=invoker.getUrl();
        if(url!=null){
	        String config = url.getParameter("interfaces");
	        if (config != null && config.length() > 0) {
	        	//可以实现有多个接口
	            String[] types = config.split(",");
	            if (types != null && types.length > 0) {
	                interfaces = new Class<?>[types.length + 2];
	                interfaces[0] = invoker.getInterface();//服务接口
	                interfaces[1] = EchoService.class;//用于回声测试
	                for (int i = 0; i < types.length; i ++) {
	                	//使用工具类ReflectUtils,通过类名字符串得到相应的Class类
	                    interfaces[i + 1] = ReflectUtils.forName(types[i]);
	                }
	            }
	        }
        }
        if (interfaces == null) {
        	//最少也要实现两个接口:invoker的父接口, EchoService回声接口
            interfaces = new Class<?>[] {invoker.getInterface(), EchoService.class};
        }
        //调用一个抽象的getProxy()方法
        return getProxy(invoker, interfaces);
	}

	/*
	 * 生成代理
	 */
	@SuppressWarnings("unchecked")
	private <T> T getProxy(Invoker<T> invoker, Class<?>[] interfaces) {
		return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces,
				new JdkInvocationHandler(invoker));
	}

	/**
	 * 服务端的Invoke -- 只有在服务端才被 使用
	 * 把从网络收到客户端的信息 转换为 请求执行服务器端业务代码
	 */
	public <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) {
		 return new RsfInvokerServer<T>(proxy, type, url);
	}
}
