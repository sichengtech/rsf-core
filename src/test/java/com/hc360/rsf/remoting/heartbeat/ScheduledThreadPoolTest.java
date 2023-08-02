/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting.heartbeat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * ScheduledExecutorService 延时测试
 * 
 * @author zhaolei 2012-5-23
 */
public class ScheduledThreadPoolTest {

	private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	static SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd dd:mm:ss");
	
	/**
	 * 要使用main方法执行
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		beepForTime();
	}

	/**
	 * beepForTime
	 */
	public static void beepForTime() {
		
		/**
		 * 要执行的任务
		 */
		final Runnable beeper = new Runnable() {
			int i=0;
			public void run() {
				System.out.println("beep:"+(i++)+"----"+sf.format(new Date(System.currentTimeMillis())));
				//throw new RuntimeException();
				
				//任务耗时2秒
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		


		int startTime=3;
		int each=5;
		System.out.println("任务将于"+startTime+"秒后开始,每"+each+"秒执行1次");
		
		/**
		 * 每each秒执行一次任务,两次任务的执行间隔是each秒（前面定义的变更）
		 * 
		 * 
		 * 创建并执行一个在给定初始延迟后首次启用的定期操作,后续操作具有给定的周期；
		 * 也就是将在 initialDelay 后开始执行,然后在 initialDelay+period 后执行,
		 * 接着在 initialDelay + 2 * period 后执行,依此类推。如果任务的任何一个执行遇到异常,
		 * 则后续执行都会被取消。否则,只能通过执行程序的取消或终止方法来终止该任务。
		 * 如果此任务的任何一个执行要花费比其周期更长的时间,则将推迟后续执行,但不会同时执行。
		 * 参数：
		 * command - 要执行的任务
		 * initialDelay - 首次执行的延迟时间
		 * period - 连续执行之间的周期
		 * unit - initialDelay 和 period 参数的时间单位 
		 * 
		 */
		//final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(beeper, startTime, each, TimeUnit.SECONDS);
		
		
		/**
		 * 
		 * 每each秒执行一次任务,两次任务的执行间隔是each秒（前面定义的变更）+任务执行用时
		 * 
		 * 创建并执行一个在给定初始延迟后首次启用的定期操作,
		 * 随后,在每一次执行终止和下一次执行开始之间都存在给定的延迟。
		 * 如果任务的任一执行遇到异常,就会取消后续执行。
		 * 否则,只能通过执行程序的取消或终止方法来终止该任务。 
		 * 参数：
		 * command - 要执行的任务
		 * initialDelay - 首次执行的延迟时间
		 * period - 连续执行之间的周期
		 * unit - initialDelay 和 period 参数的时间单位 
		 * 
		 */
		final ScheduledFuture<?> beeperHandle = scheduler.scheduleWithFixedDelay(beeper, startTime, each, TimeUnit.SECONDS);
		
		/**
		 * 创建并执行在给定延迟后启用的一次性操作。
		 * 
		 * 这里用于在N时间后取消任务
		 */
		scheduler.schedule(new Runnable() {
			public void run() {
				System.out.println("取消任务");
				beeperHandle.cancel(true);
				scheduler.shutdown();
			}
		}, 60, TimeUnit.SECONDS);
	}
}
