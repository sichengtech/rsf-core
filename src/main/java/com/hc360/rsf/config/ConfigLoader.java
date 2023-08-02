/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.hc360.rsf.common.Constants;
import com.hc360.rsf.common.utils.PortUtils;
import com.hc360.rsf.common.utils.ReflectUtils;
import com.hc360.rsf.config.callback.CallBack;
import com.hc360.rsf.registry.RegistryFactory;

/**
 * 配置加载器，核心类，万分重要，是一切的开始，这里就是入口。
 * 
 * 用于读取RSF框架的配置文件,并解析。可以完成服务端在本地暴露服务,向注册中心注册服务
 * 客户端向注册中下载服务 等工作
 * 
 * @author zhaolei 2012-6-6
 */
public class ConfigLoader {
	private static Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
	static final String SPRING_DEFAULT_NAMESPACE = "http://www.springframework.org/schema/beans";
	static final String RSF_DEFAULT_NAMESPACE = "http://code.hc360.com/schema/rsf";
	private final Map<String, Object> BEAN_MAP = new ConcurrentHashMap<String, Object>();
	/**
	 * 用于生成临时ID
	 */
	private static final AtomicLong TEMP_ID = new AtomicLong(0);
	
	static final String FILE_PREFIX="file:";
	static final String CLASS_PREFIX="classpath:";
	static final String WEB_PREFIX="/WEB-INF/";
	
	//启动标记
	private boolean isStart=false;
	
    /**
     * 释放RSF所有打开的资源
     * 
     */
	static public void destroy(){
		AbstractConfig.destroy();
	}
	static public void destroy(String msg){
		AbstractConfig.destroy(msg);
	}

	/**
	 * 构造方法,解析指定的配置文件
	 * 
	 * @param filePath 文件路径 
	 * file:D:\\config\\rsf.xml
	 * classpath:com/hc360/rsf/config/xml/rsf.xml
	 */
	public ConfigLoader(String filePath){
		logger.info("RSF加载配置文件："+filePath);
		load(filePath);
	}
	
	public ConfigLoader(String[] configFilePaths){
		if(configFilePaths!=null){
			for(String filePath:configFilePaths){
				logger.info("RSF加载配置文件："+filePath);
				load(filePath);
			}
		}else{
			throw new IllegalArgumentException("RSF配置文件路径为null");
		}
	}
	
