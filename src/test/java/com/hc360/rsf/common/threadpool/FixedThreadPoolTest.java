/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.common.threadpool;

import java.util.concurrent.ThreadPoolExecutor;
import com.hc360.rsf.common.URL;


/**
 * FixedThreadPool测试
 * 
 * @author zhaolei 2012-5-24
 */
public class FixedThreadPoolTest {
	
	public static void main(String[] a){
		FixedThreadPoolTest threadPoolTest=new FixedThreadPoolTest();
		threadPoolTest.test();
	}
	
	public void test(){
		URL url=new URL("","",0);
		ThreadPoolExecutor e = (ThreadPoolExecutor) new FixedThreadPool().getExecutor(url);
		
		for(int i=0;i<10000;i++){
			e.execute(new Run(e));
		}
		try {
			Thread.sleep(1000*5);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	
	class Run implements Runnable{
		private ThreadPoolExecutor e;
		public Run(ThreadPoolExecutor e){
			this.e=e;
		}
		public void run() {
			//做一些耗时的工作 
			String txt="";
			for(int i=0;i<1000;i++){
				txt+=".123456";
			}
			txt=null;
			
			Thread t=Thread.currentThread();
			String threadName=t.getName();
			String msg_pool = String.format("Thread pool status--" +
	        		" Thread Name: %s, Pool Size: %d (active: %d, core: %d, max: %d, largest: %d), Task: %d (completed: %d), Executor status:(isShutdown:%s, isTerminated:%s, isTerminating:%s)" , 
	                threadName, e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(), e.getLargestPoolSize(),
	                e.getTaskCount(), e.getCompletedTaskCount(), e.isShutdown(), e.isTerminated(), e.isTerminating());
			System.out.println(msg_pool);
		}
	}

}
