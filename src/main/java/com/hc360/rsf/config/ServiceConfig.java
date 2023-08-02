/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config;

import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hc360.rsf.common.Constants;
import com.hc360.rsf.common.URL;
import com.hc360.rsf.common.utils.PortUtils;
import com.hc360.rsf.registry.RegistryFactory;
import com.hc360.rsf.rpc.Invoker;
import com.hc360.rsf.rpc.RpcException;

/**
 * 通过ServiceConfig发布一个服务
 * 
 * @author zhaolei 2012-4-25
 */
public class ServiceConfig<T> extends AbstractConfig {
	private static Logger logger = LoggerFactory.getLogger(ServiceConfig.class);
	private static final long serialVersionUID = -2647470803555145186L;

	// 应用名称
	private String displayName;

	// 应用负责人
	private String owner;

	// 部门
	private String department;

	// 分层
	private String layer;

	// 接口类型
	private Class<?> interfaceClass;

	// 接口实现类引用
	private T ref;

	// 服务名称
	private String path;

	// 是否注册
	private Boolean isReg=true;

	// 服务的说明性文档
	private String ducment;

	// 字符集（目前无用）
	private String charset;

	// 协议
	private ProtocolConfig protocol;

	// 注册中心
	private List<RegistryConfig> registerCenterList;
	
	// 注册中心的id,值为<rsf:registry>的id属性,
	// 多个注册中心ID用逗号分隔,
	// 如果不想将该服务注册到任何registry,可将值设为"N/A"
	// 使用XML配置文件时,需要使用到该值 ,可能<rsf:registry>写在后面
	// 要在配置文件全解析完成才能找出<rsf:registry>
	private String registriesStr;
	
	//权重（目前无用）
	private int weight;
	
	//系统名称
	private String portalId;
	
	//是否加密通信
	private boolean security=false;
	
	/**
	 *  向服务注册中心  注册本服务
	 */
	public void registerService(){
		RegistryFactory.reg(this);
		
		//准备定时注册,会排重
		//不多余，Java编码方式（非XML方法）配置，需要以下这一行。
		List<ServiceConfig<?>> serviceConfigList=new ArrayList<ServiceConfig<?>>(1);
		serviceConfigList.add(this);
		GlobalManager.TIMER.appendServiceConfigList(serviceConfigList);
	}
	
