/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.node1.push;

import java.text.DecimalFormat;
import org.junit.Test;

import com.hc360.rsf.config.AbstractConfig;
import com.hc360.rsf.config.ClientConfig;
import com.hc360.rsf.config.ConfigLoader;
import com.hc360.rsf.config.ProtocolConfig;
import com.hc360.rsf.config.ServiceConfig;
import com.hc360.rsf.config.node1.data.UserBean;
import com.hc360.rsf.config.node1.data.UserService;
import com.hc360.rsf.config.node1.push.data.PushBean;
import com.hc360.rsf.config.node1.push.data.PushService;

/**
 * 在同一线程中推送数据的测试
 * 
 * @author zhaolei 2012-5-24
 */
public class Push4xmlTest {
	public final static DecimalFormat df = new DecimalFormat("##########0.000000000");
	
	public static int count=10;//发出请求的总次数
	public static int t=1*1000;//两次请求之间的时间时隔,ms

	@Test
	public void test_main() {
		
		String xmlPath = "classpath:com/hc360/rsf/config/node1/push/PushTest.xml";
		ConfigLoader configLoader = new ConfigLoader(xmlPath);
		configLoader.start();
		
		//pushService
		PushService userService=(PushService)configLoader.getServiceProxyBean("pushServiceImpl");
		
		for(int i=0;i<count;i++){
			try{
				PushBean result=userService.getUserInfo(i+"");
				System.out.println("userService.getUserInfo()调用结束,结果:"+result);
				
				String result2=userService.findData(i+"");
				System.out.println("userService.findData()调用结束,结果:"+result2);
				
				result2=userService.findData(i+"",100);
				System.out.println("userService.findData()调用结束,结果:"+result2);
				
				if(t>0){
					Thread.currentThread().sleep(t);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		AbstractConfig.destroy();
	}
}
