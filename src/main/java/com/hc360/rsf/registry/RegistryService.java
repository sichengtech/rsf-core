/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.registry;

import java.util.List;

/**
 * 注册中心的服务接口
 * 
 * 不可以修改现在的接口中的方法，要与“注册中心”的服务接口配合（一致）
 * 
 * @author zhaolei 2012-6-12
 */
public interface RegistryService {

	    /**
	     * 注册服务.
	     * 
	   
	     */
	    void register(RegistryBean[] services);

	    /**
	     * 取消注册服务
	     * 
	     */
	    void unregister(RegistryBean[] services);

	    /**
	     * 订阅服务
	     * 
	     */
	    void subscribe(SubscribeBean[] bean);

	    /**
	     * 取消订阅服务
	     * 
	     */
	    void unsubscribe(SubscribeBean[] bean);
	    
	    /**
	     * 查询服务列表,与订阅服务相同,拉模式,只返回一次结果。
	     * 
	     * 返回值Map说明：
	     * key：服务名（服务接口全限定名）
	     * value:多个服务提供者信息
	     */
	    List<RegistryBean> lookup(String ServiceName,String portalId);	
}
