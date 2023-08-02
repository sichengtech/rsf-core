/**
 * Protocol.java   2012-5-7
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.rpc;

import com.hc360.rsf.common.URL;


/**
 * Protocol 协议
 * 
 * @author zhaolei 2012-5-7
 */
public interface Protocol {
	/**
	 * 暴露
	 * @param invoker
	 * @throws RpcException
	 */
	void export(Invoker<?> invoker) throws RpcException;

    /**
     *  refer.
     * @param <T>
     * @param type
     * @param url
     * @return
     * @throws RpcException
     */
    <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException;
}
