/**
 * MinaChannel.java   2012-5-10
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting.transport.mina;

import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hc360.rsf.common.Constants;
import com.hc360.rsf.common.utils.AuthHelperProxy;
import com.hc360.rsf.common.utils.TrustListHelperProxy;
import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.EncryptException;
import com.hc360.rsf.remoting.IpPort;
import com.hc360.rsf.remoting.RemotingException;
import com.hc360.rsf.remoting.exchange.support.DefaultFuture;
import com.hc360.rsf.remoting.exchange.support.Request;
import com.hc360.rsf.remoting.exchange.support.Response;
import com.hc360.rsf.remoting.exchange.support.ShakeHandsBean;
import com.hc360.rsf.rpc.EchoService;
import com.hc360.rsf.rpc.RpcException;
import com.hc360.rsf.rpc.RpcInvocation;
import com.hc360.rsf.rpc.RpcResult;

/**
 * Mina对Channel接口的实现 封装一个socket连接，一个channel可以发送接收数据，包括同步与异步。可发送非加密数据。
 * 
 * @author zhaolei 2012-5-9
 */
public class MinaChannel implements Channel {

	private static Logger logger = LoggerFactory.getLogger(MinaChannel.class);
	private static final String CHANNEL_KEY = MinaChannel.class.getName()
			+ ".CHANNEL";
	private final IoSession session;
	private InetSocketAddress localAddress;
	private InetSocketAddress remoteAddress;

	/**
	 * 构造方法
	 * 
	 * @param session
	 * @param url
	 */
	private MinaChannel(IoSession session) {
		if (session == null) {
			throw new IllegalArgumentException("mina session == null");
		}
		this.session = session;
		InetSocketAddress local=(InetSocketAddress) session.getLocalAddress();
		InetSocketAddress remote=(InetSocketAddress) session.getRemoteAddress();
		
		///复制InetSocketAddress对象,防止连接关闭后,无法取到IP、端口
		if(local!=null){
			String ip_local=local.getAddress().getHostAddress();
			int port_local=local.getPort();
			this.localAddress = new InetSocketAddress(ip_local,port_local);
		}else{
			logger.warn("构造MinaChannel时,session.getLocalAddress()=null,无法取得IP地址,isConnected="+session.isConnected());
		}
		
		if(remote!=null){
			String ip_remote=remote.getAddress().getHostAddress();
			int port_remote=remote.getPort();
			this.remoteAddress = new InetSocketAddress(ip_remote,port_remote);
		}else{
			logger.warn("构造MinaChannel时,session.getRemoteAddress()=null,无法取得IP地址,isConnected="+session.isConnected());
		}
	}

	/**
	 * 把Mina的IoSession对象,包装成 MinaChannel对象.<br>
	 * 同一个IoSession对象最多会被包装一次<br>
	 * <br>
	 * 
	 * @param session
	 *            把本session包装成 MinaChannel对象
	 * @return MinaChannel
	 */
	public static MinaChannel getOrAddChannel(IoSession session) {
		if (session == null) {
			return null;
		}

		MinaChannel channel = (MinaChannel) session.getAttribute(CHANNEL_KEY);

		if (channel == null) {
			channel = new MinaChannel(session);
			if (session.isConnected()) {
				MinaChannel old = (MinaChannel) session.setAttribute(
						CHANNEL_KEY, channel);
				if (old != null) {
					// System.out.println("重复包装"+session+"---"+url);
					session.setAttribute(CHANNEL_KEY, old);
					channel = old;
				}
			}
		}
		return channel;
	}

	/**
	 * 当一个IoSession关闭时,应该清清除其中数据<br>
	 * 
	 * @param session
	 */
	public static void removeChannelIfDisconnectd(IoSession session) {
		if (session != null && !session.isConnected()) {
			session.removeAttribute(CHANNEL_KEY);
		}
	}

	/**
	 * 发出请求,并等待对方应答(同步)
	 * 
	 * @param message
	 *            发送的数据
	 * @return 处理结果
	 * @throws RemotingException
	 * @see com.hc360.rsf.remoting.Channel#request(java.lang.Object)
	 */
	public Object request(Object message) throws RemotingException {
		int timeout = Constants.DEFAULT_TIMEOUT;
		return request(message, timeout);
	}

