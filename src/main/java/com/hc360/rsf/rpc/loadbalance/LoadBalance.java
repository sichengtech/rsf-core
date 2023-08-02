package com.hc360.rsf.rpc.loadbalance;

import java.util.List;

import com.hc360.rsf.common.URL;
import com.hc360.rsf.registry.Provider;
import com.hc360.rsf.rpc.RpcException;
import com.hc360.rsf.rpc.RpcInvocation;

public interface LoadBalance {

	 /**
	 * select
	 * @param url
	 * @param invocation
	 * @return
	 * @throws RpcException
	 */
	Provider select(List<Provider> urls, RpcInvocation invocation) throws RpcException;

}
