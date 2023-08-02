package com.hc360.rsf.remoting.heartbeat;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hc360.rsf.common.Constants;
import com.hc360.rsf.config.GlobalManager;
import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.ChannelPool;
import com.hc360.rsf.remoting.exchange.support.Request;

/**
 * 心跳定时任务
 * 
 * 注意，这是异步心跳。在一轮循环中，可同时完成对多条连接 发送心跳包、验证心跳结果 
 * 
 * @author zhaolei 2012-5-21
 */
public class HeartbeatTask implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(HeartbeatTask.class);
	private ChannelPool channelPool;// 连接的池
	private int heartbeat;// 两次心跳间隔
	private int heartbeatTimeout;// 心跳超时时间


	public HeartbeatTask(ChannelPool channelPool, int heartbeat, int heartbeatTimeout) {
		this.channelPool = channelPool;
		this.heartbeat = heartbeat;
		this.heartbeatTimeout = heartbeatTimeout;
	}

	public void run() {
		try {
			try {
				start();
			} catch (Exception e) {
				logger.error("执行事件通知方法start时异常", e);
			}
			long now = System.currentTimeMillis();
			if (channelPool != null) {
				logger.debug("可用连接池数中连接数:" + channelPool.getPoolSize());
			}

			//找出池中所有的channel
			List<Channel> list=channelPool.getAllChannels();
			for(Channel channel : list){
				
				Object telnetKey = channel.getAttribute(Constants.TELNET_KEY);
				if(telnetKey!=null && telnetKey instanceof String && Constants.TELNET_KEY_VALUE.equals((String)telnetKey)){
					//这条channel是一个Telnet连接，心跳程序不对本连接进行心跳
					logger.debug("这条channel是一个Telnet连接，心跳程序不对本连接进行心跳.channel="+channel);
					continue;
				}
				
				//走到这里，channel一定不为空，因为getAllChannels()方法中检查了是否为空
				Long lastRead = (Long) channel.getAttribute(ChannelPool.KEY_READ_TIMESTAMP);
				Long lastWrite = (Long) channel.getAttribute(ChannelPool.KEY_WRITE_TIMESTAMP);
				
				// 收到数据--read
				// 发出数据--write
				
				// 发出心跳包--注意，这是异步心跳。在一轮循环中，可同时完成对多条连接 发送心跳包、验证心跳结果
				// 只有距离前一次心跳时间超过3秒，才进行第二次心跳
				// 刚刚被业务使用过的连接,不需要心跳，被业务使用过的连接也记录了最后的使用时间，是通过“最后使用时间”来判断的
				if ((lastRead != null && now - lastRead >= heartbeat) || (lastWrite != null && now - lastWrite > heartbeat)) {
					Request req = new Request();
					req.setEvent(Request.HEARTBEAT_EVENT);
					logger.debug("RSF 发送心跳包,channel={}", channel);
					channel.send(req);
				} else {
					// 如果一个新的连接,从未发送过数据,那么lastRead,lastWrite一直为空,
					// 心跳条件一直不满足,无法进行心跳,所以下面设置lastRead,lastWrite值,
					// 使第二次心跳可以正常进行。
					if (lastRead == null) {
						channel.setAttribute(ChannelPool.KEY_READ_TIMESTAMP, now);
					}
					if (lastWrite == null) {
						channel.setAttribute(ChannelPool.KEY_WRITE_TIMESTAMP, now);
					}
					logger.debug("RSF 连接刚刚被使用过不需要心跳,channel={}", channel);
				}
				
				//找出心跳失败的连接 -- 注意，这是异步心跳。在一轮循环中，可同时完成对多条连接 发送心跳包、验证心跳结果
				if (lastRead != null && now - lastRead > heartbeatTimeout) {
					logger.warn("Close channel " + channel + ", because heartbeat read idle time out.");
					try {
						// 判定连接不可用
						logger.warn("RSF Client 心跳失败,连接被从可用连接池中删除,key={},channel={}", channel.getKey(), channel);
						channelPool.removeCloseChannel(channel.getKey());
						try {
							failEvent(channel);
						} catch (Exception e) {
							logger.error("执行事件通知方法failEvent时异常", e);
						}
					} catch (Exception e) {
						// do nothing
					}
				} else {
					// 心跳成功,可以记日志
					try {
						successEvent(channel);
					} catch (Exception e) {
						logger.error("执行事件通知方法successEvent时异常", e);
					}
				}
			}
		} catch (Throwable e) {
			//有必要拦截Throwable
			logger.error("RSF Client 心跳异常", e);
		}
	}

	/**
	 * 心跳开始事件
	 */
	protected void start() {

	}

	/**
	 * 心跳成功事件
	 * 
	 * @param channel
	 */
	protected void successEvent(Channel channel) {

	}

	/**
	 * 心跳失败
	 * 
	 * @param channel
	 */
	protected void failEvent(Channel channel) {

	}
}
