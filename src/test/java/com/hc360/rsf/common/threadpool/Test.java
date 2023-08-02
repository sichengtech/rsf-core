/**
 * Copyright(c) 2000-2013 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.common.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
	创建一个可重用固定线程数的线程池，以共享的无界队列方式来运行这些线程。 
	Executors. newFixedThreadPool(int nThreads) 
	
	创建一个可根据需要创建新线程的线程池，但是在以前构造的线程可用时将重用它们，并在需要时使用提供的 ThreadFactory 创建新线程。 
	Executors. newCachedThreadPool(ThreadFactory threadFactory) 

 * 
 * @author zhaolei 2013-4-8
 */
public class Test {

	/**
	 * TODO(描述方法的作用) 
	 * @param args
	 */
	public static void main(String[] args) {
		ExecutorService es=Executors. newFixedThreadPool(50); 
		ExecutorService es2=Executors. newCachedThreadPool(); 
	}

}
