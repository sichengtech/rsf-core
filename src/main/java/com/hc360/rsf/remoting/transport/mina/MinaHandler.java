/**
 * MinaHandler.java   2012-5-10
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting.transport.mina;

import java.util.List;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hc360.rsf.common.Constants;
import com.hc360.rsf.common.utils.NetUtils;
import com.hc360.rsf.config.GlobalManager;
import com.hc360.rsf.config.RegistryConfig;
import com.hc360.rsf.remoting.HandlerDelegate;
import com.hc360.rsf.remoting.IpPort;
import com.hc360.rsf.remoting.exchange.support.Request;
import com.hc360.rsf.remoting.exchange.support.Response;
import com.hc360.rsf.rpc.EchoService;
import com.hc360.rsf.rpc.RpcInvocation;
import com.hc360.rsf.rpc.RpcResult;
import com.hc360.rsf.rpc.protocol.RsfProtocol;
import com.hc360.rsf.rpc.protocol.codec.AbstractCodec;

/**
 * Mina的IoHandler实现类 ,没做实质性的工作
 * 
 * 只是把所有工作都委托给了HandlerDelegate接口的实现类handler,
 * 
 * 来处理,以达到抽象Mina,Netty专有IoHandler的目的
 * 
 */
public class MinaHandler extends IoHandlerAdapter {
	private static Logger logger = LoggerFactory.getLogger(MinaHandler.class);

	private final HandlerDelegate handler;

	public MinaHandler(HandlerDelegate handler) {
		if (handler == null) {
			throw new IllegalArgumentException("handler == null");
		}
		this.handler = handler;
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		 MinaChannel channel = MinaChannel.getOrAddChannel(session);
		 try {
				//rsf 1.3.0 添加此功能     赵磊 2013-4-7
				//发送欢迎信息(telnet登录上来会看到这个信息)
				//会引RSF调用故障，所以注释了
//				try{
//					String msg="Welcome to RSF\n\r";
//					msg+="Version:"+Version.getVersion()+",Date:"+new Date()+"\n\r";
//					msg+="RSF telnet command $ ";
//					session.write(msg);
//				}catch(Exception e){
//					logger.error("连接创建事件,发送欢迎信息时异常!",e);
//				}
			 
			 //检查这条连接，是否是连接到注册中心的连接
			 //如果是，则触发“重新注册”、“重新下载”,
			 //保证在系统未重启但网络已回复时注册中心及时发现服务端
			 try{
				 long t1=GlobalManager.START_TIME;//系统启动时间
				 long t2=System.currentTimeMillis();//当前时间
				
				 IpPort ipport=channel.getRemoteIpPort();
				 List<RegistryConfig> registryConfigList = GlobalManager.registryConfigList;
				 if(registryConfigList!=null && ipport!=null){
					 for(RegistryConfig rc:registryConfigList){
						 String host=rc.getHost();
						 host=NetUtils.getIpByHost(host);
						 int port=rc.getPort();
						 if(host==null || ipport.getIp()==null){
							 break;
						 }
						 if(host.equals(ipport.getIp()) && port==ipport.getPort() ){
							 
							//只有系统启动1分钟后，才可以执行本逻辑
							 //防止系统刚刚启动，就与注册中心进行了多次重复的通信。
							 long t=1*60*1000;//1分钟
							 if(t2-t1  > t){
								/**
								 *  触发与注册中心的通信
								 *  1、向注册中心注册服务
								 *  2、向注册中心下载服务
								 */
								 logger.info("与注册中心建立了连接，触发注册服务动作、下载服务提供者动作。"+host+":"+port);
								 //参数500，单位ms,表示500ms后开始运行任务
								 //理想是：与注册中心的连接建立后，尽快的与开始执行重新注册、重新下载
								 GlobalManager.TIMER.tuchRegister(500);
								 break;
							 }else{
								 logger.info("与注册中心建立了连接,但系统启动时间未超过"+t+"ms,不触发注册服务动作、下载服务提供者动作。");
							 }
						 }
					 }
				 }
			 }catch(Exception e){
				 logger.error("错误：与注册中心建立了连接，触发注册服务动作、下载服务提供者动作",e);
			 }
			 
			 
			//HandlerDelegate 是链式代理类
			//调用“调用链”上的后续处理器
			handler.connected(channel);
		 } finally {
			 MinaChannel.removeChannelIfDisconnectd(session);
		 }
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		MinaChannel channel = MinaChannel.getOrAddChannel(session);
		
		//如果连接是可用的，就忽略
		if(channel.isConnected()){
			return;
		}
		try {
			//闭关一条连接,并从池中移除他
			//HandlerDelegate 是链式代理类
			//removeCloseChannel方法，放在第一层链被调用，说明他的重要性
			//保证以后如果“调用链”发生改动，也一定能调用到removeCloseChannel方法
			GlobalManager.getClient().getChannelPool().removeCloseChannel(channel);
			
			//HandlerDelegate 是链式代理类
			//调用“调用链”上的后续处理器
			handler.disconnected(channel);
		} finally {
			MinaChannel.removeChannelIfDisconnectd(session);
		}
	}

