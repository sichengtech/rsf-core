/**
 * UserBean.java   2012-5-8
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.node1.push.data;

import java.io.Serializable;

/**
 * UserBean
 * 
 * @author zhaolei 2012-5-8
 */
public class PushBean implements Serializable{
	/***/
	private static final long serialVersionUID = 1L;
	private String name;
	private int age;
	
	public String toString(){
		return "[name:"+name+",age:"+age+"]";
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
}
