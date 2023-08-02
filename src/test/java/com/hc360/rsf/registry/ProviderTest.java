package com.hc360.rsf.registry;

import junit.framework.Assert;

import org.junit.Test;

public class ProviderTest {
	@Test
	public void t1(){
		Provider p1=new Provider();
		Provider p2=new Provider();
		Assert.assertEquals(true, p1.equals(p2));
	}
	
	@Test
	public void t2(){
		Provider p1=new Provider();
		p1.setServiceName("abc.UserService");
		p1.setGroupName("groupName");
		p1.setIp("ip");
		p1.setPort(100);
		p1.setVersion("version");
		
		Provider p2=new Provider();
		p2.setServiceName("abc.UserService");
		p2.setGroupName("groupName");
		p2.setIp("ip");
		p2.setPort(100);
		p2.setVersion("version");
		
		Assert.assertEquals(true, p1.equals(p2));
	}
	
	@Test
	public void t3(){
		Provider p1=new Provider();
		p1.setServiceName("abc.UserService");
		p1.setIp("ip");
		p1.setPort(100);
		
		Provider p2=new Provider();
		p2.setServiceName("abc.UserService");
		p2.setIp("ip");
		p2.setPort(100);
		
		Assert.assertEquals(true, p1.equals(p2));
	}
	
	@Test
	public void t4(){
		Provider p1=new Provider();
		p1.setServiceName("abc.UserService1");
		p1.setIp("ip");
		p1.setPort(100);
		
		Provider p2=new Provider();
		p2.setServiceName("abc.UserService2");
		p2.setIp("ip");
		p2.setPort(100);
		
		Assert.assertEquals(false, p1.equals(p2));
	}
	
	@Test
	public void t5(){
		Provider p1=new Provider();
		p1.setServiceName("abc.UserService1");
		p1.setIp("ip1");
		p1.setPort(100);
		
		Provider p2=new Provider();
		p2.setServiceName("abc.UserService2");
		p2.setIp("ip2");
		p2.setPort(100);
		
		Assert.assertEquals(false, p1.equals(p2));
	}

}
