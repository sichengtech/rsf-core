package com.hc360.rsf.common.threadpool;

import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolInfoUtil {
	public static String getInfo(ThreadPoolExecutor e){
		Thread t=Thread.currentThread();
		String threadName=t.getName();
		String msg_pool = String.format("Thread pool status--" +
        		" Thread Name: %s, Pool Size: %d (active: %d, core: %d, max: %d, largest: %d), Task: %d (completed: %d), Executor status:(isShutdown:%s, isTerminated:%s, isTerminating:%s)" , 
                threadName, e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(), e.getLargestPoolSize(),
                e.getTaskCount(), e.getCompletedTaskCount(), e.isShutdown(), e.isTerminated(), e.isTerminating());
		return msg_pool;
	}
}
