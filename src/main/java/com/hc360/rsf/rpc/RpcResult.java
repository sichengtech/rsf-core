/**
 * RpcResult.java   2012-5-10
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.rpc;

import java.io.Serializable;

/**
 * RPC Result.
 * 封装响应体,正常时封装响应结果，异常时封装Throwable
 * 
 * @author zhaolei 2012-5-11
 */
public class RpcResult implements Serializable {

	private static final long serialVersionUID = -6925924956850004727L;

	private Object value;

	private Throwable exception;

	private int time;// 服务端业务执行耗时，纳秒

	public RpcResult() {
	}

	public RpcResult(Object value) {
		this.value = value;
	}

	public RpcResult(Throwable exception) {
		this.exception = exception;
	}

	public Object recreate() throws Throwable {
		if (exception != null) {
			throw exception;
		}
		return value;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable e) {
		this.exception = e;
	}

	public boolean hasException() {
		return exception != null;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return "RpcResult [result=" + value + ", time=" + time + ", exception=" + exception +  "]";
	}
}
