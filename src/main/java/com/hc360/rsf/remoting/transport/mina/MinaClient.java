/**
 * ClientAPI.java   2012-4-9
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting.transport.mina;

import java.net.InetSocketAddress;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hc360.rsf.common.Constants;
import com.hc360.rsf.common.URL;
import com.hc360.rsf.common.Version;
import com.hc360.rsf.config.GlobalManager;
import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.ChannelPool;
import com.hc360.rsf.remoting.Client;
import com.hc360.rsf.rpc.protocol.codec.JavaCodec;
import com.hc360.rsf.rpc.protocol.codec.mina.MinaCodecAdapter;

/**
 * Client接口的实现 
 * 作用：Mina网络通信的客户端
 * 		提供给业务开发人员使用的 客户端类
 * 		业务开发人员使用request()方法,发出一次请求,并可接收返回值
 * 
 * @author zhaolei 2012-4-9
 */
public class MinaClient implements Client {
	private static Logger logger = LoggerFactory.getLogger(MinaClient.class);
	private static MinaClient minaClient;
	private IoConnector connector = null;
	private IoHandler handler;
	private ChannelPool channelPool=new ChannelPool();
	//@newModify
	private Object lock = new Object();
	
	protected MinaClient(IoHandler handler) {
		this.handler = handler;
		if (handler == null) {
			String msg="RSF Client 异常,handler=null";
			IllegalArgumentException e=new IllegalArgumentException(msg);
			logger.error("",e);
			throw e;
		}

		// 创建IoConnector(连接器)
		connector = new NioSocketConnector(Runtime.getRuntime().availableProcessors());
		connector.getSessionConfig().setMinReadBufferSize(64);
		connector.getSessionConfig().setReadBufferSize(16384);
		connector.getSessionConfig().setMaxReadBufferSize(65536);//对并发时的吞吐量有很大影响 128k=131072,64k=65536
		connector.setConnectTimeoutMillis(5 * 1000);//创建连接的超时时间
		
		// 这个URL目前没有用,有待处理,用于取参数,但没取到,用的默认值
		URL url = new URL("rsf", "0.0.0.0", 0);
		
		// 自定义的数据编码解码器
		MinaCodecAdapter factory=new MinaCodecAdapter(new JavaCodec(),url,null);
		
		// 设置ioFilter
		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(factory));
		
		// 设置处理器
		connector.setHandler(handler);
		if(logger.isInfoEnabled()){
			logger.info("RSF Client 启动完成,版本:{}.",Version.getVersion());
		}
	}

	/**
	 * 取得Mina客户端
	 * 
	 * @param serverIP
	 *            服务器IP
	 * @param serverPort
	 *            服务器端口 ---String serverIP,int serverPort,
	 * @param handler
	 *            处理器
	 * @return
	 */
	public static synchronized MinaClient getClient(IoHandler handler) {
		if (minaClient == null) {
			minaClient = new MinaClient(handler);
		}
		return minaClient;
	}

	/**
	 * 通过URL从Client中取出 可以连接到指定服务器(serverIP,serverPort)的Channel.
	 * 如果没有相应的Channel,就创建新的Channel再返回这个Channel.
	 * 创建失败会抛异常
	 * 
	 * @param url 主要从中取出server IP,server Port
	 * @return
	 */
	public Channel getOrCreateChannel(String ip,int port) {
		Channel channel_old=getChannelFromPool(ip,port);
		if(channel_old!=null){
			return channel_old;
		}else{
			synchronized (lock) {
				channel_old=getChannelFromPool(ip,port);
				if(channel_old!=null){
					return channel_old;
				}else{
					// 创建连接--TCP连接
					ConnectFuture connectFuture=null;
					IoSession session=null;
					String serverIP=ip;//服务器IP
					int serverPort=port;//服务器port
					if(serverPort==0){
						serverPort=Constants.SERVER_PROTOCOL_PORT;
					}
					String channelKey=serverIP+":"+serverPort;//key
					try{
						//System.out.println("client创建连接开始:"+url);
						connectFuture = connector.connect(new InetSocketAddress(serverIP, serverPort));
						connectFuture.awaitUninterruptibly();// 等待连接创建完成
						session = connectFuture.getSession();
					}catch(Exception e){
						//logger.error("无法连接到远端主机--{}:{}",new Object[]{serverIP,serverPort});
						throw new RuntimeIoException("无法连接到远端主机,"+serverIP+":"+serverPort,e);
					}
		
					MinaChannel channel_new = MinaChannel.getOrAddChannel(session);
					Channel channel_old2=getChannelFromPool(serverIP,serverPort);//可能就在刚刚这个连接已被其它线程创建成功了,放入池前再检查一下
					if(channel_old2==null){
						channelPool.putChannel(channelKey,channel_new);//放入集合
						return channel_new;
					}else{
						//System.out.println("关闭重复连接:"+channel_new);
						channel_new.close("关闭重复连接");//关掉重复连接
						return channel_old2;
					}
				}
			}
		}
	}
	
	/**
	 * 取得连接池
	 */
	public ChannelPool getChannelPool(){
		return channelPool;
	}
	
	/**
	 * 通过URL从Client中取出 可以连接到指定服务器(serverIP,serverPort)的Channel.
	 * 如果没有相应的Channel,就返回空
	 * @param ip 服务器IP
	 * @param port 服务器port
	 * @return
	 */
	public Channel getChannelFromPool(String ip,int port) {
		String serverIP=ip;//服务器IP
		int serverPort=port;//服务器port
		if(serverPort==0){
			serverPort=Constants.SERVER_PROTOCOL_PORT;
		}
		String channelKey=serverIP+":"+serverPort;//key
		//System.out.println("--取连接--："+channelKey);
		return channelPool.getChannel(channelKey);
	}
	
	/**
	 * 关闭Client
	 */
	public void close() {
		//关闭连接池
		try{
			channelPool.closePool();//内部有日志输出
		}catch(Exception e){
			logger.error("关闭连接池异常",e);
			//System.out.println("关闭连接池异常,"+e.getMessage());
		}
		//关闭客户端线程池
		try{
			if(GlobalManager.executor_client!=null){
				GlobalManager.executor_client.shutdown();
				GlobalManager.executor_client=null;
				logger.info("关闭客户端线程池(log4j)");
				//System.out.println("关闭客户端线程池(systemOut)");
			}
		}catch(Exception e){
			logger.error("关闭客户端线程池异常",e);
			//System.out.println("关闭客户端线程池异常,"+e.getMessage());
		}
		
		//关闭mina
		if (connector != null && (!connector.isDisposed())) {
			try{
				connector.dispose(true);//等待处理完工作，再关闭.可能mina自己的线程池中还有任务要执行，需要等待执行完成
				logger.info("关闭 Mina Client");
				//System.out.println("关闭 Mina Client");
			}catch(Exception e){
				logger.error("关闭 Mina Client异常",e);
				//System.out.println("关闭 Mina Client异常,"+e.getMessage());
			}
		}
	}
	
	/**
	 * toString()
	 * 
	 * @return
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		StringBuilder sbl=new StringBuilder();
		sbl.append("Client=MinaClient,");
		sbl.append(channelPool.debugInfo());
		return sbl.toString();
	}

	public IoHandler getHandler() {
		return handler;
	}

	public void setHandler(IoHandler handler) {
		this.handler = handler;
	}
}
