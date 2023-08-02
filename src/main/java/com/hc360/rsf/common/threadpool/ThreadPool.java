package com.hc360.rsf.common.threadpool;

import java.util.concurrent.Executor;

import com.hc360.rsf.common.URL;

/**
 * ThreadPool
 * 
 */
public interface ThreadPool {
    
    /**
     * 线程池
     * 
     * @param url 线程参数
     * @return 线程池
     */
    Executor getExecutor(URL url);
}
