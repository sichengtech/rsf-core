/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hc360.rsf.common.utils.NamedThreadFactory;
import com.hc360.rsf.config.ClientConfig;
import com.hc360.rsf.config.RegistryConfig;
import com.hc360.rsf.config.ServiceConfig;

/**
 * 定时任务 （全局只需要new一个实例）
 * 
 * 服务端定时向注册中心注册服务
 * 客户端定时向注册中心下载服务的信息
 * 
 * @author zhaolei 2012-6-21
 */
public class Timer {
	private static Logger logger = LoggerFactory.getLogger(Timer.class);
	private static final int HEART_BEAT=60*1000;//1分钟，执行定任务的时间间隔
	
	/**
	 *	全部要注册的服务
	 *	key:注册中心IP:port
	 *	value:ServiceConfig列表  [ServiceConfig,ServiceConfig,ServiceConfig]
	 */
	List<ServiceConfig<?>> serviceConfigList=new ArrayList<ServiceConfig<?>>();
	/**
	 *	全部要订阅（下载）的服务
	 *	key:注册中心IP:port
	 *	value:ServiceName列表 [ClientConfig,ClientConfig,ClientConfig]
	 */
	private  Map<String,List<ClientConfig<?>>> clientConfigMap=new ConcurrentHashMap<String,List<ClientConfig<?>>>();;
	
	/**
	 * 定时器，
	 */
	private ScheduledFuture<?> heatbeatTimer;
	
	/**
	 * 构造方法
	 */
	public Timer(){
		//启动任务
		startHeatbeatTimer(null);
	}
	
	/**
	 * 关闭
	 * 1、停止任务
	 * 2、停止线程池中的线程
	 */
	public void close(){
		stopHeartbeatTimer();
		if(scheduledHeartbeat!=null){
			scheduledHeartbeat.shutdownNow();
		}
	}
	
	/**
	 * 执行定时任务的线程，守护线程
	 */
	private final ScheduledExecutorService scheduledHeartbeat = Executors.newScheduledThreadPool(1,
			new NamedThreadFactory("rsf-timer", true));
	/**
	 * 启动“定时注册”定时任务
	 * 每隔3分钟，向注册中心注册一次服务
	 * 
	 * @param initialDelay 第一次任务运行的时间，延后多长时间运行第一轮任务.如果为null则使用HEART_BEAT的值
	 */
	
	private void startHeatbeatTimer(Integer initialDelay) {
		stopHeartbeatTimer();
		
		try{
			logger.info("启动“定时注册”定时任务");
		} catch (Throwable e1) {
			System.out.println("RSF写日志异常，");
			e1.printStackTrace();
		}
		
		// 每heartbeat毫秒执行一次任务,两次任务的执行间隔是heartbeat毫秒+任务执行用时
		heatbeatTimer = scheduledHeartbeat.scheduleWithFixedDelay(new Runnable() {
			public void run() {
				try{
					if(serviceConfigList!=null ){
						
						RegistryFactory.regBatch(serviceConfigList);
						logger.debug("Timer定时任务执行,向注册中心注册服务,数量："+(serviceConfigList==null?0:serviceConfigList.size()));
					}
				} catch (Throwable e) {
					//有必要拦截Throwable
					try{
						logger.error("Timer定时任务执行异常--向注册中心注册服务", e);
					} catch (Throwable e1) {
						System.out.println("RSF写日志异常");
						e1.printStackTrace();
					}
				}
				try{
					for(String addr:clientConfigMap.keySet()){
						
						List<ClientConfig<?>> list=clientConfigMap.get(addr);
						for(ClientConfig<?> bean:list){
							//下载服务列表 ，并保存服务列表到ServiceProviderList
							try {
								RegistryFactory.download(bean);
							} catch (Exception e) {
							    logger.error("下载服务提供者列表异常",e);
							}
						}
						logger.debug("Timer定时任务执行,向注册中下载服务提供者列表,数量："+(list==null?0:list.size()));
					}
				} catch (Throwable e) {
					//有必要拦截Throwable
					try{
						logger.error("Timer定时任务执行异常--向注册中下载服务提供者列表", e);
					} catch (Throwable e1) {
						System.out.println("RSF写日志异常");
						e1.printStackTrace();
					}
				}
			}
		}, initialDelay==null?HEART_BEAT:initialDelay, HEART_BEAT,
				TimeUnit.MILLISECONDS);
	}
	
