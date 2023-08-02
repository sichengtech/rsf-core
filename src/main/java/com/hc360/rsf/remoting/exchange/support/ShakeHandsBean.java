/**
 * Copyright(c) 2000-2013 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting.exchange.support;

import java.io.Serializable;

/**
 * 封装三次握手的数据 
 * 
 * @author zhaolei 2013-3-27
 */
public class ShakeHandsBean implements Serializable{
	private static final long serialVersionUID = 1L;
	private byte[] singPackage;//请求签名包、应答签名包
	private String systemName;//业务系统的name
	public byte[] getSingPackage() {
		return singPackage;
	}
	public void setSingPackage(byte[] singPackage) {
		this.singPackage = singPackage;
	}
	public String getSystemName() {
		return systemName;
	}
	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}
}
