/**
 * ChannelPool.java   2012-5-8
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hc360.rsf.common.utils.NamedThreadFactory;
import com.hc360.rsf.remoting.heartbeat.CreateChannelTask;
import com.hc360.rsf.remoting.heartbeat.HeartbeatTask;

/**
 * 连接池<br>
 * 一般情况下,只在客户端有一个连接池<br>
 * 服务端不需要连接池<br>
 * 服务端须要对连接心跳时(注册中心),使用ChannelPoolVirtual类<br>
 * 
 * @author zhaolei 2012-5-8
 */
public class ChannelPool {

	public static final String KEY_READ_TIMESTAMP = "READ_TIMESTAMP";

	public static final String KEY_WRITE_TIMESTAMP = "WRITE_TIMESTAMP";

	private static Logger logger = LoggerFactory.getLogger(ChannelPool.class);

	/**
	 * 连接池的池子<br>
	 * <br>
	 * 一个Channel表示客户端与服务端之间的一条socket连接,是收发数据的通道.<br>
	 * 一个Channel对应一个Mina的session
	 * <br>
	 * key生成规则: 服务端IP:服务端prot<br>
	 * 通过一个key,可以取得一个List<Channel>.<br>
	 * 一个List<Channel>中保存当前客户端与某一个服务端之间的所有连接.<br>
	 * 默认使用采用连接复用模式,这时候List<Channel>的size()==1.<br>
	 * 只有使用专有连接模式时,List<Channel>的size()才会大于1.<br>
	 */
	private final Map<String, Channel> channelMap = new ConcurrentHashMap<String, Channel>();

	/**
	 * "心跳"的线程
	 */
	private final ScheduledExecutorService scheduled_heartbeat = Executors.newScheduledThreadPool(1,
			new NamedThreadFactory("rsf-heartbeat", true));

	/**
	 * "创建连接"的线程
	 */
	private final ScheduledExecutorService scheduled_createChannel = Executors.newScheduledThreadPool(1,
			new NamedThreadFactory("rsf-createChannel", true));

	/**
	 * 心跳  定时器
	 */
	private ScheduledFuture<?> heatbeatTimer;
	/**
	 * 创建连  接时器
	 */
	private ScheduledFuture<?> createChannelTimer;

	/**
	 * 心跳间隔,毫秒
	 */
	protected int heartbeat = 3 * 1000;

	/**
	 * 创建连接任务执行间隔
	 */
	private int createThreadTime = 10 * 1000;
	
	/**
	 * 池关闭标志
	 */
	private volatile boolean close=false;

	/**
	 * 心跳超时算法,单位毫秒<br>
	 * 本值是heartbeat的几倍,当连接不可用时,就相当于重试几次<br>
	 * 相当于“连续几次失败”<br>
	 * 本值一定要大于等于heartbeat * 2 + 1000<br>
	 */
	public int heartbeatTimeout() {
		return heartbeat * 2 + 1000;
	}

	/**
	 * 构造方法
	 */
	public ChannelPool() {
		// 防止心跳间隔被错误设置的太小
		if (heartbeat < 3 * 1000) {
			heartbeat = 3 * 1000;
		}
		//启动任务
		startHeatbeatTimer();
		//启动任务
		startCreateChannelTimer();
		close=false;
	}

	/**
	 * 保存一个channel
	 * 
	 * @param key
	 * @param channel
	 */
	public void putChannel(String key, Channel channel) {
		if (key == null || "".equals(key.trim())) {
			return;
		}
		if (channel == null) {
			return;
		}
		channelMap.put(key, channel);
	}

	/**
	 * 通过最后心跳成功的时间，来判定连接是否是通的。
	 * 在独立的守护线程中进行，超时达到heartbeatTimeout()认定连接不可用
	 * @param channel
	 * @return
	 */
	public boolean isConnected4Hearbeat(Channel channel){
		if(channel==null){
			return false;
		}
		if(!channel.isConnected()){
			return false;
		}
		long now = System.currentTimeMillis();
		//收到数据--read
		//发出数据--write
		Long lastRead = (Long) channel.getAttribute(ChannelPool.KEY_READ_TIMESTAMP);
		//Long lastWrite = (Long) channel.getAttribute(ChannelPool.KEY_WRITE_TIMESTAMP);
		if (lastRead != null && now - lastRead > heartbeatTimeout()) {
			return false;
		}else{
			return true;
		}
	}
	/**
	 * 判断连接池是否已关闭
	 */
	public boolean isClose(){
		return close;
	}
	
	/**
	 * 清空可用连接池
	 */
	protected void clearChannelPool() {
		channelMap.clear();
	}

	/**
	 * 找出 可以连接到某个服务端 的channel<br>
	 * key生成规则: 服务端IP:服务端prot 如 "127.0.0.1:1000"
	 * 
	 * @param key  key生成规则: 服务端IP:服务端prot 如 "127.0.0.1:1000"
	 * @return  Channel
	 */
	public Channel getChannel(String key) {
		return channelMap.get(key);
	}
	
	/**
	 * 池的大小
	 * @return
	 */
	public int getPoolSize(){
		return channelMap.size();
	}

