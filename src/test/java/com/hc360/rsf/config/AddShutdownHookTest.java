/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config;

/**
 * 
 * @author zhaolei 2012-4-25
 */
public class AddShutdownHookTest {

	public static void main(String[] args) throws InterruptedException {
		System.out.println("开始测试addShutdownHook");
		Thread.sleep(1000);
	}
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
            	System.out.println("关闭钩子1执行了");
            }
        }, "RsfShutdownHook"));
    }
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
            	System.out.println("关闭钩子2执行了");
            }
        }, "RsfShutdownHook"));
    }
}
