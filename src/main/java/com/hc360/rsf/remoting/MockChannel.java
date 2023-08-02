package com.hc360.rsf.remoting;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hc360.rsf.common.Constants;

/**
 * MockChannel
 * 
 * @author zhaolei 2012-5-23
 */
public class MockChannel implements Channel {

    private Map<String, Object> attributes = new HashMap<String, Object>();
    
    private volatile boolean closed = false;
    private List<Object> sentObjects = new ArrayList<Object>();
    
    public InetSocketAddress getRemoteAddress() {
        return null;
    }
    
    public InetSocketAddress getLocalAddress() {
        return null;
    }

    public boolean isConnected() {
        return false;
    }

    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    public void send(Object message) throws RemotingException {
        sentObjects.add(message);
    }

    public void send(Object message, boolean sent) throws RemotingException {
        sentObjects.add(message);
    }

    public void close(String msg) {
        closed = true;
    }

    public void close(int timeout) {
        closed = true;
    }

    public boolean isClosed() {
        return closed;
    }
    
    public List<Object> getSentObjects() {
        return Collections.unmodifiableList(sentObjects);
    }

	/**
	 * function description
	 * 
	 * @param message
	 * @return
	 * @throws RemotingException
	 * @see com.hc360.rsf.remoting.Channel#request(java.lang.Object)
	 */
	public Object request(Object message) throws RemotingException {
		int timeout=Constants.DEFAULT_TIMEOUT;
		return request(message,timeout);
	}
	public Object request(Object message,int timeout) throws RemotingException {
		sentObjects.add(message);
		return message;
	}
	public Object request(Object message, int timeout, boolean security) throws RemotingException {
		sentObjects.add(message);
		return message;
	}
	/**
	 * 进行三次握手
	 */
	public void shakeHands(){
		
	}

	/**
	 * function description
	 * 
	 * @return
	 * @see com.hc360.rsf.remoting.Channel#getRemoteIpPort()
	 */
	public IpPort getRemoteIpPort() {
		return null;
	}
	
	public String getKey(){
		return "";
	}

	public boolean isContainService(String serviceName)
			throws RemotingException {
		// TODO Auto-generated method stub
		return false;
	}
	
}