	/**
	 * 当有数据到达时,messageReceived()方法被调用
	 * 
	 * @param session
	 * @param message
	 * @throws Exception
	 * @see org.apache.mina.common.IoHandlerAdapter#messageReceived(org.apache.mina.common.IoSession,
	 *      java.lang.Object)
	 */
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		MinaChannel channel = MinaChannel.getOrAddChannel(session);
		// 处理回声测试
		// S端处理 C端发来“回声测试”请求，并响应C端。
		if (message instanceof Request) {
			Request request = (Request) message;
			
			Object obj= request.getData();
			if(obj!=null && obj instanceof RpcInvocation){
				RpcInvocation invocation = (RpcInvocation) request.getData();
				if(invocation!=null){
					String methodName = invocation.getMethodName();
					String path = invocation.getAttachments().get(Constants.PATH_KEY);// 接口名
					if (Constants.$ECHO.equals(methodName) && EchoService.class.getName().equals(path)) {
						Response response = new Response();
						response.setId(request.getId());
						response.setData(new RpcResult(invocation.getArguments()[0]));//把发来的参数原样返回
						channel.send(response);
						return ;
					}
					if (Constants.$ECHO_INTERFACE.equals(methodName) && EchoService.class.getName().equals(path)) {
						boolean rs=false;
						if(invocation.getArguments()!=null && invocation.getArguments()[0] !=null){
							Object arg=invocation.getArguments()[0];
							if(arg instanceof String){
								List<String> list=RsfProtocol.serviceNameList();
								for(String interfaceName:list){
									if(interfaceName.equals((String)arg)){
										rs=true;
										break;
									}
								}
							}
						}
						Response response = new Response();
						response.setId(request.getId());
						response.setData(new RpcResult(rs));//返回true:服务端提供这个服务，false:服务端未提供这个服务
						channel.send(response);
						return ;
					}
				}
			}
		}

		try {
			// 对常规请求的处理，99%会走这里。
			handler.received(channel, message);
		}catch(Throwable e){
			// S端发生异常时，也要响应C端,不然C端只能等待超时了。
			// S端发生的异常有两类
			// 第一类是业务异常，第二类是RSF本身异常，如线程池耗尽等等。
			// 本段代码，就是针对：第二类RSF本身异常 的处理，通知C端 S端发生异常了，不要再等待了。
			AbstractCodec.errorProce_ClientNotWait(message, channel, e);
		}
		finally {
			MinaChannel.removeChannelIfDisconnectd(session);
		}
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		MinaChannel channel = MinaChannel.getOrAddChannel(session);
		try {
			handler.sent(channel, message);
		} finally {
			MinaChannel.removeChannelIfDisconnectd(session);
		}
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		MinaChannel channel = MinaChannel.getOrAddChannel(session);
		//如果连接是可用的，就忽略
		if(channel.isConnected()){
			return;
		}
		try {
			//闭关一条连接,并从池中移除他
			//HandlerDelegate 是链式代理类
			//removeCloseChannel方法，放在第一层链被调用，说明他的重要性
			//保证以后如果“调用链”发生改动，也一定能调用到removeCloseChannel方法
			GlobalManager.getClient().getChannelPool().removeCloseChannel(channel);
			
			handler.caught(channel, cause);
			
		} finally {
			MinaChannel.removeChannelIfDisconnectd(session);
		}
	}

}
