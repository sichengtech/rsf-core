/**
 * TimeoutException.java   2012-5-10
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.remoting;

import java.net.InetSocketAddress;

/**
 * TimeoutException
 * 
 * @author zhaolei 2012-5-11
 */
public class TimeoutException extends RemotingException {

    private static final long serialVersionUID = 3122966731958222692L;
    
    public static final int CLIENT_SIDE = 0;//超时发生在客户端
    
    public static final int SERVER_SIDE = 1;//超时发生在服务端

    private final int       phase;

    public TimeoutException(boolean serverSide, Channel channel, String message){
        super(channel, message);
        this.phase = serverSide ? SERVER_SIDE : CLIENT_SIDE;
    }

    public TimeoutException(boolean serverSide, InetSocketAddress localAddress, 
                            InetSocketAddress remoteAddress, String message) {
        super(localAddress, remoteAddress, message);
        this.phase = serverSide ? SERVER_SIDE : CLIENT_SIDE;
    }

    public int getPhase() {
        return phase;
    }

    public boolean isServerSide() {
        return phase == 1;
    }

    public boolean isClientSide() {
        return phase == 0;
    }

}
