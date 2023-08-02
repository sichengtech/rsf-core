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

import com.hc360.rsf.config.GlobalManager;
import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.ChannelPool;
import com.hc360.rsf.remoting.Client;

/**
 * 服务提供者列表
 * 
 * @author zhaolei 2012-6-14
 */
public class ServiceProviderList {
	private static Logger logger = LoggerFactory.getLogger(ServiceProviderList.class);
	/**
	 * 服务提供者列表(重点)
	 * 
	 * 一个服务（服务接口）可能有多个服务提供者 <br>
	 * key=服务接口的全限定名<br>
	 * value=服务提供者集合，[Provider,Provider,Provider]<br>
	 */
	private static final Map<String, List<Provider>> SERVICE_LIST_MAP = new ConcurrentHashMap<String, List<Provider>>();

	/**
	 * 维护（添加、减少、修改）“服务提供者列表”，非常重要的方法 。
	 * 
	 * 1、注册中心某个服务发生了变化，信息会被推送到这里
	 * 2、Timer会定时向注册下载服务信息，信息会通过本方法维护
	 * 
	 * @param list 来自注册中心的“服务”的描述信息
	 */
	public static void updateServiceProviderList(List<RegistryBean> list){
		for(RegistryBean rb:list){
			
			Provider prov=new Provider();
			prov.setServiceName(rb.getServiceName());
			prov.setIp(rb.getIp());
			prov.setPort(rb.getPort());
			
			if(rb.getStat()==RegistryBean.ADD_NODE){
				logger.debug("服务提供者列表收到数据--加节点："+rb);
				ServiceProviderList.addNode(prov);
				//@modify
				//及时创建新连接
				Client client = GlobalManager.getClient();
				client.getOrCreateChannel(prov.getIp(),prov.getPort());
			}else if(rb.getStat()==RegistryBean.DECR_NODE_MANUAL){
				logger.debug("服务提供者列表收到数据--人工减节点："+rb);
				ServiceProviderList.romveNode(prov);
				
			}else if(rb.getStat()==RegistryBean.DECR_NODE_BREAKDOWN){
				logger.debug("服务提供者列表收到数据--故障减节点："+rb);
				
				Client client = GlobalManager.getClient();
				Channel channel=client.getChannelFromPool(prov.getIp(),prov.getPort());
				if(channel!=null ){
					if(channel.isConnected()){
						//channel当前是可用状态,但收到了"故障减节点"命令,为防止是误报,要测试一次
						ChannelPool pool=client.getChannelPool();
						boolean result=pool.isConnected4Hearbeat(channel);//心跳结果
						if(result){
							//2013-7-12 赵磊，新加的功能。向服务端询问是否提供某个服务
							try{
								boolean bl = channel.isContainService(rb.getServiceName());
								
								if(!bl){
									//连接是通过的，但服务端不提供此接口的服务,做减节点操作
									boolean bb=ServiceProviderList.ishas(prov);
									String msg="收到故障减节点，执行减节点5.";
									String status="客户端状态=["+ (bb?"有提供者":"无提供者")+",有连接、非断开状态、心跳通过、但服务端未提供"+prov.getServiceName()+"服务]";
									logger.debug(msg+status+rb);
									//logger.debug("故障减节点验证--实施5.此连接已不可以使用,判定发生故障."+rb);
									ServiceProviderList.romveNode(prov);
									//不需要 放入不可用列表
									
									//结束本次循环
									continue; 
								}
							}catch(Exception e){
								//为什么会异常？
								//EchoService的$echoInterface()方法是在1.3.0版本添加了，向低版本的服务端执行回声测试，一定会返回异常
								//做减节点操作
								boolean bb=ServiceProviderList.ishas(prov);
								String msg="收到故障减节点，执行减节点4.";
								String status="客户端状态=["+ (bb?"有提供者":"无提供者")+",有连接、非断开状态、心跳通过、回声测试时异常可能服务端RSF版本低不支持$echoInterface()]";
								logger.debug(msg+status+rb,e);
								//logger.error("故障减节点验证--实施4,发生异常.",e);
								//放入不可用列表
								if(bb){
									Provider p2=ServiceProviderList.find(prov);
									ServiceProviderIdelList.addNode(p2);
								}
								//从可用列表中删除
								ServiceProviderList.romveNode(prov);
								
								//结束本次循环
								continue;
							}
							
							//----------------------------
							//上面的continue，能走到这里就说明：
							//此连接是可以使用的,并没有发生故障,本次是误报
							logger.warn("故障减节点验证--误报.此连接是可以使用的,并没有发生故障."+rb);
							
							//结束本次循环，这里是终点（出口）。
							//----------------------------
							
						}else{
							//此连接是不可以使用的,做减节点操作
							boolean bl=ServiceProviderList.ishas(prov);
							String msg="收到故障减节点，执行减节点1.";
							String status="客户端状态=["+ (bl?"有提供者":"无提供者")+",有连接是断开状态、心跳不通]";
							logger.debug(msg+status+rb);
							//logger.debug("故障减节点验证--实施2.连接是断开状态,判定发生故障."+rb);
							//放入不可用列表
							if(bl){
								Provider p2=ServiceProviderList.find(prov);
								ServiceProviderIdelList.addNode(p2);
							}
							//从可用列表中删除
							ServiceProviderList.romveNode(prov);
						}
					}else{
						//连接已不是连接状态
						//从服务列表中删除这个服务提供者
						boolean bl=ServiceProviderList.ishas(prov);
						String msg="收到故障减节点，执行减节点2.";
						String status="客户端状态=["+ (bl?"有提供者":"无提供者")+",有连接是断开状态]";
						logger.debug(msg+status+rb);
						//logger.debug("故障减节点验证--实施2.连接是断开状态,判定发生故障."+rb);
						//放入不可用列表
						if(bl){
							Provider p2=ServiceProviderList.find(prov);
							ServiceProviderIdelList.addNode(p2);
						}
						//从可用列表中删除
						ServiceProviderList.romveNode(prov);
					}
				}else{
					//池中没有相应的连接,可能连接已被心跳线程移动到不可用连接池
					//从服务列表中删除这个服务提供者
					boolean bl=ServiceProviderList.ishas(prov);
					String msg="收到故障减节点，执行减节点3.";
					String status="客户端状态=["+ (bl?"有提供者":"无提供者")+",无连接]";
					logger.debug(msg+status+rb);
					//放入不可用列表
					if(bl){
						Provider p2=ServiceProviderList.find(prov);
						ServiceProviderIdelList.addNode(p2);
					}
					//从可用列表中删除
					ServiceProviderList.romveNode(prov);
				}
			}else{
				logger.error("注册中心推来数据,缺少状态标识,无法处理"+rb);
			}
		}
	}
	
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
		List<Provider> list = SERVICE_LIST_MAP.get(serviceName);
		if (list != null) {
			return list;
		}		
		return new ArrayList<Provider>();
	}
	
	/**
	 * 取得全部服务列表  
	 * @return
	 */
	public static List<Provider> findAllServiceList(){
		List<Provider> list =new ArrayList<Provider>(SERVICE_LIST_MAP.size());
		for(String key:SERVICE_LIST_MAP.keySet()){
			List<Provider> inner=SERVICE_LIST_MAP.get(key);
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
			List<Provider> list=SERVICE_LIST_MAP.get(serviceName);
			if(list==null){
				synchronized (ServiceProviderList.class) {
					list=SERVICE_LIST_MAP.get(serviceName);
					if(list==null){
						list=new CopyOnWriteArrayList<Provider>();
						SERVICE_LIST_MAP.put(serviceName, list);
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
				list.add(prov);
			}else{
				logger.debug("服务列表，添加节点,发现重复节点,忽略。"+prov);
			}
		}else{
			logger.error("服务列表，添加节点,服务接口名=null");
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
		List<Provider> list=SERVICE_LIST_MAP.get(serviceName);
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
	 * 查找一个“服务提供者Provider”
	 * 
	 * 用Provider p1对象，到SERVICE_LIST_MAP进行查找(equals方法 重写过，用于比较两个Provider对象).
	 * 找出Provider p2，p1 与 p2 的5个核心属性是相同的。但其它非核心属性可能不相同，如weight权重。
	 * 所在要使用p1找出p2.
	 * 
	 * @param p1
	 * @return
	 */
	public static Provider find(Provider p1){
		if(p1==null){
			return null;
		}
		String serviceName=p1.getServiceName();
		List<Provider> list=SERVICE_LIST_MAP.get(serviceName);
		if(list!=null){
			for(Provider p2:list){
				//equals方法 重写过，用于比较两个Provider对象
				if(p2.equals(p1)){
					return p2;
				}
			}
		}
		return null;
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
		List<Provider> list=SERVICE_LIST_MAP.get(serviceName);
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
