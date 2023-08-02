/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.node1.push.data;

import java.io.Serializable;

import com.hc360.rsf.config.callback.CallBack;

/**
 * (描述类的用途) 
 * 
 * @author zhaolei 2012-6-8
 */
public class CallBack_findData implements CallBack {

	public Object call(Serializable data) {
		System.out.println("客户端:收到服务端推送来的数据:"+data+",方法名：findData");
		return null;
	}

}
