/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.node1.pushUseThread;

import java.io.Serializable;
import java.text.DecimalFormat;

import com.hc360.rsf.config.AbstractConfig;
import com.hc360.rsf.config.ClientConfig;
import com.hc360.rsf.config.ProtocolConfig;
import com.hc360.rsf.config.ServiceConfig;
import com.hc360.rsf.config.callback.CallBack;
import com.hc360.rsf.config.node1.push.data.PushBean;
import com.hc360.rsf.config.node1.push.data.PushService;
import com.hc360.rsf.config.node1.pushUseThread.data.PushServiceImpl2;

/**
 * 在独立线程中推送数据测试
 * 
 * @author zhaolei 2012-5-24
 */
public class PushThreadTest {
	public final static DecimalFormat df = new DecimalFormat("##########0.000000000");
	
	//发出请求的总次数
	//设置为1就可以了
	//客户端向服务端注册一次回调
	//服务端在一个独立的线程中,死循每2秒环向客户端推数据
	public static int count=1;
	
	public static int t=0;//1*1000;//两次请求之间的时间时隔,ms
	/**
	 * 用main方法测试更合适
	 * @param args
	 */
	public static void main(String[] args) {
		service();//发布服务 
		client();//调用服务
	}
	
	public static void service(){
		//服务接口的实现类
		PushService userService=new PushServiceImpl2();
		
		//协议
		ProtocolConfig protocol=new ProtocolConfig();
		protocol.setPort(63634);
		protocol.setName("rsf");
		
		ServiceConfig<PushService> server=new ServiceConfig<PushService>();
		server.setDisplayName("测试服务");
		server.setDepartment("MMT开发部");
		server.setOwner("赵磊");
		server.setDucment("服务说明:提供测试功能");
		server.setInterfaceClass(PushService.class);
		server.setRef(userService);
		server.setProtocol(protocol);
		server.export();//暴露
	}
	@SuppressWarnings("static-access")
	public static void client(){
		ClientConfig<PushService> client=new ClientConfig<PushService>();
		client.setCharset("GBK");
		client.setDisplayName("测试客户端");
		client.setDepartment("MMT开发部");
		client.setOwner("赵磊");
		client.setUrl("rsf://127.0.0.1:63634");
		client.setInterfaceClass(PushService.class);
		//目前addCallBack()方法,要在setInterfaceClass()方法之后执行
		client.addCallBack("getUserInfo", new CallBack() {
			public Object call(Serializable data) {
				System.out.println("客户端:收到服务端推送来的数据:"+data+",方法名：getUserInfo");
				return null;
			}
		});
		
		PushService userService=client.getInstance();//重点,取得接口的代理
		
		for(int i=0;i<count;i++){
			try{
				PushBean result=userService.getUserInfo(i+"");
				System.out.println("userService.getUserInfo()调用结束,结果:"+result);
				
				if(t>0){
					Thread.currentThread().sleep(t);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
