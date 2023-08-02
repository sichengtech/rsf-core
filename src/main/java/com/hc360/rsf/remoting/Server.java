/**
 * Server.java   2012-5-10
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Server 网络通信服务端，启动对本地端口的监听
 * 
 * @author zhaolei 2012-5-11
 */
public interface Server  {
	
	/**
	 * 取得服务器绑定的地址
	 * @return
	 */
	InetSocketAddress getBindAddress();
    
	/**
	 * 返回服务端管理的所有Channel, 可以用于向客户写数据
	 * 
	 * @return
	 */
	List<Channel> getChannels();
	
	/**
	 * 关闭 
	 */
	void close();
	
	String statistic();
    
}
