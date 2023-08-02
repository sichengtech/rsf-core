package com.hc360.rsf.rpc.suppport;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hc360.rsf.common.utils.ReflectUtils;
import com.hc360.rsf.rpc.RpcInvocation;

/**
 * RpcUtils
 * 
 */
public class RpcUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcUtils.class);

    public static Class<?> getReturnType(RpcInvocation invocation) {
        try {
            if (invocation != null && invocation.getInvoker() != null
                    && invocation.getInvoker().getUrl() != null
                    && ! invocation.getMethodName().startsWith("$")) {
                String service = invocation.getInvoker().getUrl().getServiceInterface();
                if (service != null && service.length() > 0) {
                    Class<?> cls = ReflectUtils.forName(service);
                    Method method = cls.getMethod(invocation.getMethodName(), invocation.getParameterTypes());
                    if (method.getReturnType() == void.class) {
                        return null;
                    }
                    return method.getReturnType();
                }
            }
        } catch (Throwable t) {
            LOGGER.warn(t.getMessage(), t);
        }
        return null;
    }

    public static Type[] getReturnTypes(RpcInvocation invocation) {
        try {
            if (invocation != null && invocation.getInvoker() != null
                    && invocation.getInvoker().getUrl() != null
                    && ! invocation.getMethodName().startsWith("$")) {
                String service = invocation.getInvoker().getUrl().getServiceInterface();
                if (service != null && service.length() > 0) {
                    Class<?> cls = ReflectUtils.forName(service);
                    Method method = cls.getMethod(invocation.getMethodName(), invocation.getParameterTypes());
                    if (method.getReturnType() == void.class) {
                        return null;
                    }
                    return new Type[]{method.getReturnType(), method.getGenericReturnType()};
                }
            }
        } catch (Throwable t) {
            LOGGER.warn(t.getMessage(), t);
        }
        return null;
    }

}
