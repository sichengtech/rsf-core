/**
 * Constants.java   2012-5-10
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.common;

import java.util.regex.Pattern;

/**
 * 常量类
 * 
 * @author zhaolei 2012-6-7
 */
public class Constants {
	//--------------------------------------------------------------
	
	//服务端通信协议的值
	public static final String SERVER_PROTOCOL_NAME                = "rsf";
	
    //服务端通信协议名称的key
    public static final String  PROTOCOL_KEY                       = "protocol";
    
	//注册中心--发布与订阅服务的超时时间ms
	public static final int REGISTRY_TIMEOUT					   = 5000;
	
	//服务端协议--默认IP
	 public static final String SERVER_PROTOCOL_HOST               = "localhost";

	//服务端协议--默认端口
	public static final int SERVER_PROTOCOL_PORT                   = 63634;
	
	//注册中心--默认端口
	public static final int REGISTRY_PORT						   = 63638;
	/*
	 * 服务端协议--默认端口段
	 * 如果63634被占用，按顺序尝试其它端口
	 * 旧机制使用单一端口，新机制使用一个端口段
	 * 为了兼容旧机制，SERVER_PROTOCOL_PORT常量还要保留
	 * 但优先使用SERVER_PROTOCOL_PORT_SEGMENT常量
	 */
	public static final String SERVER_PROTOCOL_PORT_SEGMENT        = "63634-63600";
	
//	//服务端协议--请求及响应数据包大小限制,单位：字节
//	public static final int SERVER_PROTOCOL_PAYLOAD                = 8 * 1024 * 1024;// 8M

	//-------------线程池相关---------------------------------------
	
	//监视JVM线程时--线程名称前缀
    public static final String  DEFAULT_THREAD_NAME                = "rsf";
    
    //线程池core_Pool_Size的 key
    public static final String  THREADS_COREPOOLSIZE_KEY           ="corePoolSize";
    //固定 线程池core_Pool_Size的值 (固定线程池)
    public static final int     THREADS_COREPOOLSIZE_FIXED         = 200;
    //缓存 线程池core_Pool_Size的值 (缓存线程池)
    public static final int     THREADS_COREPOOLSIZE_CHCHED        = 0;
    //缓存 线程池core_Pool_Size的值(混合线程池)
    public static final int     THREADS_COREPOOLSIZE_MIXED         = 100;
    
    //线程池maximumPoolSize的key
    public static final String  THREADS_MAXIMUMPOOLSIZE_KEY        = "maximumPoolSize";
	//线程池maximumPoolSize(固定线程池)
	public static final int THREADS_MAXIMUMPOOLSIZE_FIXED          = 200;
	//线程池maximumPoolSize(缓存线程池)
	public static final int THREADS_MAXIMUMPOOLSIZE_CACHED         = Integer.MAX_VALUE;
	//线程池maximumPoolSize(混合线程池)
	public static final int THREADS_MAXIMUMPOOLSIZE_MIXED          = 400;
    
    //线程池队列的key
    public static final String  QUEUES_KEY                         = "queues";
    //线程池队列大小值(固定线程池)
    public static final int     DEFAULT_QUEUES_FIXED               = 300;
    //线程池队列大小值(缓存线程池)
    public static final int     DEFAULT_QUEUES_CACHED              = 0;
    //线程池队列大小值(混合线程池)
    public static final int     DEFAULT_QUEUES_MIXED               = 100;
    
    //线程的存活时间--key
    public static final String  THREAD_ALIVE_KEY                   = "threadalive";
    //线程的存活时间--值(固定线程池)
    public static final int     THREAD_ALIVE_FIXED                 = 60 * 1000;
    //线程的存活时间--值(缓存线程池)
    public static final int     THREAD_ALIVE_CACHED                = 60 * 1000;
    //线程的存活时间--值(混合线程池)
    public static final int     THREAD_ALIVE_MIXED                 = 60 * 1000;
    
	//线程池类型名称的key
    public static final String  THREAD_NAME_KEY                    = "threadname";
    
    //线程池类型的值 (固定线程池)
    public static final String  THREADPOOL_TYPE_FIXED              = "fixed";
    //线程池类型的值(缓存线程池)
    public static final String  THREADPOOL_TYPE_CACHED             = "cached";
    //线程池类型的值(混合线程池)
    public static final String  THREADPOOL_TYPE_MIXED              = "mixed";
    
