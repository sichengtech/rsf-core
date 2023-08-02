package com.hc360.rsf.common.utils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hc360.config.TrustListHelper;
import com.hc360.rsf.remoting.CatchConfigException;


public class TrustListHelperProxy {
      
	private static boolean isDebug = false;
	private static final Logger logger = LoggerFactory.getLogger(TrustListHelperProxy.class);
	private static String FLAG = "*";
    
	/**
	 * 检查 指定客户端系统是否在服务提供者系统的信任列表中
	 * 
	 * @param serverId  服务提供者系统 （接收方）
	 * @param clientId  客户端系统  （发送方）
	 */
	public static void judgeTrust(String serverId,String clientId){
		
		List<String> TrustSysList = null;
		String message = null;
		
		//验证参数
		if(StringUtils.isBlank(serverId)||StringUtils.isBlank(clientId)){
			message = MessageFormat.format("severId:{0},clientId:{1} 验证信认列表错误：系统名称两个至少有一个为空",serverId,clientId);
			logger.error(message);
			throw new CatchConfigException(message);
		}
		
		
		//自己调自己，不走信任列表
		if(serverId.equalsIgnoreCase(clientId)){
			logger.info("{}调用自己，不走信任列表",clientId);
			return;
		}
		
		logger.info("{}调用{},走信任列表",clientId,serverId);
		
		//获取信任列表
		try {
			TrustSysList = TrustListHelper.getTrustList(serverId);
		} catch (Exception e) {
			message = MessageFormat.format("从配置中心获取{0}的信任列表异常",serverId);
			logger.error(message,e);
			throw new CatchConfigException(message,e);
		}
		
		//用于调试模拟
		if(isDebug){
			TrustSysList = new ArrayList<String>();
//			TrustSysList.add("paysso11");
			TrustSysList.add("paysso");
		}
		
		//信任列表为空，抛异常
		if(CollectionUtils.isEmpty(TrustSysList)){
			message = MessageFormat.format("从配置中心获取{0}的信任列表为空",serverId);
			logger.error(message);
			throw new CatchConfigException(message);
		}
		
		//信任列表内没有指定的客户端系统 适应通配符“*”
		if(!TrustSysList.contains(FLAG)&&!TrustSysList.contains(clientId)){
			message = MessageFormat.format("从配置中心获取{0}的信任列表:{1};指定的客户端系统是{2}", serverId,TrustSysList,clientId);
			logger.error(message);
			throw new CatchConfigException(message);
		}
		
	}
}
