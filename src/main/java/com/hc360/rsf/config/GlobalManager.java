/**
 * Config.java   2012-4-27
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.apache.mina.core.service.IoHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hc360.rsf.common.URL;
import com.hc360.rsf.registry.RecoveryHandler;
import com.hc360.rsf.registry.Timer;
import com.hc360.rsf.remoting.Client;
import com.hc360.rsf.remoting.HandlerDelegate;
import com.hc360.rsf.remoting.Server;
import com.hc360.rsf.remoting.heartbeat.HeartbeatHandlerDelegate;
import com.hc360.rsf.remoting.transport.dispather.AllChannelHandler;
import com.hc360.rsf.remoting.transport.mina.MinaClient;
import com.hc360.rsf.remoting.transport.mina.MinaHandler;
import com.hc360.rsf.remoting.transport.mina.MinaHandlerDelegate;
import com.hc360.rsf.remoting.transport.mina.MinaServer;
import com.hc360.rsf.rpc.loadbalance.ConsistentHashLoadBalance;
import com.hc360.rsf.rpc.loadbalance.LeastActiveLoadBalance;
import com.hc360.rsf.rpc.loadbalance.LoadBalance;
import com.hc360.rsf.rpc.loadbalance.RandomLoadBalance;
import com.hc360.rsf.rpc.loadbalance.RoundRobinLoadBalance;
import com.hc360.rsf.rpc.protocol.RsfProtocol;
import com.hc360.rsf.rpc.proxy.ProxyFactory;
import com.hc360.rsf.rpc.proxy.jdk.JdkProxyFactory;

/**
 * 总装配中心
 * 
 * @author zhaolei 2012-4-27
 */
public class GlobalManager {
	private static Logger logger = LoggerFactory.getLogger(GlobalManager.class);

	/**
	 * 代理工厂,选用jdk代理工厂
	 */
	public static ProxyFactory proxyFactory = new JdkProxyFactory();

	/**
	 * 协议,选用Rsf协议
	 */
	public static RsfProtocol protocol = new RsfProtocol();
	
	/**
	 * 使用的注册中心
	 */
	public static List<RegistryConfig> registryConfigList=null;

	/**
	 * 软负载策略
	 */
	public static LoadBalance randomLoad = new RandomLoadBalance();//随机
	public static LoadBalance roundRobin = new RoundRobinLoadBalance();//轮循
	public static LoadBalance leastActive = new LeastActiveLoadBalance();//最少活跃调用
	public static LoadBalance consistentHash = new ConsistentHashLoadBalance();//Hash
	/**
	 * 负载均衡器,选用随机负载均衡器
	 */
	public static LoadBalance getLoadBalance(String key){
		if("random".equalsIgnoreCase(key)){
			return randomLoad;
		}else if("roundRobin".equalsIgnoreCase(key)){
			return roundRobin;
		}else if("leastActive".equalsIgnoreCase(key)){
			return leastActive;
		}else if("consistentHash".equalsIgnoreCase(key)){
			return consistentHash;
		}else{
			return randomLoad;
		}
	}
	

	private static volatile Client client = null;
	/**
	 * 客户端线程池引用，用于关闭线程池
	 */
	public static ExecutorService executor_client;
	/**
	 * 创建Client, 供客户端使用<br>
	 * 是单实例，在客户端总共只有一个Client就够用了<br>
	 * @param url
	 * @return
	 */
	public static Client getClient() {
		if (client == null) {
			synchronized (GlobalManager.class) {
				if (client == null) {
					
					// 第四层代理
					// 真正干活的事件处理器
					MinaHandlerDelegate myHandlerDelegate = new MinaHandlerDelegate(MinaHandlerDelegate.CLIENT_SIDE);
					
					// 第三层代理
					// 把各个事件放入独立的线程中处理
					
					//线程池需要URL参数,但其中没有关于线程池的参数，使用的默认值
					URL url = new URL("rsf", "0.0.0.0", 0);
					
					HandlerDelegate hd =  new AllChannelHandler(myHandlerDelegate,url);
					if(hd instanceof AllChannelHandler){
						//用于关闭线程池
						executor_client=((AllChannelHandler)hd ).getExecutorService();
					}
					
					// 第二层代理
					// 处理心跳事件
					HandlerDelegate hhd = new HeartbeatHandlerDelegate(hd);
					
					// 第一层代理
					// Mina的事件处理器
					// 创建连接定时任务需要URL参数: ip,port
					IoHandler minaHandler = new MinaHandler(hhd);

					client = MinaClient.getClient(minaHandler);
					logger.debug("RSF Client 线程模型={}", hd.getClass());
				}
			}
		}
		return client;
	}

