package com.hc360.rsf.remoting;

/**
 * 用于表示从配置中心获取配置异常
 * 
 * @author liuhe
 * @version 4.0 2013-6-25
 * @since 4.0	
 */
public class CatchConfigException extends RuntimeException {
	
	private static final long serialVersionUID = -6366574416969222562L;

	public CatchConfigException(String msg){
		super(msg);
	}
	
	public CatchConfigException(String msg,Exception e){
		super(msg,e);
	}
}
