/**
 * MinaServer.java   2012-3-14
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting.transport.mina;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoServiceStatistics;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hc360.rsf.common.URL;
import com.hc360.rsf.common.Version;
import com.hc360.rsf.common.utils.FileUtil;
import com.hc360.rsf.config.GlobalManager;
import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.Server;
import com.hc360.rsf.rpc.protocol.codec.JavaCodec;
import com.hc360.rsf.rpc.protocol.codec.mina.MinaCodecAdapter;

/**
 * Server接口的实现
 * Mina网络通信服务端，启动对本地端口的监听
 * @author zhaolei 2012-3-14
 */
public class MinaServer implements Server {
	private static Logger logger = LoggerFactory.getLogger(MinaServer.class);
	private InetSocketAddress bindAddress;
	private SocketAcceptor acceptor = null;
	private String host ="127.0.0.1";
	private ExecutorFilter executorFilter=null;

	/**
	 * 启动服务端监听
	 * 
	 * 构造方法需要一个URL url参数，没什么大作用。
	 * 
	 * 本系统内URL有2类，是互相不能混用的，要注意区分
	 * 第一类，Client\Server类中拥有一个全局的URL，其中的参数是全局的参数
	 * 第二类，ClientConfig类createProxy()方法，有自己的URL，其中的参数是某个接口级的参数
	 * 
	 * 构造方法需要的URL url参数，是“第一类”，用于MinaCodecAdapter于buffer大小于，但没取到，使用了认值。
	 * 编码器需要URL参数：payload(请求及响应数据包大小限制)
	 * 
	 * @param port
	 * @param minaHandler
	 * @param url
	 * @throws IOException
	 */
	public MinaServer(int port,IoHandler minaHandler,URL url) throws IOException{
		if(port<=0){
			throw new IllegalArgumentException("port 参数非法,port={}"+port);
		}
		if(minaHandler==null){
			throw new IllegalArgumentException("IoHandler 参数非法,IoHandler=null");
		}
		
		bindAddress = new InetSocketAddress(host, port);
		acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors());
		
		//对高并发时的网络吞吐量有较大影响，请小心调整。128k=131072,64k=65536
		acceptor.getSessionConfig().setMinReadBufferSize(64);
		acceptor.getSessionConfig().setReadBufferSize(16384);
		acceptor.getSessionConfig().setMaxReadBufferSize(65536);
		
		//setTcpNoDelay(true)表示参数关闭了nagle算法
		//nagle算法是：采用Nagle算法把较小的包组装为更大的帧再发出去
		//true表示设置为非延迟发送,数据马上发出去,不组装成大包再发送，这对telnet是有利的。
		acceptor.getSessionConfig().setTcpNoDelay(true); 
		
		// 如果端口忙，但TCP状态位于 TIME_WAIT ，可以重用 端口。
		// 这个参数在Windows平台与Linux平台表现的特点不一样。
		// 在Windows平台本参数表现的特点是不正确的（但不影响程序运行），多个新建立的Socket对象可以绑定在同一个端口上，
		//      非TIME_WAIT状态端口也被重用了。这样做并没有实际意义。
		// 在Linux  平台本参数表现的特点是正确的 ，只有TCP状态位于 TIME_WAIT状态，才可以重用 端口。这才是正确的行为。
		if (!System.getProperty("os.name").startsWith("Windows")) {
			//设置的是主服务监听的端口可以重用 
			//两个进程，可以运行在一个端口上
			acceptor.setReuseAddress(true);
			
			//设置每一个非主监听连接的端口可以重用
			//默认值就是true,所以不用设置了
			//acceptor.getSessionConfig().setReuseAddress(true);
		} 
		
		/*
		 * 在一个TCP连接建立之后，我们会很奇怪地发现，默认情况下，如果一端异常退出（譬如网络中断后一端退出，使地关闭请求另一端无法接收到），
		 * TCP的另一端并不能获得这种情况，仍然会保持一个半关闭的连接，对于服务端，大量半关闭的连接将会是非常致命的。
		 * SO_KEEPALIVE提供了一种手段让TCP的一端（通常服务提供者端）可以检测到这种情况。
		 * 如果我们设置了SO_KEEPALIVE，TCP在距离上一次TCP包交互2个小时（取决于操作系统和TCP实现，规范建议不低于2小时）后，
		 * 会发送一个探测包给另一端，如果接收不到响应，则在75秒后重新发送，连续10次仍然没有响应，则认为对方已经关闭，
		 * 系统会将该连接关闭。一般情况下，如果对方已经关闭，则对方的TCP层会回RST响应回来，这种情况下，同样会将连接关闭。
		 */
		acceptor.getSessionConfig().setKeepAlive(true);
		//设置接收缓冲区的大小       调大了100倍
		acceptor.getSessionConfig().setReceiveBufferSize(1024*50);
		//设置发送缓冲区的大小       调大了10倍
		acceptor.getSessionConfig().setSendBufferSize(1024*50);
		//设置主服务监听端口的监听队列的最大值为100，如果当前已经有100个连接，再新的连接来将被服务器拒绝 
		//默认值是50
		acceptor.setBacklog(100);
		
