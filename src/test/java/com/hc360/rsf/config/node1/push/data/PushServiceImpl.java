/**
 * UserServiceImpl.java   2012-4-25
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.node1.push.data;

import java.lang.reflect.Method;

import com.hc360.rsf.config.callback.CallBackHelper;
import com.hc360.rsf.config.callback.PushResult;

/**
 * 测试接口的实现类
 * @author zhaolei 2012-4-25
 */
public class PushServiceImpl implements PushService {
	
	public static void main(String[] a){
		Class clazz=PushServiceImpl.class;
		Method[] ms=clazz.getDeclaredMethods();
		for(Method m:ms){
			System.out.println(m.toString());
		}
	}

	public PushBean getUserInfo(String userName) {
		PushBean user=new PushBean();
		user.setAge(5);
		user.setName(userName);
		
		//使用工具关联key,并推送数据
		CallBackHelper.put("key");
		//取IP ,prot
		//List<CallBackWrap> list=CallBackHelper.get("key");
		PushResult[] rs=CallBackHelper.send("key","getUserInfo-DataDataDataData");
		if(rs!=null){
			for(PushResult obj:rs){
				System.out.println("推送结果:"+obj);
			}
		}
		return user;
	}
	
	public String addUser(PushBean user){
		return "ok";
	}
	
	public String findData(String id){
		//使用工具关联key,并推送数据
		CallBackHelper.put("key_findData_1");
		//取IP ,prot
		//List<CallBackWrap> list=CallBackHelper.get("key_findData");
		PushResult[] rs=CallBackHelper.send("key_findData_1","findData-DataDataData_1");
		if(rs!=null){
			for(PushResult obj:rs){
				System.out.println("推送结果:"+obj);
			}
		}
		return "123";
	}
	
	public String findData(String id,int age){
		//使用工具关联key,并推送数据
		CallBackHelper.put("key_findData_2");
		//取IP ,prot
		//List<CallBackWrap> list=CallBackHelper.get("key_findData");
		PushResult[] rs=CallBackHelper.send("key_findData_2","findData-DataDataData_2");
		if(rs!=null){
			for(PushResult obj:rs){
				System.out.println("推送结果:"+obj);
			}
		}
		return "123";
	}
	
	public void getPush(int number){
		
	}
}
