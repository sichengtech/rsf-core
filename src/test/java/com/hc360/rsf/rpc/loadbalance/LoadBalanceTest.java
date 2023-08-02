/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.rpc.loadbalance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import com.hc360.rsf.registry.Provider;
import com.hc360.rsf.rpc.RpcInvocation;

/**
 * 负载均衡测试
 * 
 * @author zhaolei 2012-5-24
 */
public class LoadBalanceTest {
	LoadBalance random=new RandomLoadBalance();//随机
	LoadBalance roundRobin=new RoundRobinLoadBalance();//轮询
	LoadBalance leastActive=new LeastActiveLoadBalance();//最少活跃
	LoadBalance consistentHash=new ConsistentHashLoadBalance();//一至HASH
	List<Provider> urls=new ArrayList<Provider>();
	RpcInvocation invocation=new RpcInvocation();
	
    @Before
    public void setup() throws Exception {
    	invocation.setMethodName("run");
    	
    	Provider url1=new Provider();//URL.valueOf("rsf://222.0.0.1:1088");
    	Provider url2=new Provider();//URL.valueOf("rsf://222.0.0.2:1088");
    	Provider url3=new Provider();//URL.valueOf("rsf://222.0.0.3:1088");
    	Provider url4=new Provider();//URL.valueOf("rsf://222.0.0.4:1088");
    	Provider url5=new Provider();//URL.valueOf("rsf://222.0.0.5:1088");
//    	url1=url1.addParameter(invocation.getMethodName()+"."+Constants.WEIGHT_KEY,10);//设置方法的权重
//    	url2=url2.addParameter(invocation.getMethodName()+"."+Constants.WEIGHT_KEY,100);//设置方法的权重
//    	url3=url3.addParameter(invocation.getMethodName()+"."+Constants.WEIGHT_KEY,100);//设置方法的权重
//    	url4=url4.addParameter(invocation.getMethodName()+"."+Constants.WEIGHT_KEY,100);//设置方法的权重
//    	url5=url5.addParameter(invocation.getMethodName()+"."+Constants.WEIGHT_KEY,100);//设置方法的权重
    	
    	url1.setIp("222.0.0.0");
    	url1.setPort(1088);
    	url1.setWeight(100);
    	url1.setServiceName("com.UserService");
    	
    	url2.setIp("222.0.0.1");
    	url2.setPort(1088);
    	url2.setWeight(100);
    	url2.setServiceName("com.UserService");
    	
    	url3.setIp("222.0.0.2");
    	url3.setPort(1089);
    	url3.setWeight(100);
    	url3.setServiceName("com.UserService");
    	
    	url4.setIp("222.0.0.3");
    	url4.setPort(10890);
    	url4.setWeight(100);
    	url4.setServiceName("com.UserService");
    	
    	url5.setIp("222.0.0.4");
    	url5.setPort(1091);
    	url5.setWeight(100);
    	url5.setServiceName("com.UserService");
    	
    	urls.add(url1);
    	urls.add(url2);
    	urls.add(url3);
    	urls.add(url4);
    	urls.add(url5);
    }
	
	/**
	 * 测试RandomLoadBalance的散落情况
	 */
	@Test
	public void test_RandomLoadBalance(){
		common(random,invocation);
	}
	@Test
	public void test_RoundRobinLoadBalance(){
		common(roundRobin,invocation);
	}
	@Test
	public void test_LeastActiveLoadBalance(){
		common(leastActive,invocation);
	}
	@Test
	public void test_ConsistentHashLoadBalance(){
		common(consistentHash,invocation);
	}
//	@Test
//	public void tt(){
//		failOver(urls,invocation);
//		System.out.println("----------");
//	}
//	private Channel failOver(List<URL> urls,RpcInvocation invocation){
//		URL url_select = GlobalManager.loadBalance.select(urls, invocation);
//		Client client = GlobalManager.getClient();
//		Channel channel = client.getChannel(url_select);
//		while( (channel==null || !channel.isConnected()) &&  urls.size()>0 ){
//			urls.remove(url_select);
//			channel=failOver(urls,invocation);//连接不可用,转移到下一个
//		}
//		return channel;
//	}
	
	private void common(LoadBalance loadbalance,RpcInvocation invocation){
		Map<String ,Integer> map=new HashMap<String ,Integer>();
		for(int i=0;i<100;i++){
			//一致hash算法,根据方法+参数hash
			//生成不同的实参
			invocation.setArguments(new Object[]{i});
			Provider url=loadbalance.select(urls, invocation);
			Integer value=map.get(url.getIp());
			if(value==null){
				value=new Integer(1);
				map.put(url.getIp(), value);
			}else{
				map.put(url.getIp(), value+1);
			}
		}
		Integer count=0;
		System.out.println("--开始测试,方法："+loadbalance.getClass().getSimpleName()+"--");
		for(String key:map.keySet()){
			Integer value=map.get(key);
			count=count+value;
			System.out.println(key+"  :  "+value+"次");
		}
		System.out.println("--总数："+count+"次--");
	}

}
