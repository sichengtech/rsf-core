/**
 * RsfProtocol.java   2012-4-28
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.rpc.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import com.hc360.rsf.common.Constants;
import com.hc360.rsf.common.URL;
import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.RemotingException;
import com.hc360.rsf.rpc.Invoker;
import com.hc360.rsf.rpc.Protocol;
import com.hc360.rsf.rpc.RpcException;
import com.hc360.rsf.rpc.RpcInvocation;

/**
 * 服务端的 代理类，都放在这里
 * 
 * @author zhaolei  2012-4-28
 */
public class RsfProtocol implements Protocol{
	/**
	 * Invoker全局上下文
	 */
	public static final Map<String, Invoker<?>> INVOKER_MAP = new ConcurrentHashMap<String, Invoker<?>>();
	
	/**
	 * 以文本的形式，输出本地提供了哪些服务
	 * 供Telnet使用，通过Telnet查询本端提供了哪些服务（文本形式）
	 * @return
	 */
	public static String serviceListStr(){
		StringBuilder sbl=new StringBuilder();
		sbl.append("service count:");
		sbl.append(INVOKER_MAP.size());
		sbl.append("\n\r");
		int i=1;
		for( String key: INVOKER_MAP.keySet()){
			sbl.append(" "+i+". ");
			sbl.append(key);
			sbl.append("\n\r");
			i++;
		}
		return sbl.toString();
	}
	
	/**
	 * 返回服务端提供的服务接口的名称的列表
	 * @return
	 */
	public static List<String> serviceNameList(){
		List<String> list=new ArrayList<String>(INVOKER_MAP.keySet().size());
		for( String key: INVOKER_MAP.keySet()){
			String interfaceName=splitInterfaceName(key);
			list.add(interfaceName);
		}
		return list;
	}
	
	/**
	 * 在本地暴露服务--常规使用（使用的最频繁）
	 * 
	 * 暴露服务时, 把一个服务的key 与一个 invoker关系起来 
	 * 
	 * key是根据invoker中的URL属计算而来
	 * 
	 * @param invoker
	 * @throws RpcException
	 * @see com.hc360.rsf.rpc.Protocol#export(com.hc360.rsf.rpc.Invoker)
	 */
	public void export(Invoker<?> invoker) throws RpcException {
		URL url=invoker.getUrl();
		String serviceKey=serviceKey(url);
		INVOKER_MAP.put(serviceKey, invoker);
	}
	/**
	 * 在本地暴露服务--只供推送数据时使用
	 * @param key
	 * @param invoker
	 * @throws RpcException
	 */
	public void export4Callback(String key,Invoker<?> invoker) throws RpcException {
		INVOKER_MAP.put(key, invoker);
	}
	
	/**
	 * create rpc invoker.
	 * RsfInvoker中,TCP连接已建立.
	 * 如果底层通信使用Mina, 那么SocketConnector,IoSession等都已被创建.
	 * 被保存在RsfInvoker的成员变量 ExchangeClient的成员变量中
	 */
	public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
		//未使用到本方法,所以注释--赵磊
		//Invoker<T> invoker=new RsfInvokerClientP2p<T>(type,url);
		//return invoker;
		return null;
	}
	
	/**
	 * 在服务端使用本方法, 找出可以提供服务Invoker对象.<br>
	 * Invoker对象可以用处理来自客户端的请求.<br>
	 * 
	 * @param channel
	 * @param inv
	 * @return
	 * @throws RemotingException
	 */
	public Invoker<?> getInvoker(Channel channel, RpcInvocation inv) throws RemotingException{
		
        int port = channel.getLocalAddress().getPort();//本地端口
        String path = inv.getAttachments().get(Constants.PATH_KEY);//接口名
        String serviceKey = serviceKey(port, path, inv.getAttachments().get(Constants.VERSION_KEY), inv.getAttachments().get(Constants.GROUP_KEY));
        Invoker<?> invoker=INVOKER_MAP.get(serviceKey);
        if (invoker == null){
            throw new RemotingException(channel, "没有这个服务接口: " + serviceKey + " in " + INVOKER_MAP.keySet() + ", may be version or group mismatch " + ", channel: consumer: " + channel.getRemoteAddress() + " --> provider: " + channel.getLocalAddress() + ", message:" + inv);
        }
        return invoker;
    }
	
	/**
	 * 在客户端使用本方法, 找出可以提供回调服务Invoker对象.<br>
	 * Invoker对象可以用处理来自服务端的推.<br>
	 * @param callbackKey
	 * @return
	 * @throws RemotingException
	 */
	public Invoker<?> getInvoker4Callback(String callbackKey) throws RemotingException{
	     return INVOKER_MAP.get(callbackKey);
	}
	
	//------------------------------------------------------------------------------------