	/**
	 * 把多种来源的配置文件转换为流 
	 * @param filePath
	 */
	private void load(String filePath){
		String xmlPath = null;
		InputStream input =null;
		if(filePath==null){
			throw new IllegalArgumentException("RSF配置文件路径为null");
		}
		xmlPath=filePath;
		if(filePath.toLowerCase().startsWith(FILE_PREFIX)){
			String path=filePath.substring(FILE_PREFIX.length(), filePath.length());
			File file =new File(path);
			try {
				input=new FileInputStream(file);
			} catch (FileNotFoundException e) {
				throw new IllegalArgumentException("RSF配置文件不存在,路径:"+filePath);
			}
			
		}else if(filePath.toLowerCase().startsWith(CLASS_PREFIX)){
			String path=filePath.substring(CLASS_PREFIX.length(), filePath.length());
			input = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
		}else{
			throw new IllegalArgumentException("RSF配置文件路径需要加前缀,"+FILE_PREFIX+"或"+CLASS_PREFIX+",路径："+filePath);
		}
		loadXML(input,xmlPath);
	}
	/**
	 * 构造方法,通过流取得配置文件并解析
	 * 
	 * @param in 输入流
	 */
	public ConfigLoader(InputStream input){
		logger.info("RSF加载配置文件："+"来自流");
		loadXML(input,"来自流");
	}
	/**
	 * 启动
	 * 
	 * 这是服务端、客户端启动的 重要方法 
	 * 
	 * 服务端启动，向注册中心注册服务
	 * 客户端启动，向注册中心下载服务
	 * 
	 */
	public synchronized void start() {
		if(isStart){
			logger.error("严重，不可以重复调用ConfigLoader的start()方法。");
			return;
		}
		
		//协议
		ProtocolConfig protocolConfig=null;
		for(String key:BEAN_MAP.keySet()){
			Object obj=BEAN_MAP.get(key);
			if(obj instanceof ProtocolConfig){
				protocolConfig=(ProtocolConfig)obj;
				break;//只支持配置一个协议
			}
		}
		
		for(String key:BEAN_MAP.keySet()){
			Object obj=BEAN_MAP.get(key);
			//服务提供者(服务端)
			if(obj instanceof ServiceConfig){
				ServiceConfig<?> serviceConfig=(ServiceConfig<?>)obj;
				//装配协议
				if(protocolConfig!=null){
					serviceConfig.setProtocol(protocolConfig);
				}
				//暴露服务
				serviceConfig.export();
				//装配注册中心
				String registriesStr=serviceConfig.getRegistriesStr();
				if(registriesStr!=null){
					String[] regs=registriesStr.split(",");
					List<RegistryConfig> list=new ArrayList<RegistryConfig>();
					for(String rs:regs){
						Object obj2=getBean(rs);
						if(obj2 instanceof RegistryConfig){
							RegistryConfig rc=(RegistryConfig)obj2;
							list.add(rc);
						}else{
							throw new RsfConfigFileException("id="+rs+"的RegistryConfig类型错误");
						}
					}
					//关联指定的注册中心
					serviceConfig.setRegistries(list);
				}else{
					//缺省关联所有注册中
					serviceConfig.setRegistries(getRegistryConfigList());
				}
			}
			//服务消费者（客户端）
			if(obj instanceof ClientConfig){
				ClientConfig<?> clientConfig=(ClientConfig<?>)obj;
				//装配注册中心
				String registriesStr=clientConfig.getRegistriesStr();
				if(registriesStr!=null){
					String[] regs=registriesStr.split(",");
					List<RegistryConfig> list=new ArrayList<RegistryConfig>();
					for(String rs:regs){
						Object obj2=getBean(rs);
						if(obj2 instanceof RegistryConfig){
							RegistryConfig rc=(RegistryConfig)obj2;
							list.add(rc);
						}else{
							throw new RsfConfigFileException("id="+rs+"的RegistryConfig类型错误");
						}
					}
					//关联指定的注册中心
					clientConfig.setRegistries(list);
				}else{
					//缺省关联所有注册中心
					clientConfig.setRegistries(getRegistryConfigList());
				}
			}
		}
		
		//---------------启动的工作 最后一步----------------------
		
		//服务端 要向注册中心 注册的服务,仅对XML配置有效
		List<ServiceConfig<?>>  list_ServiceConfig=getServiceConfigList();
		GlobalManager.TIMER.appendServiceConfigList(list_ServiceConfig);//准备定时注册
		
		//客户端要定时下载的服务,仅对XML配置有效
		List<ClientConfig<?>>  list_ClientConfig=getClientConfigList();
		for(ClientConfig<?> bean:list_ClientConfig){
			GlobalManager.TIMER.appendClientConfig(bean);//准备定时下载
		}
		
		//批量注册工具， 向多个注册中心，注册多个服务
		RegistryFactory.regBatch(list_ServiceConfig);
		
		//持有注册中心的引用
		GlobalManager.registryConfigList=getRegistryConfigList();
		
		//启动标记
		isStart=true;
	}
	
	/**
	 * 取得ProtocolConfig对象
	 * 目前只在服务端使用，全局只有一个对象
	 * 
	 * @return
	 */
	public ProtocolConfig getProtocolConfig(){
		for(String key:BEAN_MAP.keySet()){
			Object bean=BEAN_MAP.get(key);
			if(bean instanceof ProtocolConfig){
				ProtocolConfig protocolConfig=(ProtocolConfig)bean;
				return protocolConfig;
			}
		}
  		return null;
	}
	
	/**
	 * 取得全部ServiceConfig对象
	 * 一个ServiceConfig代表一个服务接口
	 * @return
	 */
	public List<ServiceConfig<?>>  getServiceConfigList(){
		List<ServiceConfig<?>> list_ServiceConfig=new ArrayList<ServiceConfig<?>>();
  		for(String key:BEAN_MAP.keySet()){
			Object bean=BEAN_MAP.get(key);
			if(bean instanceof ServiceConfig){
				ServiceConfig<?> serviceConfig=(ServiceConfig<?>)bean;
				list_ServiceConfig.add(serviceConfig);
			}
		}
  		return list_ServiceConfig;
	}
	
	/**
	 * 取得全部ClientConfig对象
	 * 一个ClientConfig代表一个客户端接口
	 * @return
	 */
	public List<ClientConfig<?>>  getClientConfigList(){
		List<ClientConfig<?>> list_ClientConfig=new ArrayList<ClientConfig<?>>();
  		for(String key:BEAN_MAP.keySet()){
			Object bean=BEAN_MAP.get(key);
			if(bean instanceof ClientConfig){
				ClientConfig<?> clientConfig=(ClientConfig<?>)bean;
				list_ClientConfig.add(clientConfig);
			}
		}
  		return list_ClientConfig;
	}

