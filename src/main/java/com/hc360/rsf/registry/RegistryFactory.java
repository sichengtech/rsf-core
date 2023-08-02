/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hc360.rsf.common.Constants;
import com.hc360.rsf.common.Version;
import com.hc360.rsf.common.utils.NetUtils;
import com.hc360.rsf.config.ClientConfig;
import com.hc360.rsf.config.RegistryConfig;
import com.hc360.rsf.config.ServiceConfig;
import com.hc360.rsf.rpc.RpcException;

/**
 * 操作注册中心的工具类<br>
 * 
 * 一个注册中心对应一个RegistryService对象<br>
 * 1、常规情况下只要有一个RegistryService就够了,负载热备工作由后端F5实现。<br>
 * 2、也允许有多个RegistryService,但IP与port不同,负载热备由程序本身负责<br>
 * <br>
 * <br>
 * 使用RegistryService,使服务提供者可以向注册中心注册服务<br>
 * <br>
 * 使用RegistryService,使服务调用者可以向注册中心订阅服务<br>
 * 
 * @author zhaolei 2012-6-12
 */
public class RegistryFactory {
	private static Logger logger = LoggerFactory.getLogger(RegistryFactory.class);
	/**
	 * 注册中心的服务接口,为了支持多个注册中心，所以放在Map中。但目前只支持一个注册中心。
	 * key=ip+":"+port;
	 * value=RegistryService（注册中心的服务接口）
	 * 
	 */
	private static final Map<String, RegistryService> REGISTRY_CENTER_CLIENT = new ConcurrentHashMap<String, RegistryService>();
	
	/**
	 * 通过IP:port,取得一个RegistryService
	 * 
	 * @param ip
	 * @param port
	 */
	private static RegistryService getRegistryService(String ip,int port){
		if(ip==null){
			throw new IllegalArgumentException("注册中心参数错误,ip="+ip);
		}
		if(port>=65535 ||port <=0){
			throw new IllegalArgumentException("注册中心参数错误,port="+port);
		}
		String key=ip+":"+port;
		return getRegistryService(key);
	}
	
	/**
	 * 通过IP与port,取得一个RegistryService
	 * 
	 * @param key   ip:port
	 */
	private static RegistryService getRegistryService(String key){
		RegistryService registryService=REGISTRY_CENTER_CLIENT.get(key);
		if(registryService==null){
			synchronized (RegistryFactory.class) {
				registryService=REGISTRY_CENTER_CLIENT.get(key);
				if(registryService==null){
					registryService=regClient(key);
					REGISTRY_CENTER_CLIENT.put(key, registryService);
				}
			}
		}
		return registryService;
	}
	
	/*
	 * 创建连接--连接到一个注册中心
	 * @param ip
	 * @param port
	 */
	private static RegistryService regClient(String addr){
		RegCallback regCB=new RegCallback();
		logger.debug("连接注册中心,地址："+addr);
		//发起注册
		ClientConfig<RegistryService> client=null;
		client=new ClientConfig<RegistryService>();
		client.setDisplayName("注册中心客户端");
		client.setDepartment("MMT开发部");
		client.setOwner("赵磊");
		client.setUrl("rsf://"+addr);
		client.setInterfaceClass(RegistryService.class);
		client.addCallBack("lookup",regCB);//回调函数
		client.setTimeout(Constants.REGISTRY_TIMEOUT);//超时时间，毫秒
		return client.getInstance();
	}
	
