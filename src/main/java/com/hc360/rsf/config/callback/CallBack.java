/**
 * CallBack.java   2012-4-26
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.callback;

import java.io.Serializable;

/**
 * 回调通用接口CallBack
 * 
 * @author zhaolei 2012-4-26
 */
public interface CallBack {
	public Object call(Serializable data);
}
