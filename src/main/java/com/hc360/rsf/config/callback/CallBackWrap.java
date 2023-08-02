/**
 * CallBackWrap.java   2012-4-26
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.callback;

import java.net.InetSocketAddress;
import com.hc360.rsf.remoting.Channel;

/**
 * CallBackWrap
 * 
 * @author zhaolei 2012-4-26
 */
public class CallBackWrap {
	private Channel channel;
	
	private CallBack callback;
	
	public CallBackWrap(Channel channel,CallBack callback){
		this.channel=channel;
		this.callback=callback;
	}
	
	public InetSocketAddress getLocalAddress() {
		return channel.getLocalAddress();
	}

	public InetSocketAddress getRemoteAddress() {
		return channel.getRemoteAddress();
	}
	
	public boolean isConnected(){
		if(channel!=null){
			return channel.isConnected();
		}else{
			return false;
		}
	}

	public CallBack getCallback() {
		return callback;
	}
}
