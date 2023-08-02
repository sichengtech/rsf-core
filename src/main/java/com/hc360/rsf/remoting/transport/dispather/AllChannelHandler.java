package com.hc360.rsf.remoting.transport.dispather;

import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hc360.rsf.common.Constants;
import com.hc360.rsf.common.URL;
import com.hc360.rsf.common.threadpool.CachedThreadPool;
import com.hc360.rsf.common.threadpool.FixedThreadPool;
import com.hc360.rsf.common.threadpool.MixedThreadPool;
import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.ExecutionException;
import com.hc360.rsf.remoting.HandlerDelegate;
import com.hc360.rsf.remoting.RemotingException;
import com.hc360.rsf.remoting.transport.dispather.ChannelEventRunnable.ChannelState;
import com.hc360.rsf.remoting.transport.mina.MinaHandlerDelegate;

/**
 * AllChannelHandler <br>
 * <br>
 * 在服务端,每当一个请求到来时,服务端都产生一个新的线程来处理。当处理一些数据库等较耗时的操作比较适合。<br>
 * 以下消息派发到线程池,包括 收到数据事件,连接创建事件,连接断开事件,异常事件等。 <br>
 * 注：发送数据(sent)事件除外,直接在IO线程上执行。<br>
 * 
 * @author zhaolei 2012-5-18
 */
public class AllChannelHandler implements HandlerDelegate {
	private static Logger logger = LoggerFactory.getLogger(AllChannelHandler.class);
	
	/**
	 * 线程池
	 */
	protected ExecutorService executor;

	/**
	 * 处理器
	 */
	protected final HandlerDelegate handler;

	/**
	 * URL
	 */
	protected final URL url;

	/**
	 * 构造方法
	 * 
	 * @param handler
	 * @param url
	 */
	public AllChannelHandler(HandlerDelegate handler, URL url) {
		this.handler = handler;
		this.url = url;
		
		//根据URL中的参数，选用一种线程池
		String threadName=url.getParameter(Constants.THREAD_NAME_KEY);
		if(Constants.THREADPOOL_TYPE_FIXED.equalsIgnoreCase(threadName)){
			this.executor = (ExecutorService) new FixedThreadPool().getExecutor(url);
		}else if(Constants.THREADPOOL_TYPE_CACHED.equalsIgnoreCase(threadName)){
			this.executor = (ExecutorService) new CachedThreadPool().getExecutor(url);
		}else if(Constants.THREADPOOL_TYPE_MIXED.equalsIgnoreCase(threadName)){
			this.executor = (ExecutorService) new MixedThreadPool().getExecutor(url);
		}else{
			
			//如果配置文件中没有指明线程池的类型，则使用默认值 
			//服务端：fixed，客户端：cached
			if(handler instanceof MinaHandlerDelegate){
				MinaHandlerDelegate md=(MinaHandlerDelegate)handler;
				//在客户端使用缓存线程池
				if(md.getSide().equals(MinaHandlerDelegate.CLIENT_SIDE)){
					this.executor = (ExecutorService) new CachedThreadPool().getExecutor(url);
				}
				//在服务端使用混合线程池
				if(md.getSide().equals(MinaHandlerDelegate.SERVER_SIDE)){
					this.executor = (ExecutorService) new MixedThreadPool().getExecutor(url);
				}
			}else{
				//默认使用混合线程池
				this.executor = (ExecutorService) new MixedThreadPool().getExecutor(url);
			}
		}
	}

	/**
	 * on channel connected.
	 * 
	 * @param channel
	 *            channel.
	 */
	public void connected(Channel channel) throws RemotingException {
		ExecutorService cexecutor = getExecutorService();
		try {
			cexecutor.execute(new ChannelEventRunnable(channel, handler, ChannelState.CONNECTED));
		} catch (Exception t) {
			ExecutionException e = new ExecutionException("connect event", channel, getClass()
					+ " error when process connected event .", t);
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * on channel disconnected.
	 * 
	 * @param channel
	 *            channel.
	 */
	public void disconnected(Channel channel) throws RemotingException {
		ExecutorService cexecutor = getExecutorService();
		try {
			if(!cexecutor.isShutdown() ){
				cexecutor.execute(new ChannelEventRunnable(channel, handler, ChannelState.DISCONNECTED));
			}
		} catch (Exception t) {
			ExecutionException e = new ExecutionException("disconnect event", channel, getClass()
					+ " error when process disconnected event .", t);
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * on message received.
	 * 
	 * @param channel
	 *            channel.
	 * @param message
	 *            message.
	 */
	public void received(Channel channel, Object message) throws RemotingException {
		ExecutorService cexecutor = getExecutorService();
		try {
			cexecutor.execute(new ChannelEventRunnable(channel, handler, ChannelState.RECEIVED, message));
		} catch (Exception t) {
			ExecutionException e = new ExecutionException(message, channel, getClass()
					+ " error when process received event .", t);
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * on exception caught.
	 * 
	 * @param channel
	 *            channel.
	 * @param exception
	 *            exception.
	 */
	public void caught(Channel channel, Throwable exception) throws RemotingException {
		ExecutorService cexecutor = getExecutorService();
		try {
			cexecutor.execute(new ChannelEventRunnable(channel, handler, ChannelState.CAUGHT, exception));
		} catch (Exception t) {
			ExecutionException e = new ExecutionException("caught event", channel, getClass()
					+ " error when process caught event .", t);
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * on sent.
	 * 
	 * @param channel
	 * @param message
	 * @throws RemotingException
	 * @see com.hc360.rsf.remoting.HandlerDelegate#sent(com.hc360.rsf.remoting.Channel,
	 *      java.lang.Object)
	 */
	public void sent(Channel channel, Object message) throws RemotingException {
		handler.sent(channel, message);
	}
	
	public ExecutorService getExecutorService() {
		return executor;
	}
}
