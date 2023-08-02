/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.rpc.proxy;

import com.hc360.rsf.common.URL;
import com.hc360.rsf.rpc.Invoker;
import com.hc360.rsf.rpc.RpcException;


/**
 * ProxyFactory. 
 * 
 * 创建代理类
 * 可代理客户端的业务接口上的某个方法
 * 可代理服务端的业务接口上的某个方法
 * 
 */
public interface ProxyFactory {

	/**
	 * 创建客户端代理
	 * 
	 * 客户端的Invoke--只有在客户端才被使用
	 * 
	 * 把客户端的请求 转换为 网络通信发向服务器端
	 */
    <T> T getProxy(Invoker<T> invoker) throws RpcException;

	/**
	 * 创建服务端代理
	 * 
	 * 服务端的Invoke -- 只有在服务端才被 使用
	 * 
	 * 把从网络收到客户端的信息 转换为 请求执行服务器端业务代码
	 */
    <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) throws RpcException;

}
