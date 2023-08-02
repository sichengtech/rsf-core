/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.hc360.rsf.common.Constants;
import com.hc360.rsf.common.URL;
import com.hc360.rsf.config.GlobalManager;
import com.hc360.rsf.config.ProtocolConfig;
import com.hc360.rsf.config.ServiceConfig;
import com.hc360.rsf.config.node1.data.UserService;
import com.hc360.rsf.config.node1.data.UserServiceImpl;
import com.hc360.rsf.registry.Provider;
import com.hc360.rsf.registry.ServiceProviderList;
import com.hc360.rsf.remoting.transport.mina.MinaClient;

/**
 * Channel Pool Test
 * 
 * @author zhaolei 2012-6-16
 */
public class ChannelPoolTest {
	

	/**
	 * 用main方法更合适,请观察控制台的输出
	 * @param a
	 * @throws InterruptedException
	 */
	public static void main(String[] a) throws InterruptedException{
		//准备3个服务端,可以接受连接
		service();
		
		//创建3个URL
		List<URL> url_list=new ArrayList<URL>();
		Map<String,String> parameters=new HashMap<String,String>();
		parameters.put(Constants.PATH_KEY, "com.hc360.rsf.UserService");
		url_list.add(new URL("rsf","127.0.0.1",10001,"com.hc360.rsf.UserService",parameters));
		url_list.add(new URL("rsf","127.0.0.1",10002,"com.hc360.rsf.UserService",parameters));
		url_list.add(new URL("rsf","127.0.0.1",10003,"com.hc360.rsf.UserService",parameters));
		
		//把3个URL添加到服务列表
		for(URL uu:url_list){
			Provider p=new Provider();
			p.setIp(uu.getIp());
			p.setPort(uu.getPort());
			p.setServiceName(uu.getPath());
			ServiceProviderList.addNode(p);
//			ServiceProviderList.addServiceList(url_list);
		}
		
		//client创建时,会创建连接池,建连接池中的“创建连接线程”会马上依据服务列表创建3个连接
		MinaClient client =(MinaClient) GlobalManager.getClient();
		
		//休息一会,让心跳线程运行一会
		Thread.sleep(10*1000);
		
		//停掉服务,连接断开了
		Map<String, Server> server_list = GlobalManager.SERVER_LIST;
		for(String key:server_list.keySet()){
			Server server=server_list.get(key);
			server.close();
		}
		
		//休息一会,让创建连接线程运行一会,创建连接一直失败
		Thread.sleep(60*1000);
		
		//准备3个服务端,可以接受连接,创建连接将会成功
		service();
		
		Thread.sleep(40*1000);
		URL u=new URL("rsf","127.0.0.1",10004,"com.hc360.rsf.UserService",parameters);
		url_list.add(u);
		//把1个URL添加到服务列表,查看会不会主动新建连接
		for(URL uu:url_list){
			Provider p=new Provider();
			p.setIp(uu.getIp());
			p.setPort(uu.getPort());
			p.setServiceName(uu.getPath());
			ServiceProviderList.addNode(p);
		}
		
		//休息一会
		Thread.sleep(40*1000);
		Channel channel=client.getOrCreateChannel(u.getIp(),u.getPort());
		channel.close("");
		
		//从服务列表中减一个URL 
		Provider p2=new Provider();
		p2.setIp(u.getIp());
		p2.setPort(u.getPort());
		p2.setServiceName(u.getPath());
		ServiceProviderList.romveNode(p2);
		//停止相应的服务端,查连接的变化
		server_list = GlobalManager.SERVER_LIST;
		for(String key:server_list.keySet()){
			Server server=server_list.get(key);
			InetSocketAddress addr=server.getBindAddress();
			if(addr.getPort()==u.getPort()){
				server.close();//关闭一个服务端
			}
		}
		
		//休息一会
		Thread.sleep(10*1000);
		
	}
	
	private static void service(){
		//服务接口的实现类
		UserService userService=new UserServiceImpl();
		
		//启动一个服务端
		ProtocolConfig protocol=new ProtocolConfig();
		protocol.setPort(10001);
		protocol.setName("rsf");
		ServiceConfig<UserService> server=new ServiceConfig<UserService>();
		server.setDisplayName("测试服务1");
		server.setDepartment("MMT开发部");
		server.setOwner("赵磊");
		server.setDucment("服务说明:提供测试功能1");
		server.setInterfaceClass(UserService.class);
		server.setRef(userService);
		server.setProtocol(protocol);
		server.export();//暴露
		
		//启动一个服务端
		ProtocolConfig protocol2=new ProtocolConfig();
		protocol2.setPort(10002);
		protocol2.setName("rsf");
		ServiceConfig<UserService> server2=new ServiceConfig<UserService>();
		server2.setDisplayName("测试服务2");
		server2.setDepartment("MMT开发部");
		server2.setOwner("赵磊");
		server2.setDucment("服务说明:提供测试功能2");
		server2.setInterfaceClass(UserService.class);
		server2.setRef(userService);
		server2.setProtocol(protocol2);
		server2.export();//暴露
		
		//启动一个服务端
		ProtocolConfig protocol3=new ProtocolConfig();
		protocol3.setPort(10003);
		protocol3.setName("rsf");
		ServiceConfig<UserService> server3=new ServiceConfig<UserService>();
		server3.setDisplayName("测试服务3");
		server3.setDepartment("MMT开发部");
		server3.setOwner("赵磊");
		server3.setDucment("服务说明:提供测试功能3");
		server3.setInterfaceClass(UserService.class);
		server3.setRef(userService);
		server3.setProtocol(protocol3);
		server3.export();//暴露
		
		//启动一个服务端
		ProtocolConfig protocol4=new ProtocolConfig();
		protocol4.setPort(10004);
		protocol4.setName("rsf");
		ServiceConfig<UserService> server4=new ServiceConfig<UserService>();
		server4.setDisplayName("测试服务3");
		server4.setDepartment("MMT开发部");
		server4.setOwner("赵磊");
		server4.setDucment("服务说明:提供测试功能3");
		server4.setInterfaceClass(UserService.class);
		server4.setRef(userService);
		server4.setProtocol(protocol4);
		server4.export();//暴露
	}
}