    //固定池类的线程名称前缀 
    public static final String  THREADPOOL_NAME_FIXED              = "rsf-fixed";
    //缓存池类的线程名称前缀 
    public static final String  THREADPOOL_NAME_CACHED             = "rsf-cached";
    //混合池类的线程名称前缀
    public static final String  THREADPOOL_NAME_MIXED       	   = "rsf-mixed";
    
    //方法载重量的key
    public static final String  PAYLOAD_KEY                        = "payload";
    
    //方法载重量的值--8M
    public static final int     DEFAULT_PAYLOAD                    = 8 * 1024 * 1024;
    
    
	//--------------------------------------------------------------
	
	//服务接口--默认权重
	public static final int     SERVICE_WEIGHT                     = 100;
	
	//--------------------------------------------------------------
	
	//客户端--负载均衡策略,可选值：random,roundrobin,leastactive,分别表示：随机,轮循,最少活跃调用
	public static final String  CLIENT_LOADBALANCE                 = "random";
	
    public static final String  TIMEOUT_KEY                        = "timeout";
    //客户端--请求超时 ms
    public static final int     DEFAULT_TIMEOUT                    = 3000;
    
    
    //--------------------------------------------------------------
    //纳秒转毫秒的时间倍数
    public static final double  TIME_C                             =1000000D;

    public static final String  LOADBALANCE_KEY                    = "loadbalance";
    
    //--------------------------------------------------------------
    
    //回声测试，的方法名常量
    public static final String  $ECHO                              = "$echo";
    public static final String  $ECHO_INTERFACE                    = "$echoInterface";
    
    
    
    //--------------------------------------------------------------
    // 通信层缓存 key
    public static final String  BUFFER_KEY                         = "buffer";
    // 通信层缓存值
    public static final int     DEFAULT_BUFFER_SIZE                = 8 * 1024;
    // 通信层缓存值
    public static final int     MAX_BUFFER_SIZE                    = 16 * 1024;
    // 通信层缓存值
    public static final int     MIN_BUFFER_SIZE                    = 1 * 1024;

    
	//--------------------------------------------------------------
	public static final Pattern COMMA_SPLIT_PATTERN                = Pattern.compile("\\s*[,]+\\s*");
	public static final String  GROUP_KEY                          = "group";
	public static final String  INTERFACE_KEY                      = "interface";
	public static final String  VERSION_KEY                        = "version";
	public static final String  DEFAULT_KEY_PREFIX                 = "default.";
	public static final String  ANYHOST_KEY                        = "anyhost";
	public static final String  ANYHOST                            = "0.0.0.0";
    public static final String  IS_CALLBACK_SERVICE                = "is_callback_service";
    //channel中callback的invokers的key
    public static final String  CHANNEL_CALLBACK_KEY               = "channel.callback.invokers.key";
    public static final String  DEFAULT_LOADBALANCE                = "random";
    public static final int     DEFAULT_WEIGHT                     = 100;
    public static final String  REGISTRY_KEY                       = "registry";
    public static final String  APPLICATION_KEY                    = "application";
    public static final String  WEIGHT_KEY                         = "weight";
    public static final String  TOKEN_KEY                          = "token";
    public static final String  CHARSET_KEY                        = "charset";
    public static final String  PATH_KEY                           = "path";
    public static final String  RSF_VERSION_KEY                    = "rsf";
    public static final String  REGISTRY_SEPARATOR                 = "|";
    
    
  //------------------------加密通信  ------------------------------------------
    //是否是加密通信 ( key)
    public static final String ISSECURITY_KEY	 				= "isSecurity";
    
    //会话密钥 保存的 key
    public static final String SESSION_KEY	 					= "session_key";
    //三次握手--请求超时 ms
    public static final int    SHAKEHANDS_TIMEOUT               = 10*1000;
    
    //客户端被单独说明的方法集合  的  key
    public static final String    CLIENT_METHOD_LIST_INFO       = "method_list_info";
    
    public static final String TELNET_KEY 						= "telnet_key";
    public static final String TELNET_KEY_VALUE 				= "telnet_key_value";
    
    //回声测试
    public static final int ECHO_TIMEOUT = 3000;
    
    //每个服务在不可用列表中存放的数量
    public static final int IDLE_NUM_PER_SERVICE = 10;
}
