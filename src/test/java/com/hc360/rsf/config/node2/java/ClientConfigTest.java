/**
 * ClientConfigTest.java   2012-4-25
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.node2.java;

import java.text.DecimalFormat;

import com.hc360.rsf.common.Constants;
import com.hc360.rsf.config.AbstractConfig;
import com.hc360.rsf.config.ClientConfig;
import com.hc360.rsf.config.node1.data.UserBean;
import com.hc360.rsf.config.node1.data.UserService;

/**
 * 
 * 两节点之间通信测试，Client与Server之间通信测试,与注册中心无关
 * 
 * 
 * 客户端测试代码
 * @author zhaolei 2012-4-25
 */
public class ClientConfigTest {
	public final static DecimalFormat df = new DecimalFormat("##########0.000000000");
	
	public static int count=1;//发出请求的总次数
	public static int t=0;//2*1000;//两次请求之间的时间时隔,ms

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws InterruptedException {
		ClientConfig<UserService> client=new ClientConfig<UserService>();
		client.setCharset("GBK");
		client.setDisplayName("测试客户端");
		client.setDepartment("MMT开发部");
		client.setOwner("赵磊");
		//client.setUrl("rsf://192.168.119.147:63634");//rsf://127.0.0.1:10001/com.hc360.rsf.config.UserService?a=1
		client.setUrl("rsf://127.0.0.1:63634");
		client.setInterfaceClass(UserService.class);
		UserService userService=client.getInstance();//重点,取得接口的代理
		
		long t7=System.nanoTime();
		for(int i=0;i<count;i++){
			try{
				long t1=System.nanoTime();
				userService.addUser(new UserBean());
				//UserBean result=userService.getUserInfo("rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634rsf://192.168.119.147:63634");
				//long t2=System.nanoTime();
				//System.out.println("调用结束,用时:"+df.format((t2-t1)/Constants.TIME_C)+" "+result);
				
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				if(t>0){
					Thread.currentThread().sleep(t);
				}
			}
		}
		long t8=System.nanoTime();
		System.out.println("总用时:"+df.format((t8-t7)/Constants.TIME_C)+",总计:"+count+"次,平均:"+df.format((t8-t7)/Constants.TIME_C/count));
		
		AbstractConfig.destroy();
	}
}
