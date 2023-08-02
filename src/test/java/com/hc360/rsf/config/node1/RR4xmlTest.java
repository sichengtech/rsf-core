/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.node1;

import java.text.DecimalFormat;
import org.junit.Test;

import com.hc360.rsf.common.Constants;
import com.hc360.rsf.config.AbstractConfig;
import com.hc360.rsf.config.ConfigLoader;
import com.hc360.rsf.config.node1.data.UserBean;
import com.hc360.rsf.config.node1.data.UserService;

/**
 * NODE1中的测试都是，在一个主方法中同时启动Server与Client,进行测试
 * 
 * 常规的同步请求应答测试,使用XML配置方式
 * 
 * @author zhaolei 2012-5-24
 */
public class RR4xmlTest {
	public final static DecimalFormat df = new DecimalFormat("##########0.000000000");
	
	public static int count=1  ;//发出请求的总次数
	public static int t=0;//1*1000;//两次请求之间的时间时隔,ms
	
	@SuppressWarnings("static-access")
	@Test
	public void test_main(){
		String xmlPath = "classpath:com/hc360/rsf/config/node1/RRTest.xml";
		ConfigLoader configLoader = new ConfigLoader(xmlPath);
		configLoader.start();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		UserService userService=(UserService)configLoader.getServiceProxyBean("clientUserServiceImpl");
		
		System.out.println("预热：");
		long t5=System.nanoTime();
		UserBean result=userService.getUserInfo("预热");
		long t6=System.nanoTime();
		System.out.println("预热完成,用时:"+df.format((t6-t5)/Constants.TIME_C));
		
		
		System.out.println("开始测试：");
		long t7=System.nanoTime();
		for(int i=0;i<count;i++){
			try{
				result=userService.getUserInfo(i+"");
				System.out.println("调用结束,结果:"+result);
				
				if(t>0){
					Thread.currentThread().sleep(t);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		long t8=System.nanoTime();
		System.out.println("总用时:"+df.format((t8-t7)/Constants.TIME_C)+",总计:"+count+"次,平均:"+df.format((t8-t7)/Constants.TIME_C/count));
		AbstractConfig.destroy();
	}
}
