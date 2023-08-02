/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.common.serialize.support.hsessian;

import java.io.Serializable;

/**
 * (描述类的用途) 
 * 
 * @author zhaolei 2012-6-19
 */
public class Parent implements Serializable{
	private static final long serialVersionUID = -7594285452236373005L;
	
	private String name="Parent";
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
