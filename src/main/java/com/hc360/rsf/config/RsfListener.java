/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 启动器
 * 
 * @author zhaolei 2012-7-2
 */
public class RsfListener implements ServletContextListener {
	private static Logger logger = LoggerFactory.getLogger(RsfListener.class);
	private static ConfigLoader configLoader=null;
	public static ConfigLoader getConfigLoader(){
		return configLoader;
	}
	public static void setConfigLoader(ConfigLoader configLoader){
		if(configLoader!=null){
			RsfListener.configLoader=configLoader;
		}
	}
	
	/**
	 * function description
	 * 
	 * @param servletcontextevent
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent servletcontextevent) {
		logger.info("RSF RsfListener 启动");
		start(servletcontextevent.getServletContext());
	}
	
	/**
	 * function description
	 * 
	 * @param servletcontextevent
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent servletcontextevent) {
		stop();
	}
	//启动方法
	protected static void start(ServletContext servletContext){
		try{
			if(configLoader==null){
				String rsfConfigFilePaths=servletContext.getInitParameter("rsfConfigFilePaths");
				if(rsfConfigFilePaths!=null){
					String paths[]=rsfConfigFilePaths.split(",");
					for(int i=0;i<paths.length;i++){
						String path=paths[i].trim();
						path=path.replaceAll("\t", "");
						path=path.replaceAll("\n", "");
						path=path.replaceAll("\r", "");
						paths[i]=path;
						if( path.toUpperCase().startsWith(ConfigLoader.WEB_PREFIX) ){
							String webPath =servletContext.getRealPath(path); 
							paths[i]=ConfigLoader.FILE_PREFIX+webPath;
						}
					}
					configLoader = new ConfigLoader(paths);
					configLoader.start();
				}else{
					String xmlPath = "classpath:rsf.xml";
					configLoader = new ConfigLoader(xmlPath);
					configLoader.start();
				}
			}
		}catch(Exception e){
			//吞掉异常，让容器可以继续启动
			logger.error("RSF RsfListener 启动发生异常",e);
			e.printStackTrace();
		}
	}
	
	protected static void stop(){
		logger.info("RSF 退出");
		AbstractConfig.destroy();
		logger.info("RSF 退出完成");
	}

}
