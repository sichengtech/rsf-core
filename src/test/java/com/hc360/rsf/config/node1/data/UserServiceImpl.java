/**
 * UserServiceImpl.java   2012-4-25
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.node1.data;

import com.hc360.rsf.config.callback.AddressTool;

/**
 * 测试接口的实现类
 */
public class UserServiceImpl implements UserService {
	public UserBean getUserInfo(String userName) {
		UserBean user=new UserBean();
		user.setAge(5);
		user.setName(userName);
		
		System.out.println("我是服务端  "+AddressTool.toStringInfo());
		
		return user;
	}
	
	public String addUser(UserBean user){
		return "ok";
	}
	
	public String findData(String id){
		return "";
	}
	
	public void getPush(int number){ }
}
