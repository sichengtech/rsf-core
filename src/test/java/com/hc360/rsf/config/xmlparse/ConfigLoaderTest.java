/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.xmlparse;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.hc360.rsf.common.Constants;
import com.hc360.rsf.config.ClientConfig;
import com.hc360.rsf.config.ClientMethodConfig;
import com.hc360.rsf.config.ConfigLoader;
import com.hc360.rsf.config.ProtocolConfig;
import com.hc360.rsf.config.RegistryConfig;
import com.hc360.rsf.config.ServiceConfig;
import com.hc360.rsf.config.node1.data.UserService;

/**
 * ConfigLoader Test
 * 
 * @author zhaolei 2012-6-7
 */
public class ConfigLoaderTest {
	
	/**
	 * 常规读取配置文件测试
	 */
	@Test
	public void test_read(){
		String xmlPath = "classpath:com/hc360/rsf/config/xmlparse/ConfigLoader.xml";
		ConfigLoader configLoader = new ConfigLoader(xmlPath);
		
		RegistryConfig reg1=(RegistryConfig)configLoader.getBean("reg1");
		Assert.assertEquals(reg1.getHost(), "127.0.0.1");
		Assert.assertEquals(reg1.getPort(), 63638);
		Assert.assertEquals(reg1.getTimeout(), 5000);
		
		RegistryConfig reg2=(RegistryConfig)configLoader.getBean("reg2");
		Assert.assertEquals(reg2.getHost(), "192.168.5.63");
		Assert.assertEquals(reg2.getPort(), 63638);
		Assert.assertEquals(reg2.getTimeout(), 5000);
		
		ServiceConfig userService=(ServiceConfig)configLoader.getBean("userServiceImpl");
		Assert.assertNotNull(userService);
		Assert.assertEquals(userService.getDisplayName(), "用户测试服务");
		Assert.assertEquals(userService.getOwner(), "赵磊");
		Assert.assertEquals(userService.getDepartment(), "MMT开发部");
		Assert.assertNotNull(userService.getInterfaceClass());
		Assert.assertNotNull(userService.getRef());
		Assert.assertNotNull(userService.getClass());
		Assert.assertEquals(userService.getRegistriesStr(), "reg1,reg2");
		Assert.assertTrue(userService.getIsReg());
		Assert.assertEquals(userService.getWeight(), 80);
		Assert.assertNotNull(userService.getDucment());
		
		ProtocolConfig pro=(ProtocolConfig)configLoader.getBean("protocol1");
		Assert.assertEquals(pro.getHost(), "localhost");
		//Assert.assertEquals(pro.getPort(), 63634);
		Assert.assertNotNull(pro.getPorts());//动态端口
		Assert.assertEquals(pro.getName(), "rsf");
		Assert.assertEquals(pro.getMaximumPoolSize().intValue(), 200);
		Assert.assertEquals(pro.getPayload(), Constants.DEFAULT_PAYLOAD);
		Assert.assertEquals(pro.getThreadpool(), "fixed");
		
		ClientConfig client=(ClientConfig)configLoader.getBean("clientUserServiceImpl");
		Assert.assertEquals(client.getDisplayName(), "调用用户测试服务");
		Assert.assertEquals(client.getOwner(), "张三");
		Assert.assertEquals(client.getDepartment(), "MMT开发部");
		Assert.assertNotNull(client.getInterfaceClass());
		Assert.assertEquals(client.getTimeout(), 3000);
		Assert.assertEquals(client.getLoadbalance(), "random");
		
		List<ClientMethodConfig> list=client.getClientMethodConfigs();
		Assert.assertEquals(list.size(), 1);
		Assert.assertEquals(list.get(0).getName(), "addUser");
		Assert.assertEquals(list.get(0).getParameterTypesStr(), "com.hc360.rsf.config.node1.data.UserBean");
		Assert.assertEquals(list.get(0).getTimeout(), 5000);
		Assert.assertEquals(list.get(0).isAsync(), false);
		
		//测试二
		String xmlPath2 = "classpath:com/hc360/rsf/config/xmlparse/ConfigLoader2.xml";
		ConfigLoader configLoader2 = new ConfigLoader(xmlPath2);
		Object obj2=configLoader2.getBean("protocol3");
		System.out.println("--"+obj2);
		
		//测试三--测试加载多个配置文件
		ConfigLoader configLoader3 = new ConfigLoader(new String[]{xmlPath2,xmlPath});
		//Object obj3=configLoader3.getBean("protocol4");
		int count=configLoader3.getBeanCount();
		Assert.assertEquals(count, 10);
		System.out.println("--bean的总数量："+count);
		
		
		//取接口的代理类测试
		UserService uu=(UserService)configLoader3.getServiceProxyBean("clientUserServiceImpl");
		Assert.assertNotNull(uu);
		
		UserService userService2=configLoader3.getServiceProxyBeanT(UserService.class);
		Assert.assertTrue(userService2 instanceof UserService);
	}
}
