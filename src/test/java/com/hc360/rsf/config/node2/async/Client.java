/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.node2.async;

import com.hc360.rsf.config.AbstractConfig;
import com.hc360.rsf.config.ConfigLoader;
import com.hc360.rsf.config.callback.AddressTool;
import com.hc360.rsf.config.node1.data.UserBean;
import com.hc360.rsf.config.node1.data.UserService;
import com.hc360.rsf.config.node1.push.data.PushBean;
import com.hc360.rsf.config.node1.push.data.PushService;
import com.hc360.rsf.rpc.EchoService;

/**
 * 异步通信测试
 * 两节点之间通信测试，Client、Server之间通信测试
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
		String xmlPath = "classpath:com/hc360/rsf/config/node2/async/rsf_client.xml";
		ConfigLoader configLoader = new ConfigLoader(xmlPath);
		configLoader.start();
		UserService userService= (UserService) configLoader.getServiceProxyBean("clientUserServiceImpl");//配置文件中的id
		for(int i=1;i<=1;i++){
			try {
				UserBean user=userService.getUserInfo(""+i);
				System.out.println("结果："+user);
				
				if(i%100==0){
					System.out.println("完成了"+(i)+"次");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
			}
		}
		
		PushService pushService=(PushService)configLoader.getServiceProxyBean("pushServiceImpl");
		for(int i=0;i<1;i++){
			try{
				PushBean result=pushService.getUserInfo(i+"");
				System.out.println("userService.getUserInfo()调用结束,结果:"+result);
				
				String result2=pushService.findData(i+"");
				System.out.println("userService.findData()调用结束,结果:"+result2);
				
				result2=pushService.findData(i+"",100);
				System.out.println("userService.findData()调用结束,结果:"+result2);
				
				//if(t>0){
				//	Thread.currentThread().sleep(1000);
				//}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		//AbstractConfig.destroy();
	}
}