	/**
	 * 发出请求,并等待对方应答(同步)
	 * 
	 * @param message
	 *            发送的数据
	 * @param timeout
	 *            超时时间
	 * @return 处理结果
	 * @throws RemotingException
	 * @see com.hc360.rsf.remoting.Channel#request(java.lang.Object)
	 */
	public Object request(Object message, int timeout) throws RemotingException {
		return request(message, timeout, false);
	}

	/**
	 * 发出请求,并等待对方应答(同步)
	 * 
	 * @param message
	 *            发送的数据
	 * @param timeout
	 *            超时时间
	 * @param security
	 *            是否是加密通信
	 * @return 处理结果
	 * @throws RemotingException
	 * @see com.hc360.rsf.remoting.Channel#request(java.lang.Object)
	 */
	public Object request(Object message, int timeout, boolean security)
			throws RemotingException {
		if (logger.isDebugEnabled()) {
			logger.debug("RSF Client send data,data={},URL={}",
					message == null ? "null" : message.toString(),
					session == null ? "" : session.toString());
		}

		// 准备数据
		Request request = new Request();
		request.setData(message);
		if (security) {
			request.setSecurity(true);
		}
		DefaultFuture<Response> future = new DefaultFuture<Response>(this,
				request, timeout);

		// @modify
		if (!session.isConnected()) {
			String message1 = MessageFormat
					.format("{0}--->{1}的连接session断了,当时的session：{2},session.isConnected():{3}",
							localAddress.getAddress().getHostAddress(),
							(remoteAddress == null) ? null : remoteAddress
									.getAddress().getHostAddress(),
							(session == null) ? null : session.toString(),
							(session == null) ? false : session.isConnected());
			logger.error(message1);
			throw new RemotingException(localAddress, remoteAddress, message1);
		}

		// 发送数据
		session.write(request);
		// 返回数据,get()会阻塞等到数据返回
		Response response = future.get();
		if (logger.isDebugEnabled()) {
			logger.debug("RSF Client receive data,{}",
					response == null ? "null" : response.toString());
		}
		return response;
	}

