/**
 * RsfInvokerClientP2p.java   2012-4-27
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.rpc.protocol;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hc360.rsf.common.Constants;
import com.hc360.rsf.common.URL;
import com.hc360.rsf.config.ClientMethodConfig;
import com.hc360.rsf.config.GlobalManager;
import com.hc360.rsf.config.callback.AddressTool;
import com.hc360.rsf.registry.Provider;
import com.hc360.rsf.registry.ServiceProviderList;
import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.Client;
import com.hc360.rsf.remoting.RemotingException;
import com.hc360.rsf.remoting.exchange.support.Request;
import com.hc360.rsf.remoting.exchange.support.Response;
import com.hc360.rsf.remoting.transport.mina.MinaClient;
import com.hc360.rsf.rpc.DataTooLengthException;
import com.hc360.rsf.rpc.Invoker;
import com.hc360.rsf.rpc.RpcException;
import com.hc360.rsf.rpc.RpcInvocation;
import com.hc360.rsf.rpc.RpcResult;

/**
 * Invoker接口的实现
 * 
 * 作用：在客户端使用，客户端“发起调用”，具有点对点通信能力。
 * 
 * @author zhaolei 2012-4-27
 */
public class RsfInvokerClientP2p<T> implements Invoker<T> {
	public final static DecimalFormat DF = new DecimalFormat("##########0.000");
	private static Logger logger = LoggerFactory.getLogger(RsfInvokerClientP2p.class);
	protected final Class<T> serviceType;
	protected final URL url;//点对点直连时使用的URL
	protected List<ClientMethodConfig> clientMethodList_info;//接口中需要单独说明的方法

	public RsfInvokerClientP2p(Class<T> serviceType, URL url) {
		this.serviceType = serviceType;
		this.url = url;
	}

	/**
	 * client完成网络通信
	 * 
	 * 真正发出调用,通过网络传输到服务器端
	 * 
	 * @param invocation
	 * @return
	 * @see com.hc360.rsf.rpc.Invoker#invoke(com.hc360.rsf.rpc.RpcInvocation)
	 */
	public RpcResult invoke(RpcInvocation invocation) {
		//用URL信息创建一个连接
		//子类复写了本方法，实现了软负载，选择连接
		Channel channel = doInvoker(invocation);
		
		//三次握手(选择性执行) 
		String security=url.getParameter(Constants.ISSECURITY_KEY);
		if("true".equals(security)){
			String session_key=(String)channel.getAttribute(Constants.SESSION_KEY);
			if(session_key==null){
				//需要进行三次握手
				channel.shakeHands();
			}
		}
		
		//把连接放入当前线程，客户端可以通过AddressTool工具取得本端与远端的ip\port
		//在这里put,后续的代码也没有remove,业务上要求不remove,经过分析是安全的，不用担心内存问题。
		AddressTool.putChannel(channel);
		
		if (!isAsynchronous(invocation)) {
			// 同步调用
			Object res = null;
			Response response =null;
			long t1=System.nanoTime();
			try {
				// 发出请求，并返回结果
				int timeout=url.getParameter(Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT);
				if("true".equals(security)){
					res = channel.request(invocation,timeout,true);//加密
				}else{
					res = channel.request(invocation,timeout,false);//非加密
				}
			} catch (DataTooLengthException e1) {
				//String msg = "调用" + serviceType.getName() + " " + invocation.getMethodName() + "()时异常,"+channel;
				throw e1;
			} catch (RemotingException e1) {
				String msg = "调用" + serviceType.getName() + " " + invocation.getMethodName() + "()时异常,"+channel;
				logger.error(msg,e1);
				throw new RpcException(RpcException.TIMEOUT_EXCEPTION,msg, e1);
			}
			RpcResult rpcResult=null;
			if (res instanceof Response) {
				// 在进入这里之前,已处理服务端超时异常、服务端业务异常。
				// 如果有以上异常,就会在前一步抛出,不会进入这里。
				response = (Response) res;
				rpcResult= (RpcResult) response.getData();
			}else{
				rpcResult=new RpcResult(res);
			}
			
			//拼接日志文本
			if(logger.isInfoEnabled()){
				long t2=System.nanoTime();
				String time_server=null;
				if(rpcResult.getTime()==-1){
					time_server="不支持";
				}else{
					time_server=DF.format(rpcResult.getTime()/Constants.TIME_C)+"ms";
				}
				String time_net=null;
				if(rpcResult.getTime()==-1){
					time_net="不支持";
				}else{
					time_net=DF.format(((t2-t1-rpcResult.getTime())/Constants.TIME_C ))+"ms";
				}
				int size=0;
				if(response!=null){
					size=response.getSize();
				}
				logger.info("调用完成,收到数据包:"+size+"Byte,总耗时:"+DF.format((t2-t1)/Constants.TIME_C)+"ms,业务耗时:"+time_server+",网络耗时:"+time_net+",目标:"+serviceType.getName() + "#" + invocation.getMethodName() + "(),"+channel.getLocalAddress()+"->"+channel.getRemoteAddress());
			}
			return rpcResult;
		} else {
			// 异步调用无返回值以实现  ，有回调函数的异步调用目前未实现
			
			// 准备数据
			Request request  = new Request();
			request.setData(invocation);
			request.setTwoWay(false);//单向通信
			if("true".equals(security)){
				request.setSecurity(true);
			}
			// 发送数据
			try {
				logger.info("发起异步调用 channel："+channel+",url:"+url.toString());
				channel.send(request);
			} catch (RemotingException e) {
				String msg="发起异常调用异常 channel："+channel+",url:"+url.toString();
				logger.error(msg,e);
				throw new RuntimeException(msg,e);
			}
			return new RpcResult();
		}
	}
	
