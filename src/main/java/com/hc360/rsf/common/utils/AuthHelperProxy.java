package com.hc360.rsf.common.utils;

import java.util.UUID;
import com.hc360.secret.AuthHelper;
import com.hc360.secret.SecretException;

/**
 * AuthHelperProxy是AuthHelper的代理类。
 * 
 * AuthHelper是用于加密通信的加密工具类，从RSF 1.3.0开始加入的新功能。
 * 为了达到，只有使用到加密通信功能时，才依赖AuthHelper类。不使用加密功能时，可以不引入AuthHelper所在的jar包的目的，
 * AuthHelperProxy类旦生了，用于起到“类加载隔离”的作用。
 * 
 * @author zhaorai
 *
 */
public class AuthHelperProxy {
	
	//调度时使用的模拟值
	private static boolean isDebug=false;
	
	/**
	 * 生成一个不重复的字符串，做为会话密钥。
	 * @return 会话密钥
	 */
	public static String  generateSessionKey(){
		if(isDebug){
			return "123456";
		}else{
			return UUID.randomUUID().toString();
		}
	}
	
	/**
	 * 获取业务系统的名称
	 * @return
	 */
	public static String getSystemName(){
		if(isDebug){
			return "sso";
		}else{
			return  AuthHelper.getSysId();
		}
	}

	
	/**
	 * client端获取请求签名包
	 * AuthHelper工具使用client端系统名称，向配置管理中心下载我方的公钥、私钥，并使用私钥对会话密钥签章，返回签名包。
	 * @param systemName  client端系统名称
	 * @param sessionKey  会话密钥
	 * @return 请求签名包
	 * @throws SecretException 
	 */
	public static byte[] getRequestSignPackage(String systemName,String sessionKey) throws SecretException{
		if(isDebug){
			return null;
		}else{
			return AuthHelper.getRequestSignPackage(systemName,sessionKey);
			//return AuthHelper.getRequestSignPackage();
		}
	}
	
	/**
	 * client端验证”应答签名包”
	 * AuthHelper工具使用server端系统名称，向配置管理中心下载server方的公钥,验章，并返回会话密钥。
	 * @param systemName  Server端系统名称
	 * @param signPackage  应答签名包
	 * @return  会话密钥
	 */
	public static String authResponseSignPackage(String systemName,byte[] signPackage)throws SecretException{
		if(isDebug){
			return "123456";
		}else{
			return AuthHelper.authResponseSignPackage(systemName,signPackage);
		}
	}
	
	
	/**
	 * Server端验证请求签名包
	 * AuthHelper工具使用client端系统名称，向配置管理中心下载client方的公钥,验章，并返回会话密钥。
	 * @param systemName client端的系统名称
	 * @param signPackage 请求签名包
	 * @return 会话密钥
	 */
	public static String authRequestSignPackage(String systemName,byte[] signPackage)throws SecretException{
		if(isDebug){
			return "123456";
		}else{
			return AuthHelper.authRequestSignPackage(systemName,signPackage);
		}
	}
	
	/**
	 * Server端获得应答签名包
	 * AuthHelper工具使用Server端系统ID, 向配置管理中心下载我方的公钥、私钥并使用私钥对会话密钥签章，返回签名包。
	 * 
	 * @param systemName  Server端的系统名称
	 * @param sessionKey Server端的会话密钥
	 * @return 签名包
	 * @throws SecretException
	 */
	public static byte[] getResponseSignPackage(String systemName,String sessionKey)throws SecretException{
		if(isDebug){
			return null;
		}else{
			return AuthHelper.getResponseSignPackage(systemName,sessionKey);
		}
	}
	
	/**
	 * 加密
	 * @param sessionKey
	 * @param data
	 * @return
	 * @throws SecretException
	 */
	public static byte[] sessionEncrypt(String sessionKey,byte data[]) throws SecretException{
		if(isDebug){
			if(sessionKey==null){
				return null;
			}
			//模拟加密--只是修改了数据结构，并未加密
			byte[] strBin=sessionKey.getBytes();
			byte[] rs=new byte[strBin.length + data.length];
			System.arraycopy(strBin, 0, rs, 0, strBin.length);
			System.arraycopy(data, 0, rs, strBin.length, data.length);
			return rs;
		}else{
			return AuthHelper.sessionEncrypt(sessionKey, data);
		}
	}
	/**
	 * 解密
	 * @param sessionKey
	 * @param data
	 * @return
	 * @throws SecretException
	 */
	public static byte[] sessionDecrypt(String sessionKey,byte data[]) throws SecretException{
		if(isDebug){
			if(sessionKey==null){
				return null;
			}
			//模拟解密--只是修改了数据结构，并未解密
			byte[] strBin=sessionKey.getBytes();
			byte[] rs=new byte[data.length - strBin.length];
			System.arraycopy(data, strBin.length, rs, 0, rs.length);
			return rs;
		}else{
			return AuthHelper.sessionDecrypt(sessionKey, data);
		}
	}
	
	public static void main(String[] a){
		String b=AuthHelper.getSysId();
		System.out.println(b);
	}
}
