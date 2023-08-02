/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config;

import java.net.InetSocketAddress;

import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import com.hc360.rsf.common.URL;
import com.hc360.rsf.remoting.HandlerDelegate;
import com.hc360.rsf.remoting.heartbeat.HeartbeatHandlerDelegate;
import com.hc360.rsf.remoting.transport.dispather.AllChannelHandler;
import com.hc360.rsf.remoting.transport.mina.MinaHandler;
import com.hc360.rsf.remoting.transport.mina.MinaHandlerDelegate;

/**
 * 关闭测试 
 * 
 * @author zhaolei 2012-6-1
 */
public class CloseTest {
	private IoConnector connector = null;

	/**
	 *  
	 * @param args
	 */
	public static void main(String[] args) {
		CloseTest ct=new CloseTest();
		ct.init();
	}
	
	public void init(){
		// 这个URL目前没有用,有待处理,用于线程池取参数,但没取到,用的默认值
		URL url = new URL("rsf", "0.0.0.0", 0);
		// 真正干活的事件处理器
		MinaHandlerDelegate myHandlerDelegate = new MinaHandlerDelegate(MinaHandlerDelegate.CLIENT_SIDE);
		// 把各个事件放入独立的线程中处理
		HandlerDelegate hd =  new AllChannelHandler(myHandlerDelegate, url);
		// 处理心跳事件
//		HandlerDelegate hhd = new HeartbeatHandlerDelegate(hd, url);
		HandlerDelegate hhd = new HeartbeatHandlerDelegate(hd);
		// Mina的事件处
//		IoHandler minaHandler = new MinaHandler(url, myHandlerDelegate);
		IoHandler minaHandler = new MinaHandler( myHandlerDelegate);
		
		
		
		// 创建IoConnector(连接器)
		connector = new NioSocketConnector(Runtime.getRuntime().availableProcessors());
		connector.addListener(new Listener());
		connector.getSessionConfig().setMinReadBufferSize(64);
		connector.getSessionConfig().setReadBufferSize(16384);
		connector.getSessionConfig().setMaxReadBufferSize(65536);

		// Mina提供的对象编码解码器
		ObjectSerializationCodecFactory factory = new ObjectSerializationCodecFactory();
		
		// 设置ioFilter
		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(factory));
		
		// 设置处理器
		connector.setHandler(minaHandler);
		System.out.println("生成connector："+connector);
		
		getChannel();
	}
	
	public void getChannel() {
		// 创建连接--TCP连接
		ConnectFuture connectFuture = connector.connect(new InetSocketAddress("127.0.0.1", 1088));
		// 等待连接创建完成
		connectFuture.awaitUninterruptibly();
		
		IoSession session = connectFuture.getSession();
		System.out.println("生成session："+session);
		
		CloseFuture cf=session.close(true);
		cf.awaitUninterruptibly();
		System.out.println("关闭session："+session);
		
		//connector.dispose();
	}
	
	public class Listener implements IoServiceListener{

		public void serviceActivated(IoService ioservice) throws Exception {
			System.out.println("serviceActivated");
		}

		public void serviceDeactivated(IoService ioservice) throws Exception {
			System.out.println("serviceDeactivated");
			connector.dispose();
		}

		public void serviceIdle(IoService ioservice, IdleStatus idlestatus) throws Exception {
			System.out.println("serviceIdle");
		}

		public void sessionCreated(IoSession iosession) throws Exception {
			System.out.println("sessionCreated");
		}

		public void sessionDestroyed(IoSession iosession) throws Exception {
			System.out.println("sessionDestroyed");
		}
	}

}
