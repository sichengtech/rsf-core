/**
 * MyHandler.java   2012-4-13
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting.transport.mina;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hc360.rsf.common.Constants;
import com.hc360.rsf.common.URL;
import com.hc360.rsf.common.Version;
import com.hc360.rsf.common.threadpool.ThreadPoolInfoUtil;
import com.hc360.rsf.common.utils.AuthHelperProxy;
import com.hc360.rsf.common.utils.EvnUtil;
import com.hc360.rsf.common.utils.StringUtils;
import com.hc360.rsf.common.utils.TrustListHelperProxy;
import com.hc360.rsf.config.ClientConfig;
import com.hc360.rsf.config.GlobalManager;
import com.hc360.rsf.config.callback.AddressTool;
import com.hc360.rsf.config.callback.CallBack;
import com.hc360.rsf.config.callback.CallBackTool;
import com.hc360.rsf.config.callback.CallBackWrap;
import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.EncryptException;
import com.hc360.rsf.remoting.HandlerDelegate;
import com.hc360.rsf.remoting.RemotingException;
import com.hc360.rsf.remoting.Server;
import com.hc360.rsf.remoting.exchange.support.DefaultFuture;
import com.hc360.rsf.remoting.exchange.support.Request;
import com.hc360.rsf.remoting.exchange.support.Response;
import com.hc360.rsf.remoting.exchange.support.ShakeHandsBean;
import com.hc360.rsf.rpc.DataTooLengthException;
import com.hc360.rsf.rpc.Invoker;
import com.hc360.rsf.rpc.RpcInvocation;
import com.hc360.rsf.rpc.RpcResult;
import com.hc360.rsf.rpc.protocol.RsfInvokerCallback;
import com.hc360.rsf.rpc.protocol.RsfInvokerClientP2p;
import com.hc360.rsf.rpc.protocol.RsfProtocol;
import com.hc360.rsf.rpc.protocol.codec.AbstractCodec;

/**
 * MinaHandlerDelegate 处理器<br>
 * <br>
 * Mina的IoHandler被委托给本类来处理<br>
 * 
 * @author zhaolei 2012-4-13
 */
public class MinaHandlerDelegate implements HandlerDelegate {
	private static Logger logger = LoggerFactory.getLogger(MinaHandlerDelegate.class);

	/**
	 * 运行在客户端这一边
	 */
	public static final String CLIENT_SIDE = "client_side";

	/**
	 * 运行在服务端这一边
	 */
	public static final String SERVER_SIDE = "server_side";

	/**
	 * 用于指明 MinaHandlerDelegate,运行在哪一边
	 */
	private String side;
	
	public String getSide() {
		return side;
	}

	public void setSide(String side) {
		this.side = side;
	}

	/**
	 * 构造方法
	 * 
	 * @param side
	 *            用于指明 MinaHandlerDelegate,运行在哪一边
	 */
	public MinaHandlerDelegate(String side) {
		if (CLIENT_SIDE.equals(side) || SERVER_SIDE.equals(side)) {
			this.side = side;
		} else {
			throw new IllegalArgumentException("MinaHandlerDelegate类的side实参错误,side==" + side);
		}
	}

	/**
	 * 消息发出时被调用
	 * 
	 * @param channel
	 * @param message
	 * @throws RemotingException
	 * @see com.hc360.rsf.remoting.HandlerDelegate#sent(com.hc360.rsf.remoting.Channel,
	 *      java.lang.Object)
	 */
	public void sent(Channel channel, Object message) throws RemotingException {
		if(message instanceof Response){
			Response res=(Response)message;
			if(res.isHeartbeat()){
				if(logger.isDebugEnabled()){
					logger.debug("回应心跳事件, side={},channel={},message={}", new Object[] { side, channel, message });
				}
				return;
			}
		}
		if(logger.isDebugEnabled()){
			logger.debug("消息发出事件, side={},channel={},message={}", new Object[] { side, channel, message });
		}
		
		
		// 这里不能再执行channel.send(message),否则会死循环
		// 本方法是因发送消息而被触的,再执行channel.send(message)会再触发
		// logger.debug("data sent, side={},message={}", side, message);
		// channel.send(message);
	}

