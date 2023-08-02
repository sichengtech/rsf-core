/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.common.serialize.support.hsessian;

/**
 * (描述类的用途) 
 * 
 * @author zhaolei 2012-6-19
 */
public class SubClass extends Parent {
	private static final long serialVersionUID = 1L;
	
	private String name="SubClass";
	
	private boolean good=false;
	
	public boolean isGood() {
		return good;
	}
	public void setGood(boolean good) {
		this.good = good;
	}
	
}