	/**
	 * 取得bean,包括ClientConfig,ServiceCinfig,RegistryConfig等类型的bean.
	 * 本方法并不常用，业务人员常用的方法是getServiceProxyBean()
	 * 
	 * @param BeanName
	 * @return
	 */
	public Object getBean(String BeanName){
		return BEAN_MAP.get(BeanName);
	}
	/**
	 * 返回bean的总数量 ,包括ClientConfig,ServiceCinfig,RegistryConfig等类型的bean.
	 * @return
	 */
	public int getBeanCount(){
		return BEAN_MAP.size();
	}
	/**
	 * 按ID取得"远程服务接口"的"本地代理"
	 * 
	 * @param beanId
	 * @return
	 */
	public Object getServiceProxyBean(String beanId){
		Object obj=BEAN_MAP.get(beanId);
		if(obj!=null && obj instanceof ClientConfig){
			ClientConfig<?> clientConfig=(ClientConfig<?>)obj;
			return clientConfig.getInstance();
		}else{
			return null;
		}
	}
	
	/**
	 * 按类型取得"远程服务接口"的"本地代理"
	 * 
	 * @param clazz
	 * @return Object[]
	 */
	public Object[] getServiceProxyBean(Class clazz){
		List<Object> list=new ArrayList<Object>();
		for(String key:BEAN_MAP.keySet()){
			Object obj=BEAN_MAP.get(key);
			if(obj!=null && obj instanceof ClientConfig){
				ClientConfig<?> clientConfig=(ClientConfig<?>)obj;
				Object rs=clientConfig.getInstance();
				Class[] cc=rs.getClass().getInterfaces();
				for(Class c:cc){
					if(c==clazz ){
						list.add(rs);
						break;
					}
				}
			}
		}
		return list.toArray(new Object[list.size()]);
	}
	
	/**
	 * 按类型取得"远程服务接口"的"本地代理"
	 * 
	 * @param clazz
	 * @return Class<T>
	 */
	public <T> T getServiceProxyBeanT(Class<T> clazz){
		Object[] rs=getServiceProxyBean(clazz);
		if(rs!=null && rs.length>0){
			return (T)rs[0];
		}
		return null;
	}
	
	/*
	 * 解析配置文件
	 * @param input 输入流
	 * @param xmlPath 配置文件路径
	 */
	private void loadXML(InputStream input,String xmlPath) {
		if(input==null){
			throw new IllegalArgumentException("无法找到文件："+xmlPath);
		}
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true); // 提供对 XML 名称空间的支持。
		DocumentBuilder builder = null;
		Document document = null;
		try {
			builder = builderFactory.newDocumentBuilder();
			document = builder.parse(input);
		} catch (Exception e) {
			throw new RuntimeException("解析配置文件异常："+xmlPath,e);
		} 

		// 获取文档的根元素,赋值给rootElement变量
		Element rootElement = document.getDocumentElement();
		
		// 此节点的名称空间 URI；如果它未被指定,则返回 null。
		String namespaceURI = rootElement.getNamespaceURI();
		String s=rootElement.getAttribute("xmlns:rsf");
		
		//判断必需写命名空间,可以是默认命名空间,可以是非默认命名空间。
		boolean verify=false;
		if(RSF_DEFAULT_NAMESPACE.equalsIgnoreCase(s) || RSF_DEFAULT_NAMESPACE.equalsIgnoreCase(namespaceURI)){
			verify=true;
		}
		if(!verify){
			throw new DOMException(DOMException.NAMESPACE_ERR, "RSF配置文件缺少命名空间："+RSF_DEFAULT_NAMESPACE); 
		}

