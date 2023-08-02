/**
 * Copyright(c) 2000-2013 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.rpc.protocol.codec;

/**
 * TODO(描述类的用途) 
 * 
 * @author zhaolei 2013-3-14
 */
public class ExchangeCodecTest {

	// message flag.--请求
    protected static final byte     FLAG_REQUEST       = (byte) 0x80;//128 二进制 10000000
    
    public static final byte ID = 16;
    
	public static void main(String[] args) {
		 // 16字节协议头 ，长度为16字节的空byte数组
        byte[] header = new byte[16];
        
        
        
        header[2] = (byte) (FLAG_REQUEST | ID);
        
        System.out.println(header[2]);

	}

}