	/**
	 * 在本地暴露服务
	 */
	@SuppressWarnings("unchecked")
	public synchronized void export() {
		if(protocol == null){
			//如果protocol为空,使用RSF框架默认配置文件中的属性来构造一个protocol对象
			protocol=new ProtocolConfig();
		}
		//默认值 --服务端通信协议
		if(protocol.getName()==null){
			protocol.setName(Constants.SERVER_PROTOCOL_NAME);
		}
		//默认值 --IP
		if(protocol.getHost()==null){
			protocol.setHost(Constants.SERVER_PROTOCOL_HOST);
		}
		//默认值 --端口
		if(protocol.getPort()<=0 && protocol.getPorts()==null){
			/*
			 * 赵磊修改 2012-9-20
			 * 
			 * 服务端，监听的端口，默认监听63634端口
			 * 旧机制使用单一端口，适合于点对点的通信场景
			 * 
			 * 服务端协议--默认监听端口段中的一个可用端口
			 * 解决模拟分布式环境中，一台物理服务上运行多个同一个项目的逻辑节点，端口被占用问题
			 * 如果63634被占用，按顺序尝试其它端口，最终选择一个可用的端口
			 * 
			 * 新机制使用端口段中的一个可用端口，适合于有“服务注册中心”的场景
			 */
			String defaultPort=String.valueOf(Constants.SERVER_PROTOCOL_PORT_SEGMENT);
			Integer[] ports=PortUtils.analysis(defaultPort);
			//新机制,设置端口段，按顺序使用其中一个可用端口，适合于有“服务注册中心”的场景
			protocol.setPorts(ports);
		}
		//默认值--线程池类型
		if(protocol.getThreadpool()==null ||"".equals(protocol.getThreadpool().trim())){
			protocol.setThreadpool(String.valueOf(Constants.THREADPOOL_TYPE_MIXED));
		}else{
			if(! (Constants.THREADPOOL_TYPE_FIXED.equalsIgnoreCase(protocol.getThreadpool().trim()) 
					|| Constants.THREADPOOL_TYPE_CACHED.equalsIgnoreCase(protocol.getThreadpool().trim())
					|| Constants.THREADPOOL_TYPE_MIXED.equalsIgnoreCase(protocol.getThreadpool().trim())
			)){
				throw new IllegalArgumentException("线程池类型配置错误，必须fixed/cached/mixed 3选1");
			}
		}
		try {
			/*
			 * 服务端协议--默认监听端口段中的一个可用端口
			 * 解决模拟分布式环境中，一台物理服务上运行多个同一个项目的逻辑节点，端口被占用问题
			 * 如果63634被占用，按顺序尝试其它端口，最终选择一个可用的端口
			 * 
			 * 新机制使用端口段中的一个可用端口，适合于有“服务注册中心”的场景
			 * 但以后将优先使用ports数组
			 */
			Integer[] ports=protocol.getPorts();
			if(ports!=null){
				//新机制使用端口段中的一个可用端口，适合于有“服务注册中心”的场景
				for(int i=0;i<ports.length;i++){
					int temp_port=ports[i];
					try{
						//核心：启动服务端的端口监听
						startServer(temp_port);
						
						//将最终确定使用的端口，一定要回写到protocol对象中，
						//因为后面的“暴露服务”与“发布服务”都会执行protocol.getPort()取出服务端监听的端口
						protocol.setPort(temp_port);
						
						//绑定端口成功，跳出循环
						break;
					}catch(IOException e){
						//绑定端口失败
						if(i!=ports.length-1){
							logger.warn("RSF Server 绑定端口失败,端口"+temp_port+"被占用,正在尝试下一个端口.并关闭之前打开的线程池");
							if(GlobalManager.executor_server!=null){
								GlobalManager.executor_server.shutdownNow();
							}
						}else{
							//已尝试过所有事先准备好的端口，但都已被占用
							logger.error("RSF Server 启动失败,端口"+temp_port+"被占用，以无端口可用。");
							throw new RpcException("RSF Server 绑定端口失败,端口"+temp_port+"被占用",e);//服务端动态端口
						}
					}
				}
			}else{
				//旧机制使用单一端口，适合于点对点的通信场景
				//为了兼容旧机制，port还要保留，因为有RSF的早期用户（配置管理中心）项目使用了setPort(),getPort()方法,设置端口
				try{
					startServer(protocol.getPort());
				}catch(BindException e){
					throw new RpcException("RSF Server 绑定端口失败失败,端口"+protocol.getPort()+"被占用",e);
				}
			}
		} catch (Exception e) {
			throw new RpcException("RSF Server 启动失败",e);//服务端动态端口
		}
		// 使用URL类封装：协议名,prot,ip,interface名称等参数
		// 在服务端使用的URL都是在此处创造的。
		// Invoker中有持有本URL对象的引用,用于生成Invoker的key
		
		// 本系统内URL有2类，是互相不能混用的，要注意区分
		// 第一类，Client\Server类中拥有一个全局的URL，其中的参数是全局的参数
		// 第二类，ClientConfig类createProxy()方法，有自己的URL，其中的参数是某个接口级的参数
		// 下面的URL是第二类
		
		URL url = new URL(protocol.getName(), protocol.getHost(), protocol.getPort());
		url = url.setPath(interfaceClass.getName());
		setPath(interfaceClass.getName());
		
		//从RSF 1.3.0 开始加入此代码
		//临时保存生成的多个参数
		Map<String, String>  parameters=new HashMap<String,String>();
		if(isSecurity()){
			parameters.put(Constants.ISSECURITY_KEY, "true");//只在本端使用此参数
		}
		url =url.addParameters(parameters);
		url=url.addParameter(Constants.PAYLOAD_KEY,protocol.getPayload());
		
		//从RSF 1.0.0 开始加入此代码
		@SuppressWarnings("rawtypes")
		Invoker<?> invoker = GlobalManager.proxyFactory.getInvoker(ref, (Class) interfaceClass, url);
		logger.info("RSF ServerConfig 暴露服务:{},服务显示名称：{}", interfaceClass.getName(),displayName);
		GlobalManager.protocol.export(invoker);
	}
	