	/**
	 * 客户端执行三次握手
	 * 
	 * 三次握手： 客户端执行第一次 服务端执行第二次 客户端执行再第三次
	 * 
	 * 同步方法，一个channel只需要执行一次“三次握手”
	 */
	public synchronized void shakeHands() {
		// 防止重复执行握手
		String session_key = (String) this.getAttribute(Constants.SESSION_KEY);
		if (session_key != null) {
			// 不需要进行三次握手
			return;
		}

		long t1 = System.currentTimeMillis();

		// 获取客户端的系统名称
		String systemName_client = AuthHelperProxy.getSystemName();
		if (systemName_client == null) {
			String msg = "加密通信-获取系统名称异常(客户端)，systemName:" + systemName_client;
			logger.error(msg);
			throw new EncryptException(msg);
		} else {
			logger.info("加密通信-获取系统名称(客户端)，systemName:" + systemName_client);
		}

		// 生成会话密钥
		String sessionKey_client = AuthHelperProxy.generateSessionKey();
		if (sessionKey_client == null) {
			String msg = "加密通信-生成会话密钥异常，sessionKey:null";
			logger.error(msg);
			throw new EncryptException(msg);
		} else {
			logger.info("加密通信-生成会话密钥，sessionKey:保密");
		}

		// client端获得请求签名包
		byte[] singPackageResquest = null;
		try {
			singPackageResquest = AuthHelperProxy.getRequestSignPackage(
					systemName_client, sessionKey_client);
			logger.debug("加密通信-客户端获取请求签名包成功");
		} catch (Exception e) {
			String msg = "加密通信-客户端获得请求签名包异常,systemName_client:"
					+ systemName_client;
			logger.error(msg, e);
			throw new EncryptException(msg, e);
		}

		Response response = null;

		// 执行三次握手 (第一次握手)
		// 1、获得请求签名包 byte[] getRequestSignPackage(client端系统ID ，会话密钥)
		// 2、AuthHelper工具使用client端系统ID，向配置管理中心下载我方的公钥、私钥，并使用私钥对会话密钥签章，返回签名包。
		// 3、把请求签名包从客户端发向服务端
		try {
			ShakeHandsBean shb = new ShakeHandsBean();
			shb.setSingPackage(singPackageResquest);
			shb.setSystemName(systemName_client);
			Request request = new Request();
			request.setShakehands(true);// 握手标志
			request.setSecurity(false);// 握手时为非加密通信
			request.setData(shb);
			DefaultFuture<Response> future = new DefaultFuture<Response>(this,
					request, Constants.SHAKEHANDS_TIMEOUT);
			// 发送数据
			session.write(request);
			// 返回数据,get()会阻塞等到数据返回
			response = future.get();
		} catch (RemotingException e1) {
			String msg = "加密通信-客户端执行三次握手时异常,第一握(发送请求签名包),channel:" + this;
			throw new RpcException(RpcException.TIMEOUT_EXCEPTION, msg, e1);
		}

		// 客户端三次握手(第三次握手)
		// client端验证”应答签名包” authResponseSignPackage(Server端系统ID，应答签名包)，返回“会话密钥”。
		// AuthHelper工具使用server端系统ID，向配置管理中心下载server方的公钥,验章，并返回会话密钥。
		// 完成握手。
		if (response.isShakehands()) {
			ShakeHandsBean shb2 = (ShakeHandsBean) response.getData();
			byte[] singPackageResponse = shb2.getSingPackage();
			String systemName_server = shb2.getSystemName();

			// @motify 2013-06-25 liuhe 添加信任列表过滤 双向都是信任的
			TrustListHelperProxy.judgeTrust(systemName_client,
					systemName_server);

			String sessionKey_server = null;// 会话密钥
			try {
				sessionKey_server = AuthHelperProxy.authResponseSignPackage(
						systemName_server, singPackageResponse);
			} catch (Exception e) {
				String msg = "加密通信-客户端执行三次握手时异常,第三握(验证应答签名包),channel:" + this;
				logger.error(msg, e);
				throw new EncryptException(msg, e);
			}
			long t2 = System.currentTimeMillis();
			long t3 = (t2 - t1);// 耗时

			// 重点：在channel中保存会话密钥，表示本连接是完成三次握手的连接，可进行加密、非加密的通信。
			if (sessionKey_server != null
					&& sessionKey_server.equals(sessionKey_client)) {
				this.setAttribute(Constants.SESSION_KEY, sessionKey_server);
				if (logger.isInfoEnabled()) {
					logger.info("三次握手成功,耗时" + t3 + "ms,systemName_client:"
							+ systemName_client + ",channel:" + this);
				}
			} else {
				String msg = "三次握手失败,耗时" + t3 + "ms,systemName_client:"
						+ systemName_client + ",channel:" + this;
				logger.error(msg);
				throw new RpcException(RpcException.BIZ_EXCEPTION, msg);
			}
		} else {
			logger.error("不会走到这里");
		}
	}

	/**
	 * 发送数据<br>
	 * 
	 * @param message
	 * @throws RemotingException
	 * @see com.hc360.rsf.remoting.Channel#send(java.lang.Object)
	 */
	public void send(Object message) throws RemotingException {
		session.write(message);// 发送数据
	}

	/**
	 * 发送 数据<br>
	 * 
	 * @param message
	 * @param sent
	 *            是否等待发送完成
	 * @throws RemotingException
	 * @see com.hc360.rsf.remoting.Channel#send(java.lang.Object, boolean)
	 */
	public void send(Object message, boolean sent) throws RemotingException {
		boolean success = true;
		int timeout = 0;
		try {
			// 发送数据
			WriteFuture future = session.write(message);
			if (sent) {
				timeout = Constants.DEFAULT_TIMEOUT;
				success = future.join(timeout);
			}
		} catch (Throwable e) {
			throw new RemotingException(this, "Failed to send message "
					+ message + " to " + getRemoteAddress() + ", cause: "
					+ e.getMessage(), e);
		}

		if (!success) {
			throw new RemotingException(this, "Failed to send message "
					+ message + " to " + getRemoteAddress() + "in timeout("
					+ timeout + "ms) limit");
		}
	}

	/**
	 * function description
	 * 
	 * @return
	 * @see com.hc360.rsf.remoting.Channel#getLocalAddress()
	 */
	public InetSocketAddress getLocalAddress() {
		return localAddress;
	}

