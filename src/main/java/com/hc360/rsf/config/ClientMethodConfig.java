/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config;

import com.hc360.rsf.config.callback.CallBack;

/**
 * 代表接口或类的一个方法
 * 
 * @author zhaolei 2012-6-8
 */
public class ClientMethodConfig {
	// 方法名
	private String name;
	// 方法参数类型,用于区分重载方法
	@SuppressWarnings("rawtypes")
	private Class[] parameterTypes;
	// 方法参数类型,用于区分重载方法,用于XML配置文件
	private String parameterTypesStr;
	// 回调接口
	private CallBack callback;
	// 回调接口的String形式,用于XML配置文件
	private String callbackStr;
	// 本方法超时时间
	private int timeout;
	// 是否异步
	private boolean async;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@SuppressWarnings("rawtypes")
	public Class[] getParameterTypes() {
		return parameterTypes;
	}

	@SuppressWarnings("rawtypes")
	public void setParameterTypes(Class[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public CallBack getCallback() {
		return callback;
	}

	public void setCallback(CallBack callback) {
		this.callback = callback;
	}

	public String getCallbackStr() {
		return callbackStr;
	}

	public void setCallbackStr(String callbackStr) {
		this.callbackStr = callbackStr;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public String getParameterTypesStr() {
		return parameterTypesStr;
	}

	public void setParameterTypesStr(String parameterTypesStr) {
		this.parameterTypesStr = parameterTypesStr;
	}
}
