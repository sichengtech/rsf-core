/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.node2.xml;

import com.hc360.rsf.config.ConfigLoader;

/**
 * 两节点之间通信测试，Client、Server之间通信测试
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
		String xmlPath = "classpath:com/hc360/rsf/config/node2/xml/rsf_server.xml";
		ConfigLoader configLoader = new ConfigLoader(xmlPath);
		configLoader.start();
	}
}
