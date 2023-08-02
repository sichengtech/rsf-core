/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.rpc.proxy.jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hc360.rsf.common.Constants;
import com.hc360.rsf.common.URL;
import com.hc360.rsf.rpc.EchoService;
import com.hc360.rsf.rpc.Invoker;
import com.hc360.rsf.rpc.RpcInvocation;
import com.hc360.rsf.rpc.RpcResult;

/**
 * InvokerHandler
 * 这是java动态代理InvocationHandler接口的实现,
 * 是一个"包装再次代理"
 */
public class JdkInvocationHandler implements InvocationHandler {
	private static Logger logger = LoggerFactory.getLogger(JdkInvocationHandler.class);
    private final Invoker<?> invoker;
    private final URL url;
    
    public JdkInvocationHandler(Invoker<?> invoker){
        this.invoker = invoker;
        this.url=invoker.getUrl();
    }

    /**
     * 必需实现的父接口的 方法
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
       
        //getDeclaringClass():返回表示声明由此 Method 对象表示的方法的类或接口的 Class 对象。
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(invoker, args);
        }
        if ("toString".equals(methodName) && parameterTypes.length == 0) {
            return invoker.toString();
        }
        if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
            return invoker.hashCode();
        }
        if ("equals".equals(methodName) && parameterTypes.length == 1) {
            return invoker.equals(args[0]);
        }
        
        Map<String, String> parametersUrl=new HashMap<String, String>();
        //携带参数到服务端：服务接口名
        if(Constants.$ECHO.equals(methodName)){
        	//回声
            parametersUrl.put(Constants.PATH_KEY, EchoService.class.getName());
        }else if(Constants.$ECHO_INTERFACE.equals(methodName)){
        	//修改实参参数
        	args=new Object[]{invoker.getInterface().getName()};
        	//修改实参类型
        	parameterTypes=new Class<?>[]{String.class};
        	//回声
            parametersUrl.put(Constants.PATH_KEY, EchoService.class.getName());
        }else{
        	//常规业务
            parametersUrl.put(Constants.PATH_KEY, url.getParameter(Constants.PATH_KEY));
        }
        //携带参数到服务端：回调函数名称
        parametersUrl.put(Constants.CHANNEL_CALLBACK_KEY, url.getParameter(Constants.CHANNEL_CALLBACK_KEY));
        //携带参数到服务端：是否有回调函数
        parametersUrl.put(Constants.IS_CALLBACK_SERVICE, "true");
        
        //RPCInvocation是对 方法名,方法的参数类型,方法的实参   的封装. 
        RpcInvocation rpcInvocation=new RpcInvocation(method.getName(),parameterTypes,args);
        String interfaceName=invoker.getInterface().getName();
        rpcInvocation.setAttachments(parametersUrl);
        if(logger.isDebugEnabled()){
        	logger.debug("RSF JdkProxy invoke,interfaceName={},methodName={},Attachments={}",new Object[]{interfaceName, methodName,rpcInvocation.getAttachments()});
        }
        RpcResult rs=invoker.invoke(rpcInvocation);
        //返回调用结果
        return rs.recreate();
    }
}
