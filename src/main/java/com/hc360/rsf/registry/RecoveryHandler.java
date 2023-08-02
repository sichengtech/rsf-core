package com.hc360.rsf.registry;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hc360.rsf.common.utils.CollectionUtils;
import com.hc360.rsf.common.utils.NamedThreadFactory;
import com.hc360.rsf.config.GlobalManager;
import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.Client;
import com.hc360.rsf.remoting.RemotingException;

/**
 * 从不可用服务列表中恢复节点数据到可用服务列表的线程
 * 
 * @author liuhe
 * @version 4.0 2013-8-1
 * @since 4.0	
 */
public class RecoveryHandler {
	
	private static Logger logger = LoggerFactory.getLogger(RecoveryHandler.class);
	private static final int RUN_INTERVAL=5*60*1000;//5分钟，执行定任务的时间间隔
	private static final int INIT_DELAY = 1000;  //初始化延迟
	
	private ScheduledFuture<?> recoveryFuture;
	
	//执行定时任务的线程，守护线程
	private final ScheduledExecutorService scheduledRecovery = Executors.newScheduledThreadPool(1,
			new NamedThreadFactory("rsf-recovery", true));
	
	/**
	 * 构造方法 这样是不是不太好
	 */
	public RecoveryHandler(){
		//启动任务
		startRecoveryTask();
	}
	
	/**
	 * 启动任务
	 * 
	 */
	private void startRecoveryTask() {
		
		// 每heartbeat毫秒执行一次任务,两次任务的执行间隔是heartbeat毫秒+任务执行用时
		recoveryFuture = scheduledRecovery.scheduleWithFixedDelay(new Runnable() {
			public void run() {
				try {
					//得到所有的不可用列表
					List<Provider> allIdleProviderList = ServiceProviderIdelList.findAllServiceList();
					
					//如果不可用列表为空，不做任何处理
					if(CollectionUtils.isEmpty(allIdleProviderList))
						return ;
					
					logger.debug("本次要恢复的源allIdleProviderList大小是{},明细是:{},",allIdleProviderList.size(),allIdleProviderList);
					Client client = GlobalManager.getClient();
					Channel channel = null;
					boolean isRecovery = false;
					for(Provider provider:allIdleProviderList){
						try{
							//可能从channelMap中得到一个,这样说明它用的端口已经叫其他服务占用了
							channel = client.getOrCreateChannel(provider.getIp(),provider.getPort());		
						}catch(Exception e){
							logger.error("恢复时，创建连接时异常",e);
						}
						
						if(channel != null && channel.isConnected()){
							try {
								isRecovery = channel.isContainService(provider.getServiceName());
								if(isRecovery){
									//将从不可用列表中找到的可用提供者恢复到可用列表中,并从不可用列表中移除
									ServiceProviderList.addNode(provider);
									ServiceProviderIdelList.romveNode(provider);
									logger.info("通过RecoveryHandler线程，{}从ServiceProviderIdelList恢复到ServiceProviderList里。",provider);
								}
							} catch (RemotingException e) {
						        logger.warn(MessageFormat.format("在RecoveryHandler线程中，{0}回声测试{1}出现异常",channel,provider.getServiceName()),e);
							}
						}
					}
				} catch (Throwable e) {
					logger.error("RecoveryHandler 执行恢复时出现异常",e);
				}
			}
		}, INIT_DELAY, RUN_INTERVAL,TimeUnit.MILLISECONDS);
	}
	
	/**
	 * 关闭
	 * 1、停止任务
	 * 2、停止线程池中的线程  
	 */
	public void close(){
		stopRecoveryFuture();
		if(scheduledRecovery!=null){
			scheduledRecovery.shutdownNow();
		}
	}
	
	/**
	 * 停止 任务
	 */
	private void stopRecoveryFuture() {
		if (recoveryFuture != null && !recoveryFuture.isCancelled()) {
			try {
				recoveryFuture.cancel(true);
				logger.info("关闭recovery定时任务");
			} catch (Throwable e) {
				logger.error("stopRecoveryFuture 异常",e);
			}
		}
		recoveryFuture = null;
	}

}
