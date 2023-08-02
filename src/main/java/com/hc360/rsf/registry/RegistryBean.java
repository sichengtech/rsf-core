/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.registry;

import java.io.Serializable;

/**
 * 封装一条注册信息
 * 
 * @author zhaolei 2012-6-12
 */
public class RegistryBean implements Serializable{
	public static int ADD_NODE=1;//1：加节点
	public static int DECR_NODE_BREAKDOWN=-1;//  -1：故障减节点
	public static int DECR_NODE_MANUAL=-2;// -2：人工减节点
	
	private static final long serialVersionUID = 1L;
	
	String serviceName;// 服务名
	String portalId;// 系统标识
	String layer;// 服务所在层
	String descibe;// 服务接口总体功能描述
	String displayName;//显示名称
	String owner;// 服务发布人
	String department;// 服务发布人部门
	String version;// 服务接口版本
	String ip;//服务提供者的ip,   请使用AddressTool.getRemoteAddress()方法取得IP
	int port;//服务提供者的端口
	int weights=100;//权重
	String token;//令牌
	String url;//URL
	String jarVersion;//RSF jar包的版本
	
	//@newAdd 
	int clientPort;  //服务提供者与注册中心连接所用的端口
	
	/*
	 * 操作标识  1：加节点、  -1：故障减节点、-2：人工减节点
	 * 以上状态与数据库是一致的。
	 */
	int stat;
	
	public String toString(){
		StringBuilder sbl=new StringBuilder();
		sbl.append("RegistryBean[");
		sbl.append("stat=");
		sbl.append(stat==ADD_NODE?"加节点":stat==DECR_NODE_BREAKDOWN?"故障减节点":stat==DECR_NODE_MANUAL?"人工减节点":"无");
		sbl.append(",displayName=");
		sbl.append(displayName);
		sbl.append(",serviceName=");
		sbl.append(serviceName);
		sbl.append(",portalId=");
		sbl.append(portalId);
		sbl.append(",layer=");
		sbl.append(layer);
		sbl.append(",descibe=");
		sbl.append("内容太长略...");
		sbl.append(",owner=");
		sbl.append(owner);
		sbl.append(",department=");
		sbl.append(department);
		sbl.append(",version=");
		sbl.append(version);
		sbl.append(",ip=");
		sbl.append(ip);
		sbl.append(",port=");
		sbl.append(port);
		sbl.append(",weights=");
		sbl.append(weights);
		sbl.append(",token=");
		sbl.append(token);
		sbl.append(",jarVersion=");
		sbl.append(jarVersion);
		sbl.append("]");
		return sbl.toString();
	}
	
	
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String service_name) {
		this.serviceName = service_name;
	}
	public String getPortalId() {
		return portalId;
	}
	public void setPortalId(String portal_id) {
		this.portalId = portal_id;
	}
	public String getLayer() {
		return layer;
	}
	public void setLayer(String layer) {
		this.layer = layer;
	}
	public String getDescibe() {
		return descibe;
	}
	public void setDescibe(String descibe) {
		this.descibe = descibe;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
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

	public int getWeights() {
		return weights;
	}

	public void setWeights(int weights) {
		this.weights = weights;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getStat() {
		return stat;
	}

	public void setStat(int stat) {
		this.stat = stat;
	}


	public String getJarVersion() {
		return jarVersion;
	}


	public void setJarVersion(String jarVersion) {
		this.jarVersion = jarVersion;
	}


	public String getDisplayName() {
		return displayName;
	}


	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

    //@modify
	public int getClientPort() {
		return clientPort;
	}


	public void setClientPort(int clientPort) {
		this.clientPort = clientPort;
	}
}
