/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.common.remoting.transport.mina;

import java.net.InetSocketAddress;

import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

/**
 * TODO(描述类的用途) 
 * 
 * @author zhaolei 2012-7-23
 */
public class MinaClientTest {
	private static NioSocketConnector connector = null;
	
	public static void main(String[] args) throws Exception{
		MinaClientTest t=new MinaClientTest();
		t.start();
		
	}
	
	public void start() throws Exception{
		// 创建IoConnector(连接器)
		connector = new NioSocketConnector(Runtime.getRuntime().availableProcessors());
		connector.getSessionConfig().setMinReadBufferSize(64);
		connector.getSessionConfig().setReadBufferSize(16384);
		connector.getSessionConfig().setMaxReadBufferSize(65536);//对并发时的吞吐量有很大影响 128k=131072,64k=65536
		connector.setConnectTimeoutMillis(5 * 1000);//创建连接的超时时间
		
		// 设置IoSession闲置时间，参数单位是秒
		// 时间到会触发sessionIdle方法，反复触发
		connector.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
	    //connector.setWorkerTimeout(1);   //1秒钟（默认超时60秒）  
		
		// Mina提供的对象编码解码器
		ObjectSerializationCodecFactory factory = new ObjectSerializationCodecFactory();
		
		// 设置ioFilter
		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(factory));
		
		// 设置处理器
		connector.setHandler(new IoHandlerAdapter(){
			public void sessionOpened(IoSession iosession) throws Exception {
				System.out.println("sessionOpened:"+iosession);
			}
			public void messageReceived(IoSession iosession, Object obj) throws Exception {
				 System.out.println(obj);
		    }
			public void sessionClosed(IoSession iosession) throws Exception {
				System.out.println("sessionClosed:"+iosession);
			}
		    public void sessionIdle(IoSession iosession, IdleStatus idlestatus) throws Exception {
		    	System.out.println("sessionIdle:"+iosession+"--"+idlestatus);
		    	iosession.write("1");
		    }
		});
		
		System.out.println("开始了");
		get();
		//get();
		System.out.println("完成了");
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		connector.dispose(true);
		connector.dispose();
	}
	
	public void get() throws InterruptedException{
		ConnectFuture connectFuture=null;
		connectFuture = connector.connect(new InetSocketAddress("127.0.0.1", 63634));
		connectFuture.awaitUninterruptibly();// 等待连接创建完成
		IoSession session1 = connectFuture.getSession();
		IoSession session2 = connectFuture.getSession();
		if(session1==session2 ){
			System.out.println("session1==session2");
		}
		System.out.println();
		System.out.println("session1:"+session1+" hashCode:"+session1.hashCode());
		System.out.println("session2:"+session2+" hashCode:"+session2.hashCode());
		System.out.println();
		
		Thread.sleep(1000);
		
		//session1.close(true);
		
		if(session1.isClosing()){
			System.out.println("session1 is Closing");
		}else{
			System.out.println("session1 is not Closing");
		}
		if(session1.isConnected()){
			System.out.println("session1 is Connected()");
		}else{
			System.out.println("session1 is not Connected()");
		}
		//----------------------------
		if(session2.isClosing()){
			System.out.println("session2 is Closing");
		}else{
			System.out.println("session2 is not Closing");
		}
		if(session2.isConnected()){
			System.out.println("session2 is Connected()");
		}else{
			System.out.println("session2 is not Connected()");
		}
		
//		session1.write("1");
//		session2.write("2");
		
		//-----------------------------
		
//		connectFuture = connector.connect(new InetSocketAddress("127.0.0.1", 63634));
//		connectFuture.awaitUninterruptibly();// 等待连接创建完成
//		IoSession session3 = connectFuture.getSession();
////		CloseFuture closeFuture=session1.getCloseFuture().awaitUninterruptibly();
////		session1=closeFuture.getSession();
//		
//		if(session3.isClosing()){
//			System.out.println("session3 is Closing");
//		}else{
//			System.out.println("session3 is not Closing");
//		}
//		if(session3.isConnected()){
//			System.out.println("session3 is Connected()");
//		}else{
//			System.out.println("session3 is not Connected()");
//		}
	}

}
