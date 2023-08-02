/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.spring_loader;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hc360.rsf.config.ConfigLoader;

/**
 * 
 * 三节点之间通信测试，Client、Server、注册中心之间通信测试
 * 测试前请保证“注册中心”可以访问
 * 
 * 
 * 模拟服务提供者 （服务端）
 * 
 * @author zhaolei 2012-6-27
 */
public class Server {

	/**
	 * 加载rsf_server.xml配置文件
	 * 
	 * 在本地暴露服务
	 * 
	 * 向注册中心注册服务
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		ApplicationContext ctx =new ClassPathXmlApplicationContext("com/hc360/rsf/config/spring_loader/applicationContext.xml");
		
//		String xmlPath = "classpath:com/hc360/rsf/config/spring_loader/rsf_server.xml";
//		ConfigLoader configLoader = new ConfigLoader(xmlPath);
//		configLoader.start();
	}
}
