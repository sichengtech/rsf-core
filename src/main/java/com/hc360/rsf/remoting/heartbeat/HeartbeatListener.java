/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting.heartbeat;

import com.hc360.rsf.remoting.Channel;

/**
 * 心跳事件监听器,不要在其中做耗时业务
 * 
 * @author zhaolei 2012-6-20
 */
public interface HeartbeatListener {
	
	/**
	 * 心跳成功时触发 
	 * @param channel
	 */
	public void success(Channel channel);
	
	/**
	 * 心跳失败时触发
	 * @param channel
	 */
	public void fail(Channel channel);

	/**
	 * 心跳开始事件 (一轮心跳的开始,每3秒执行一轮心跳,可以对多个连接进行异步心跳)
	 */
	public void start();
}
