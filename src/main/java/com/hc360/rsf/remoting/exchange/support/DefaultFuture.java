/**
 * DefaultFuture.java   2012-4-12
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting.exchange.support;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hc360.rsf.common.Constants;
import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.RemotingException;
import com.hc360.rsf.remoting.SerializableException;
import com.hc360.rsf.remoting.TimeoutException;
import com.hc360.rsf.rpc.DataTooLengthException;
import com.hc360.rsf.rpc.RpcResult;

/**
 * 异步转同步的类
 * 
 * @author zhaolei 2012-4-12
 */
public class DefaultFuture<T> implements Future<T> {
	private static Logger logger = LoggerFactory.getLogger(DefaultFuture.class);
	public static final int DEFAULT_TIMEOUT = Constants.DEFAULT_TIMEOUT;

	/** 全局的,任务上下文 */
	private static final Map<Long, DefaultFuture<Object>> FUTURES = new ConcurrentHashMap<Long, DefaultFuture<Object>>();

	/** 唯一标识 */
	private final long id;

	/** 超时时间 ms */
	private final int timeout;

	private final Lock lock = new ReentrantLock();

	private final Condition done = lock.newCondition();

	private boolean isCancelled = false; // 任务是否被取消
	private Channel channel;
	private Request request;
	private Response response;

	/**
	 * 构造方法
	 * 
	 * @param session
	 * @param request
	 * @param timeout
	 *            超时,单位 ms
	 */
	public DefaultFuture(Channel channel, Request request, int timeout) {
		this.id = request.getId();
		this.channel = channel;
		this.timeout = timeout;
		this.request = request;
		FUTURES.put(id, (DefaultFuture<Object>) this);
	}

	/**
	 * 试图取消对此任务的执行。
	 * 
	 * @param mayInterruptIfRunning
	 * @return
	 * @see java.util.concurrent.Future#cancel(boolean)
	 */
	public boolean cancel(boolean mayInterruptIfRunning) {
		if (isDone()) {
			return false;
		}
		FUTURES.remove(id);
		isCancelled = true;
		return true;
	}

	/**
	 * 如果在任务正常完成前将其取消,则返回 true。
	 * 
	 * @return
	 * @see java.util.concurrent.Future#isCancelled()
	 */
	public boolean isCancelled() {
		return isCancelled;
	}

	/**
	 * 如果任务已完成,则返回 true。
	 * 
	 * @return
	 * @see java.util.concurrent.Future#isDone()
	 */
	public boolean isDone() {
		return response != null;
	}

