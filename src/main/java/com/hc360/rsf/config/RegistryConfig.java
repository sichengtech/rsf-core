/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config;

/**
 *  RegistryConfig 注册中心
 * 
 * @author zhaolei 2012-5-18
 */
public class RegistryConfig {
	String host;
	int port;
	int timeout;
	
	public RegistryConfig(){ }
	
	public RegistryConfig(String host,int port,int timeout){
		this.host=host;
		this.port=port;
		this.timeout=timeout;
	}

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

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
}
