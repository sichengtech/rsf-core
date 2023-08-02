/**
 * Invoker.java   2012-4-26
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.rpc;

import java.util.List;

import com.hc360.rsf.common.URL;
import com.hc360.rsf.config.ClientMethodConfig;

/**
 * Invoker “调用”接口
 * 
 * 是客户端“发起调用”的核心接口
 * 
 * 是服务“调用真实业务”的核心接口
 * 
 * @author zhaolei 2012-4-26
 */
public interface Invoker<T>  {
	
	URL getUrl();
	 
    /**
     * get service interface.
     * 
     * @return service interface.
     */
    Class<T> getInterface();

    /**
     * invoke.
     * 
     * @param invocation
     * @return result
     * @throws RpcException
     */
    RpcResult invoke(RpcInvocation invocation) ;


}