	/**
	 * 连接创建时被调用
	 * 
	 * @param channel
	 * @throws RemotingException
	 * @see com.hc360.rsf.remoting.HandlerDelegate#connected(com.hc360.rsf.remoting.Channel)
	 */
	public void connected(Channel channel) throws RemotingException {
		if(logger.isDebugEnabled()){
			logger.debug("连接创建事件, side={},channel={}", side, channel);
		}
	}

	/**
	 * 连接关闭时被调用
	 * 
	 * @param channel
	 * @throws RemotingException
	 * @see com.hc360.rsf.remoting.HandlerDelegate#disconnected(com.hc360.rsf.remoting.Channel)
	 */
	public void disconnected(Channel channel) throws RemotingException {
		if(logger.isDebugEnabled()){
			logger.debug("连接关闭事件, side={},channel={}", side, channel);
		}
	}

	/**
	 * 连接发生异常时被调用
	 * 为了不干扰开发人员排错，将mina连接异常用debug级别来屏蔽
	 * @param channel
	 * @param exception
	 * @throws RemotingException
	 * @see com.hc360.rsf.remoting.HandlerDelegate#caught(com.hc360.rsf.remoting.Channel,
	 *      java.lang.Throwable)
	 */
	public void caught(Channel channel, Throwable exception) throws RemotingException {
		if(logger.isDebugEnabled()){
			logger.debug("连接发生异常事件, side="+side+",channel="+channel,exception);
		}
		logger.error("连接发生异常事件, side="+side+",channel="+channel,exception);
	}


	/**
	 * 消息到达时被调用
	 * 
	 * ====================
	 * |接收端的核心处理方法|
	 * ====================
	 * 
	 * @param channel
	 * @param message
	 * @throws RemotingException
	 * @see com.hc360.rsf.remoting.HandlerDelegate#received(com.hc360.rsf.remoting.Channel,
	 *      java.lang.Object)
	 */
	public void received(Channel channel, Object message) throws RemotingException {
		if(logger.isDebugEnabled()){
			logger.debug("消息到达事件, side={},channel={},message={}", new Object[] { side, channel, message });
		}

		/**
		 * 在服务端接收到客户端的发来的数据 
		 */
		if (SERVER_SIDE.equals(side)) {
			if (message instanceof Request) {
				Request request = (Request) message;
				if(request.isBroken()){
					//在解码阶段，判定请求是坏，后续业务不需要执行。
					return;
				}
				if(request.isShakehands()){
					//服务端三次握手，只有建立“安全的连接”时才执行三次握手
					server_side_request_shakehands(channel,request);
				}else{
					//常规的请求应答模式会走这里，99.9%会走这里
					server_side_request(channel,request);
				}
			} else if(message instanceof Response){
				/**
				 * 服务端接收到"推送数据"的反馈结果. 正常情况下,应该返回一个"OK"字符串,表示客户端确认成功收到了推送的数据.
				 * 是请求响应模式
				 */
				Response response=(Response) message;
				server_side_response(channel,response);
			}else{
				//服务端接收到telnet连接发来的请求
				server_side_telnet(channel, message);
			}
		}
		
		/**
		 * 在客户端接收到服务的发来的数据 
		 */
		if (CLIENT_SIDE.equals(side)) {
			if (message instanceof Request) {
				/**
				 * 在客户端,接收到服务推送回来的数据. 客户端调用事先注册回调函数来处理接收到的数据,处理完成后向服务端返回一个"OK"字符串.
				 */
				Request request = (Request) message;
				client_side_request(channel,request);
			} else {
				Response response=(Response) message;
				if(message instanceof Response){
					/**
					 * 在客户端,接收到服务推返回的数据(请求响应模式). 客户端把结果赋值给等待的线程,并让线程停止阻塞,向下运行.
					 * 99.9%会走这里
					 */
					client_side_response(channel,response);
				}else{
					//不会走到这里
					logger.error("RSF逻辑错误，程序不应走到这里");
				}
			}
		}
	}
	
