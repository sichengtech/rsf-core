/**
 * ServerConfigTest.java   2012-4-27
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.node2.java;

import com.hc360.rsf.config.ServiceConfig;
import com.hc360.rsf.config.node1.data.UserService;
import com.hc360.rsf.config.node1.data.UserServiceImpl;


/**
 * 两节点之间通信测试，Client与Server之间通信测试,与注册中心无关
 * 
 * 测试  服务端
 * 
 * @author zhaolei 2012-4-27
 */
public class ServiceConfigTest {

	public static void main(String[] args) {
		UserService userService=new UserServiceImpl();
		
		ServiceConfig<UserService> server=new ServiceConfig<UserService>();
		server.setDisplayName("测试服务");
		server.setDepartment("MMT开发部");
		server.setOwner("赵磊");
		server.setDucment("服务说明:提供测试功能");
		server.setInterfaceClass(UserService.class);
		server.setRef(userService);
		server.export();//暴露
	}
}
