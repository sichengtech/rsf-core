/**
 * Serializable.java   2013-11-25
 * Copyright(c) 2000-2013 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting;

import java.io.IOException;

/**
 * 序列化异常
 * 
 * @author liwanchun
 * @version 4.0 2013-11-25
 * @since 4.0	
 */
public class SerializableException extends IOException {
	private static final long serialVersionUID = 1L;
	public SerializableException(){
		super();
	}
	public SerializableException(String errMsg){
		super(errMsg);
	}
	public SerializableException(String errMsg,Exception e){
		super(errMsg,e);
	}

}
