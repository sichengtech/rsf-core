/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hc360.rsf.common.Constants;

/**
 * 不可用  服务提供者列表
 * 
 * @author zhaolei 2012-6-14
 */
public class ServiceProviderIdelList {
	private static Logger logger = LoggerFactory.getLogger(ServiceProviderIdelList.class);
	/**
	 * 服务提供者列表(重点)
	 * 
	 * 一个服务（服务接口）可能有多个服务提供者 <br>
	 * key=服务接口的全限定名<br>
	 * value=服务提供者集合，[Provider,Provider,Provider]<br>
	 */
	private static final Map<String, List<Provider>> SERVICE_LIST_MAP_IDLE = new ConcurrentHashMap<String, List<Provider>>();

	/**
	 * 通过服务名取出服务提供者列表
	 * 
	 * @param serviceName
	 *            服务名
	 * @return 服务提供者列表
	 */
	public static List<Provider> findServiceList(String serviceName) {
		if (serviceName == null || "".equals(serviceName.trim())) {
			throw new IllegalArgumentException("参数错误,serviceName=null");
		}
		List<Provider> list = SERVICE_LIST_MAP_IDLE.get(serviceName);
		if (list != null) {
			return list;
		}		
		return new ArrayList<Provider>();
	}
	
	/**
	 * 取得全部不可用服务列表  
	 * @return
	 */
	public static List<Provider> findAllServiceList(){
		List<Provider> list =new ArrayList<Provider>(SERVICE_LIST_MAP_IDLE.size());
		for(String key:SERVICE_LIST_MAP_IDLE.keySet()){
			List<Provider> inner=SERVICE_LIST_MAP_IDLE.get(key);
			list.addAll(inner);
		}
		return list;
	}

	/**
	 * 添加一个服务的 一个提供节点,可用列表
	 * 要排重
	 * 
	 * @param url
	 */
	public static void addNode(Provider prov){
		if(prov==null){
			return ;
		}
		String serviceName=prov.getServiceName();
		if(serviceName!=null){
			List<Provider> list=SERVICE_LIST_MAP_IDLE.get(serviceName);
			if(list==null){
				synchronized (ServiceProviderIdelList.class) {
					list=SERVICE_LIST_MAP_IDLE.get(serviceName);
					if(list==null){
						list=new CopyOnWriteArrayList<Provider>();
						SERVICE_LIST_MAP_IDLE.put(serviceName, list);
					}
				}
			}
			//过滤重复URL			
			boolean cf=false;//重复标记
			for(Provider u:list){
				if(u.equals(prov)){
					//发现重复
					cf=true;
				}
			}
			if(!cf){
				
				/**
				 * 一个服务，最多只保留5个，不可用的提供者节点。
				 * 原因1：这里的“提供者节点”都是不可用的，强行创建连接，把5个都试一遍，也需要时间的。
				 * 原因2：防止内存溢出
				 * 
				 */
				if(list.size()>=Constants.IDLE_NUM_PER_SERVICE){
					list.remove(0);
				}
				//把新的放进去
				logger.debug("不可用服务列表，添加节点."+prov);
				list.add(prov);
			}else{
				logger.debug("不可用服务列表，添加节点,发现重复节点,忽略。"+prov);
			}
		}else{
			logger.error("不可用服务列表，添加节点,服务接口名=null");
		}
	}
	
	/**
	 * 删除一个节点
	 * @param url
	 */
	public static void romveNode(Provider url){
		if(url==null){
			return ;
		}
		String serviceName=url.getServiceName();
		List<Provider> list=SERVICE_LIST_MAP_IDLE.get(serviceName);
		if(list!=null){
			for(Provider u:list){
				//重写了equals方法
				if(u.equals(url)){
					logger.debug("删除节点,"+u.toString());					
					list.remove(u);
				}
			}
		}
	}
	
	/**
	 * 测试列表中是否包含  特定的URL
	 * @param p1
	 */
	public static boolean ishas(Provider p1){
		if(p1==null){
			return false;
		}
		String serviceName=p1.getServiceName();
		List<Provider> list=SERVICE_LIST_MAP_IDLE.get(serviceName);
		if(list!=null){
			for(Provider p2:list){
				//equals方法 重写过，用于比较两个Provider对象
				if(p2.equals(p1)){
					return true;
				}
			}
		}
		return false;
	}
}