	/**
	 * 找出池中所有的channel
	 * 
	 * @return
	 */
	public List<Channel> getAllChannels() {
		Set<String> set = channelMap.keySet();
		List<Channel> list = new ArrayList<Channel>();
		for (String key : set) {
			Channel c=channelMap.get(key);
			if(c!=null){
				list.add(c);
			}else{
				//检查发现value为空，就清理掉
				channelMap.remove(key);
			}
		}
		return list;
	}

	/**
	 * 生成debugInfo
	 * 
	 * @return
	 */
	public String debugInfo() {
		StringBuilder sbl = new StringBuilder();
		sbl.append("ChannelPool,size=");
		sbl.append(channelMap.size());
		sbl.append(",");
		Set<String> set = channelMap.keySet();
		sbl.append("target server=[");
		for (String key : set) {
			sbl.append(key);
			sbl.append(",");
		}
		sbl.append("]");
		return sbl.toString();
	}
	
	/**
	 * 闭关一条连接,并从池中移除他
	 */
	public void removeCloseChannel(Channel channel){
		if(channel!=null){
			String key=channel.getKey();
			if(key == null)
				return;
			Channel channel4=channelMap.remove(key);
			if(channel4!=null){
				channel4.close("连接池-清理池中的连接");
				logger.debug("连接池-清理池中的连接，channel="+channel);
			}
		}
	}
	/**
	 * 闭关一条连接,并从池中移除他
	 */
	public void removeCloseChannel(String key){
		if(key!=null){
			Channel channel=channelMap.remove(key);
			if(channel!=null){
				channel.close("连接池-清理池中的连接");
				logger.debug("连接池-清理池中的连接，channel="+channel);
			}
		}
	}
	
	/**
	 * close 关闭连接池（关闭池中的所有连接）
	 */
	public void closePool() {
		if(!isClose()){
			//关闭池中的所有连接
			List<Channel> list=getAllChannels();
			for(Channel channel:list){
				try{
					if(channel!=null){
						channel.close("关闭连接池（关闭池中的所有连接）");
					}
				}catch(Exception e){
					logger.error("关闭连接异常",e);
				}
			}
			
			//关闭线程池
			try {
				stopHeartbeatTimer();
				stopCreateChannelTimer();
			} catch (Throwable e) {
				if (logger.isWarnEnabled()) {
					logger.warn(e.getMessage(), e);
				}
			}
			try {
				scheduled_heartbeat.shutdown();
			} catch (Throwable e) {
				if (logger.isWarnEnabled()) {
					logger.warn(e.getMessage(), e);
				}
			}
			try {
				scheduled_createChannel.shutdown();
			} catch (Throwable e) {
				if (logger.isWarnEnabled()) {
					logger.warn(e.getMessage(), e);
				}
			}
			close=true;
			logger.info("关闭连接池");
			//System.out.println("关闭连接池");
		}
	}

	/**
	 * 启动心跳定时任务
	 */
	private void startHeatbeatTimer() {
		//停止前一次任务
		stopHeartbeatTimer();
		
		//创建新的 心跳任务
		//HeartbeatTask heartbeatTask=new HeartbeatTask(this, heartbeat, heartbeatTimeout());
		HeartbeatTask heartbeatTask=createTaskHeatbeat(channelMap);
		
		//启动心跳任务
		// 每heartbeat毫秒执行一次任务,两次任务的执行间隔是heartbeat毫秒+任务执行用时
		heatbeatTimer = scheduled_heartbeat.scheduleWithFixedDelay(heartbeatTask, heartbeat, heartbeat, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * 创建心跳任务的方法
	 * 
	 * 子类会重写本方法 
	 * 
	 * @param channelMap
	 * @return
	 */
	protected HeartbeatTask createTaskHeatbeat(Map<String, Channel> channelMap){
		return new HeartbeatTask(this, heartbeat, heartbeatTimeout());
	}

	/**
	 * 启动创建连接定时任务
	 */
	private void startCreateChannelTimer() {
		//停止前一次任务
		stopCreateChannelTimer();
		
		//创建新的 创建连接任务
		CreateChannelTask createChannelTask= new CreateChannelTask(this);
		
		//启动任务
		// 每createThreadTime毫秒执行一次任务,两次任务的执行间隔是createThreadTime毫秒+任务执行用时
		// 第二个参数为什么设置为1000ms?因为请求发启时会检查有无连接，无则创建。
		// 创建连接定时任务 在启动后1000ms时，第一次执行，发现连接已创建成功，就不再创建。
		// 如果第二个参数设置为0ms，会创建两个连接，重复了。虽然程序会自动关闭重复连接，但还是不重复最好。
		createChannelTimer = scheduled_createChannel.scheduleWithFixedDelay(createChannelTask, 1000, createThreadTime, TimeUnit.MILLISECONDS);
	}

	/**
	 * 停止任务
	 */
	private void stopHeartbeatTimer() {
		if (heatbeatTimer != null && !heatbeatTimer.isCancelled()) {
			try {
				heatbeatTimer.cancel(true);
			} catch (Throwable e) {
				logger.warn(e.getMessage(), e);
			}
		}
		heatbeatTimer = null;
	}

	/**
	 * 停止任务
	 */
	private void stopCreateChannelTimer() {
		if (createChannelTimer != null && !createChannelTimer.isCancelled()) {
			try {
				createChannelTimer.cancel(true);
			} catch (Throwable e) {
				logger.warn(e.getMessage(), e);
			}
		}
		createChannelTimer = null;
	}

}
