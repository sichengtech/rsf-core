/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hc360.rsf.common.utils.NamedThreadFactory;
import com.hc360.rsf.config.callback.AddressTool;
import com.hc360.rsf.config.callback.CallBackHelper;
import com.hc360.rsf.config.callback.PushResult;

/**
 * 模拟注册中心
 * 
 * @author zhaolei 2012-6-12
 */
public class RegistryServiceImpl implements RegistryService {
	private static Logger logger = LoggerFactory.getLogger(RegistryServiceImpl.class);
	
	public RegistryServiceImpl(){
		startCreateChannelTimer();
	}
	
	/**
	 * 服务列表  <br>
	 * key:serviceName <br>
	 * value:List<RegistryBean> <br>
	 */
	static Map<String,List<RegistryBean>> map=new HashMap<String,List<RegistryBean>>();
	
	/**
	 * 兴趣列表 <br>
	 * key:serviceName<br>
	 * value:127.0.0.1:2563<br>
	 */
	static Map<String,List<String>> interestMap=new HashMap<String,List<String>>();

	/**
	 * 注册服务.
	 * 
	 * @param services
	 * @see com.hc360.rsf.registry.RegistryService#register(com.hc360.rsf.registry.RegistryBean[])
	 */
	public void register(RegistryBean[] services) {
		String ip=AddressTool.getRemoteAddress().getAddress().getHostAddress();
		if(services==null){
			logger.debug("注册中心--注册服务,RegistryBean[]=null");
		}else{
			for(RegistryBean bean:services){
				bean.setIp(ip);
				logger.debug("注册中心--注册服务："+bean.toString());
				add(bean);
			}
		}
	}
	
	/*
	 * 注册服务
	 * @param bean
	 */
	private void add(RegistryBean bean){
		if(bean==null){
			logger.error("注册中心--注册服务时异常,RegistryBean=null");
			return ;
		}
		if(bean.getServiceName()==null || "".equals(bean.getServiceName().trim())){
			logger.error("注册中心--注册服务时异常,ServiceName=null,"+bean);
			return ;
		}
		
		String key=bean.getServiceName();
		List<RegistryBean> list=map.get(key);
		if(list==null){
			list=new ArrayList<RegistryBean>();
			map.put(key, list);
			list.add(bean);
		}else{
			//查重复
			boolean cf=false;
			String k1=bean.getIp()+":"+bean.getPort();
			for(RegistryBean b2:list){
				String k2=b2.getIp()+":"+b2.getPort();
				if(k1.equals(k2)){
					//发现重复
					logger.debug("注册中心--发现重复服务,不再重复注册。"+bean.toString());
					cf=true;
					break;
				}
			}
			if(!cf){
				list.add(bean);
			}
		}
	}

	/**
	 * function description
	 * 
	 * @param services
	 * @see com.hc360.rsf.registry.RegistryService#unregister(com.hc360.rsf.registry.RegistryBean[])
	 */
	public void unregister(RegistryBean[] services) {
		
	}

	/**
	 * function description
	 * 
	 * @param services
	 * @see com.hc360.rsf.registry.RegistryService#subscribe(com.hc360.rsf.registry.RegistryBean[])
	 */
	public void subscribe(SubscribeBean[] bean) {
		if(bean!=null){
			for(SubscribeBean subscribeBean:bean){
				logger.debug("注册中心--订阅服务："+subscribeBean);
			}
		}
	}

	/**
	 * function description
	 * 
	 * @param services
	 * @see com.hc360.rsf.registry.RegistryService#unsubscribe(com.hc360.rsf.registry.RegistryBean[])
	 */
	public void unsubscribe(SubscribeBean[] bean) {

	}

	/**
	 * function description
	 * 
	 * @param services
	 * @return
	 * @see com.hc360.rsf.registry.RegistryService#lookup(com.hc360.rsf.registry.RegistryBean[])
	 */
	public  List<RegistryBean> lookup(String serviceName,String portalId){
		if(serviceName!=null ){
			//关联推送工具--可重复PUT
			CallBackHelper.put(serviceName);
			
			//常规返回结果 
			List<RegistryBean> rs_list=map.get(serviceName);
			String msg="注册中心--下载服务列表,serviceName="+serviceName+",下载成功,列表长度="+rs_list.size();
			for(RegistryBean rb:rs_list){
				
				rb.setStat(RegistryBean.ADD_NODE);//都是加节点
				
				msg+="\r\n\t"+serviceName+"--"+rb.toString();
			}
			logger.debug(msg);
			return rs_list;
		}else{
			logger.debug("注册中心--下载服务列表,serviceName="+serviceName+",下载失败");
			return null;
		}
	}
	
	/**
	 * 启动推送定时任务--用于测试目的
	 */
	private final ScheduledExecutorService scheduled_createChannel = Executors.newScheduledThreadPool(1,
			new NamedThreadFactory("rsf-remoting-client-pushServiceList", true));
	/**
	 * 启动推送定时任务--用于测试目的
	 */
	private void startCreateChannelTimer() {
		// 每heartbeat毫秒执行一次任务,两次任务的执行间隔是heartbeat毫秒+任务执行用时
		scheduled_createChannel.scheduleWithFixedDelay(new Runnable(){
			public void run() {
				try{
					String serviceName="com.hc360.rsf.config.p2p.requestresponse.UserService";
					List<RegistryBean> list=map.get(serviceName);
					if(list==null){
						return ;
					}
					
					for(RegistryBean rb:list){
//						RegistryBean rb=new RegistryBean();
//						rb.setIp("127.0.0.1");
//						rb.setPort(63634);
//						rb.setServiceName(serviceName);
						int r=new Random().nextInt(100);
						if(r>40){
							rb.setStat(RegistryBean.ADD_NODE);//+节点,  60%命中机会
						}else if(20 <r && r <=40){
							rb.setStat(RegistryBean.DECR_NODE_BREAKDOWN);//故障-节点, 20%命中机会
						}else if(20 >=r){
							rb.setStat(RegistryBean.DECR_NODE_MANUAL);//人工-节点,  20%命中机会
						}else{
							System.out.println("不应该进入这里");
						}
					}
//					List<RegistryBean> list=new ArrayList<RegistryBean>();
//					list.add(rb);
					
					for(RegistryBean rbi:list){
						logger.debug("注册中心--推送数据给订阅者,serviceName="+serviceName+",RegistryBean="+rbi);
					}
					PushResult[] rs=CallBackHelper.send(serviceName,list.toArray(new RegistryBean[list.size()]));
					for(PushResult pr:rs){
						logger.debug("注册中心--推送数据给订阅者-结果,PushResult="+pr);
					}
					
				}catch(Throwable e){
					logger.error("",e);
				}
			}
			
		}, 5*1000, 5*1000,
				TimeUnit.MILLISECONDS);
	}
}
