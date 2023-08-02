/**
 * ServerConfigTest.java   2012-4-27
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.node3.java;

import com.hc360.rsf.config.ProtocolConfig;
import com.hc360.rsf.config.RegistryConfig;
import com.hc360.rsf.config.ServiceConfig;
import com.hc360.rsf.config.node1.data.UserService;
import com.hc360.rsf.config.node1.data.UserServiceImpl;
/**
 * 三节点之间通信测试，Client、Server、注册中心 之间通信测试
 * 
 * 测试  服务端
 * 
 * @author zhaolei 2012-4-27
 */
public class ServiceConfigTest {

	public static void main(String[] args) {
		UserService userService=new UserServiceImpl();
		
		//协议
		ProtocolConfig protocol=new ProtocolConfig();
		protocol.setPort(63634);
		protocol.setName("rsf");
		
		//注册中心
		RegistryConfig register=new RegistryConfig();
		register.setHost("register.org.hc360.com");
		register.setPort(63638);
		
		ServiceConfig<UserService> server=new ServiceConfig<UserService>();
		server.setDisplayName("测试服务");
		server.setDepartment("MMT开发部");
		server.setOwner("赵磊");
		server.setDucment("服务说明:提供测试功能");
		server.setPortalId("测试");
		server.setInterfaceClass(UserService.class);
		server.setRef(userService);
		server.setProtocol(protocol);//协议
		server.setRegistry(register);//注册中心地址
		server.export();//暴露
		server.registerService();//向服务注册中心  注册本服务
	}
}
