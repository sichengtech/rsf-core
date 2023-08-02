/**
 * Copyright(c) 2000-2013 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 启动器 
 * 
 * @author zhaolei 2013-4-10
 */
public class RsfServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.getLogger(RsfServlet.class);
	private static ConfigLoader configLoader=null;
	public static ConfigLoader getConfigLoader(){
		return configLoader;
	}
	public static void setConfigLoader(ConfigLoader configLoader){
		if(configLoader!=null){
			RsfServlet.configLoader=configLoader;
		}
	}
	public void init(ServletConfig config) throws ServletException {
		logger.info("RSF RsfServlet 启动");
		RsfListener.start(config.getServletContext());
	}
	
	public void destroy(){
		RsfListener.stop();
    }
	
}
