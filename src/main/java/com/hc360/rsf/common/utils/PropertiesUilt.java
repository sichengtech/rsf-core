/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 读取Properties属性文件
 * 
 * @author zhaolei 2012-5-21
 */
public class PropertiesUilt {
	/**
	 * RSF框架默认的配置文件
	 */
	private static Properties propDefault = null;

	/**
	 * 用户修改后的配置文件
	 */
	private static Properties propUser = null;

	/**
	 * isInit
	 */
	private static boolean isInit = false;

	/**
	 * 读取配置文件中的某项值
	 */
	public static String getValue(String key) {
		if (!isInit) {
			try {
				init();
			} catch (IOException e) {
				throw new RuntimeException("RSF启动时读配置文件异常", e);
			}
			isInit = true;
		}
		if (key == null) {
			throw new NullPointerException("读取配置文件中的某项值异常,key=null");
		}
		String value = null;
		if (propUser != null) {
			value = propUser.getProperty(key);// 先读用户的配置文件
		}
		if (value == null && propDefault != null) {
			value = propDefault.getProperty(key);// 后读RSF框架默认的配置文件
		}
		return value;
	}

	/**
	 * init
	 * 
	 * @throws IOException
	 */
	public static synchronized void init() throws IOException {
		if (!isInit) {
			ClassLoader classLoader = ClassHelper.getClassLoader();
			InputStream inputDefault = classLoader.getResourceAsStream("com/hc360/rsf/config/rsf.properties");
			InputStream inputUser = classLoader.getResourceAsStream("rsf.properties");
			if (inputDefault != null) {
				propDefault = new Properties();
				propDefault.load(inputDefault);
			}
			if (inputUser != null) {
				propUser = new Properties();
				propUser.load(inputUser);
			}
		}
	}
}
