/**
 * UserBean.java   2012-5-8
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.node1.data;
import java.io.Serializable;
/**
 * UserBean
 */
public class UserBean implements Serializable{
	private static final long serialVersionUID = 1L;
	private String name;
	private int age;
	private Object obj;
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
	public Object getObj() {
		return obj;
	}
	public void setObj(Object obj) {
		this.obj = obj;
	}
}
