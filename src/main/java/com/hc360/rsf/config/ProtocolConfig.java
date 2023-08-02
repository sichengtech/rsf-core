/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config;

/**
 * ProotocolConfig 协议
 * 
 * @author zhaolei 2012-5-18
 */
public class ProtocolConfig {

	// 服务IP地址(多网卡时使用)--目前没用到
	private String host;

	/*
	 * 服务端，监听的端口，默认监听63634端口
	 * 旧机制使用单一端口，适合于点对点的通信场景
	 */
	private int port;
	
	/*
	 * 服务端协议--默认监听端口段中的一个可用端口
	 * 解决模拟分布式环境中，一台物理服务上运行多个同一个项目的逻辑节点，端口被占用问题
	 * 如果63634被占用，按顺序尝试其它端口，最终选择一个可用的端口
	 * 
	 * 新机制使用端口段中的一个可用端口，适合于有“服务注册中心”的场景
	 * 为了兼容旧机制，port还要保留，因为有RSF的早期用户（配置管理中心）项目使用了setPort(),getPort()方法,设置端口
	 * 但以后将优先使用ports数组
	 */
	private Integer[] ports;

	// 协议名称
	private String name;

	// 心跳间隔(单位 ms)
	//private Integer heartbeat;

	//请求及响应数据包大小限制,单位：字节
	private int payload;
	
	//线程池类型,可选：fixed/cached
	private String threadpool;

//	//服务线程池大小(固定大小) 1.1 版本起就有了这个属性
//	private int threads;
	
	//线程池的基本大小,从1.3.0版本起加入的属性
	private Integer corePoolSize;
	//线程池最大大小,从1.3.0版本起加入的属性
	private Integer maximumPoolSize;
	//线程池任务队列大小,从1.3.0版本起加入的属性
	private Integer queueSize;
	//线程活动保持时间,从1.3.0版本起加入的属性
	private Integer keepalive;
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

//	public int getThreads() {
//		return maximumPoolSize;
//	}
//
//	public void setThreads(int threads) {
//		this.maximumPoolSize = threads;
//	}

	public int getPayload() {
		return payload;
	}

	public void setPayload(int payload) {
		this.payload = payload;
	}

	public String getThreadpool() {
		return threadpool;
	}

	public void setThreadpool(String threadpool) {
		this.threadpool = threadpool;
	}

	public Integer[] getPorts() {
		return ports;
	}

	public void setPorts(Integer[] ports) {
		this.ports = ports;
	}

	public Integer getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(Integer corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public Integer getMaximumPoolSize() {
		return maximumPoolSize;
	}

	public void setMaximumPoolSize(Integer maximumPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
	}

	public Integer getQueueSize() {
		return queueSize;
	}

	public void setQueueSize(Integer queueSize) {
		this.queueSize = queueSize;
	}

	public Integer getKeepalive() {
		return keepalive;
	}

	public void setKeepalive(Integer keepalive) {
		this.keepalive = keepalive;
	}
}