	//服务端三次握手(第二次握手)
	//1.Server端验证“请求签名包” ，传入（client端的系统ID，请求签名包），返回“会话密钥”，保存于内存。
	//2.AuthHelper工具使用client端系统ID，向配置管理中心下载client方的公钥,验章，并返回会话密钥。
	//3.获得应答签名包  byte[] getResponseSignPackage(Server端系统ID，会话密钥)
	//4.AuthHelper工具使用Server端系统ID, 向配置管理中心下载我方的公钥、私钥并使用私钥对会话密钥签章，返回签名包。
	//5.把应答签名包从服务端发向客户端
	private void server_side_request_shakehands(Channel channel,Request request) throws RemotingException{
		long t1=System.currentTimeMillis();
		ShakeHandsBean shb_request = (ShakeHandsBean) request.getData();
		String systemName_client=shb_request.getSystemName();//客户端系统名称
		
		//@motify 2013-06-25 liuhe 添加信任列表过滤  双向都是信任的    --start
		String systemName_server=AuthHelperProxy.getSystemName();//服务端系统名称
		try {
			TrustListHelperProxy.judgeTrust(systemName_server, systemName_client);
		} catch (Exception e1) {
			logger.error(e1.getMessage(),e1);
			server_side_request_shakehands_error_pro(e1.getMessage(),e1,channel,request);
			return;
		}
		// --end
		
		
		byte[] singPackage_request=shb_request.getSingPackage();//请求签名包
		String sessionKey_client=null;//会话密钥
		try{
			//Server端验证请求签名包，传入（client端的系统ID，请求签名包），返回“会话密钥”。
			sessionKey_client=AuthHelperProxy.authRequestSignPackage(systemName_client,singPackage_request);
			//“会话密钥”，保存于channel中。
			channel.setAttribute(Constants.SESSION_KEY,sessionKey_client);
		}catch(Exception e){
			String msg_err = "服务端执行三次握手时异常,第二握(验证请求签名包),systemName:"+systemName_client+",channel:"+channel;
			server_side_request_shakehands_error_pro(msg_err,e,channel,request);
			return;
		}
		
		
	
		if(systemName_client==null){
			String msg="加密通信-获取系统名称异常(服务端)，systemName:"+systemName_server;
			logger.error(msg);
			throw new EncryptException(msg);
		}
		byte[] singPackage_response=null;//应答签名包
		try{
			//客户端与服务端使用同一个会话密钥
			//Server端获得应答签名包  
			singPackage_response= AuthHelperProxy.getResponseSignPackage(systemName_server,sessionKey_client);
		}catch(Exception e){
			String msg_err = "服务端执行三次握手时异常,第二握(获得应答签名包),systemName:"+systemName_server+",channel:"+channel;
			server_side_request_shakehands_error_pro(msg_err,e,channel,request);
			return;
		}
		long t2=System.currentTimeMillis();
		logger.info( "服务端执行三次握手成功,第二握,耗时"+(t2-t1)+"ms,channel:"+channel);
		ShakeHandsBean shb_response=new ShakeHandsBean();
		shb_response.setSingPackage(singPackage_response);
		shb_response.setSystemName(systemName_server);
		Response response = new Response(request.getId());
		response.setShakehands(true);//握手包
		response.setSecurity(false);//握手时为非加密通信
		response.setData(shb_response);
		channel.send(response);//异步发送，不等待
	}
	