	/**
	 * 注册工具
	 * 向1个注册中心，注册1个服务
	 */
	public static void reg(ServiceConfig<?> serviceConfig){
		List<ServiceConfig<?>> serviceConfigList=new ArrayList<ServiceConfig<?>>(1);
		serviceConfigList.add(serviceConfig);
		regBatch(serviceConfigList);
	}
	/**
	 * 批量注册工具
	 * 向多个注册中心，注册多个服务
	 * 
	 * 	key:注册中心的IP:port
	 * 	value:List<ServiceConfig>
	 * @param registry_map
	 */
	public static void regBatch(List<ServiceConfig<?>> serviceConfigList){
		// 整理要注册的服务
		// key:注册中心的IP:port
		// value:List<ServiceConfig>
		Map<String, List<ServiceConfig<?>>> registry_map = new HashMap<String, List<ServiceConfig<?>>>();
		for (ServiceConfig<?> serviceConfig : serviceConfigList) {
			List<RegistryConfig> list = serviceConfig.getRegistries();
			if (list != null) {
				//@modify 对指定服务的注册中心排重
				list = filterRepetition(list);
				for (RegistryConfig rc : list) {
					String ip = rc.getHost();
					int port = rc.getPort();
					List<ServiceConfig<?>> serviceConfig_list = registry_map.get(ip + ":" + port);
					if (serviceConfig_list == null) {
						serviceConfig_list = new ArrayList<ServiceConfig<?>>();
						registry_map.put(ip + ":" + port, serviceConfig_list);
					}
					serviceConfig_list.add(serviceConfig);
				}
			}
		}
		RegistryFactory.regBatch(registry_map);//批量注册工具， 向多个注册中心，注册多个服务
	}
	
	/**
	 * 对注册中心配置列表进行排重
	 * 排重依据：ip+port
	 * 注意：因为在调用处已经对入参进行判空操作，所以方法内不再做对入参进行判空操作
	 * @param registryConfigList  要排重的注册中心配置列表
	 * @return
	 */
	private static List<RegistryConfig> filterRepetition(List<RegistryConfig> registryConfigList){
        List<RegistryConfig> result = new ArrayList<RegistryConfig>();
        String sourceIp = null;
        String resultIp = null;
        for (RegistryConfig rc : registryConfigList) {
        	sourceIp = NetUtils.getIpByHost(rc.getHost());
        	if(result.isEmpty()){
        		result.add(rc);
        	}else{
        		for(RegistryConfig rc1:result){
        			resultIp = NetUtils.getIpByHost(rc1.getHost());
        			logger.debug("sourceIp:{},resultIp:{},have the same ip and port?{}",new Object[]{sourceIp,resultIp,sourceIp.equals(resultIp)&&rc.getPort() == rc1.getPort()});
        			if(!(sourceIp.equals(resultIp)&&rc.getPort() == rc1.getPort()))
        				result.add(rc);
        		}
        	}
        }
        logger.debug("filter collection size :{},result size:{}",registryConfigList.size(),result.size());
        return result;
	}
	