//    /**
//     * 判断是客户端还是服务端
//     * @param channel
//     * @return
//     */
//    private boolean isClientSide(Channel channel) {
//        InetSocketAddress address = channel.getRemoteAddress();
//        URL url = channel.getUrl();
//        return url.getPort() == address.getPort() && 
//                    NetUtils.filterLocalHost(channel.getUrl().getIp())
//                    .equals(NetUtils.filterLocalHost(address.getAddress().getHostAddress()));
//    }
//    

    /**
	 * 生成服务的key 
	 * 
	 * 当暴露一个服务器时,这个服务对象是保存在一个全局的map中的,需要一个key来唯一标识它
	 * 本方法就是用来生成这个key的
     * @param url
     * @return
     */
	protected static String serviceKey(URL url) {
	    return serviceKey(url.getPort(), url.getPath(), url.getParameter(Constants.VERSION_KEY),
                         url.getParameter(Constants.GROUP_KEY));
	}
	
	/**
	 * 生成服务的key 
	 * 
	 * 当暴露一个服务器时,这个服务对象是保存在一个全局的map中的,需要一个key来唯一标识它
	 * 本方法就是用来生成这个key的
	 * 
	 * @param port
	 * @param serviceName
	 * @param serviceVersion
	 * @param serviceGroup
	 * @return
	 */
	protected static String serviceKey(int port, String serviceName, String serviceVersion, String serviceGroup) {
		StringBuilder buf = new StringBuilder();
		if (serviceGroup != null && serviceGroup.length() > 0) {
			buf.append(serviceGroup);
			buf.append("/");
		}
		buf.append(serviceName);
		if (serviceVersion != null && serviceVersion.length() > 0 && ! "0.0.0".equals(serviceVersion)) {
			buf.append(":");
			buf.append(serviceVersion);
		}
		buf.append(":");
		buf.append(port);
		return buf.toString();
	}
	
	/**
	 * 从serviceKey中分离出接口名
	 * @param serviceKey   支持RSF标准的key格式 --- 组名/接口名:版本号:端口号
	 * @return 接口名
	 */
	protected static String splitInterfaceName(String serviceKey){
		int index_1=serviceKey.indexOf("/");
		if(index_1!=-1){
			int index_2=serviceKey.indexOf(":",index_1);
			if(index_2!=-1){
				return serviceKey.substring(index_1+1, index_2);
			}else{
				return serviceKey.substring(index_1+1,serviceKey.length());
			}
		}else{
			int index_3=serviceKey.indexOf(":");
			if(index_3!=-1){
				return serviceKey.substring(0, index_3);
			}else{
				return serviceKey;
			}
		}
	}
	
	public static void main(String[] a){
		String serviceKey="group1/UserInterface:1.3.0:63634";
		String rs=splitInterfaceName(serviceKey);
		System.out.println(rs);
		
		serviceKey="group1/63634";
		rs=splitInterfaceName(serviceKey);
		System.out.println(rs);
		
		serviceKey="UserInterface:1.3.0";
		rs=splitInterfaceName(serviceKey);
		System.out.println(rs);
		
		serviceKey="Uabcdefg";
		rs=splitInterfaceName(serviceKey);
		System.out.println(rs);
	}
}
