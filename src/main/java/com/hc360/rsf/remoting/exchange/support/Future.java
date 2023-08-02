/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting.exchange.support;

import java.util.concurrent.TimeUnit;
import com.hc360.rsf.remoting.RemotingException;


/**
 * 为什么不使用java.util.concurrent.Future接口？
 * 因为对他的异常处理不满意
 * 
 * @author zhaolei 2012-5-22
 */
public interface Future<V> {

    boolean cancel(boolean mayInterruptIfRunning);

    boolean isCancelled();

    boolean isDone();

    V get() throws RemotingException;

    V get(long timeout, TimeUnit unit) throws RemotingException;
}
