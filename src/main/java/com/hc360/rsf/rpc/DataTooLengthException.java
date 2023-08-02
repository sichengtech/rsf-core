/**
 * RpcException.java   2012-4-26
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.rpc;

/**
 * 数据太长异常
 * 数据长度太大超过服务端的限定 
 * 
 * @author zhaolei 2012-7-10
 */
public final class DataTooLengthException extends RuntimeException {

	private static final long serialVersionUID = 7815426752583648734L;


    public DataTooLengthException() {
        super();
    }

    public DataTooLengthException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataTooLengthException(String message) {
        super(message);
    }

    public DataTooLengthException(Throwable cause) {
        super(cause);
    }

}
