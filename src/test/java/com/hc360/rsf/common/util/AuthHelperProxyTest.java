/**
 * Copyright(c) 2000-2013 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.common.util;

import com.hc360.rsf.common.utils.AuthHelperProxy;
import com.hc360.secret.SecretException;

/**
 * TODO(描述类的用途) 
 * 
 * @author zhaolei 2013-4-20
 */
public class AuthHelperProxyTest {

	/**
	 * TODO(描述方法的作用) 
	 * @param args
	 * @throws SecretException 
	 */
	public static void main(String[] args) throws SecretException {
		//key
		String session_key="key";
		
		//数据
		byte[] src_data=new byte[]{-111, 67, 48, 40, 99, 111, 109, 46, 104, 99, 51, 54, 48, 46, 114, 115, 102, 46, 99, 111, 110, 102, 105, 103, 46, 110, 111, 100, 101, 49, 46, 100, 97, 116, 97, 46, 85, 115, 101, 114, 66, 101, 97, 110, -109, 4, 110, 97, 109, 101, 3, 97, 103, 101, 3, 111, 98, 106, 96, 1, 49, -107, 78, 73, 0, 27, -30, 112};
		System.out.print("输入：[");
		for(byte b:src_data){
			System.out.print(b);
			System.out.print(",");
		}
		System.out.print("]");
		System.out.println();
		
		
		//加密
		byte[] c_data=AuthHelperProxy.sessionEncrypt(session_key, src_data);
		//解密
		byte[] out_data=AuthHelperProxy.sessionDecrypt(session_key, c_data);
		
		System.out.print("输出：[");
		for(byte b:out_data){
			System.out.print(b);
			System.out.print(",");
		}
		System.out.print("]");
		System.out.println();

	}

}
