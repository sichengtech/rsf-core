/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.node3.xml;

import com.hc360.rsf.config.AbstractConfig;
import com.hc360.rsf.config.ConfigLoader;
import com.hc360.rsf.config.callback.AddressTool;
import com.hc360.rsf.config.node1.data.UserBean;
import com.hc360.rsf.config.node1.data.UserService;
import com.hc360.rsf.rpc.EchoService;

/**
 * 
 * 三节点之间通信测试，Client、Server、注册中心之间通信测试
 * 测试前请保证“注册中心”可以访问
 * 
 * 
 * 模拟服务调用者（客户端）
 * 
 * @author zhaolei 2012-6-27
 */
public class Client {

	/**
	 * 加载rsf_server.xml配置文件
	 * 
	 * 从注册下载服务提供者列表
	 * 
	 * 发起调用
	 *  
	 * @param args
	 */
	public static void main(String[] args) {
		String xmlPath = "classpath:com/hc360/rsf/config/node3/xml/rsf_client.xml";
		ConfigLoader configLoader = new ConfigLoader(xmlPath);
		configLoader.start();
		UserService userService= (UserService) configLoader.getServiceProxyBean("clientUserServiceImpl");//配置文件中的id
		EchoService echo=(EchoService)userService;
		for(int i=1;i<=10000;i++){
			try {
				//System.out.println("--我是客户端  "+AddressTool.toStringInfo());
				// 像调用本地方法一样调用远程服务接口。
				UserBean user=userService.getUserInfo(""+i);
				System.out.println("调用完成："+user);
				
//				Object rs=echo.$echo("回声："+i);
//				System.out.println(rs);
//				userService.addUser(new UserBean());
				if(i%100==0){
					System.out.println("完成了"+(i)+"次");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		AbstractConfig.destroy();
	}
}
