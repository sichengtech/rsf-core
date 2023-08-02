/**
 * Request.java   2012-4-12
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting.exchange.support;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * rsf协议--请求头的java语言封装类
 * 
 * Request 封装一次请求<br>
 * <br>
 * 请求类型:<br>
 * 1:常规业务请求<br>
 * 2:心跳请求<br>
 * <br>
 * 本类不会被直接序列化,是通过rsf协议封装后传输的<br>
 * 
 * @author zhaolei 2012-4-12
 */
public class Request implements Serializable{
	private static final long serialVersionUID = 1L;
	/**
	 * 心跳事件的常量
	 */
	public static final String HEARTBEAT_EVENT = null;

	/**
	 * 生成当前JVM内唯一的ID,用于异步转同步<br>
	 * 由transient修饰,不会被序列化.<br>
	 */
	private static final transient AtomicLong INVOKE_ID = new AtomicLong(0);
	
    /**
     * 版本
     */
    private String  version;

    /**
     * 双向
     */
    private boolean twoWay   = true;
    
    /**
     * broken
     */
    private boolean broken   = false;

	/**
	 * 事件
	 */
	private boolean event = false;

	/**
	 * 唯一标识
	 */
	private final long id;

	/**
	 * 封装真实的传输数据,如返回结果
	 */
	private Object data;

	/**
	 * 返回的异常信息
	 */
	private Throwable error;
	
	/**
	 * 数据包大小 ，字节
	 * 请求“包体”的数据长度
	 * 本属性，由transient关键修饰，不参与序列化。客户端向服务端发起请求，时并不携带这个值。
	 * 当服务端收请求后，计算出“包体”的长度，并为本属性设置值，然后执行后续业务逻辑。
	 * 在后续业务逻辑中，判断请求的“包体”的长度，是否是超过了，某个服务接口的对“包体”在限制。
	 */
	private transient int size;
	
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
	public Request() {
		this.id = generateId();
	}

	/**
	 * 构造方法
	 * @param id
	 */
	public Request(long id) {
		this.id = id;
	}

    /**
     * toString()
     * @return
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Request [id=" + id + ", version=" + version + ", twoway=" + twoWay + ", event=" + event
               + ", broken=" + broken + ", data=" + (data == this ? "this" : data) + "]";
    }

	/**
	 * 生成ID,getAndIncrement()增长到MAX_VALUE时,再增长会变为MIN_VALUE,负数也可以做为ID
	 * @return long
	 */
	private long generateId() {
		return INVOKE_ID.getAndIncrement();
	}
	
	/**
	 * 是否是心跳<br>
	 * mEvent为真,并且mData中没有用户的数据,就可以认定为这是一次心跳请求<br>
	 * @return
	 */
	public boolean isHeartbeat() {
		return event && HEARTBEAT_EVENT == data;
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

	public long getId() {
		return id;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Throwable getError() {
		return error;
	}

	public void setError(Throwable error) {
		this.error = error;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isTwoWay() {
		return twoWay;
	}

	public void setTwoWay(boolean twoWay) {
		this.twoWay = twoWay;
	}

	public boolean isBroken() {
		return broken;
	}

	public void setBroken(boolean broken) {
		this.broken = broken;
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
