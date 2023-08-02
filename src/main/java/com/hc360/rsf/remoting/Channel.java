/**
 * Channel.java   2012-5-10
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting;

import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * Channel封装一个socket连接，一个channel可以发送数据，包括同步与异步
 * 
 * @author zhaolei 2012-5-11
 */
/**
 * class description
 * 
 * @author liwanchun
 * @version 4.0 2013-8-1
 * @since 4.0	
 */
public interface Channel extends Serializable{
    /**
     * get url
     * 
     * @return IpPort
     */
	IpPort getRemoteIpPort();
	
	String getKey();
	
	/**
	 * 进行三次握手
	 */
	void shakeHands();


	/**
	 * 发出请求,并等待对方应答(同步)
	 * @param message 数据 
	 * @return  处理结果
	 * @throws RemotingException
	 */
	Object request(Object message)throws RemotingException;
	
	/**
	 * 发出请求,并等待对方应答(同步)
	 * 
	 * @param message 发送的数据
	 * @param timeout 超时时间
	 * @return 处理结果
	 * @throws RemotingException
	 */
	public Object request(Object message,int timeout) throws RemotingException ;
	
	/**
	 * 发出请求,并等待对方应答(同步)
	 * 
	 * @param message 发送的数据
	 * @param timeout 超时时间
	 * @param security 是否是加密通信
	 * @return 处理结果
	 * @throws RemotingException
	 * @see com.hc360.rsf.remoting.Channel#request(java.lang.Object)
	 */
	public Object request(Object message,int timeout,boolean security) throws RemotingException ;
	
    /**
     * 发送数据,不等待发送完成（异步）
     * @param message 数据
     * @throws RemotingException
     */
    void send(Object message) throws RemotingException;

    /**
     * 发送数据（异步）
     * @param message 数据
     * @param sent 是否等待发送完成
     */
    void send(Object message, boolean sent) throws RemotingException;

    /**
     * get local address.
     * 
     * @return local address.
     */
    InetSocketAddress getLocalAddress();
    
    /**
     * get remote address.
     * 
     * @return remote address.
     */
    InetSocketAddress getRemoteAddress();
    
    /**
     * has attribute.
     * 
     * @param key key.
     * @return has or has not.
     */
    boolean hasAttribute(String key);

    /**
     * get attribute.
     * 
     * @param key key.
     * @return value.
     */
    Object getAttribute(String key);

    /**
     * set attribute.
     * 
     * @param key key.
     * @param value value.
     */
    void setAttribute(String key,Object value);
    
    /**
     * remove attribute.
     * 
     * @param key key.
     */
    void removeAttribute(String key);
    
    /**
     * 判断连接是否可用<br>
     * <br>
     * 连接是否已断开，可以使用通信层面的channel.isConnected()判定，准确性如下表：<br>
     * ----------前提条件------------------------结论----------------<br>
     * |两点正常通信时，正常退出服务端 | 客户端的isConnected()可以感知     |<br>
     * |两点正常通信时，kill服务端进程 | 客户端的isConnected()可以感知     |<br>
     * |两点正常通信时，禁用客户端网卡 | 客户端的isConnected()可以感知      |<br>
     * |两点正常通信时，拨断网线----- | 客户端的isConnected()不可以感知 |<br>
     * --------------------------------------------------------------<br>
     * 通过以上表格可以看出，使用channel.isConnected()判定连接是否可用，大多时候是可靠的。<br>
     * 除了“拨断网线”这种情况。就是说isConnected()不能做到100%准确。<br>
     * <br>
     */
    boolean isConnected();
    
    /**
     * close
     */
    //public void close();
    public void close(String msg);
    
    /**
     * 判断服务端是否存在指定的服务
     * 
     * @param serviceName
     * @return
     * @throws RemotingException
     */
    boolean isContainService(String serviceName) throws RemotingException;

}