	/**
	 * 
	 * 在服务端记录错误日志，并向客户端返回错误信息
	 * @param msg_err
	 * @param e
	 * @param channel
	 * @param request
	 * @throws RemotingException
	 */
	private void server_side_request_shakehands_error_pro(String msg_err,Exception e,Channel channel,Request request) throws RemotingException{
		logger.error(msg_err,e);
		Response response = new Response();
		response.setId(request.getId());
		response.setData(null);// 当有异常发生时,data值不会被传递
		response.setStatus(Response.SHAKEHANDS_ERROR);// 握手异常，重要
		response.setErrorMsg(StringUtils.toString(e));// 传给调用者,让调用者知道发生了什么异常
		channel.send(response);//异步，不等待
	}
	
	private void server_side_request(Channel channel,Request request) throws RemotingException{
		RpcInvocation invocation = (RpcInvocation) request.getData();
		
		// 取出服务提供者
		Invoker<?> invoker = null;
		try{
			invoker=GlobalManager.protocol.getInvoker(channel, invocation);
		}catch(RemotingException e){
			//没有找到服务提供者
			Response response = new Response();
			response.setId(request.getId());
			response.setData(null);// 当有异常发生时,data值不会被传递
			response.setStatus(Response.SERVICE_NOT_FOUND);// 重要
			response.setErrorMsg(StringUtils.toString(e));// 传给调用者,让调用者知道发生了什么异常
			channel.send(response);//异步，不等待
			return;
		}
		
		//验证 加密标识是否对称
		URL url=invoker.getUrl();
		
		try{
            //检查有没有超载
            checkPayload( channel, url, request.getSize(),invocation.toString());
    	}catch(Exception e){
        	// S端发生异常时，也要响应C端,不然C端只能等待超时了。
			// S端发生的异常有两类
			// 第一类是业务异常，第二类是RSF本身异常，如线程池耗尽等等。
			// 本段代码，就是针对：第二类RSF本身异常 的处理，通知C端 S端发生异常了，不要再等待了。
			AbstractCodec.errorProce_ClientNotWait(request, channel, e,Response.DATA_TOO_LENGTH);
			//注意这里，后面的逻辑会处理is Broken 
			request.setBroken(true);
			return;
    	}
		
		String security=url.getParameter(Constants.ISSECURITY_KEY);
		if(request.isSecurity() != "true".equals(security)){
			String msg="\n客户端与服务端加密标识不对称，客户端要求"+(request.isSecurity()?"加密":"不加密")+",服务端要求"+("true".equals(security)?"加密":"不加密")+",服务端无法处理该请求。\n";
			logger.warn(msg);
			Response response = new Response();
			response.setId(request.getId());
			response.setData(null);// 当有异常发生时,data值不会被传递
			response.setStatus(Response.ENCRYPT_UNSYMMETRIC_ERROR);// 重要
			response.setErrorMsg(msg);// 传给调用者,让调用者知道发生了什么异常
			channel.send(response);//异步，不等待
			return;
		}
		
		//处理回调函数--如果有
		String findCallBacKey = invocation.getAttachment(Constants.CHANNEL_CALLBACK_KEY);
		if (findCallBacKey != null) {
			String[] keys = findCallBacKey.split("#");
			for (String key : keys) {
				// 发现回调函数,要生成代理,用于推送数据
				if(logger.isDebugEnabled()){
					logger.debug("RSF Server 发现回调函数并生成代理,callbackKey="+key);
				}
				CallBackWrap callBackWrap = (CallBackWrap) channel.getAttribute(key);
				if (callBackWrap == null) {
					// 服务端需要使用这个值,找出提供服务的invoker
					URL _url = new URL("", "", 0);// IP,端口目前没有用到
					_url = _url.addParameter(Constants.PATH_KEY, key);// 回调服务提供者Invoker的key,必要参数
					_url = _url.addParameter(Constants.IS_CALLBACK_SERVICE, true);// 标识这是回调,必要参数

					// 向客户端推送消息时，使用的Invoker
					// 这里只是做好准备，并没有推送消息。
					Invoker<CallBack> invoker_callback = new RsfInvokerCallback<CallBack>(
							CallBack.class, _url, channel);
					CallBack callback = (CallBack) GlobalManager.proxyFactory
							.getProxy(invoker_callback);
					callBackWrap = new CallBackWrap(channel, callback);
					channel.setAttribute(key, callBackWrap);// 保存
				}
			}
		}

		//本地业务方法调用 --请求应答模式
		String path = invocation.getAttachments().get(Constants.PATH_KEY);// 接口名
		String callbackKey = ClientConfig.callbackKey(path, invocation.getMethodName(),
				invocation.getParameterTypes());// 计算key
		CallBackWrap callBackWrap = (CallBackWrap) channel.getAttribute(callbackKey);
		if (callBackWrap != null) {
			CallBackTool.putPushTool(callBackWrap);// 使用之前,把工具放入线程
		}
		AddressTool.putChannel(channel);// 使用之前,把工具放入线程
		Response response = new Response();
		response.setId(request.getId());
		
		if("true".equals(security)){
			response.setSecurity(true);//是加密通信
		}
		
		RpcResult rs = invoker.invoke(invocation);//发启真正地调用，调用真实的业务处理方法---重点
		
		if(logger.isInfoEnabled()){
			String time_server=RsfInvokerClientP2p.DF.format((rs.getTime()/Constants.TIME_C ));
			logger.info("处理调用"+(request.isTwoWay()?"(同步)":"(异步)")+",收到数据包:"+request.getSize()+"Byte,业务耗时:"+time_server+"ms,目标:" +path+"#"+invocation.getMethodName()+"(),"+channel.getRemoteAddress()+"->"+channel.getLocalAddress());
		}
		if (callBackWrap != null) {
			CallBackTool.remove();// 使用完成后,把工具从线程中清除
		}
		AddressTool.remove();// 使用完成后,把工具从线程中清除
		
		//如果是双向通信，则返回处理结果
		if(request.isTwoWay()){
			Throwable e = rs.getException();
			if (e == null) {
				response.setData(rs);
			} else {
				response.setData(null);// 当有异常发生时,值不需要被传递
				response.setStatus(Response.SERVICE_ERROR);// 重要
				response.setErrorMsg(StringUtils.toString(e));// 传给调用者,让调用者知道发生了什么异常
			}
			// 向客户端返回服务器端的处理结果
			channel.send(response);
		}

	}
	private void server_side_response(Channel channel,Response response){
		DefaultFuture.received(channel,response);
	}
	/**
	 * 处理Telnet请求
	 * 
	 * 目前，返回的中文都是GBK编码，未能根据操作的使用的编码智能选择telnet响应数据的编码
	 * 
	 * @param channel
	 * @param message
	 * @throws RemotingException
	 */
	private void server_side_telnet(Channel channel, Object message) throws RemotingException{
		if(message instanceof String){
			
			Object telnetKey = channel.getAttribute(Constants.TELNET_KEY);
			if(telnetKey==null){
				//发生了telnet通信，标识这条channel是一个Telnet连接，心跳程序不对本连接进行心跳
				channel.setAttribute(Constants.TELNET_KEY,Constants.TELNET_KEY_VALUE);
			}
			
			String newLine="RSF telnet command $ ";
			String b="\n\r";
			//String b="\n\r";
			String rs="";
			try{
				if("help".equals(message)){
					rs="help       Display help information"+b;
					rs+=" version    View the RSF server version number"+b;
					rs+=" list       View the list of services"+b;
					rs+=" threadpool To check the thread pool real-time information"+b;
					rs+=" jvm        Look at the JVM information"+b;
					rs+=" stat       To view statistics"+b;
					rs+=" charset    The Telnet communication Chinese codes"+b;
					rs+=" set charset=utf-8     Set up a Telnet communication Chinese codes"+b;
				}else if("version".equals(message)){
					rs=Version.getVersion();
				}else if("list".equals(message)){
					rs=RsfProtocol.serviceListStr();
				}else if("threadpool".equals(message)){
					if(GlobalManager.executor_server!=null){
						rs+=ThreadPoolInfoUtil.getInfo((ThreadPoolExecutor) GlobalManager.executor_server);
					}
					if(GlobalManager.executor_client!=null){
						if(GlobalManager.executor_server!=null){
							rs+=b;
						}
						rs+=ThreadPoolInfoUtil.getInfo((ThreadPoolExecutor) GlobalManager.executor_client);
					}
				}else if("jvm".equals(message)){
					rs=EvnUtil.jvmInfo();
				}else if("stat".equals(message)){
					Map<String, Server> map=GlobalManager.SERVER_LIST;
					for(String key:map.keySet()){
						Server server=map.get(key);
						rs=server.statistic();
					}
				}else if("charset".equals(message)){
					Object attribute = channel.getAttribute(Constants.CHARSET_KEY);
		            if (attribute instanceof String) {
		            	rs=(String)attribute;
		            } else if (attribute instanceof Charset) {
		            	rs= ((Charset) attribute).displayName();
		            }
				}else if(((String)message).trim().indexOf("set")==0){
					//是设置属性命令，如:set charset=gbk
					String command=((String)message).trim();
					String[] arr=command.split(" ");
					if(arr==null || arr.length<2){
						rs="Error command!";
					}else{
						String[] arr2=arr[1].split("=");
						if(arr2==null || arr2.length<2){
							rs="Error command!";
						}else{
							String key=arr2[0];
							String value=arr2[1];
							if("charset".equals(key)){
								try {
									channel.setAttribute(Constants.CHARSET_KEY, Charset.forName(value));
				                } catch (Throwable t) {
				                    rs="Error command!";
				                    logger.warn(message +" is error", t);
				                }
								rs="success";
							}
						}
					}
				}else {
					rs="Error command!";
				}
				
				if(rs!=null){
					logger.info("处理telnet命令:"+message+",结果:"+rs);
					channel.send(" "+rs+b+newLine);
				}
			}catch(Exception e){
				String s="处理telnet命令:"+message+" 时异常";
				logger.info(s,e);
				try{
					channel.send(" "+s+e.toString()+b+newLine);
				}catch(Exception e1){}
			}
		}
	}
	