	/*
	 * 批量注册工具
	 * 向多个注册中心，注册多个服务
	 * 
	 * 	key:注册中心的IP:port
	 * 	value:List<ServiceConfig>
	 * @param registry_map
	 */
	private static void regBatch(Map<String,List<ServiceConfig<?>>> registry_map){
		for(String key:registry_map.keySet()){
			List<ServiceConfig<?>> list=registry_map.get(key);
			//List<RegistryBean> registryBean_list=new ArrayList<RegistryBean>();
			String ip=key.split(":")[0];
			int port =Integer.valueOf(key.split(":")[1]);
			RegistryService registryService=RegistryFactory.getRegistryService(ip,port);
			for(ServiceConfig<?> sc:list){
				//判断是否要注册
				if(sc.getIsReg()){
					RegistryBean rb=new RegistryBean();
					rb.setDisplayName(sc.getDisplayName());//显示名
					rb.setDepartment(sc.getDepartment());//部门
					rb.setDescibe(sc.getDucment());//文档
					rb.setLayer(sc.getLayer());//服务所在层
					rb.setOwner(sc.getOwner());//责任人
					rb.setPortalId(sc.getPortalId());//系统标识
					rb.setServiceName(sc.getPath());
					rb.setVersion(null);//目前无用
					rb.setIp(null);//NetUtils.getLocalHost()，请不要设置IP，注册中心会取channel的IP，解决多网卡问题
					rb.setPort(sc.getProtocol().getPort());//端口
					rb.setJarVersion(Version.getVersion());//RSF jar包的版本
					
					
					if(rb.getServiceName() == null || "".equals(rb.getServiceName())){
						throw new RpcException("发布服务失败，缺少必要参数：ServiceName");
					}
					if(rb.getPortalId() == null || "".equals(rb.getPortalId())){
						throw new RpcException("发布服务失败，缺少必要参数：PortalId");
					}
					if(rb.getDescibe() == null || "".equals(rb.getDescibe())){
						throw new RpcException("发布服务失败，缺少必要参数：Descibe");
					}
					if(rb.getOwner() == null || "".equals(rb.getOwner())){
						throw new RpcException("发布服务失败，缺少必要参数：Owner");
					}
					if(rb.getDepartment() == null || "".equals(rb.getDepartment())){
						throw new RpcException("发布服务失败，缺少必要参数：Department");
					}
					//IP是空，不可以设置IP，因为服务器有多块网卡，你设置的是哪块网卡的IP?
					//“服务提供者”与“服务注册中心”建立连接后，一定是选择了多块网卡中的一块
					//所在要在“服务注册中心”这端，取建立的这条连接，再取出IP
					//if(rb.getIp() == null || "".equals(rb.getIp())){
					//	throw new RpcException("发布服务失败，缺少必要参数：Ip");
					//}
					if(rb.getPort() <= 0){
						throw new RpcException("发布服务失败，缺少必要参数：Port");
					}
					
					//registryBean_list.add(rb);
					logger.info("注册服务:"+rb.toString()+",注册中心:"+key);
					try{
						registryService.register(new RegistryBean[]{rb});
					}catch(Exception e){
						//如果发生了异常，后续还有“定时注册”来保证注册工作的进行
						logger.info("注册服务时发生异常:"+rb.toString()+",注册中心:"+key,e);
					}
				}
			}
			
			// 赵磊 2013-7-24
			//取消批量注册，改为单个注册
			//一次注册的超时时间是值是5秒，（大量服务）批量注册的执行时间可能会超过5秒，从而产生超时异常。
			//所以改为单个注册，通信次数变多了，但不会报超时错误。
			
//			if(registryBean_list!=null && registryBean_list.size()>0){
//				logger.info("向"+key+"注册中心,注册"+registryBean_list.size()+"个服务");
//				registryService.register(registryBean_list.toArray(new RegistryBean[0]));
//			}
		}
	}
	/**
	 * 下载服务列表 ，并保存服务列表到ServiceProviderList
	 * @param serviceName
	 * @param addr ip:port
	 * @return
	 */
	public static List<Provider> download(ClientConfig<?> clientConfig){
		String addr=null;
		List<RegistryConfig> list_r=clientConfig.getRegistries();
		if(list_r!=null && list_r.size()>0){
			//有多个注册中心,目前只能使用第一个。
			//只向第一个注册中心注册，
			//如果以后有了多个注册中心，请发造这里
			RegistryConfig rc=list_r.get(0);
			addr=rc.getHost()+":"+rc.getPort();
		}	
		
		String serviceName=clientConfig.getInterfaceClass().getName();
		String portalId=clientConfig.getPortalId();
			
		//取得注册中心的服务接口的代理类，用于与注册中心通信
		RegistryService registryService = RegistryFactory.getRegistryService(addr);
		if(logger.isDebugEnabled()){
			logger.debug("从注册中心下载"+serviceName+"服务的提供者，注册中心地址=" + addr+",portalId="+portalId);
		}
		List<RegistryBean> service_list = registryService.lookup(serviceName,portalId);
		
		if (service_list == null || service_list.size() == 0) {
			throw new RpcException(
					"无法从注册中心下载服务提供者列表,interfaceName=" + serviceName);
		}
		
		//重点
		//更新服务提供者列表
		ServiceProviderList.updateServiceProviderList(service_list);
		
		//added by liuhe, 20130603
		List<Provider> list = ServiceProviderList.findServiceList(serviceName);
		
		return list;
	}
}
