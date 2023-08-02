/**
 * UserService.java   2012-4-25
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.node1.data;

/**
 * 接口说明，请详细说明本接口的业务功能 
 */
public interface UserService {
	/**
	 * (描述方法的作用) 
	 * @param userName 描述参数
	 * @return 描述返回值
	 */
	public UserBean getUserInfo(String userName);
	
	/**
	 * (描述方法的作用) 
	 * @param user 描述参数
	 * @return 描述返回值
	 */
	public String addUser(UserBean user);
	
	/**
	 * (描述方法的作用) 
	 * @param id 描述参数
	 * @return 描述返回值
	 */
	public String findData(String id);
	
	/**
	 * (描述方法的作用) 
	 * @param number 描述参数
	 */
	public void getPush(int number);
}