	/**
	 * 停止 任务
	 */
	private void stopHeartbeatTimer() {
		if (heatbeatTimer != null && !heatbeatTimer.isCancelled()) {
			try {
				heatbeatTimer.cancel(true);
				logger.info("关闭Timer定时任务");
			} catch (Throwable e) {
				try{
					logger.warn(e.getMessage(), e);
				} catch (Throwable e1) {
					System.out.println("RSF写日志异常");
					e1.printStackTrace();
				}
			}
		}
		heatbeatTimer = null;
	}

	public List<ServiceConfig<?>> getServiceConfigList() {
		return serviceConfigList;
	}

	public void setServiceConfigList(List<ServiceConfig<?>> serviceConfigList) {
		this.serviceConfigList = serviceConfigList;
	}
	
	public Map<String, List<ClientConfig<?>>> getClientConfigMap() {
		return clientConfigMap;
	}
	/**
	 * 通过服务名，找出ClientConfig
	 * @param interfaceName 服务名、接口名
	 * @return ClientConfig
	 */
	public ClientConfig<?> getClientConfig(String interfaceName) {
		if(interfaceName==null){
			return null;
		}
		Map<String, List<ClientConfig<?>>> clientConfigMap=getClientConfigMap();
		for(String key:clientConfigMap.keySet()){
			List<ClientConfig<?>> list=clientConfigMap.get(key);
			for(ClientConfig<?> cc:list){
				String name=cc.getInterfaceClass().getName();
				if(interfaceName.equals(name)){
					return cc;
				}
			}
		}
		return null;
	}
	
	public void setClientConfigMap(Map<String, List<ClientConfig<?>>> subscribe_map) {
		this.clientConfigMap = subscribe_map;
	}
	
	/** 
	 * 记录多个“要注册的服务”
	 * 追加,做排重工作 
	 * @param serviceConfigList
	 */
	public void appendServiceConfigList(List<ServiceConfig<?>> list){
		if(serviceConfigList!=null){
			for(ServiceConfig<?> sc_outer : list){
				if( ! serviceConfigList.contains(sc_outer)){
					serviceConfigList.add(sc_outer);
				}
			}
		}
	}

	/**
	 * 记录一个“要下载的服务”，会排重
	 * @param serviceName
	 * @param ip
	 * @param port
	 */
	public void appendClientConfig(ClientConfig<?> bean){
		String addr=null;
		List<RegistryConfig> list_r=bean.getRegistries();
		if(list_r!=null && list_r.size()>0){
			//有多个注册中心,目前只能使用第一个。
			//只向第一个注册中心注册，
			//如果以后有了多个注册中心，请改造这里
			RegistryConfig rc=list_r.get(0);
			addr=rc.getHost()+":"+rc.getPort();
		}
		
		if(addr==null){
			return ;
		}
		List<ClientConfig<?>> list=clientConfigMap.get(addr);
		if(list==null){
			synchronized (this) {
				list=clientConfigMap.get(addr);
				if(list==null){
					list=new ArrayList<ClientConfig<?>>();
					clientConfigMap.put(addr, list);
				}
			}
		}
		if(!list.contains(bean)){
			list.add(bean);
		}
	}
	
	/**
	 *  触发与注册中心的通信
	 *  1、向注册中心注册服务
	 *  2、向注册中心下载服务
	 */
	public void tuchRegister(Integer initialDelay){
		stopHeartbeatTimer();
		startHeatbeatTimer(initialDelay);
	}
}