		// 监听端口
		acceptor.setDefaultLocalAddress(new InetSocketAddress(port));
		// 线程池
		executorFilter=new ExecutorFilter(1, 16);
		acceptor.getFilterChain().addLast("executor", executorFilter);
		
		//关闭的时候使用RST的方式（非常规关闭方法）
		//正常关闭TCP连接要经过4次握手称为优雅关闭，TCP还提供了另外一种非优雅的关闭方式RST(Reset)。
		//设置了它后，MINA在调用了close()方法后，TCP不会再进入TIME_WAIT状态了，直接进入CLOSED状态。
		//acceptor.getSessionConfig().setSoLinger(0);
		
		MinaCodecAdapter factory=new MinaCodecAdapter(new JavaCodec(),url,null);
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(factory));// 设置ioFilter

		acceptor.setHandler(minaHandler);
		acceptor.bind();
		if(logger.isInfoEnabled()){
			logger.info("RSF Server 启动完成,版本:{},绑定本机端口:{}.",Version.getVersion(),port);
		}
	}
	
    public InetSocketAddress getBindAddress() {
        return bindAddress;
    }

	/**
	 * 返回服务端管理的所有Channel, 可以用于向客户写数据
	 * 
	 * @return
	 */
	public List<Channel> getChannels() {
		Map<Long, IoSession> map = acceptor.getManagedSessions();
		List<Channel> list = new ArrayList<Channel>();
		Set<Long> keyset = map.keySet();
		for (Long key : keyset) {
			IoSession session = map.get(key);
			Channel channel = MinaChannel.getOrAddChannel(session);
			list.add(channel);
		}
		return list;
	}
	
	public void close(){
		//关闭服务端线程池
		try{
			logger.info("关闭服务端线程池");
			//System.out.println("关闭服务端线程池");
			if(GlobalManager.executor_server!=null){
				GlobalManager.executor_server.shutdown();
			}
			if(executorFilter!=null){
				executorFilter.destroy();
			}
		}catch(Exception e){
			logger.error("关闭服务端线程池异常",e);
			//System.out.println("关闭服务端线程池异常,"+e.getMessage());
		}
		
		if(acceptor!=null){
			try{
				acceptor.unbind();
				logger.info("解绑 Mina Server 端口:{}",bindAddress!=null?bindAddress.getPort():"null");
				//System.out.println("解绑 Mina Server 端口:"+(bindAddress!=null?bindAddress.getPort():"null") );
			}catch(Exception e){
				logger.error("关闭 Mina Server异常",e);
				//System.out.println("关闭 Mina Server异常,"+e.getMessage());
			}
			try{
				acceptor.dispose();
				logger.info("关闭 Mina Server,地址:{}",bindAddress);
				//System.out.println("关闭 Mina Server,地址:"+bindAddress);
			}catch(Exception e){
				logger.error("关闭 Mina Server异常",e);
				//System.out.println("关闭 Mina Server异常,"+e.getMessage());
			}
			
			Map<String, Server> server_list = GlobalManager.SERVER_LIST;
			server_list.remove(String.valueOf(bindAddress.getPort()));
		}
	}
	
	public String statistic(){
		String b="\n\r";
		IoServiceStatistics  statistic=acceptor.getStatistics();
		if(statistic!=null){
			statistic.updateThroughput(System.currentTimeMillis());
			StringBuilder sbl=new StringBuilder();
			sbl.append("读(byte)："+ FileUtil.fileSize(statistic.getReadBytes()) + b);
			sbl.append(" 写(byte)："+ FileUtil.fileSize(statistic.getWrittenBytes()) + b);
			sbl.append(" 当前读吞吐量(byte)/秒："+ FileUtil.fileSize((long)statistic.getReadBytesThroughput()) + b);
			sbl.append(" 当前写吞吐量(byte)/秒："+ FileUtil.fileSize((long)statistic.getWrittenBytesThroughput()) + b);
			sbl.append(" 最高读吞吐量(byte)/秒："+ FileUtil.fileSize((long)statistic.getLargestReadBytesThroughput()) + b);
			sbl.append(" 最高写吞吐量(byte)/秒："+ FileUtil.fileSize((long)statistic.getLargestWrittenBytesThroughput()) + b);
			
			sbl.append(" 读(Messages)："+ statistic.getReadMessages() + b);
			sbl.append(" 写(Messages)："+ statistic.getWrittenMessages() + b);
			sbl.append(" 读吞吐量(Messages)/秒："+FileUtil.DF.format(statistic.getReadMessagesThroughput()) + b);
			sbl.append(" 写吞吐量(Messages)/秒："+ FileUtil.DF.format(statistic.getWrittenMessagesThroughput()) + b);
			sbl.append(" 最高读吞吐量(Messages)/秒："+ FileUtil.DF.format(statistic.getLargestReadMessagesThroughput()) + b);
			sbl.append(" 最高写吞吐量(Messages)/秒："+ FileUtil.DF.format(statistic.getLargestWrittenMessagesThroughput()) + b);
			
			sbl.append(" 历史累积Session总数："+statistic.getCumulativeManagedSessionCount() + b);
			sbl.append(" 最高Session数量达到："+statistic.getLargestManagedSessionCount() + b);
	        return sbl.toString();
		}else{
			return "error!";
		}
	}
}
