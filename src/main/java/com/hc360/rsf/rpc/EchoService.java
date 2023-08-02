/**
 * EchoService.java   2012-5-10
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.rpc;
/**
 * Echo service.  回音
 */
public interface EchoService {
	
    /**
     * 回声测试,连接级测试，测试连接通不能，不管服务端有没有此服务(接口)
     * 由于“软负载”的存在， 当服务端有多个节点时，测试的是哪一个服务端节点，不可控
     * 
     * @param message 发送的测试字符串，
     * @return message. 返回的“回声”，发送的测试字符串被原样返回，表示连接是通的
     */
    Object $echo(Object message);
    
    /**
     * 回声测试，接口级测试，测试连接要通，服务端还要有此服务(接口)
     * 由于“软负载”的存在， 当服务端有多个节点时，测试的是哪一个服务端节点，不可控
     * 
     * @return boolean. 有无
     */
    boolean $echoInterface();

}
