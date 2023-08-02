/**
 * Client.java   2012-4-27
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting;

import com.hc360.rsf.common.URL;

/**
 * 网络通信的客户端接口
 * 
 * 可以有多种实现 ,如mina,netty
 * 
 * Client主要起管理作用,真正的收发数据是由Channel来完成的.
 * 
 * @author zhaolei 2012-4-27
 */
public interface Client {
	
	/**
	 * 通过URL从Client中取出 可以连接到指定服务器(serverIP,serverPort)的Channel.
	 * 如果没有相应的Channel,就创建新的Channel再返回这个Channel.
	 * 创建失败会抛异常
	 * 
	 * @param url 主要从中取出server IP,server Port
	 * @return
	 */
//	public Channel getOrCreateChannel(URL url);
	public Channel getOrCreateChannel(String ip,int port);
	
	/**
	 * 通过URL从Client中取出 可以连接到指定服务器(serverIP,serverPort)的Channel.
	 * 如果没有相应的Channel,就返回空
	 * @param url
	 * @return
	 */
//	public Channel getChannel(URL url);
	public Channel getChannelFromPool(String ip,int port);
	
	/**
	 * 关闭Client
	 */
	public void close();
	
	/**
	 * 取得连接池
	 */
	public ChannelPool getChannelPool();
	
}
