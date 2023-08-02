package com.hc360.rsf.rpc.loadbalance;

import java.util.List;
import com.hc360.rsf.registry.Provider;
import com.hc360.rsf.rpc.RpcInvocation;

/**
 * AbstractLoadBalance
 * 
 */
public abstract class AbstractLoadBalance implements LoadBalance {

	public Provider select(List<Provider> urls, RpcInvocation invocation) {
		if (urls == null || urls.size() == 0){
			return null;
		}
		if (urls.size() == 1) {
			return urls.get(0);
		}
		return doSelect(urls, invocation);
	}

	protected abstract Provider doSelect(List<Provider> invokers, RpcInvocation invocation);

	protected int getWeight(Provider url, RpcInvocation invocation) {
//		return url.getMethodParameter(invocation.getMethodName(), Constants.WEIGHT_KEY,
//				Constants.DEFAULT_WEIGHT);
		return url.getWeight();
		
	}

}