	/**
	 * 把启动的Server端，放入全局的MAP中
	 * 在“销毁”(close)时，取出所有Server对象，集中销毁
	 * 一般只有一个Server对象
	 */
	public static final Map<String, Server> SERVER_LIST = new ConcurrentHashMap<String, Server>();
	
	
	/**
	 * 服务端线程池引用，用于关闭线程池
	 */
	public static ExecutorService executor_server;
	/**
	 * 创建Server, 供服务端使用,一个Server只能绑定在一个端口上,一个端口上只能绑定一个Server<br>
	 * 每执行一次,先检查指定端口有没有对应的Server,有就返回Server,没有产生一个新的Server并返回Server<br>
	 * 想同时存在多个Server,要指定不同的端口<br>
	 * 
	 * @return
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public static Server startServer(URL url) throws NumberFormatException, IOException {
		
		// 本系统内URL有2类，是互相不能混用的，要注意区分
		// 第一类，Client\Server类中拥有一个全局的URL，其中的参数是全局的参数
		// 第二类，ClientConfig类createProxy()方法，有自己的URL，其中的参数是某个接口级的参数
		
		int port =url.getPort();
		Server server=SERVER_LIST.get(String.valueOf(port));
		if(server==null){
			synchronized (GlobalManager.class) {
				server=SERVER_LIST.get(String.valueOf(port));
				if(server==null){
					// 第四层代理
					// 真正干活的事件处理器
					MinaHandlerDelegate myHandlerDelegate = new MinaHandlerDelegate(MinaHandlerDelegate.SERVER_SIDE);
					
					// 第三层代理
					// 把各个事件放入独立的线程中处理
					
					HandlerDelegate hd = new AllChannelHandler(myHandlerDelegate,url);
					if(hd instanceof AllChannelHandler){
						//用于关闭线程池
						executor_server=((AllChannelHandler)hd ).getExecutorService();
					}
					
					//第二层代理
					// 处理心跳事件
					// 心跳任务需要URL参数：两次心跳时间间隔
					HandlerDelegate hhd = new HeartbeatHandlerDelegate(hd);
					
					//第一层代理
					// Mina的事件处理器
					// 编码器需要URL参数：payload(请求及响应数据包大小限制)
					IoHandler minaHandler = new MinaHandler(hhd);
					// 编码器需要URL参数：payload(请求及响应数据包大小限制)
					server = new MinaServer(port, minaHandler,url);
					logger.debug("RSF Server 线程模型={}", hd.getClass());
					SERVER_LIST.put(String.valueOf(port),server);
				}
			}
		}
		return server;
	}
	
	/**
	 * 全局唯一Timer
	 * 定时向注册中心发布服务，定时下载服务提供者列表
	 */
	public static final Timer TIMER=new Timer(); 
	
	/**
	 * 全局启动时间
	 */
	public static final long START_TIME=System.currentTimeMillis();
	
	/**
	 * 客户端定时尝试从可不用连接服务列表恢复节点数据到可用服务列表
	 * 由于是服务列表的操作，感觉放到channelPool里不合适
	 */
	public static final RecoveryHandler RECOVERY_HANDLER = new RecoveryHandler();
}
