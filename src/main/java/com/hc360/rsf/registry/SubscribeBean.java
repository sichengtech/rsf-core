/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.registry;

import java.io.Serializable;

/**
 * 下载服务提供者信息的封装类
 * 订阅信息的封装
 * 
 * @author zhaolei 2012-6-25
 */
public class SubscribeBean implements Serializable{
	private static final long serialVersionUID = 1L;
	
	String serviceName;// 服务名
	String portalId;// 系统标识
	String ip;//不要使用本属性，  请使用AddressTool.getRemoteAddress()方法取得IP
	int port;//不要使用本属性，请使用AddressTool.getRemoteAddress()方法取得port
	String clietnTime;//启动时间
	String jarVersion;//RSF jar包的版本
	
	public String toString(){
		StringBuilder sbl=new StringBuilder();
		sbl.append("serviceName=");
		sbl.append(serviceName);
		sbl.append(",portalId=");
		sbl.append(portalId);
		sbl.append(",ip=");
		sbl.append(ip);
		sbl.append(",port=");
		sbl.append(port);
		sbl.append(",clietnTime=");
		sbl.append(clietnTime);
		sbl.append(",jarVersion=");
		sbl.append(jarVersion);
		return sbl.toString();
	}
	
	
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getPortalId() {
		return portalId;
	}
	public void setPortalId(String portalId) {
		this.portalId = portalId;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getClietnTime() {
		return clietnTime;
	}
	public void setClietnTime(String clietnTime) {
		this.clietnTime = clietnTime;
	}
	public String getJarVersion() {
		return jarVersion;
	}
	public void setJarVersion(String jarVersion) {
		this.jarVersion = jarVersion;
	}
	
}
