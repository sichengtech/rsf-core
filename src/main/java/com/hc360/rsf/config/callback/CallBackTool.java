/**
 * PushTool.java   2012-5-10
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.callback;

/**
 * CallBackTool
 * 
 * 在当前线程中取出推送数据的工具
 * 
 * 客户端写了回调函数,服务端在当前线程中取出推送数据的工具,数据推送到客户端的回调函数
 * 
 * @author zhaolei 2012-5-11
 */
public class CallBackTool {
	static ThreadLocal<CallBackWrap> threadLocal=new ThreadLocal<CallBackWrap>();

	/**
	 * 从当前线程中取得数据推送工具
	 * @return
	 */
	public static CallBackWrap getPushTool(){
		return threadLocal.get();
	}
	
	/**
	 * 向 当前线程中放入数据推送工具
	 * @param callback
	 */
	public static void putPushTool(CallBackWrap callback){
		threadLocal.set(callback);
	}
	
	/**
	 * remove
	 */
	public static void remove(){
		threadLocal.remove();
	}
}
