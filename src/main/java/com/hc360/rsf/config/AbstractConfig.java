/**
 * AbstractConfig.java   2012-4-25
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config;

import java.io.Serializable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hc360.rsf.remoting.Client;
import com.hc360.rsf.remoting.Server;

/**
 * AbstractConfig是所有Config类的抽象父类,在这里做一些公共的处理
 * 
 * @author zhaolei  2012-4-25
 */
public abstract class AbstractConfig implements Serializable {
	private static final long serialVersionUID = 2959085513666904744L;
	private static Logger logger = LoggerFactory.getLogger(AbstractConfig.class);
	 

	/**
	 * 关闭钩子<br>
	 * <br>
	 * 这里执行清理工作,如关闭连接等等<br>
	 * <br>
	 * 程序正常退出会执行关闭钩子<br>
	 * 如果程序终止(进程被杀)则无法保证是否能运行关闭钩子<br>
	 */
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
            	//System.out.println("--执行关闭钩子,执行退出程序,释放资源--");
            	destroy();
            }
        }, "RsfShutdownHook"));
    }
    
    /**
     * 释放RSF所有打开的资源
     * 
     * RSF的destroy只有这一处
     */
    public static void destroy(){
    	 destroy(null);
    }
    public static void destroy(String msg){
    	if (logger.isInfoEnabled()) {
            logger.info("执行退出程序,释放资源。msg:"+msg);
            //System.out.println("执行退出程序,释放资源。");
        }
        
    	//关闭Timer定时任务
    	try{
    		GlobalManager.TIMER.close();
    	}catch(Exception e){ }
    	
    	//@modify 关闭RecoveryHandler定时任务
    	try{
    		GlobalManager.RECOVERY_HANDLER.close();
    	}catch(Exception e){ }
    	
    	//关闭Client
    	try{
    		Client client=GlobalManager.getClient();
    		if(client!=null){
    			client.close();
    		}
    	}catch(Exception e){ }
    	
    	//关闭Server
    	try{
    		Map<String, Server> server_list=GlobalManager.SERVER_LIST;
    		for(String key:server_list.keySet()){
    			Server server=server_list.get(key);
    			if(server!=null){
    				server.close();
    			}
    		}
    	}catch(Exception e){ }
    }
}
