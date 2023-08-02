package com.hc360.rsf.remoting.heartbeat;

import java.util.List;
import org.apache.mina.core.RuntimeIoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hc360.rsf.config.GlobalManager;
import com.hc360.rsf.registry.Provider;
import com.hc360.rsf.registry.ServiceProviderList;
import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.ChannelPool;
import com.hc360.rsf.remoting.Client;

/**
 * 创建连接定时任务
 * 
 * @author zhaolei 2012-6-16
 */
public class CreateChannelTask implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(CreateChannelTask.class);
	private ChannelPool channelPool;
	
	public CreateChannelTask(ChannelPool channelPool) {
		this.channelPool = channelPool;
	}
	
	public void run() {
		try {
			//比较规则
			//"可用连接的池"与服务列表比较,相比服务列表缺少的连接,要尝试恢复（新建）连接,成功后放入"可用连接的池"。
			//全部的  服务提供者列表
			List<Provider> allServiceList = ServiceProviderList.findAllServiceList();
			
			for(Provider url:allServiceList){
				String key=url.getIp()+":"+url.getPort();
				Channel channel=channelPool.getChannel(key);
				if(channel==null){
					//无连接，需要创建
					try{
						Client client = GlobalManager.getClient();
						client.getOrCreateChannel(url.getIp(),url.getPort());
					}catch(RuntimeIoException e){
						LOGGER.error("创建连接定时任务--在创建连接时异常，ip:"+url.getIp()+",port:"+url.getPort(),e);
					}
				}else{
					if(channel.isConnected()){
						//是连接状态的
						//什么也不用做
					}else{
						//是断开状态，要清理，并重新创建
						channelPool.removeCloseChannel(key);
						
						try{
							Client client = GlobalManager.getClient();
							client.getOrCreateChannel(url.getIp(),url.getPort());
						}catch(RuntimeIoException e){
							LOGGER.error("创建连接定时任务--在创建连接时异常，ip:"+url.getIp()+",port:"+url.getPort(),e);
						}
					}
				}
			}
		} catch (Throwable e) {
			//有必要拦截Throwable
			LOGGER.error("创建连接定时任务--总异常",e);
		}
	}
}
