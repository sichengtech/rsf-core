/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.node3.xml;

import java.util.concurrent.atomic.AtomicLong;

import com.hc360.rsf.config.ConfigLoader;
import com.hc360.rsf.config.node1.data.UserBean;
import com.hc360.rsf.config.node1.data.UserService;

/**
 * 
 * 三节点之间通信测试，Client、Server、注册中心之间通信测试
 * 测试前请保证“注册中心”可以访问
 * 
 * 模拟服务调用者（客户端）的多线程调用 
 * 
 * @author zhaolei 2012-6-27
 */
public class ClientThreadTest {

	/**
	 * 加载rsf_client.xml配置文件
	 * 
	 * 从注册下载服务提供者列表
	 * 
	 * 发起调用
	 *  
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		String xmlPath = "classpath:com/hc360/rsf/config/node3/xml/rsf_client.xml";
		ConfigLoader configLoader = new ConfigLoader(xmlPath);
		configLoader.start();
		// 请缓存userService对象，
		final UserService userService= (UserService) configLoader.getServiceProxyBean("clientUserServiceImpl");//配置文件中的id
		
		int thread_size=50;//并发数量
		final int length=500;//每并发执行的次数
		final AtomicLong atom=new AtomicLong(0);
		long t1=System.currentTimeMillis();
		Thread[] arr=new Thread[thread_size];
		
		//线程数
		for(int i=0;i<thread_size;i++){
			Thread t=new Thread() {
				public void run() {
					//每线程循环数
					for(int i=0;i<length;i++){
						try {
							atom.incrementAndGet();
							String s=userService.findData(""+i);
						} catch (Exception e) {
							//e.printStackTrace();
							throw new RuntimeException(e);
						}
					}
				}
			};
			t.start();
			arr[i]=t;
		}

		//main线程的等待子线程执行完任务
		for(int i=0;i<thread_size;i++){
			try {
				
				arr[i].join();
			} catch (InterruptedException e) {
				System.out.println("main线程的等待子线程执行完任务，但被打断了");
				e.printStackTrace();
			}

		}
		long t2=System.currentTimeMillis();
		
		System.out.println("================================");
		System.out.println("main线程执行完任务,并发数量="+thread_size+",每并发执行的次数="+length+",耗时="+(t2-t1));
		System.out.println("================================");
		System.out.println("发出调用次数："+atom.get());
		
		Thread.sleep(5*1000);
		System.out.println("============关闭================");
		ConfigLoader.destroy();
	}
}
