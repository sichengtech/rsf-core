package com.hc360.rsf.rpc.loadbalance;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.hc360.rsf.common.Constants;
import com.hc360.rsf.registry.Provider;
import com.hc360.rsf.rpc.RpcInvocation;

/**
 * ConsistentHashLoadBalance
 * 
 */
public class ConsistentHashLoadBalance extends AbstractLoadBalance {

    private final ConcurrentMap<String, ConsistentHashSelector<?>> selectors = new ConcurrentHashMap<String, ConsistentHashSelector<?>>();

    @SuppressWarnings("unchecked")
    @Override
    protected Provider doSelect(List<Provider> urls, RpcInvocation invocation) {
        String key = urls.get(0).getServiceKey() + "." + invocation.getMethodName();
        int identityHashCode = System.identityHashCode(urls);
        ConsistentHashSelector<?> selector = (ConsistentHashSelector<?>) selectors.get(key);
        if (selector == null || selector.getIdentityHashCode() != identityHashCode) {
            selectors.put(key, new ConsistentHashSelector(urls, invocation.getMethodName(), identityHashCode));
            selector = (ConsistentHashSelector<?>) selectors.get(key);
        }
        return selector.select(invocation);
    }

    private static final class ConsistentHashSelector<T> {

        private final TreeMap<Long, Provider> virtualInvokers;

        private final int                       replicaNumber;
        
        private final int                       identityHashCode;
        
        private final int[]                     argumentIndex;

        public ConsistentHashSelector(List<Provider> invokers, String methodName, int identityHashCode) {
            this.virtualInvokers = new TreeMap<Long, Provider>();
            this.identityHashCode = System.identityHashCode(invokers);
            Provider url = invokers.get(0);
            this.replicaNumber = 160;//url.getMethodParameter(methodName, "hash.nodes", 160);
//            String[] index = Constants.COMMA_SPLIT_PATTERN.split(url.getMethodParameter(methodName, "hash.arguments", "0"));
            String[] index = Constants.COMMA_SPLIT_PATTERN.split("0");
            argumentIndex = new int[index.length];
            for (int i = 0; i < index.length; i ++) {
                argumentIndex[i] = Integer.parseInt(index[i]);
            }
            for (Provider invoker : invokers) {
                for (int i = 0; i < replicaNumber / 4; i++) {
//                    byte[] digest = md5(invoker.toFullString() + i);
                    byte[] digest = md5(invoker.getServiceKey() + i);
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
        }

        public int getIdentityHashCode() {
            return identityHashCode;
        }

        public Provider select(RpcInvocation invocation) {
        	String key;
        	if(invocation.getArguments()==null){
        		key = "";
        	}else{
        		key = toKey(invocation.getArguments());
        	}
            byte[] digest = md5(key);
            return sekectForKey(hash(digest, 0));
        }

        private String toKey(Object[] args) {
            StringBuilder buf = new StringBuilder();
            for (int i : argumentIndex) {
                if (i >= 0 && i < args.length) {
                    buf.append(args[i]);
                }
            }
            return buf.toString();
        }

        private Provider sekectForKey(long hash) {
        	Provider invoker;
            Long key = hash;
            if (!virtualInvokers.containsKey(key)) {
                SortedMap<Long, Provider> tailMap = virtualInvokers.tailMap(key);
                if (tailMap.isEmpty()) {
                    key = virtualInvokers.firstKey();
                } else {
                    key = tailMap.firstKey();
                }
            }
            invoker = virtualInvokers.get(key);
            return invoker;
        }

        private long hash(byte[] digest, int number) {
            return (((long) (digest[3 + number * 4] & 0xFF) << 24)
                    | ((long) (digest[2 + number * 4] & 0xFF) << 16)
                    | ((long) (digest[1 + number * 4] & 0xFF) << 8) 
                    | (digest[0 + number * 4] & 0xFF)) 
                    & 0xFFFFFFFFL;
        }

        private byte[] md5(String value) {
            MessageDigest md5;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            md5.reset();
            byte[] bytes = null;
            try {
                bytes = value.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            md5.update(bytes);
            return md5.digest();
        }

    }

}
