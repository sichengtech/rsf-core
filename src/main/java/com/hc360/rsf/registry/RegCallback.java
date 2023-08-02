/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.registry;

import java.io.Serializable;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hc360.rsf.config.callback.CallBack;

/**
 * 接受注册中心推来的数据
 * 
 * @author zhaolei 2012-6-18
 */
public class RegCallback implements CallBack {
	private static Logger logger = LoggerFactory.getLogger(RegCallback.class);
	/**
	 * function description
	 * 
	 * @param data
	 * @return
	 * @see com.hc360.rsf.config.callback.CallBack#call(java.io.Serializable)
	 */
	public Object call(Serializable data) {
		if(data !=null){
			try{
				ArrayList<RegistryBean> list=(ArrayList<RegistryBean>)data;
				logger.debug("RSF客户端收到注册中心推送来的数据,服务提供者发生了变化，list长度:"+list.size());
				
				ServiceProviderList.updateServiceProviderList(list);
			}catch(Exception e){
				logger.error("RSF客户端收到注册中心推送来的数据,发生错误",e);
			}
		}
		return null;
	}
}
