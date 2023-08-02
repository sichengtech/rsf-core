/**
 * ConsumerConfig.java   2012-4-25
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hc360.rsf.common.Constants;
import com.hc360.rsf.common.URL;
import com.hc360.rsf.config.callback.CallBack;
import com.hc360.rsf.config.callback.CallBackException;
import com.hc360.rsf.registry.Provider;
import com.hc360.rsf.registry.ServiceProviderList;
import com.hc360.rsf.rpc.Invoker;
import com.hc360.rsf.rpc.RpcException;
import com.hc360.rsf.rpc.protocol.RsfInvokerClientP2p;
import com.hc360.rsf.rpc.protocol.RsfInvokerClientReg;

/**
 * 客户端
 * 
 * 用途：生成服务接口的本地代理
 * 
 * @author zhaolei 2012-4-25
 */
public class ClientConfig<T> extends AbstractConfig {
	private static Logger logger = LoggerFactory.getLogger(ClientConfig.class);
	private static final long serialVersionUID = 6728560129984523451L;
	private transient boolean initialized;
	private final Map<String,CallBack> callbackMap=new ConcurrentHashMap<String,CallBack>(); 

	// 应用名称
	private String displayName;

	// 应用负责人
	private String owner;

	// 应用负责人部门
	private String department;

	// 接口类型
	private Class<?> interfaceClass;

	// 点对点直连服务提供者地址,
	// 本值为空,才向注册中心订阅服务列表
	// 本值不为空时,将直连目标
	// 127.0.0.1, 指明ip,使用默认协议、端口
	// 127.0.0.1:63634，指明ip,端口，使用默认协议
	// rsf://127.0.0.1:63634，指明ip、端口、协议
	private String url;

	// 接口代理类引用
	private transient T ref;

	// 字符集
	private String charset;
	
	// 远程服务调用超时时间(毫秒),默认5000ms
	private int timeout;

	// 注册中心
	protected List<RegistryConfig> registerCenterList=new ArrayList<RegistryConfig>();
	
	/*注册中心的id,值为<rsf:registry>的id属性,
	多个注册中心ID用逗号分隔,
	如果不想将该服务注册到任何registry,可将值设为"N/A"
	使用XML配置文件时,需要使用到该值 ,可能<rsf:registry>写在后面
	要在配置文件全解析完成才能找出<rsf:registry>
	*/ 
	private String registriesStr;
	
	//客户端--负载均衡策略,
	//可选值：random,roundrobin,leastactive,
	//分别表示：随机,轮循,最少活跃调用
	private String loadbalance;
	
	//本地模拟
	private String mock;
	
	//接口的全部方法
	protected List<ClientMethodConfig> clientMethodConfigs;
	
	//系统名称
	private String portalId;
	
	//是否加密通信
	private boolean security=false;
	
	public RegistryConfig getRegistry() {
		return registerCenterList == null || registerCenterList.size() == 0 ? null : registerCenterList.get(0);
	}

	public void setRegistry(RegistryConfig registry) {
		if(registerCenterList==null){
			registerCenterList = new ArrayList<RegistryConfig>(1);
		}
		registerCenterList.add(registry);
	}

	public List<RegistryConfig> getRegistries() {
		return registerCenterList;
	}

	public void setRegistries(List<RegistryConfig> registries) {
		this.registerCenterList = (List<RegistryConfig>) registries;
	}

	/**
	 * 取得业务接口的"代理"
	 * 
	 * @return
	 */
	public synchronized T getInstance() {
		if (ref == null) {
			init();
		}else{
			logger.debug("RSF Client are already initialization,interfaceClass={}",interfaceClass);
		}
		return ref;
	}

