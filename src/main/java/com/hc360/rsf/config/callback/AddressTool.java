/**
 * PushTool.java   2012-5-10
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.callback;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hc360.rsf.remoting.Channel;

/**
 * AddressTool 工具类
 * 
 * 在当前线程中取出IP,port信息
 * 
 * 以被业务系统使用，
 * 
 * @author zhaolei 2012-5-11
 */
public class AddressTool {
	static ThreadLocal<Address> threadLocal=new ThreadLocal<Address>();
	private static Logger logger = LoggerFactory.getLogger(AddressTool.class);
	
	public static String toStringInfo(){
		StringBuilder sbl=new StringBuilder(100);
		sbl.append("本端地址：");
		sbl.append(getLocalIp());
		sbl.append(":");
		sbl.append(getLocalPort());
		sbl.append(",");
		sbl.append("远端地址：");
		sbl.append(getRemoteIp());
		sbl.append(":");
		sbl.append(getRemotePort());
		return sbl.toString();
	}
	/**
	 * 取得本端IP 
	 * @return
	 */
	public static String getLocalIp(){
		Address address=threadLocal.get();
		if(address!=null){
			return address.ip_local;
		}else{
			return null;
		}
	}
	/**
	 * 取得本端port 
	 * @return
	 */
	public static int getLocalPort(){
		Address address=threadLocal.get();
		if(address!=null){
			return address.port_local;
		}else{
			return -1;
		}
	}
	/**
	 * 取得远端IP
	 * @return
	 */
	public static String getRemoteIp(){
		Address address=threadLocal.get();
		if(address!=null){
			return address.ip_remote;
		}else{
			return null;
		}
	}
	/**
	 * 取得远端port 
	 * @return
	 */
	public static int getRemotePort(){
		Address address=threadLocal.get();
		if(address!=null){
			return address.port_remote;
		}else{
			return -1;
		}
	}

	/**
	 * 取得本端的InetSocketAddress
	 * @return
	 */
	public static InetSocketAddress getLocalAddress(){
		Address address=threadLocal.get();
		return address.getLocalAddress();
	}
	/**
	 * 取得远端的InetSocketAddress
	 * @return
	 */
	public static InetSocketAddress getRemoteAddress(){
		Address address=threadLocal.get();
		return address.getRemoteAddress();
	}
	
	/**
	 * putChannel
	 * @param channel
	 */
	public static void putChannel(Channel channel){
		if(channel!=null && channel.getLocalAddress()!=null && channel.getRemoteAddress()!=null){
			String ip_local=channel.getLocalAddress().getAddress().getHostAddress();
			int port_local=channel.getLocalAddress().getPort();
			String ip_remote=channel.getRemoteAddress().getAddress().getHostAddress();
			int port_remote=channel.getRemoteAddress().getPort();
			threadLocal.set(new Address(ip_local,port_local,ip_remote,port_remote));
		}else{
			logger.warn("putChannel方法入参channel==null");
		}
	}
	
	/**
	 * remove
	 */
	public static void remove(){
		threadLocal.remove();
	}
	
	/**
	 * 内部类，封装 通信双方的IP地址、端口
	 * 
	 * @author zhaolei 2013-3-8
	 */
	static class Address{
		public String ip_local;
		public int port_local;
		public String ip_remote;
		public int port_remote;
		public Address(){}
		public Address(String ip_local,int port_local,String ip_remote,int port_remote){
			this.ip_local=ip_local;
			this.port_local=port_local;
			this.ip_remote=ip_remote;
			this.port_remote=port_remote;
		}
		public InetSocketAddress getRemoteAddress(){
				 return new InetSocketAddress(ip_remote,port_remote);
		}
		public InetSocketAddress getLocalAddress(){
			return new InetSocketAddress(ip_local,port_remote);
		}
	}
}
