/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.node1;

import java.text.DecimalFormat;

import org.junit.Test;

import com.hc360.rsf.common.Constants;
import com.hc360.rsf.config.AbstractConfig;
import com.hc360.rsf.config.ClientConfig;
import com.hc360.rsf.config.ProtocolConfig;
import com.hc360.rsf.config.ServiceConfig;
import com.hc360.rsf.config.node1.data.UserBean;
import com.hc360.rsf.config.node1.data.UserService;
import com.hc360.rsf.config.node1.data.UserServiceImpl;

/**
 * 
 * NODE1中的测试都是，在一个主方法中同时启动Server与Client,进行测试
 * 
 * 
 * 常规的同步请求应答测试,使用编码方式来设置服务端与客户端
 * 
 * @author zhaolei 2012-5-24
 */
public class RR4javaTest {
	public final static DecimalFormat df = new DecimalFormat("##########0.000000000");
	
	public static int count=10  ;//发出请求的总次数
	public static int t=1*100;//两次请求之间的时间时隔,ms
	
	/**
	 * 测试方法
	 */
	@Test
	public void test_main() {
		service();//发布服务 
		client();//调用服务
		//System.exit(-1);
	}
	
	public static void main(String[] a){
		new RR4javaTest().test_main();
	}
	
	private void service(){
		//服务接口的实现类
		UserService userService=new UserServiceImpl();
		
		//协议
		ProtocolConfig protocol=new ProtocolConfig();
		protocol.setPort(63634);
		protocol.setName("rsf");
		
		ServiceConfig<UserService> server=new ServiceConfig<UserService>();
		server.setDisplayName("测试服务");
		server.setDepartment("MMT开发部");
		server.setOwner("赵磊");
		server.setDucment("服务说明:提供测试功能");
		server.setInterfaceClass(UserService.class);
		server.setRef(userService);
		server.setProtocol(protocol);
		server.export();//暴露
	}
	@SuppressWarnings("static-access")
	private void client(){
		ClientConfig<UserService> client=new ClientConfig<UserService>();
		client.setCharset("GBK");
		client.setDisplayName("测试客户端");
		client.setDepartment("MMT开发部");
		client.setOwner("赵磊");
		client.setUrl("rsf://127.0.0.1:63634");
		client.setInterfaceClass(UserService.class);
		
		UserService userService=client.getInstance();//重点,取得接口的代理
		
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
