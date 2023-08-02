/**
 * UserService.java   2012-4-25
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.node1.push.data;

/**
 * 测试接口
 * 
 * @author zhaolei 2012-4-25
 */
public interface PushService {
	public PushBean getUserInfo(String userName);
	
	public String addUser(PushBean user);
	
	public String findData(String id);
	
	public String findData(String id,int age);
	
	public void getPush(int number);
}
