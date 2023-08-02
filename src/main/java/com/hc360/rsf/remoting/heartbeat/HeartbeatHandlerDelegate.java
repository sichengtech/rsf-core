/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting.heartbeat;

import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.ChannelPool;
import com.hc360.rsf.remoting.HandlerDelegate;
import com.hc360.rsf.remoting.RemotingException;
import com.hc360.rsf.remoting.exchange.support.Request;
import com.hc360.rsf.remoting.exchange.support.Response;

/**
 * 处理心跳器
 * 
 * @author zhaolei 2012-5-23
 */
public class HeartbeatHandlerDelegate implements HandlerDelegate {

	/**
	 * 下一个处理器
	 */
	protected final HandlerDelegate handler;

	/**
	 * 构造方法
	 * 
	 * @param handler
	 * @param url
	 */
	public HeartbeatHandlerDelegate(HandlerDelegate handler) {
		this.handler = handler;
	}

	/**
	 * function description
	 * 
	 * @param channel
	 * @throws RemotingException
	 * @see com.hc360.rsf.remoting.HandlerDelegate#connected(com.hc360.rsf.remoting.Channel)
	 */
	public void connected(Channel channel) throws RemotingException {
		handler.connected(channel);
	}

	/**
	 * function description
	 * 
	 * @param channel
	 * @throws RemotingException
	 * @see com.hc360.rsf.remoting.HandlerDelegate#disconnected(com.hc360.rsf.remoting.Channel)
	 */
	public void disconnected(Channel channel) throws RemotingException {
		handler.disconnected(channel);
	}

	/**
	 * function description
	 * 
	 * @param channel
	 * @param message
	 * @throws RemotingException
	 * @see com.hc360.rsf.remoting.HandlerDelegate#sent(com.hc360.rsf.remoting.Channel,
	 *      java.lang.Object)
	 */
	public void sent(Channel channel, Object message) throws RemotingException {
		// Channel每次读写,都记录下时间,如果刚刚读写过,是不需要心跳测试的
		channel.setAttribute(ChannelPool.KEY_WRITE_TIMESTAMP,
				System.currentTimeMillis());
		handler.sent(channel, message);

	}

	/**
	 * function description
	 * 
	 * @param channel
	 * @param message
	 * @throws RemotingException
	 * @see com.hc360.rsf.remoting.HandlerDelegate#received(com.hc360.rsf.remoting.Channel,
	 *      java.lang.Object)
	 */
	public void received(Channel channel, Object message) throws RemotingException {
		//Channel每次读写,都记录下时间,如果刚刚读写过,是不需要心跳测试的
		channel.setAttribute(ChannelPool.KEY_READ_TIMESTAMP, System.currentTimeMillis());
		
		//客户端心跳包到达服务端
		if (message instanceof Request) {
			Request request = (Request) message;
			if (request.isEvent()) {
				if (request.isHeartbeat()) {// 是一次心跳请求
					Response res = new Response(request.getId());// 带回ID
					res.setEvent(Response.HEARTBEAT_EVENT);// 标识为心跳事件
					channel.send(res);// 返回心跳
					return;
				}
			}
		}
		
		//服务端心跳包返回到客户端
		if (message instanceof Response) {
			Response response = (Response) message;
			if (response.isEvent()) {
				if (response.isHeartbeat()) {
					// 是一次心跳应答
					//什么也不做,因为上面以经执行了channel.setAttribute(ChannelPool.KEY_READ_TIMESTAMP,time);
					return;
				}
			}
		}
		
		handler.received(channel, message);
	}

	/**
	 * function description
	 * 
	 * @param channel
	 * @param exception
	 * @throws RemotingException
	 * @see com.hc360.rsf.remoting.HandlerDelegate#caught(com.hc360.rsf.remoting.Channel,
	 *      java.lang.Throwable)
	 */
	public void caught(Channel channel, Throwable exception)
			throws RemotingException {
		handler.caught(channel, exception);
	}
}