	/**
	 * 判断某个方法，是否是发起异步调用 
	 * @param invocation
	 * @return
	 */
	private boolean isAsynchronous(RpcInvocation invocation){
		String methodName=invocation.getMethodName();
		if(clientMethodList_info==null){
			return false;
		}
		for(ClientMethodConfig methodConfig: clientMethodList_info){
			if(methodConfig.isAsync()){
				if(methodName.equals(methodConfig.getName())){
					Class[] p=methodConfig.getParameterTypes();
					Class[] b=invocation.getParameterTypes();
					if(p!=null && b!=null){
						if(p.length==b.length){
							for(int i=0;i<p.length;i++){
								if( ! p[i].equals(b[i])){
									return false;
								}
								return true;
							}
						}
					}else{
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 *  通过URL取出连接,取不到连接时,就会创建新连接,创建失后败抛异常。<br>
	 *  <br>
	 *  子类会重写本方法,添加集群能力。从注册中心下载服务列表,连接多选 一,做软负载<br>
	 *  <br>
	 * @param url
	 * @return
	 */
	protected Channel doInvoker(RpcInvocation invocation){
		Client client = GlobalManager.getClient();
		Channel channel = client.getOrCreateChannel(url.getIp(),url.getPort());		
		if (channel == null || !channel.isConnected()){
			System.out.println(
					MessageFormat
					.format("channel is not avalible[p2p], url: {0}, threadnum: {1};",
							url.getIp() +":"+ url.getPort(), Thread.currentThread()));
		}		
		return channel;		
	}

	/**
	 * function description
	 * 
	 * @return
	 * @see com.hc360.rsf.rpc.Invoker#getUrl()
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * function description
	 * 
	 * @return
	 * @see com.hc360.rsf.rpc.Invoker#getInterface()
	 */
	public Class<T> getInterface() {
		return serviceType;
	}

	public List<ClientMethodConfig> getClientMethodList_info() {
		return clientMethodList_info;
	}

	public void setClientMethodList_info(
			List<ClientMethodConfig> clientMethodList_info) {
		this.clientMethodList_info = clientMethodList_info;
	}
	
	//@modify @date 2013-05-28
	/**
	 * 用于测试
	 * 
	 * @param serviceName 接口名
	 */
	protected void logException(String serviceName,String message) {
		System.out.println(message);
		List<Provider> provider_list = ServiceProviderList.findServiceList(serviceName);
		
		//由于得到client时需要iohandler，同时minaclient只会有一个，且再之前已经初始化，所以给null值应该不会出错
		String channelMap_debugInfo = MinaClient.getClient(null).getChannelPool().debugInfo();
		//Map<String, Channel> channelMapFail = MinaClient.getClient(null).getChannelPool().getChannelMapFail();
		
		StringBuffer sb = new StringBuffer();
		int index = 0;
		sb.append(", providerlist: ");
		if (provider_list != null){
			for (Provider urltmp : provider_list){
				sb.append(", index: " + index + ", node: " + urltmp.getIp() + ":"+urltmp.getPort() + "; ");
				index++;
			}			
		}
		
		index = 0;
		sb.append(", active channel: ");
		sb.append(channelMap_debugInfo);
		
		System.out.println(MessageFormat.format("no channel[in invoker]. serviceName: {0}, " + sb.toString(), serviceName));	
	}
}
