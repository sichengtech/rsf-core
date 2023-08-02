/**
 * CallBackHelper.java   2012-5-10
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.callback;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hc360.rsf.config.callback.PushResult.ChannelState;

/**
 * CallBackHelper
 * 
 * @author zhaolei 2012-5-10
 */
public class CallBackHelper {
	private static Logger logger = LoggerFactory.getLogger(CallBackHelper.class);
	/**
	 * CallBack全局上下文,供服务端使用
	 */
	public static final Map<String, List<CallBackWrap>> CALLBACK_MAP = new ConcurrentHashMap<String, List<CallBackWrap>>();

	/**
	 * 把当前的CallBack与一个key关联
	 * 
	 * @param key
	 */
	public static void put(String key) {
		if (key == null || "".equals(key.trim())) {
			return;
		}
		final CallBackWrap callBackWrap = CallBackTool.getPushTool();
		if(callBackWrap==null){
			return ;
		}

		List<CallBackWrap> list = CALLBACK_MAP.get(key);
		//99%不会进入下面的IF体
		if (list == null) {
			synchronized (CallBackHelper.class) {
				list = CALLBACK_MAP.get(key);
				if (list == null) {
					/**
					 * 不使用CopyOnWriteArrayList 会报 不能修改集合异常
					 * 
					 * 由于写操作远小于读操作,使用CopyOnWriteArrayList性能上可接受
					 */
					list = new CopyOnWriteArrayList<CallBackWrap>();
					CALLBACK_MAP.put(key, list);
				}
			}
		}
		if (list.size() == 0) {
			synchronized (CallBackHelper.class) {
				if (list.size() == 0) {
					list.add(callBackWrap);
				}
			}
		} else {
			// 检查是否已经存在了相同的对象引用
			// 只有在不存相同的对象引用时,才添加,保证不重复添加
			if (!list.contains(callBackWrap)) {
				synchronized (CallBackHelper.class) {
					if (!list.contains(callBackWrap)) {
						list.add(callBackWrap);
					}
				}
			}
		}
		logger.debug("关联数据推送函数,key={},关联的连接数 ={}", key, list.size());
	}

	/**
	 * 通过ip,prot找出1个 CallBack, 并通过这个CallBack推数据
	 * 
	 * @param ip
	 * @param port
	 * @param data
	 * @return
	 */
	public static PushResult send(String ip, int port, Serializable data) {
		if (ip == null || "".equals(ip.trim())) {
			return new PushResult(false,ChannelState.INVALID_ARGUMENTS);
		}
		if (port <= 0) {
			return new PushResult(false,ChannelState.INVALID_ARGUMENTS);
		}

		List<CallBackWrap> allList = new ArrayList<CallBackWrap>(1);
		for (String kk : CALLBACK_MAP.keySet()) {
			List<CallBackWrap> list = CALLBACK_MAP.get(kk);
			for (CallBackWrap cbw : list) {
				InetSocketAddress isa = cbw.getRemoteAddress();
				if (isa.getAddress().getHostAddress() != null
						&& isa.getAddress().getHostAddress().equals(ip.trim())) {
					if (isa.getPort() == port) {
						allList.add(cbw);
						break;// 最多只能找到一个
					}
				}
			}
		}
		PushResult[] prs = send(allList,data);
		if (prs != null && prs.length > 0) {
			return prs[0];
		}
		return null;
	}

	/**
	 * 通过key,ip,prot找出1个 CallBack, 并通过这个CallBack推数据
	 * @param key
	 * @param ip
	 * @param port
	 * @param data
	 * @return
	 */
	public static PushResult send(String key, String ip, int port, Serializable data) {
		if (ip == null || "".equals(ip.trim())) {
			return new PushResult(false,ChannelState.INVALID_ARGUMENTS);
		}
		if (port <= 0) {
			return new PushResult(false,ChannelState.INVALID_ARGUMENTS);
		}
		if (key == null || "".equals(key.trim())) {
			return new PushResult(false,ChannelState.INVALID_ARGUMENTS);
		}
		List<CallBackWrap> list = CALLBACK_MAP.get(key);

		List<CallBackWrap> allList = new ArrayList<CallBackWrap>(1);
		for (CallBackWrap cbw : list) {
			InetSocketAddress isa = cbw.getRemoteAddress();
			if (isa.getAddress().getHostAddress() != null
					&& isa.getAddress().getHostAddress().equals(ip.trim())) {
				if (isa.getPort() == port) {
					allList.add(cbw);
					break;// 最多只能找到一个
				}
			}
		}
		PushResult[] prs = send(allList,data);
		if (prs != null && prs.length > 0) {
			return prs[0];
		}
		return null;
	}

	/**
	 * 通过key找出多个 CallBack, 并通过这些CallBack推数据
	 * 
	 * @param key
	 * @param data
	 * @return
	 */
	public static PushResult[] send(String key, Serializable data) {
		if (key == null || "".equals(key.trim())) {
			return new PushResult[0];
		}
		List<CallBackWrap> list = CALLBACK_MAP.get(key);
		return send(list,data);
	}

	private static PushResult[] send(List<CallBackWrap> list,Serializable data) {
		List<PushResult> rsList = new ArrayList<PushResult>();
		if (list != null) {
			for(int i=0;i<list.size();i++){
			//for (CallBackWrap cbwr : list) {
				CallBackWrap cbwr=list.get(i);
				if (!cbwr.isConnected()) {
					// 查检推送工具集合中,是否存在连接已关闭的推送工具
					// 并移除他,连接闭关不用返回了
					list.remove(cbwr);
					i--;
					//System.out.println("-------777-----------------");
				} else {
					CallBack callBack;
					callBack = cbwr.getCallback();// 注意：是同步发送
					InetSocketAddress remoteAddress = cbwr.getRemoteAddress();
					try {
						Object rs = callBack.call(data);// 推送数据
						PushResult pushResult = new PushResult();
						pushResult.setSuccess("OK".equals(rs));
						pushResult.setIp(remoteAddress.getAddress().getHostAddress());
						pushResult.setPort(remoteAddress.getPort());
						if (!pushResult.isSuccess()) {
							if (rs instanceof Exception) {
								pushResult.setCauseFailing(PushResult.ChannelState.EXCEPTION); // 接收端异常
							}
						}
						rsList.add(pushResult);
					} catch (RuntimeException e) {
						// 超时
						PushResult pushResult = new PushResult();
						pushResult.setSuccess(false);
						pushResult.setIp(remoteAddress.getAddress().getHostAddress());
						pushResult.setPort(remoteAddress.getPort());
						pushResult.setCauseFailing(PushResult.ChannelState.TIMEOUT); // 超时
						rsList.add(pushResult);
					}
				}
			}
		}
		return rsList.toArray(new PushResult[0]);
	}

	public static List<CallBackWrap> get(String key) {
		return CALLBACK_MAP.get(key);
	}
}