	private void startServer(int prot) throws NumberFormatException, IOException{
		//为什么没把这段代码，向上提到ConfigLoader类的start()方法？
		//为了兼容XML配置与java编码配置两种使用方式 。
		//向上提到ConfigLoader类的start()方法后，只对XML配置生效。
		//java编码配置就会有问题。
		
		// 本系统内URL有2类，是互相不能混用的，要注意区分
		// 第一类，Client\Server类中拥有一个全局的URL，其中的参数是全局的参数
		// 第二类，ClientConfig类createProxy()方法，有自己的URL，其中的参数是某个接口级的参数
		// 下面的URL是第一类
		
		URL url=new URL(protocol.getName(),"0.0.0.0",prot);//协议、IP、端口
		url=url.addParameter(Constants.THREAD_NAME_KEY, protocol.getThreadpool());//选用一种线程池
		url=url.addParameter(Constants.THREADS_COREPOOLSIZE_KEY, protocol.getCorePoolSize());//指定线程池核心大小
		url=url.addParameter(Constants.THREADS_MAXIMUMPOOLSIZE_KEY, protocol.getMaximumPoolSize());//指定线程池最大大小
		url=url.addParameter(Constants.QUEUES_KEY, protocol.getQueueSize());//指定线程池队列大小
		url=url.addParameter(Constants.THREAD_ALIVE_KEY, protocol.getKeepalive());//指定线程空闲时间
		url=url.addParameter(Constants.PAYLOAD_KEY,protocol.getPayload());
		GlobalManager.startServer(url);//一个端口只会启动一次，里面有检查
	}

	public RegistryConfig getRegistry() {
		return registerCenterList == null || registerCenterList.size() == 0 ? null : registerCenterList.get(0);
	}

	public void setRegistry(RegistryConfig registry) {
		List<RegistryConfig> registries = new ArrayList<RegistryConfig>(1);
		registries.add(registry);
		this.registerCenterList = registries;
	}

	public List<RegistryConfig> getRegistries() {
		return registerCenterList;
	}

	public void setRegistries(List<RegistryConfig> registries) {
		this.registerCenterList = (List<RegistryConfig>) registries;
	}

	public ProtocolConfig getProtocol() {
		return protocol;
	}

	public void setProtocol(ProtocolConfig protocol) {
		this.protocol = protocol;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getLayer() {
		return layer;
	}

	public void setLayer(String layer) {
		this.layer = layer;
	}

	public Class<?> getInterfaceClass() {
		return interfaceClass;
	}

	public void setInterfaceClass(Class<?> interfaceClass) {
		this.interfaceClass = interfaceClass;
	}

	public T getRef() {
		return ref;
	}

	public void setRef(T ref) {
		this.ref = ref;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Boolean getIsReg() {
		return isReg;
	}

	public void setIsReg(Boolean isReg) {
		this.isReg = isReg;
	}

	public String getRegistriesStr() {
		return registriesStr;
	}

	public void setRegistriesStr(String registriesStr) {
		this.registriesStr = registriesStr;
	}

	public String getDucment() {
		return ducment;
	}

	public void setDucment(String ducment) {
		this.ducment = ducment;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getPortalId() {
		return portalId;
	}

	public void setPortalId(String portalId) {
		this.portalId = portalId;
	}

	public boolean isSecurity() {
		return security;
	}

	public void setSecurity(boolean security) {
		this.security = security;
	}
	
}
