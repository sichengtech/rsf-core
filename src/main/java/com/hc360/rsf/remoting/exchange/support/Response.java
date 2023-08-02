/**
 * Response.java   2012-4-12
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting.exchange.support;

import java.io.Serializable;

/**
 * rsf协议--响应头的java语言封装类
 * 
 * Response 封装一次响应<br>
 * <br>
 * 响应类型:<br>
 * 1:常规业务响应<br>
 * 2:心跳响应<br>
 * 本类不会被直接序列化,是通过rsf协议封装后传输的<br>
 * 
 * @author zhaolei 2012-4-12
 */
public class Response implements Serializable{
	private static final long serialVersionUID = 1L;

	//常量--心跳事件
	public static final String HEARTBEAT_EVENT = null;
	
	/////////////////////////////////////////////////
	//以下异常值比较重要，但在哪里使用了你知道吗？
	//在DefaultFuture类的returnFromResponse()方法中使用了
	//在客户端根据不同值，构造相应的异常返回给客户端的“调用者”
	//目前起的作用并不大，正在完善  --赵磊
	/////////////////////////////////////////////////
	
	//空闲--你想使用时，请修改常量名
	public static final byte 空闲0 = 0;
	public static final byte 空闲18 = 18;
	
	
	//客户端发送请求时发生了序列化异常
	//如果服务端了生了序列化异常异常  使用的是BAD_RESPONSE ，
	//目前服务端发生的所有异常，统一使用的是BAD_RESPONSE
	public static final byte CLIENT_SERIALIZABLE_ERROR = 19;
	//OK 处理正确
	public static final byte OK = 20;//1 0100
	//坏的请求
	public static final byte BAD_REQUEST = 21;
	//坏的响应
	public static final byte BAD_RESPONSE = 22;
	//服务未找到
	public static final byte SERVICE_NOT_FOUND = 23;
	//握手异常
	public static final byte SHAKEHANDS_ERROR = 24;
	//加密解密异常
	public static final byte ENCRYPT_ERROR = 25;
	//服务异常--业务异常
	public static final byte SERVICE_ERROR = 26;
	//RSF服务端异常
	public static final byte SERVER_ERROR = 27;
	//RSF客户端异常
	public static final byte CLIENT_ERROR = 28;
	//客户端与服务端加密标识不对称异常
	public static final byte ENCRYPT_UNSYMMETRIC_ERROR = 29;
	//数据长度太大超过服务端的限定
	public static final byte DATA_TOO_LENGTH = 30;//0001 1110
	//服务端超时（是服务端执行业务超时吗？目前未被使用）
	public static final byte SERVER_TIMEOUT = 31;//0001 1111
	
    /**
     * status  状态，值取自上面的常量
     */
    private byte             status  = Response.OK;//这是默认值
    /**
     * 版本
     */
    private String           version;

	/**
	 * 事件
	 */
	private boolean event = false;

	/**
	 * 生成当前JVM内唯一的ID,从Request对象中获取,用于异步转同步.
	 */
	private long id;

	/**
	 * 封装真实的传输数据,如返回结果
	 */
	private Object data;

	/**
	 * 返回的异常信息
	 */
	private String errorMsg;
	
	/**
	 * 数据包大小 ，字节
	 */
	private int size;
	
	/**
	 * 是否是加密通信，RSF1.3.0加入
	 */
	private boolean security;
	
	/**
	 * 是否是握手事件，RSF1.3.0加入
	 */
	private boolean shakehands;
	
	/**
	 * 构造方法
	 */
	public Response() {
	}

	/**
	 * 构造方法
	 * @param id
	 */
	public Response(long id) {
		this.id = id;
	}
	
	/**
	 * 构造方法
	 * @param id
	 * @param version
	 */
	public Response(long id,String version) {
		this.id = id;
		this.version=version;
	}

    @Override
    public String toString() {
    	//errorMsg 是异常发生时的调用堆栈信息,有很多行
    	//如果输出会影响日志的排版,这里不输出
    	String err=null;
    	if(errorMsg!=null){
    		//Windows Enter 
    		//Linux Enter 
    		//new byte[] { '\r', '\n' } , new byte[] { '\n' }  }
    		int index=errorMsg.indexOf("\r\n");
    		if(index==-1){
    			index=errorMsg.indexOf('\n');
    		}
    		if(index!=-1){
    			err=errorMsg.substring(0, index);
    		}else{
    			err="errorMsg省略";
    		}
    	}
        return "Response [id=" + id + ", version=" + version + ", status=" + status + ", event=" + event
               + ", error="+ err + ", result=" + (data == this ? "this" : data) + "]";
    }

	public boolean isEvent() {
		return event;
	}

	/**
	 * setEvent()
	 * @param event值应该是HEARTBEAT_EVENT
	 */
	public void setEvent(String event) {
		this.event = true;
		data = event;
	}

	/**
	 * 是否是心跳
	 * mEvent为真,并且mData中没有用户的数据,就可以认定为这是一次心跳请求
	 * @return
	 */
	public boolean isHeartbeat() {
		return event && HEARTBEAT_EVENT == data;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String mVersion) {
		this.version = mVersion;
	}

	public byte getStatus() {
		return status;
	}

	public void setStatus(byte mStatus) {
		this.status = mStatus;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public boolean isSecurity() {
		return security;
	}

	public void setSecurity(boolean security) {
		this.security = security;
	}

	public boolean isShakehands() {
		return shakehands;
	}

	public void setShakehands(boolean shakehands) {
		this.shakehands = shakehands;
	}
}
