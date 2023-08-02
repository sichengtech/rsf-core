/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting.heartbeat;

import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.ChannelPool;

/**
 * 在HeartbeatTask类的基础上增加了事件通知能力 
 * 
 * @author zhaolei 2012-6-20
 */
public class HeartbeatTaskNotify extends HeartbeatTask {
	/**
	 * 事件监听器,不要在其中做耗时业务
	 */
	private HeartbeatListener heartbeatListener;
	/**
	 * 构造方法
	 * 
	 * @param channelMap
	 * @param channelMapFail
	 * @param heartbeat
	 * @param heartbeatTimeout
	 */
	public HeartbeatTaskNotify(ChannelPool channelPool,
			int heartbeat, int heartbeatTimeout,HeartbeatListener heartbeatListener) {
		super(channelPool, heartbeat, heartbeatTimeout);
		this.heartbeatListener=heartbeatListener;
	}
	/**
	 * 心跳开始事件 
	 */
	protected void start(){
		if(heartbeatListener!=null){
			heartbeatListener.start();
		}
	}
	/**
	 * 心跳成功事件 
	 * @param channel
	 */
	protected void successEvent(Channel channel){
		if(heartbeatListener!=null){
			heartbeatListener.success(channel);
		}
	}
	/**
	 * 心跳失败
	 * @param channel
	 */
	protected void failEvent(Channel channel){
		if(heartbeatListener!=null){
			heartbeatListener.fail(channel);
		}
	}
}
