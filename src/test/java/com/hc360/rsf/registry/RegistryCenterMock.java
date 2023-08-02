/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.registry;

import com.hc360.rsf.config.ProtocolConfig;
import com.hc360.rsf.config.ServiceConfig;
import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.ChannelPoolVirtual;
import com.hc360.rsf.remoting.heartbeat.HeartbeatListener;

/**
 * 模拟注册中心,用于开发
 * 
 * 启动模拟注册中心,工作在63638端口,接受服务注册,与服务订阅
 * 
 * @author zhaolei 2012-6-12
 */
public class RegistryCenterMock {

	/**
	 * 启动模拟注册中心
	 * @param args
	 */
	public static void main(String[] args) {
		RegistryService registryService=new RegistryServiceImpl();
		
		ProtocolConfig protocol=new ProtocolConfig();
		protocol.setName("rsf");
		protocol.setPort(63638);
		
		ServiceConfig<RegistryService> server_cfg=new ServiceConfig<RegistryService>();
		server_cfg.setDisplayName("注册中心");
		server_cfg.setDepartment("MMT开发部");
		server_cfg.setOwner("赵磊");
		server_cfg.setDucment("服务说明:注册中心测试");
		server_cfg.setInterfaceClass(RegistryService.class);
		server_cfg.setRef(registryService);
		server_cfg.setProtocol(protocol);
		server_cfg.export();//暴露
		
		HeartbeatListener heartbeatListener=new HeartbeatListener(){
			public void success(Channel channel) {
				System.out.println("心跳成功:"+channel);
			}
			public void fail(Channel channel) {
				System.out.println("心跳失败:"+channel);
			}
			public void start(){
				System.out.println("一轮心跳开始");
			}
		};
		
		//创建一个连接池,其中包含本端所有连接,
		//池具有心跳能力,具有心跳事件通知能力
		ChannelPoolVirtual pool=new ChannelPoolVirtual(heartbeatListener);
	}
}
