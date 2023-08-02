package com.hc360.rsf.rpc.loadbalance;

import java.util.List;

import com.hc360.rsf.common.Constants;
import com.hc360.rsf.common.URL;
import com.hc360.rsf.registry.Provider;
import com.hc360.rsf.rpc.RpcException;
import com.hc360.rsf.rpc.RpcInvocation;

/**
 * LoadBalanceAdptive
 * 
 */
public class LoadBalanceAdptive implements LoadBalance {

    public Provider select(List<Provider> urls, RpcInvocation invocation) throws RpcException {
        if (urls == null || urls.size() == 0) {
            return null;
        }
        Provider url = urls.get(0);
//        String method = invocation.getMethodName();
//        String name;
//        if (method == null || method.length() == 0) {
//            name = url.getParameter(Constants.LOADBALANCE_KEY, Constants.DEFAULT_LOADBALANCE);
//        } else {
//            name = url.getMethodParameter(method, Constants.LOADBALANCE_KEY, Constants.DEFAULT_LOADBALANCE);
//        }
        //LoadBalance loadbalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(name);
        LoadBalance loadbalance = null;
        return loadbalance.select(urls, invocation);
    }

}