	/**
	 * function description
	 * 
	 * @return
	 * @see com.hc360.rsf.remoting.Channel#getRemoteAddress()
	 */
	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	/**
	 * 判断连接是否可用<br>
	 * <br>
	 * 连接是否已断开，可以使用通信层面的channel.isConnected()判定，准确性如下表：<br>
	 * ----------前提条件------------------------结论----------------<br>
	 * |两点正常通信时,正常退出服务端 | 客户端的isConnected()可以感知 |<br>
	 * |两点正常通信时,kill服务端进程 | 客户端的isConnected()可以感知 |<br>
	 * |两点正常通信时,禁用客户端网卡 | 客户端的isConnected()可以感知 |<br>
	 * |两点正常通信时,拨断网线----- | 客户端的isConnected()不可以感知 |<br>
	 * --------------------------------------------------------------<br>
	 * 通过以上表格可以看出，使用channel.isConnected()判定连接是否可用，大多时候是可靠的。<br>
	 * 除了“拨断网线”这种情况。就是说isConnected()不能做到100%准确。<br>
	 * <br>
	 */
	public boolean isConnected() {
		return session.isConnected();
	}

//	/**
//	 * 关闭session
//	 */
//	public void close() {
//		close(null);
//	}

	/**
	 * 关闭session
	 */
	public void close(String msg) {  
		if (session != null) {
			//if (session.isConnected()) {
				try {
					removeChannelIfDisconnectd(session);
				} catch (Exception e) {
					if (msg != null) {
						logger.warn(msg + "" + e.getMessage(), e);
					} else {
						logger.warn(e.getMessage(), e);
					}

				}
				try {
					if (logger.isInfoEnabled()) {
						if (msg != null) {
							logger.info(msg + " 关闭连接 " + session);
						} else {
							logger.info("关闭连接 " + session);
						}
					}
					session.close(true);
				} catch (Exception e) {
					if (msg != null) {
						logger.warn(msg + "" + e.getMessage(), e);
					} else {
						logger.warn(e.getMessage(), e);
					}
				}
			//}
		}
	}

	public boolean hasAttribute(String key) {
		return session.containsAttribute(key);
	}

	public Object getAttribute(String key) {
		return session.getAttribute(key);
	}

	public void setAttribute(String key, Object value) {
		session.setAttribute(key, value);
	}

	public void removeAttribute(String key) {
		session.removeAttribute(key);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((session == null) ? 0 : session.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MinaChannel other = (MinaChannel) obj;
		if (session == null) {
			if (other.session != null) {
				return false;
			}
		} else if (!session.equals(other.session)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		if (localAddress != null && remoteAddress != null) {
			return localAddress.toString() + "-->" + remoteAddress.toString();
		} else if (session != null) {
			return session.toString();
		} else {
			return "channel is closed,无法取出通信双方IP、端口";
		}
	}

	/**
	 * function description
	 * 
	 * @return
	 * @see com.hc360.rsf.remoting.Channel#getRemoteIpPort()
	 */
	public IpPort getRemoteIpPort() {
		InetSocketAddress remote= getRemoteAddress();
		
		if(remote!=null){
			String ip_remote=remote.getAddress().getHostAddress();
			int port_remote=remote.getPort();
			return  new IpPort( ip_remote, port_remote);
		}
		return null;
	}
	
	public String getKey(){
		InetSocketAddress remote= getRemoteAddress();
		if(remote!=null){
			String ip_remote=remote.getAddress().getHostAddress();
			int port_remote=remote.getPort();
			return ip_remote+":"+port_remote;
		}
		return null;
	}
	
	/**
	 * 判断服务端是否存在指定的服务
	 * 
	 * @param serviceName  服务名
	 * @return
	 * @throws RemotingException 不对错误进行任何处理，交由上层逻辑处理
	 */
	public boolean isContainService(String serviceName) throws RemotingException{
		Map<String, String> parametersUrl=new HashMap<String, String>();
		parametersUrl.put(Constants.PATH_KEY, EchoService.class.getName());
        //RPCInvocation是对 方法名,方法的参数类型,方法的实参   的封装. 
        RpcInvocation rpcInvocation=new RpcInvocation(Constants.$ECHO_INTERFACE,new Class<?>[]{String.class},new Object[]{serviceName});
        rpcInvocation.setAttachments(parametersUrl);
    	Response r =(Response)request(rpcInvocation, Constants.ECHO_TIMEOUT);
	    RpcResult rpcResult=(RpcResult)r.getData();
	    boolean bool = (Boolean)rpcResult.getValue();
	    logger.debug("{}发回声测试{}是否存在的结果是：{}",new Object[]{this,serviceName,bool});
	    return bool;
	}
}
