package com.hc360.rsf.remoting;

/**
 * 加密通信时发生的异常
 * @author zhaorai
 *
 */
public class EncryptException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EncryptException(String msg){
		super(msg);
	}
	
	public EncryptException(String msg,Exception e){
		super(msg,e);
	}
}
