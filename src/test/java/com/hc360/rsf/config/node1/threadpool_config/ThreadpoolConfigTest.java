/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.node1.threadpool_config;

import java.text.DecimalFormat;

import com.hc360.rsf.config.ConfigLoader;
/**
 * NODE1中的测试都是，在一个主方法中同时启动Server与Client,进行测试
 * 
 * 常规的同步请求应答测试,使用XML配置方式
 * 
 * @author zhaolei 2012-5-24
 */
public class ThreadpoolConfigTest {
	public final static DecimalFormat df = new DecimalFormat("##########0.000000000");
	
	public static int count=1  ;//发出请求的总次数
	public static int t=0;//1*1000;//两次请求之间的时间时隔,ms
	
	public static void main(String[] a){
		String xmlPath = "classpath:com/hc360/rsf/config/node1/threadpool_config/threadpool.xml";
		ConfigLoader configLoader = new ConfigLoader(xmlPath);
		configLoader.start();
		
		
	}
}
