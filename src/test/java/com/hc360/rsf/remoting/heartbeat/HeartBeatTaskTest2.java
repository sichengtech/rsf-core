/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting.heartbeat;

import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.ChannelPoolVirtual;

/**
 * 测试
 * 创建连接定时任务与心跳定时任务
 * 在主线程退出后，能否正常退出
 * 
 * @author zhaolei 2012-7-3
 */
public class HeartBeatTaskTest2 {

	/**
	 * 描述方法的作用
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		HeartbeatListener heartbeatListener=new HeartbeatListener(){
			public void success(Channel channel) {
				System.out.println("心跳成功:"+channel);
			}
			public void fail(Channel channel) {
				System.out.println("心跳失败:"+channel);
			}
			public void start(){
				System.out.println("一轮心跳开始");
			}
		};
		//创建一个连接池,其中包含本端所有连接,
		//池具有心跳能力,具有心跳事件通知能力
		ChannelPoolVirtual pool=new ChannelPoolVirtual(heartbeatListener);
		
		Thread.sleep(10000);
		System.out.println("主线程结束");
	}

}