		// 获取rootElement的所有子节点（不包括属性节点）,返回一个NodeList对象
		NodeList childNodes = rootElement.getChildNodes();
		pares(childNodes);
	}
	
	/*
	 * 取出所有的注册中心 
	 * @return
	 */
	public List<RegistryConfig> getRegistryConfigList(){
		List<RegistryConfig> list=new ArrayList<RegistryConfig>();
		for(String key:BEAN_MAP.keySet()){
			Object obj=BEAN_MAP.get(key);
			if(obj instanceof RegistryConfig){
				list.add((RegistryConfig)obj);
			}
		}
		return list;
	}
	
	private void putBean(String id, Object bean){
		Object obj=BEAN_MAP.get(id);
		if(obj==null){
			BEAN_MAP.put(id, bean);
		}else{
			throw new RsfConfigFileException("Bean id="+id+",发现重得的Bean id");
		}
	}
	
	private void pares(NodeList childNodes){
		for (int i = 0; i < childNodes.getLength(); i++) {
			// 获取childNodes的第i个节点
			Node node = childNodes.item(i);

			// 此节点的名称空间 URI；如果它未被指定,则返回 null。
			String namespaceURI_s = node.getNamespaceURI();
			
			// 是RSF框架配置文件的命名空间
			if (RSF_DEFAULT_NAMESPACE.equalsIgnoreCase(namespaceURI_s)) {
				if(node.getNodeType() == Node.ELEMENT_NODE){
					paresElement((Element)node);
				}
			}
		}
	}
	
	private Object paresElement(Element ele){
		String nodeName=ele.getTagName();
		String prefix=ele.getPrefix();
		if(prefix!=null){
			nodeName=nodeName.substring(prefix.length()+1, nodeName.length());
		}
		if("registry".equalsIgnoreCase(nodeName)){
			return paresRegistryConfig(ele);
		}
		if("protocol".equalsIgnoreCase(nodeName)){
			return paresProtocolConfig(ele);
		}
		if("service".equalsIgnoreCase(nodeName)){
			return paresServiceConfig(ele);
		}
		if("document".equalsIgnoreCase(nodeName)){
			NodeList childNodes=ele.getChildNodes();
			pares(childNodes);
			return ele.getTextContent();
		}
		
		if("client".equalsIgnoreCase(nodeName)){
			return paresClientConfig(ele);
		}
		if("clientMethod".equalsIgnoreCase(nodeName)){
			return paresClientMethodConfig(ele);
		}
		return null;
	}
	private RegistryConfig paresRegistryConfig(Element ele){
		String id=ele.getAttribute("id");
		String host=ele.getAttribute("host");
		String port=ele.getAttribute("port");
		String timeout=ele.getAttribute("timeout");

		//检查
		if(host==null ||"".equals(host.trim())){
			throw new RsfConfigFileException(ele,"host=null");
		}
		
		//默认值 
		if(port==null ||"".equals(port.trim())){
			port=String.valueOf(Constants.REGISTRY_PORT);
		}
		
		//默认值 
		if(timeout==null ||"".equals(timeout.trim())){
			timeout=String.valueOf(Constants.REGISTRY_TIMEOUT);
		}
		
		RegistryConfig registryConfig=new RegistryConfig();
		registryConfig.setHost(host.trim());
		
		//检查
		try{
			int p=Integer.valueOf(port);
			registryConfig.setPort(p);
		}catch(NumberFormatException e){
			throw new RsfConfigFileException(ele,"port="+port,e);
		}
		//检查
		try{
			int t=Integer.valueOf(timeout);
			registryConfig.setTimeout(t);
		}catch(NumberFormatException e){
			throw new RsfConfigFileException(ele,"timeout="+timeout,e);
		}
		if(id==null || "".equals(id.trim())){
			id=registryConfig.toString();
		}
		putBean(id.trim(), registryConfig);
		return registryConfig;
	}
	private ProtocolConfig paresProtocolConfig(Element ele){
		String id=ele.getAttribute("id");
		String host=ele.getAttribute("host");
		String port=ele.getAttribute("port");
		String name=ele.getAttribute("name");
		String payload=ele.getAttribute("payload");
		String threadpool=ele.getAttribute("threadpool");
		String threads=ele.getAttribute("threads");
		String corePoolSize=ele.getAttribute("corePoolSize");
		String maximumPoolSize=ele.getAttribute("maximumPoolSize");
		String queueSize=ele.getAttribute("queueSize");
		String keepalive=ele.getAttribute("keepalive");
		
		//默认值 
		if(host==null ||"".equals(host.trim())){
			host=String.valueOf(Constants.SERVER_PROTOCOL_HOST);
		}
		//默认值 
		if(port==null ||"".equals(port.trim())){
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
			 * 为了兼容旧机制，port还要保留，因为“配置管理中心”项目使用了setPort(),getPort()方法
			 * 但以后将优先使用ports数组
			 */
			//port=String.valueOf(Constants.SERVER_PROTOCOL_PORT);
			port=String.valueOf(Constants.SERVER_PROTOCOL_PORT_SEGMENT);
		}
		//默认值 
		if(name==null ||"".equals(name.trim())){
			name=String.valueOf(Constants.SERVER_PROTOCOL_NAME);
		}

		//默认值  线程池的类型
		if(threadpool==null ||"".equals(threadpool.trim())){
			threadpool=String.valueOf(Constants.THREADPOOL_TYPE_MIXED);
		}else{
			if(! (Constants.THREADPOOL_TYPE_FIXED.equalsIgnoreCase(threadpool.trim()) 
					|| Constants.THREADPOOL_TYPE_CACHED.equalsIgnoreCase(threadpool.trim())
					|| Constants.THREADPOOL_TYPE_MIXED.equalsIgnoreCase(threadpool.trim())
					
			)){
				throw new RsfConfigFileException(ele,"线程池类型配置错误，必须fixed/cached/mixed 3选1");
			}
		}
		
		ProtocolConfig protocolConfig=new ProtocolConfig();
		protocolConfig.setHost(host);
		protocolConfig.setName(name);
		protocolConfig.setThreadpool(threadpool);
		//检查 端口
		try{
			//int pp=Integer.valueOf(port);
			Integer[] ports=PortUtils.analysis(port);
			if(ports.length==1){
				//旧机制使用单一端口，适合于点对点的通信场景
				protocolConfig.setPort(ports[0]);
			}else{
				//新机制,设置端口段，按顺序使用其中一个可用端口，适合于有“服务注册中心”的场景
				protocolConfig.setPorts(ports);
			}
		}catch(Exception e){
			throw new RsfConfigFileException(ele,"port="+port,e);
		}
		//检查 maximumPoolSize  (了为兼容旧版本，所以保存)
		try{
			if(threads!=null && !"".equals(threads.trim())){
				int tt=Integer.valueOf(threads);
				protocolConfig.setMaximumPoolSize(tt);
			}
		}catch(NumberFormatException e){
			throw new RsfConfigFileException(ele,"threads="+threads,e);
		}
		//检查 corePoolSize
		try{
			if(corePoolSize!=null && !"".equals(corePoolSize.trim())){
				Integer tt=Integer.valueOf(corePoolSize);
				protocolConfig.setCorePoolSize(tt);
			}
		}catch(NumberFormatException e){
			throw new RsfConfigFileException(ele,"corePoolSize="+corePoolSize,e);
		}
		//检查 maximumPoolSize （优先级高于threads参数）
		try{
			if(maximumPoolSize!=null && !"".equals(maximumPoolSize.trim())){
				Integer tt=Integer.valueOf(maximumPoolSize);
				protocolConfig.setMaximumPoolSize(tt);
			}
		}catch(NumberFormatException e){
			throw new RsfConfigFileException(ele,"maximumPoolSize="+maximumPoolSize,e);
		}
		
		//检查queueSize
		try{
			if(queueSize!=null && !"".equals(queueSize.trim())){
				Integer tt=Integer.valueOf(queueSize);
				protocolConfig.setQueueSize(tt);
			}
		}catch(NumberFormatException e){
			throw new RsfConfigFileException(ele,"queueSize="+queueSize,e);
		}
		
		//检查 keepalive
		try{
			if(keepalive!=null && !"".equals(keepalive.trim())){
				Integer tt=Integer.valueOf(keepalive);
				protocolConfig.setKeepalive(tt);
			}
		}catch(NumberFormatException e){
			throw new RsfConfigFileException(ele,"keepalive="+keepalive,e);
		}
		
		//检查 负载
		try{
			if(payload!=null && !"".equals(payload.trim())){
				int pay=Integer.valueOf(payload);
				protocolConfig.setPayload(pay);
			}
		}catch(NumberFormatException e){
			throw new RsfConfigFileException(ele,"payload="+payload,e);
		}
		if(id==null || "".equals(id.trim())){
			id=protocolConfig.toString();
		}
		putBean(id.trim(), protocolConfig);
		return protocolConfig;
	}
	private ServiceConfig paresServiceConfig(Element ele){
		String id=ele.getAttribute("id");
		String displayName=ele.getAttribute("displayName");
		String owner=ele.getAttribute("owner");
		String department=ele.getAttribute("department");
		String interfaceClass=ele.getAttribute("interfaceClass");
		String springBean=ele.getAttribute("springBean");
		String clazz=ele.getAttribute("class");
		String registries=ele.getAttribute("registries");
		String register=ele.getAttribute("register");
		String weight=ele.getAttribute("weight");
		String portalId=ele.getAttribute("portalId");
		String security=ele.getAttribute("security");//加密
		
		//check
		if(displayName==null ||"".equals(displayName.trim())){
			throw new RsfConfigFileException(ele,"displayName=null");
		}
		if(owner==null ||"".equals(owner.trim())){
			throw new RsfConfigFileException(ele,"owner=null");
		}
		if(department==null ||"".equals(department.trim())){
			throw new RsfConfigFileException(ele,"department=null");
		}
		if(interfaceClass==null ||"".equals(interfaceClass.trim())){
			throw new RsfConfigFileException(ele,"interfaceClass=null");
		}
		if( (springBean==null ||"".equals(springBean.trim())) && (clazz==null ||"".equals(clazz.trim())) ){
			throw new RsfConfigFileException(ele,"springBean=null,class=null,只能使用其中一属性");
		}
		if( (springBean!=null && (!"".equals(springBean.trim()))) && (clazz!=null && (!"".equals(clazz.trim()))) ){
			throw new RsfConfigFileException(ele,"springBean=notNull,class=notNull,只能使用其中一属性");
		}
		
		if(portalId==null ||"".equals(portalId.trim())){
			throw new RsfConfigFileException(ele,"portalId=null");
		}
		
		ServiceConfig service=new ServiceConfig();
		//服务的显示名称,用于显示在注册中心
		if(displayName!=null){
			service.setDisplayName(displayName.trim());
		}
		//服务的创建人
		if(owner!=null){
			service.setOwner(owner.trim());
		}
		//服务创建人所属部门
		if(department!=null){
			service.setDepartment(department.trim());
		}
		//向指定注册中心注册,在多个注册中心时使用,
		//值为<rsf:registry>的id属性,多个注册中心ID用逗号分隔,
		if(registries!=null && !"".equals(registries.trim())){
			service.setRegistriesStr(registries.trim());
		}
		//是否注册本接口,默认值true
		if(register!=null && !"".equals(register.trim())){
			service.setIsReg("true".equalsIgnoreCase(register.trim()));
		}else{
			service.setIsReg(true);
		}
		//权重,默认值 100
		if(weight==null || "".equals(weight.trim())){
			service.setWeight(Constants.SERVICE_WEIGHT);
		}else{
			try{
				int w=Integer.valueOf(weight.trim());
				service.setWeight(w);
			}catch(NumberFormatException e){
				service.setWeight(Constants.SERVICE_WEIGHT);
			}
		}
		
		//系统标识
		if(portalId!=null){
			service.setPortalId(portalId.trim());
		}
		
		//服务接口
		if(interfaceClass!=null){
			try {
				Class clzz = ReflectUtils.name2class(interfaceClass.trim());
				service.setInterfaceClass(clzz);
			} catch (ClassNotFoundException e) {
				throw new RsfConfigFileException(ele,"不能找到服务接口："+interfaceClass,e);
			}
			
		}
		//服务接口的实现类
		if(  clazz!=null && (!"".equals(clazz.trim()))  ){
			Class clzz=null;
			try {
				clzz = ReflectUtils.name2class(clazz.trim());
			} catch (ClassNotFoundException e1) {
				throw new RsfConfigFileException(ele,"不能找到服务接口的实现类："+clazz,e1);
			}
			try {
				Object obj = clzz.newInstance();
				service.setRef(obj);
			} catch (InstantiationException e) {
				throw new RsfConfigFileException(ele,"实例化对象异常,请保证有一个无参构造方法,"+clazz,e);
			} catch (IllegalAccessException e) {
				throw new RsfConfigFileException(ele,"实例化对象异常,请保证有一个无参构造方法,"+clazz,e);
			}
		}
		//服务接口的实现类(spring)
		if(springBean!=null && (!"".equals(springBean.trim()))){
			try {
				if(springBean.toLowerCase().trim().equals("auto")){
					Class clzz = ReflectUtils.name2class(interfaceClass.trim());
					Map map = RsfSpringLoader.getBean(clzz);
					if(map==null){
						throw new RsfConfigFileException(ele,"从Spring容器中取得对象失败,inerface="+interfaceClass.trim());
					}else{
						if(map.size()==1){
							for(Object key:map.keySet()){
								Object obj=map.get(key);
								service.setRef(obj);
								springBean=(String)key;//回写，后面会用到
								break;
								//只有一个，只会取到一个
							}
						}else{
							String msg="从Spring容器中取得对象失败,找到了"+map.size()+"个实现类，无法决定使用哪一个.inerface="+interfaceClass.trim();
							throw new RsfConfigFileException(ele,msg);
						}
					}
					
				}else{
					Object obj = RsfSpringLoader.getBean(springBean);
					service.setRef(obj);
				}
			} catch (Exception e) {
				throw new RsfConfigFileException(ele,"从Spring容器中取得对象失败,bean id="+springBean,e);
			} 
		}
		
		if(security!=null && !"".equals(security.trim())){
			service.setSecurity( Boolean.valueOf(security.trim()));
		}
		
		//检查服务接口的实现类是否实现了指定的服务接口
		boolean bl=ReflectUtils.isInstance(service.getRef(), interfaceClass.trim());
		if(!bl){
			throw new RsfConfigFileException(ele,clazz+"类没有实现接口"+interfaceClass);
		}
		
		NodeList childNodes=ele.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			if(childNodes.item(i).getNodeType() == Node.ELEMENT_NODE){
				String data=(String)paresElement((Element)childNodes.item(i));
				service.setDucment(data);
				break;
			}
		}
		
		//检查是否写了文档
		String ducment=service.getDucment();
		if(ducment==null || "".equals(ducment.trim())){
			String err="本服务接口的说明文档,内容应尽量全面。包括服务接口总体业务说明、每个方法的业务说明,方法接收参数说明,方法返回值说明。";
			throw new RsfConfigFileException(ele,"ducment=null,"+err);
		}
		if(id==null || "".equals(id.trim())){
			//前面有检查，保证以下两个IF体只能进入一个IF体
			if(  clazz!=null && (!"".equals(clazz.trim()))  ){
				id=clazz.trim();
			}else if(springBean!=null && (!"".equals(springBean.trim()))){
				id=springBean.trim();
			}else{
				id="service_id_"+TEMP_ID.getAndIncrement();
			}
		}
		putBean(id.trim(), service);
		return service;
	}
	private ClientConfig paresClientConfig(Element ele){
		String id=ele.getAttribute("id");
		String displayName=ele.getAttribute("displayName");
		String owner=ele.getAttribute("owner");
		String department=ele.getAttribute("department");
		String interfaceClass=ele.getAttribute("interfaceClass");
		String timeout=ele.getAttribute("timeout");
		String registries=ele.getAttribute("registries");
		String loadbalance=ele.getAttribute("loadbalance");
		String mock=ele.getAttribute("mock");
		String url=ele.getAttribute("url");
		String portalId=ele.getAttribute("portalId");
		String security=ele.getAttribute("security");//加密
		
		//check
		if(displayName==null ||"".equals(displayName.trim())){
			throw new RsfConfigFileException(ele,"displayName=null");
		}
		if(owner==null ||"".equals(owner.trim())){
			throw new RsfConfigFileException(ele,"owner=null");
		}
		if(department==null ||"".equals(department.trim())){
			throw new RsfConfigFileException(ele,"department=null");
		}
		if(interfaceClass==null ||"".equals(interfaceClass.trim())){
			throw new RsfConfigFileException(ele,"interfaceClass=null");
		}
		if(portalId==null ||"".equals(portalId.trim())){
			throw new RsfConfigFileException(ele,"portalId=null");
		}
		
		ClientConfig client=new ClientConfig();
		//服务的显示名称,用于显示在注册中心
		if(displayName!=null){
			client.setDisplayName(displayName.trim());
		}
		//服务的创建人
		if(owner!=null){
			client.setOwner(owner.trim());
		}
		//服务创建人所属部门
		if(department!=null){
			client.setDepartment(department.trim());
		}
		//向指定注册中心注册,在多个注册中心时使用,
		//值为<rsf:registry>的id属性,多个注册中心ID用逗号分隔,
		if(registries!=null && !"".equals(registries.trim())){
			client.setRegistriesStr(registries.trim());
		}
		
		//服务接口
		if(interfaceClass!=null){
			try {
				Class<?> clzz = ReflectUtils.name2class(interfaceClass.trim());
				client.setInterfaceClass(clzz);
			} catch (ClassNotFoundException e) {
				throw new RsfConfigFileException(ele,"不能找到服务接口："+interfaceClass,e);
			}
		}
		
		//系统标识
		if(portalId!=null){
			client.setPortalId(portalId.trim());
		}
		
		//mock
		if(mock!=null && !"".equals(mock.trim())){
			Class<?> clzz=null;
			try {
				clzz = ReflectUtils.name2class(mock.trim());
			} catch (ClassNotFoundException e1) {
				throw new RsfConfigFileException(ele,"不能找到Mock实现类："+mock,e1);
			}
			try {
				Object obj = clzz.newInstance();
				client.setRef(obj);
				client.setMock(mock.trim());
			} catch (InstantiationException e) {
				throw new RsfConfigFileException(ele,"实例化Mock对象异常,请保证有一个无参构造方法,"+mock,e);
			} catch (IllegalAccessException e) {
				throw new RsfConfigFileException(ele,"实例化Mock对象异常,请保证有一个无参构造方法,"+mock,e);
			}
			
			//检查服务接口的实现类是否实现了指定的服务接口
			boolean bl=ReflectUtils.isInstance(client.getRef(), interfaceClass.trim());
			if(!bl){
				throw new RsfConfigFileException(ele,mock+"类没有实现接口"+interfaceClass);
			}
		}
		
		//默认值 
		if(timeout==null || "".equals(timeout.trim())){
			client.setTimeout(Constants.DEFAULT_TIMEOUT);
		}else{
			try{
				int t=Integer.valueOf(timeout.trim());
				client.setTimeout(t);
			}catch(NumberFormatException e){
				client.setTimeout(Constants.DEFAULT_TIMEOUT);
			}
		}
		if(loadbalance==null || "".equals(loadbalance.trim())){
			client.setLoadbalance(Constants.CLIENT_LOADBALANCE);
		}else{
			client.setLoadbalance(loadbalance.trim());
		}
		
		if(url!=null && !"".equals(url.trim())){
			client.setUrl(url.trim());
		}
		if(security!=null && !"".equals(security.trim())){
			client.setSecurity( Boolean.valueOf(security.trim()));
		}
		
		NodeList childNodes=ele.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			if(childNodes.item(i).getNodeType() == Node.ELEMENT_NODE){
				ClientMethodConfig method=(ClientMethodConfig)paresElement((Element)childNodes.item(i));
				List<ClientMethodConfig> list=client.getClientMethodConfigs();
				if(null==list){
					list=new ArrayList<ClientMethodConfig>();
					client.setClientMethodConfigs(list);
				}
				list.add(method);
				
				//注册回调函数
				if(method.getCallback()!=null){
					if(method.getParameterTypes()!=null){
						client.addCallBack(method.getName(),method.getParameterTypes(), method.getCallback());
					}else{
						client.addCallBack(method.getName(), method.getCallback());
					}
				}
			}
		}
		
		if(id==null || "".equals(id.trim())){
			id=client.toString();
		}
		putBean(id.trim(), client);
		return client;
	}

	private ClientMethodConfig paresClientMethodConfig(Element ele){
		String id=ele.getAttribute("id");
		String name=ele.getAttribute("name");
		String parameterTypes=ele.getAttribute("parameterTypes");
		String callback=ele.getAttribute("callback");
		String timeout=ele.getAttribute("timeout");
		String async=ele.getAttribute("async");
		
		//check
		if(name==null ||"".equals(name.trim())){
			throw new RsfConfigFileException(ele,"name=null");
		}
		
		ClientMethodConfig method=new ClientMethodConfig();
		method.setName(name.trim());
		
		//方法参数类型
		if(parameterTypes!=null && !"".equals(parameterTypes.trim())){
			method.setParameterTypesStr(parameterTypes.trim());
			String[] pts=parameterTypes.trim().split(",");
			Class<?>[] types = new Class<?>[pts.length];
            for (int i = 0; i < pts.length; i ++) {
            	try{
            		types[i] = ReflectUtils.name2class(pts[i]);
            	}catch(Exception e){
            		throw new RsfConfigFileException(ele,"找不到类："+pts[i],e);
            	}
            }
            method.setParameterTypes(types);
		}
		//回调函数
		if(callback!=null && !"".equals(callback.trim())){
			method.setCallbackStr(callback.trim());

			Class clzz=null;
			try {
				clzz = ReflectUtils.name2class(callback.trim());
			} catch (ClassNotFoundException e1) {
				throw new RsfConfigFileException(ele,"不能找到回调函数类："+callback,e1);
			}
			try {
				Object obj = clzz.newInstance();
				method.setCallback((CallBack)obj);
			} catch (InstantiationException e) {
				throw new RsfConfigFileException(ele,"实例化回调函数对象异常,请保证有一个无参构造方法,"+callback,e);
			} catch (IllegalAccessException e) {
				throw new RsfConfigFileException(ele,"实例化回调函数对象异常,请保证有一个无参构造方法,"+callback,e);
			}
			
			//检查回调函数类是否实现了指定CallBack接口
			boolean bl=ReflectUtils.isInstance(method.getCallback(), CallBack.class.getName());
			if(!bl){
				throw new RsfConfigFileException(ele,"回调函数类没有实现CallBack接口,"+callback);
			}
		}
		
		//超时时间
		if(timeout!=null && !"".equals(timeout.trim())){
			try{
				int to=Integer.valueOf(timeout.trim());
				method.setTimeout(to);
			}catch(Exception e){
				throw new RsfConfigFileException(ele,"数字格式化异常,timeout="+timeout,e);
			}
		}
		//本方法是否异步执行
		if(async!=null && !"".equals(async.trim())){
			method.setAsync("true".equalsIgnoreCase(async.trim()));
		}else{
			method.setAsync(false);
		}
		return method;
	}
}