	/**
	 * 如有必要,等待计算完成,然后获取其结果。
	 * 
	 * @return
	 * @see java.util.concurrent.Future#get()
	 */
	public T get() throws RemotingException {
		return get(timeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * 如有必要,最多等待为使计算完成所给定的时间之后,获取其结果（如果结果可用）。
	 * 
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 *             果当前的线程在等待时被中断
	 * @throws ExecutionException
	 *             如果计算抛出异常
	 * @throws TimeoutException
	 *             如果等待超时
	 * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
	 */
	@SuppressWarnings("unchecked")
	public T get(long timeout, TimeUnit unit) throws RemotingException {
		if (timeout <= 0) {
			timeout = DEFAULT_TIMEOUT;
		}
		if (!isDone()) {
			long start = System.currentTimeMillis();
			lock.lock();
			try {
				while (!isDone()) {
					/**
					 * 异步转同步,在这里等待--赵磊
					 */
					try {
						done.await(timeout, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						logger.warn("get()客户端等待服务端响应,await()被意外打断");
					}
					if (isDone()) {
						break;
					}
					if (System.currentTimeMillis() - start > timeout) {
						throw new TimeoutException(false, channel, "调用发起端等待响应超时,timeout="+timeout+" ms");
					}
				}
			} finally {
				lock.unlock();
				
				//@newNodify 一旦调用get方法就清除全局的注册数据
				//rsf内部使用，以后维护时注意这点
				FUTURES.remove(id);//防御，防止FUTURES内容不断的增长，内存溢出。客户端等待响应超要清理FUTURES中的相应数据
			}
		}
		// 这里处理远端（服务端）异常
		return (T) returnFromResponse();
	}

	/**
	 * 重点：这里处理远端（服务端）的异常
	 * 
	 * @return
	 * @throws RemotingException
	 */
	private Object returnFromResponse() throws RemotingException {
		Response res = response;
		if (res == null) {
			throw new IllegalStateException("response cannot be null");
		}
		//OK 处理正确
		if (res.getStatus() == Response.OK) {
			return response;
		}
		//服务端超时
		//服务端超时不同于客户端超时,C端超时是客户等待S端返回，但长时间未返回，C端认定的超时
		//服务端超时是，S端调用S端的业务代，但长时间未执行完成，S端认定超时，并通知C端。
		//服务端超时目前未实现，          --赵磊
		if ( res.getStatus() == Response.SERVER_TIMEOUT) {
			throw new TimeoutException(res.getStatus() == Response.SERVER_TIMEOUT, channel, res.getErrorMsg());
		}
		//数据长度太大超过服务端的限定
		if ( res.getStatus() == Response.DATA_TOO_LENGTH) {
			throw new DataTooLengthException(res.getErrorMsg());
		}
		//客户端发生异常，发送的对象没有实现Serializable接口
		if(res.getStatus() == Response.CLIENT_SERIALIZABLE_ERROR){
			//能走到这里，说明C向S发送请求前，序列化异常
			//说明要发送的对象没有实现Serializable接口
			//再处理时，已把异常对象放入Response,下一行Object一定是Exception
			Object obj=res.getData();
			
			//由于只能抛出RemotingException，所以要把SerializableException转换成RemotingException类型再抛出
			if(obj instanceof SerializableException){
				//100%走这里
				throw new RemotingException(channel,(SerializableException)obj);
			}else{
				// 0%走这里，但还是写了。防止已后有人修改代码时产生了新的Bug,这里对未来做一个预防
				// 你说多余不？   2013-11-26  赵磊
				SerializableException se= new SerializableException("序列化异常,请检查被传输对象和对象的成员是否实现了Serializable接口");
				throw new RemotingException(channel,se);
			}
		}
		
		//其它类型的异常会走这里
		Object obj = res.getData();
		if (obj instanceof RpcResult) {
			//不会走下面的逻辑
			RpcResult rr = (RpcResult) obj;
			Throwable t = rr.getException();
			throw new RemotingException(channel, t);
		} else {
			throw new RemotingException(channel, "此异常信息是服务端异常,显示给调用者查看：" + channel.toString()
					+"\r\n"+ res.getErrorMsg() + "\t---------服务端的异常信息结束------------");
		}
	}

	/**
	 * 有数据从服务端到达时,received方法会被调用
	 * 
	 * @param channel
	 * @param response
	 */
	public static void received(Channel channel, Response response) {
		// 取出并从集合中删除
		DefaultFuture<Object> future = FUTURES.remove(response.getId());
		if (future != null) {
			// 进入这里
			future.doReceived(response);
		} else {
			logger.warn("The timeout response finally returned at "
					+ (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()))
					// 石龙飞 注释掉超时返回的response内容，有些情况这个里面信息量特别大，会导致日志骤增。 20150706
//					+ ", response "
//					+ response
					+ (channel == null ? "" : ", channel: " + channel.getLocalAddress() + " -> "
							+ channel.getRemoteAddress()));
		}
	}

	/*
	 * DefaultFuture类是重点, 本方法也是重点 --赵磊
	 * 
	 * 在这里释放 "客户端" 等待的线程
	 * 
	 * @param res
	 */
	private void doReceived(Response res) {
		lock.lock();
		try {
			response = res;
			if (done != null) {
				done.signal();// 重点:发出信号
			}
		} finally {
			lock.unlock();
		}
	}

	public static DefaultFuture getFuture(long id) {
		return FUTURES.get(id);
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

}
