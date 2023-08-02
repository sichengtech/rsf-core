/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config;

import org.w3c.dom.Node;

/**
 * RSF配置文件编写异常类 
 * 
 * @author zhaolei 2012-6-7
 */
public class RsfConfigFileException extends RuntimeException {
	private static final long serialVersionUID = -6890548936049019619L;
	private String detailMessage;
	private String q="RSF配置文件编写不正确,";
	public RsfConfigFileException(Node node,String msg,Exception e){
		super(e);
		if(msg==null && node==null){
			detailMessage=q;
		}else if(msg!=null && node==null){
			detailMessage=q+msg;
		}else if(msg==null && node!=null){
			detailMessage=q+node.getNodeName()+"元素,"+(msg==null?"":msg);
		}else{
			detailMessage=q+node.getNodeName()+"元素,"+(msg==null?"":msg);
		}
	}
	public RsfConfigFileException(Node node,String msg){
		if(msg==null && node==null){
			detailMessage=q;
		}else if(msg!=null && node==null){
			detailMessage=q+msg;
		}else if(msg==null && node!=null){
			detailMessage=q+node.getNodeName()+"元素,"+(msg==null?"":msg);
		}else{
			detailMessage=q+node.getNodeName()+"元素,"+(msg==null?"":msg);
		}
	}
	public RsfConfigFileException(String msg){
		if(msg==null){
			detailMessage=q;
		}else{
			detailMessage=q+msg;
		}
	}
	
	public String getMessage() {
        return detailMessage;
    }
}
