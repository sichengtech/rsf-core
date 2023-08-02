/**
 * UserServiceImpl.java   2012-4-25
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.node1.pushUseThread.data;

import com.hc360.rsf.config.callback.CallBackHelper;
import com.hc360.rsf.config.callback.CallBackTool;
import com.hc360.rsf.config.callback.CallBackWrap;
import com.hc360.rsf.config.callback.PushResult;
import com.hc360.rsf.config.node1.push.data.PushBean;
import com.hc360.rsf.config.node1.push.data.PushService;

/**
 * PushService测试接口的实现
 * @author zhaolei 2012-4-25
 */
public class PushServiceImpl2 implements PushService {
	public PushServiceImpl2(){
		t();
	}

	/**
	 * 在独立线程中推送数据测试
	 * 
	 * @param userName
	 * @return
	 * @see com.hc360.rsf.config.node1.push.data.PushService#getUserInfo(java.lang.String)
	 */
	public PushBean getUserInfo(String userName) {
		PushBean user=new PushBean();
		user.setAge(5);
		user.setName(userName);
		//使用工具关联key,并推送数据
		CallBackHelper.put("key");
		return user;
	}
	
	private void t(){
		
		//取得CallBack
//		final CallBackWrap cbwr=CallBackTool.getPushTool();
		new Thread(){
			public void run(){
				while(true){
//					Object rs=cbwr.getCallback().call("我是数据");
//					System.out.println("服务端:"+rs);
					
					//取IP ,prot
					//List<CallBackWrap> list=CallBackHelper.get("key");
					PushResult[] rs=CallBackHelper.send("key","getUserInfo-DataDataDataData");
					if(rs!=null){
						for(PushResult obj:rs){
							System.out.println("推送结果:"+obj);
						}
					}
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
					}
				}
			}
		}.start();
		
		
	}
	
	public String addUser(PushBean user){
		return "ok";
	}
	
	public String findData(String id){
		return "";
	}
	
	public void getPush(int number){
		
	}

	/**
	 * function description
	 * 
	 * @param id
	 * @param age
	 * @return
	 * @see com.hc360.rsf.config.node1.push.data.PushService#findData(java.lang.String, int)
	 */
	public String findData(String id, int age) {
		return null;
	}
}