	private void client_side_request(Channel channel,Request request) throws RemotingException{
		RpcInvocation invocation = (RpcInvocation) request.getData();
		String is_callback_service = invocation.getAttachment(Constants.IS_CALLBACK_SERVICE);
		if ("true".equals(is_callback_service)) {
			String channel_callback_key = invocation.getAttachment(Constants.PATH_KEY);
			Invoker<?> invoker = GlobalManager.protocol.getInvoker4Callback(channel_callback_key);

			Response response = new Response();
			response.setId(request.getId());
			RpcResult rs = invoker.invoke(invocation);
			Throwable e = rs.getException();
			if (e == null) {
				rs.setValue("OK");
				response.setData(rs);
			} else {
				response.setErrorMsg(e.toString());
			}
			channel.send(response);// 向服务端返回(发出)处理结果
		}
	}
	
	private void client_side_response(Channel channel,Response response){
		DefaultFuture.received(channel, response);
	}
	
    protected void checkPayload(Channel channel,URL url, long dataSize,String serviceName) throws IOException {
    	int payload=0;
        if (url != null ) {
        	//默认的有效载荷,8M
            payload = url.getPositiveParameter(Constants.PAYLOAD_KEY, Constants.DEFAULT_PAYLOAD);
        }
        if (dataSize > payload) {
        	String msg="数据长度太大超过服务端的限定，dataSize: " + dataSize + " byte, max payload: " + payload + " byte, channel: " + channel;
        	DataTooLengthException e = new DataTooLengthException(msg);
        	logger.error(msg);
            throw e;
        }
    }
}
