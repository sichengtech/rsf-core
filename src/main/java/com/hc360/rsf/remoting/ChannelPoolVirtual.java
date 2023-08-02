/**
 * ChannelPool.java   2012-5-8
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hc360.rsf.config.GlobalManager;
import com.hc360.rsf.remoting.heartbeat.HeartbeatListener;
import com.hc360.rsf.remoting.heartbeat.HeartbeatTask;
import com.hc360.rsf.remoting.heartbeat.HeartbeatTaskNotify;

/**
 * 虚拟连接池，主要为了使用连接池的心跳功能。
 * 服务端不需要连接池<br>
 * 服务端须要对连接心跳时(注册中心),使用ChannelPoolVirtual类<br>
 * 
 * @author zhaolei 2012-5-8
 */
public class ChannelPoolVirtual extends ChannelPool{
	//private static Logger logger = LoggerFactory.getLogger(ChannelPoolVirtual.class);
	private HeartbeatListener heartbeatListener;

	
	/**
	 * 构造方法
	 * 
	 * @param channelMap
	 * @param channelMapFail
	 */
	public ChannelPoolVirtual(final HeartbeatListener heartbeatListener){
		super();
		this.heartbeatListener=heartbeatListener;
	}
	
	@Override
	protected HeartbeatTask createTaskHeatbeat(Map<String, Channel> channelMap){
		HeartbeatListener heartbeatListener_inner=new HeartbeatListener(){
			/**
			 * 心跳成功时触发
			 */
			public void success(Channel channel) {
				heartbeatListener.success(channel);
			}
			/**
			 * 心跳失败时触发
			 */
			public void fail(Channel channel) {
				heartbeatListener.fail(channel);
			}
			/**
			 * 心跳开始事件 (一轮心跳的开始,每3秒执行一轮心跳,可以对多个连接进行异步心跳)
			 * 
			 * 服务端的连接随时都在发生变化(增减),每次心跳时,要取得最新的全部连接.
			 * 
			 */
			public void start(){
				heartbeatListener.start();
				
				//这里清理池中旧连接,准备放入新连接
				clearChannelPool();
				Map<String, Server> server_list=GlobalManager.SERVER_LIST;
				for(String key:server_list.keySet()){
					
					Server server=server_list.get(key);
					//取出所有连接
					List<Channel> list_channel=server.getChannels();
					for(Channel channel: list_channel){
						InetSocketAddress isa=channel.getRemoteAddress();//远端的IP,port
						String ip=isa.getAddress().getHostAddress();
						int port=isa.getPort();
						String key2=ip+":"+port;
						putChannel(key2, channel);//放入可用连接池
					}
				}
			}
		};
		//第二个参数必须为空,心跳不放入"不可用连接池"
		return new HeartbeatTaskNotify(this,heartbeat,heartbeatTimeout(),heartbeatListener_inner);
	}
}
