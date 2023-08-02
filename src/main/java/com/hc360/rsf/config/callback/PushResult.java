/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.callback;

/**
 * 推送结果
 * 
 * @author zhaolei 2012-5-16
 */
public class PushResult {
	String ip;// 目标IP
	int port;// 目标端口
	boolean isSuccess=false;// 是否成功
	ChannelState causeFailing;// 失败原因
	
	public PushResult(){}
	
	public PushResult(boolean isSuccess,ChannelState causeFailing){
		this.isSuccess=isSuccess;
		this.causeFailing=causeFailing;
	}
	
	public String toString(){
		StringBuilder sbl=new StringBuilder();
		sbl.append("ip:");
		sbl.append(ip);
		sbl.append(",");
		sbl.append("port:");
		sbl.append(port);
		sbl.append(",");
		sbl.append("isSuccess:");
		sbl.append(isSuccess);
		sbl.append(",");
		sbl.append("causeFailing:");
		sbl.append(causeFailing);
		return sbl.toString();
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

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public ChannelState getCauseFailing() {
		return causeFailing;
	}

	public void setCauseFailing(ChannelState causeFailing) {
		this.causeFailing = causeFailing;
	}

	/**
	 * 失败原因
	 * 
	 */
	public enum ChannelState {

		/**
		 * 超时
		 */
		TIMEOUT,

		/**
		 * 连接已关闭
		 */
		CONNECTION_CLOSED,

		/**
		 * 接收端异常
		 */
		EXCEPTION,
		
		/**
		 * 无效参数
		 */
		INVALID_ARGUMENTS
	}
}
