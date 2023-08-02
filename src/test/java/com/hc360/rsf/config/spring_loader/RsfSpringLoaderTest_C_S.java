/**
 * Copyright(c) 2000-2013 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.spring_loader;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hc360.rsf.config.ConfigLoader;
import com.hc360.rsf.config.RsfSpringLoader;
import com.hc360.rsf.config.node1.data.UserBean;
import com.hc360.rsf.config.node1.data.UserService;

/**
 * 启动Spring容器，用于展开关于spring的测试
 * 
 * 请适当修改applicationContext.xml文件
 * 
 * @author zhaolei 2013-3-7
 */
public class RsfSpringLoaderTest_C_S {

	/**
	 * 主方法 
	 * @param args
	 */
	public static void main(String[] args) {
		ApplicationContext ctx =new ClassPathXmlApplicationContext("com/hc360/rsf/config/spring_loader/applicationContext.xml");
		try {
			Thread.sleep(1*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		UserService userService=RsfSpringLoader.getConfigLoader().getServiceProxyBeanT(UserService.class);
		for(int i=1;i<=5;i++){
			// 像调用本地方法一样调用远程服务接口。
			UserBean user=userService.getUserInfo(""+i);
		}
		
		try {
			Thread.sleep(1*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ConfigLoader.destroy();
	}
}
