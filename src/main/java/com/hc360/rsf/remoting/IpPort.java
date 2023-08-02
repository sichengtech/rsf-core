package com.hc360.rsf.remoting;

public class IpPort {
	String ip;
	int port;
	
	public String toString(){
		return ip+":"+port;
	}
	
	public IpPort(String ip ,int port){
		this.ip=ip;
		this.port=port;
	}
	public IpPort(){
		
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
}
