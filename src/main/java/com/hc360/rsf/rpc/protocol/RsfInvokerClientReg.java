/**
 * RsfInvokerClientReg.java   2012-6-15
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.rpc.protocol;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hc360.rsf.common.Constants;
import com.hc360.rsf.common.URL;
import com.hc360.rsf.config.ClientConfig;
import com.hc360.rsf.config.GlobalManager;
import com.hc360.rsf.registry.Provider;
import com.hc360.rsf.registry.RegistryFactory;
import com.hc360.rsf.registry.ServiceProviderIdelList;
import com.hc360.rsf.registry.ServiceProviderList;
import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.Client;
import com.hc360.rsf.remoting.RemotingException;
import com.hc360.rsf.rpc.EchoService;
import com.hc360.rsf.rpc.RpcException;
import com.hc360.rsf.rpc.RpcInvocation;
import com.hc360.rsf.rpc.loadbalance.LoadBalance;

/**
 * Invoker接口的实现
 * 
 * 作用：在客户端使用，客户端“发起调用”。
 * 
 * 扩展发父类RsfInvokerClientP2p的功能，
 * 
 * 本类添加了新的能力,可从注册中心下载服务列表,创建多个连接并选中其中 一个发起调用,实现了软负载。
 * 
 * @author zhaolei 2012-6-15
 */
public class RsfInvokerClientReg<T> extends RsfInvokerClientP2p<T> {
	
	//@modify
	private static final Logger logger = LoggerFactory.getLogger(RsfInvokerClientReg.class);
	private volatile boolean init = false;// 是否下载过服务列表  
	private ClientConfig<?> clientConfig;

	/**
	 * 构造方法
	 * 
	 * @param serviceType
	 * @param url
	 */
	public RsfInvokerClientReg(Class<T> serviceType, URL url) {
		super(serviceType,url);
	}
	public RsfInvokerClientReg(Class<T> serviceType, URL url,ClientConfig<?> clientConfig) {
		super(serviceType,url);
		this.clientConfig=clientConfig;
	}
	
	/**
	 * 重写了父类的方法，提供增加功能
	 * 从注册中心下载服务列表,连接多选 一,做软负载
	 * 
	 * @param invocation
	 * @return
	 * @see com.hc360.rsf.rpc.Invoker#invoke(com.hc360.rsf.rpc.RpcInvocation)
	 */
	protected Channel doInvoker(RpcInvocation invocation){
		String serviceName = getInterface().getName();//服务名
		if (!init) {
			synchronized (this) {
				if (!init) {
					//准备定时下载
					//不多余，Java编码方式（非XML方法）配置，需要以下这一行。
					GlobalManager.TIMER.appendClientConfig(clientConfig);
					
					List<Provider> url_list=ServiceProviderList.findServiceList(serviceName);
					if(url_list==null || url_list.size()==0){
						url_list=RegistryFactory.download(clientConfig);//下载服务列表 ，并保存服务列表到ServiceProviderList
					}
					
					// 创建当前服务的所有连接
					// 这里也可以不创建，在发起调用时（下面），再创建，也可以
					Client client = GlobalManager.getClient();
					for(Provider url:url_list){
						try{
							client.getOrCreateChannel(url.getIp(),url.getPort());
						}catch(Exception e){
							//创建失败--忽略，以后由定时任务负责创建连接
						}
					}
					init = true;
				}
			}
		}
		//服务提供者列表,复制一个新的List,防修改
		List<Provider> providerList = new CopyOnWriteArrayList<Provider>(ServiceProviderList.findServiceList(serviceName));
		Channel channel =null;
		String path = invocation.getAttachments().get(Constants.PATH_KEY);// 接口名
		boolean bl=EchoService.class.getName().equals(path);
		
		if(!bl){
			//这是防御性代码(第一防线)
			if (providerList == null || providerList.size() == 0){
				//99.99%不会走入这里
				//无服务提供者列表，说明注册中心也没有。
				//从 “不可用--服务列表”中查找，做为一种补救措施
				//不可用服务 提供者列表,复制一个新的List,防修改
				providerList = new CopyOnWriteArrayList<Provider>(ServiceProviderIdelList.findServiceList(serviceName));
				if(providerList!=null && providerList.size()>0){
					String message = "serviceName="+serviceName+",无法找到服务提供者,无法发出请求,可能是服务端没有启动或网络故障。";
					logger.error(message);
					String message2 = "但是从不可用服务提供者列表中找到"+providerList.size()+"个过时的服务提供者，将尝试性建立连接并发起调用：\r\n";
					for(int i=0;i<providerList.size();i++){
						Provider p=providerList.get(i);
						message2+="不可用的、过时的服务提供者："+p.toString();
						if(i<providerList.size()-1){
							message2+="\r\n";
						}
					}
					logger.error(message2);
				}
				
				//失败转移+负载均衡 从idle列表中
				channel = failOver(providerList,invocation,true);
				// TODO  回声测试，并恢复到可用列表
			}else{
				//99.99%走这里
				//失败转移+负载均衡 从active列表中
				channel = failOver(providerList,invocation,false);
			}
		}else{
			//为回声测试获得channel
			channel = failOver(providerList,invocation,false);
		}
		
		String message = null;
		if(channel==null){							
			message = "无法找到服务提供者,无法发出请求,可能是服务端没有启动或网络故障,channel=null,serviceName="+serviceName;
			logger.error(message);
		    throw new RpcException(RpcException.FORBIDDEN_EXCEPTION,message);
	    }
		if(!channel.isConnected()){
			message = "无法找到服务提供者,无法发出请求,可能是服务端没有启动或网络故障,channel="+channel.toString()+"但channel已断开,serviceName="+serviceName;
			logger.error(message);
			channel.close("发起调用时，已选择了channel,但发现channel已是断开状态");//会触发连接关闭事件，在关闭事件中，把连接从连接池中移除
		    throw new RpcException(RpcException.FORBIDDEN_EXCEPTION,message);
	    }
		
		return channel;
	}
	/**
	 * 失败转移+负载均衡
	 * @param urls
	 * @param invocation
	 * @return
	 */
	private Channel failOver(List<Provider> urls,RpcInvocation invocation){
		if (urls == null || urls.size() == 0){
			return null;
		}
		
		
		URL url=getUrl();
		String loadBalance_name=url.getParameter(Constants.LOADBALANCE_KEY);
		// 取出软负载的实现方式 
		LoadBalance LoadBalance=GlobalManager.getLoadBalance(loadBalance_name);
		
		Provider url_select = LoadBalance.select(urls, invocation);
		Client client = GlobalManager.getClient();
		
		//从连接池中取连接，如果池中没有连接就新建连接
		Channel channel = null;
		try{
			channel=client.getOrCreateChannel(url_select.getIp(),url_select.getPort());		
		}catch(Exception e){
			logger.error("创建连接时异常",e);
		}
		urls.remove(url_select);
		while( (channel==null || !channel.isConnected()) &&  urls.size()>0 ){
			channel=failOver(urls,invocation);//连接不可用,转移到下一个
		}
		return channel;
	}
	
