package com.hc360.rsf.config;

import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;

/**
 * 以静态变量保存Spring ApplicationContext, 可在任何代码任何地方任何时候中取出ApplicaitonContext.
 * 
 */
public class RsfSpringLoader implements ApplicationContextAware {
	private static Logger logger = LoggerFactory.getLogger(RsfSpringLoader.class);
	private static boolean init=false;

	private static ApplicationContext applicationContext;
	
	private static ConfigLoader configLoader=null;
	
	/**
	 * 业务系统需要取得 ConfigLoader
	 * @return
	 */
	public static ConfigLoader getConfigLoader(){
		return configLoader;
	}
	
	private Resource[] rsfConfigLocations;
	/**
	 * 注入rsf配置文件路径 
	 * @param rsfConfigLocations
	 */
	
	public void setRsfConfigLocations(final Resource[] paths) {
		if(paths==null){
			return ;
		}
		rsfConfigLocations=new Resource[paths.length];
		System.arraycopy(paths, 0, rsfConfigLocations, 0, paths.length);
	}
	
	/**
	 * 启动RSF
	 */
	private void start(){
		try{
			if(rsfConfigLocations==null || rsfConfigLocations.length==0){
				logger.info("RsfSpringLoader 无配置文件，不执行启动");
				return ;
			}
			init=true;	
			logger.info("RsfSpringLoader 启动");
			int len = rsfConfigLocations.length;
			String[] confs = new String[len];
			for(int i=0;i<len;i++){
				try {
					Resource res = rsfConfigLocations[i];
					String path=res.getFile().getAbsolutePath();
					//System.out.println("RSF载入配置文件："+path);
					confs[i] = "file:"+path;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(confs!=null){
				for(int i=0;i<confs.length;i++){
					String path=confs[i];
					if( path.toUpperCase().startsWith(ConfigLoader.WEB_PREFIX) ){
						ApplicationContext applicationContext=getApplicationContext();
						if(applicationContext instanceof WebApplicationContext){
							WebApplicationContext webApplicationContext=(WebApplicationContext)applicationContext;
							String webPath =webApplicationContext.getServletContext().getRealPath(path); 
							confs[i]=ConfigLoader.FILE_PREFIX+webPath;
						}else{
							String msg="RsfSpringLoader 启动失败，当前使用为非web应用，无"+ConfigLoader.WEB_PREFIX;
							logger.error(msg);
							throw new RuntimeException(msg); 
						}
					}
				}
				configLoader = new ConfigLoader(confs);
				configLoader.start();
			}
		}catch(Exception e){
			//吞掉异常，让容器可以继续启动
			logger.error("RSF RsfSpringLoader 启动发生异常",e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Spring退出，释放资源
	 * 
	 * @param servletcontextevent
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	public void destroy() {
		logger.info("RsfSpringLoader 退出");
		ConfigLoader.destroy("spring容器退出");
		logger.info("RsfSpringLoader 退出完成");
	}

	/**
	 * 实现ApplicationContextAware接口的context注入函数, 将其存入静态变量.
	 */

	public void setApplicationContext(ApplicationContext applicationContext) {
		RsfSpringLoader.applicationContext = applicationContext; // NOSONAR
		//这里是重点
		if(!init){
			start();
		}
	}

	/**
	 * 取得存储在静态变量中的ApplicationContext.
	 */
	public static ApplicationContext getApplicationContext() {
		checkApplicationContext();
		return applicationContext;
	}

	/**
	 * 从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型.
	 */
	public static Object getBean(String name) {
		checkApplicationContext();
		return applicationContext.getBean(name);
	}

	/**
	 * 从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型.
	 */
	@SuppressWarnings("rawtypes")
	public static Map getBean(Class clazz) {
		checkApplicationContext();
		return applicationContext.getBeansOfType(clazz);
	}
	
	@SuppressWarnings("rawtypes")
	public static String[] getBeanNamesForType(Class clazz){
		checkApplicationContext();
		return applicationContext.getBeanNamesForType(clazz);
	}

	/**
	 * 清除applicationContext静态变量.
	 */
	public static void cleanApplicationContext() {
		applicationContext = null;
	}

	private static void checkApplicationContext() {
		if (applicationContext == null) {
			throw new IllegalStateException(
					"applicaitonContext未注入,请在spring配置文件中定义RsfSpringLoader");
		}
	}
}
