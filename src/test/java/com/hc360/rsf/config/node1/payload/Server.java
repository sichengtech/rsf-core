/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.node1.payload;

import com.hc360.rsf.config.ConfigLoader;

/**
 * 测试  负载 检查 功能
 * 
 * 模拟服务提供者 （服务端）
 * 
 * @author zhaolei 2012-6-27
 */
public class Server {

	/**
	 * 加载rsf_server.xml配置文件
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String xmlPath = "classpath:com/hc360/rsf/config/node1/payload/rsf_server.xml";
		ConfigLoader configLoader = new ConfigLoader(xmlPath);
		configLoader.start();
	}
}