	/**
	 * 得到可能可用的channel
	 * 
	 * @param providers  服务提供者列表 (取决于isIdle的值，true 为不可用服务提供者列表   false 为可用服务提供者列表)
	 * @param invocation 调用的方法封装对象
	 * @param isIdle 决定是否是从不可用服务列表中恢复
	 * @return  channel(可能是连接的，也可能是非连接的) or null
	 */
	private Channel failOver(List<Provider> providers,RpcInvocation invocation,boolean isIdle){
		//入参检验
		if (providers == null || providers.size() == 0){
			return null;
		}
		
		URL url=getUrl();
		String loadBalance_name=url.getParameter(Constants.LOADBALANCE_KEY);
		// 取出软负载的实现方式 
		LoadBalance loadBalance=GlobalManager.getLoadBalance(loadBalance_name);
		
		Channel channel = getChannel(loadBalance,providers,invocation,isIdle);
		
		logger.debug("经过failOver得到的channel:{}{}",channel,(channel==null?"":",连接状态是:"+(channel.isConnected()?true:false)));
		return channel;
	}
	
	/**
	 * 递归的去寻找可用的channel
	 * 
	 * @param loadBalance 服务节点选择器
	 * @param providers  服务节点集合
	 * @param invocation 调用的方法封装对象
	 * @param isIdle   决定是否是从不可用服务列表中恢复
	 * @return  channel(可能是连接的，也可能是非连接的) or null
	 */
	private Channel getChannel(LoadBalance loadBalance,List<Provider> providers,RpcInvocation invocation,boolean isIdle){
		Channel channel = null;
		Client client = GlobalManager.getClient();
		Provider provider_select = loadBalance.select(providers, invocation);
		logger.debug("isIdle:{},provider_select:{}",isIdle,provider_select);
		//从连接池中取连接，如果池中没有连接就新建连接
		try{
			channel = client.getOrCreateChannel(provider_select.getIp(),provider_select.getPort());		
		}catch(Exception e){
			logger.error("创建连接时异常",e);
		}
		
		boolean contain = false;
		//处理完一个服务提供者节点，将它移除
		providers.remove(provider_select);
		if(channel != null && channel.isConnected()){
			//从可用服务列表取的时候，直接返回channel
			if(!isIdle){
				return channel;
			}
			
			//做回声方式是否存在的测试
			try {
				contain = channel.isContainService(provider_select.getServiceName());
				if(contain){
					//将从不可用列表中找到的可用提供者恢复到可用列表中,并从不可用列表中移除
					ServiceProviderList.addNode(provider_select);
					ServiceProviderIdelList.romveNode(provider_select);
					logger.info("{}从ServiceProviderIdelList恢复到ServiceProviderList里。",provider_select);
					return channel;
				}
			} catch (RemotingException e) {
		        logger.error(MessageFormat.format("{0}回声测试{1}出现异常",channel,provider_select.getServiceName()),e);
			}
		}
		
		//这个节点不能用，进行下个节点的尝试
		while(providers.size()>0){
			channel = getChannel(loadBalance,providers,invocation,isIdle);
		}
		
		//防止得到与其他系统的连接 
		if(isIdle&&channel!=null&&channel.isConnected()&&!contain){
			logger.debug("可用列表没有提供者，从不可用列表中寻找，但一样没有找到，将channel设置为空");
			channel = null; 
		}
		return channel;
	}
}