	private void init() {
		if (initialized) {
			logger.debug("RSF Client are already initialization,interfaceClass={}",interfaceClass);
			return;
		}
		//临时保存生成的多个参数
		Map<String, String>  parameterMap=new HashMap<String,String>();

		if (interfaceClass == null) {
			throw new RpcException("参数错误,没有指定接口,interfaceClass == null");
		}
        
        //检查是否有回调函数,做要暴露处理
		Method[] methods=interfaceClass.getDeclaredMethods();
		for(Method m:methods){
			Class<?>[] parameterTypes = m.getParameterTypes();
			String callbackKey=ClientConfig.callbackKey(interfaceClass.getName(),m.getName(),parameterTypes);
			CallBack callback=callbackMap.get(callbackKey);		
			if(callback!=null){
				//暴露callback
		    	logger.info("RSF Client exprot callback function,interfaceClass={},key={}", interfaceClass.getName(),callbackKey);
		    	Invoker<CallBack> invoker = GlobalManager.proxyFactory.getInvoker(callback, CallBack.class, null);
		    	GlobalManager.protocol.export4Callback(callbackKey,invoker);
		    	
		    	String key_all=parameterMap.get(Constants.CHANNEL_CALLBACK_KEY);
		    	if(key_all!=null){
		    		//使用#做为分隔符
		    		key_all=key_all+"#"+callbackKey;
		    		parameterMap.put(Constants.CHANNEL_CALLBACK_KEY, key_all);
		    	}else{
		    		parameterMap.put(Constants.CHANNEL_CALLBACK_KEY, callbackKey);
		    	}
			}
		}        
		
		initialized = true;
		// 创建接口的代理对象
		ref = createProxy(parameterMap);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private T createProxy(Map<String, String> parameters) {
		//从RSF 1.0.0 开始加入此代码
		URL _url=null;
		boolean p2p=true;
		if(url!=null){
			_url = URL.valueOf(url);//点对点直连
			 p2p=true;
			
		}else{
			_url = new URL("","",0);//走注册中心,IP,PORT将从注册中心取得
			 p2p=false;
		}
		//从RSF 1.3.0 开始加入此代码
		if(isSecurity()){
			parameters.put(Constants.ISSECURITY_KEY, "true");//只在本端使用此参数
		}
		//从RSF 1.0.0 开始加入此代码
		parameters.put(Constants.PATH_KEY, interfaceClass.getName());//本参数会通过网络传递到服务端
		parameters.put(Constants.TIMEOUT_KEY,String.valueOf(getTimeout()));//请求超时时间
		parameters.put(Constants.LOADBALANCE_KEY,getLoadbalance());//软负载策略
		_url =_url.addParameters(parameters);//服务端需要服务名,找出提供此服务的invoker
		_url=_url.setPath(interfaceClass.getName());//只在本端使用此参数
		
		Invoker<?> invoker =null;
		if(p2p){
			//点对点通信
			Provider p=new Provider();
			p.setIp(_url.getIp());
			p.setPort(_url.getPort());
			p.setServiceName(_url.getPath());
			
			ServiceProviderList.addNode(p);//点对点连接加入服务列表，会被定时与线程池对比，发现少连接时创建连接。
			invoker=new RsfInvokerClientP2p(interfaceClass,_url);//点对点直连时,url中有IP、Port、服务名 
			((RsfInvokerClientP2p)invoker).setClientMethodList_info(getClientMethodConfigs());
		}else{
			//三点通信
			List<RegistryConfig> list=getRegistries();
			StringBuilder sbl=new StringBuilder();
			for(RegistryConfig rc:list){
				sbl.append(rc.getHost());
				sbl.append(":");
				sbl.append(rc.getPort());
				sbl.append(Constants.REGISTRY_SEPARATOR);
			}
			_url=_url.addParameter(Constants.REGISTRY_KEY, sbl.toString());
			
			//走注册中心时,url中没有IP与Port ,将从注册中心下载URL列表
			invoker=new RsfInvokerClientReg(interfaceClass,_url,this);
			((RsfInvokerClientReg)invoker).setClientMethodList_info(getClientMethodConfigs());
		}
		return (T) GlobalManager.proxyFactory.getProxy(invoker);
	}

	/**
	 * 为某一个方法,添加回调函数<br>
	 * 当有重载方法时,请使用addCallBack(CallBack callBack, String methodName, Class<?>... parameterTypes)<br>
	 * @param userKey 在服务端通过这个key找到对应的回调工具
	 * @param callBack 回调函数
	 * @param methodName 方法名
	 */
	public void addCallBack(String methodName,CallBack callBack) {
		if(callBack==null){
			logger.warn("RSF Client 注册回调函数失败,callBack=null");
			return ;
		}
		if(methodName==null || "".equals(methodName.trim())){
			logger.warn("RSF Client 注册回调函数失败,methodName=null");
			return ;
		}		
		
		//检查是否有重载方法
		Method[] methods=interfaceClass.getDeclaredMethods();
		int count=0;
		Method temp_m=null;
		for(Method m:methods){
			if(methodName.equals(m.getName())){
				temp_m=m;
				count++;
			}
		}
		if(count!=1){
			String mag="RSF Client 注册回调函数失败,发现重载方法,method="+methodName;
			CallBackException e=new CallBackException(mag);
			logger.error("",e);
		}else{
			Class<?>[] parameterTypes=temp_m.getParameterTypes();
			addCallBack( methodName, parameterTypes,callBack);
		}
		
	}

	/**
	 * 为某一个方法,添加回调函数<br>
	 * @param userKey  在服务端通过这个key找到对应的回调工具
	 * @param callBack 回调函数
	 * @param methodName 方法名
	 * @param parameterTypes 方法的形参列表
	 */
	public void addCallBack(String methodName, Class<?>[] parameterTypes,CallBack callBack) {

		String callbackKey=ClientConfig.callbackKey(interfaceClass.getName(),methodName,parameterTypes);
		
		//检查方法是否存在
		Method method=null;
		try {
			method = interfaceClass.getDeclaredMethod(methodName,parameterTypes);
		} catch (SecurityException e1) {
			//在下面处理异常
		} catch (NoSuchMethodException e1) {
			//在下面处理异常
		}
		if(method==null){
			String mag="RSF Client 客户端生成回调函数失败,方法不存在,callbackKey="+callbackKey;
			CallBackException e=new CallBackException(mag);
			logger.error("",e);
		}
		callbackMap.put(callbackKey, callBack);
		logger.debug("RSF Client 客户端生成回调函数成功,callbackKey="+callbackKey);
	}
	
	/**
	 * 生成回调函数的Key，使用这个key来保存一个回调函数 
	 * @param methodName 方法名
	 * @param parameterTypes 方法参数类型
	 * @return
	 */
	public static String callbackKey(String interfaceName,String methodName, Class<?>[] parameterTypes){
		StringBuilder sbl = new StringBuilder();
		sbl.append(interfaceName);
		sbl.append(" ");
		sbl.append(methodName);
		sbl.append("(");
		if (parameterTypes != null) {
			for(int i=0;i<parameterTypes.length;i++){
				Class<?> c=parameterTypes[i];
				sbl.append(c.getName());
				if(i!=parameterTypes.length-1){
					sbl.append(",");
				}
			}
		}
		sbl.append(")");
		return sbl.toString();
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

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Class<?> getInterfaceClass() {
		return interfaceClass;
	}

	public void setInterfaceClass(Class<?> interfaceClass) {
		this.interfaceClass = interfaceClass;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public T getRef() {
		return ref;
	}

	public void setRef(T ref) {
		this.ref = ref;
	}

	public String getRegistriesStr() {
		return registriesStr;
	}

	public void setRegistriesStr(String registriesStr) {
		this.registriesStr = registriesStr;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getLoadbalance() {
		return loadbalance;
	}

	public void setLoadbalance(String loadbalance) {
		this.loadbalance = loadbalance;
	}

	public List<ClientMethodConfig> getClientMethodConfigs() {
		return clientMethodConfigs;
	}

	public void setClientMethodConfigs(List<ClientMethodConfig> clientMethodConfigs) {
		this.clientMethodConfigs = clientMethodConfigs;
	}

	public String getMock() {
		return mock;
	}

	public void setMock(String mock) {
		this.mock = mock;
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
