package com.hc360.rsf.common.utils;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Random;

public class EvnUtil {
	
	public static String jvmInfo(){
		
	  	float fFreeMemory=(float)Runtime.getRuntime().freeMemory();
		float fTotalMemory=(float)Runtime.getRuntime().totalMemory();
		float fmaxMemory=(float)Runtime.getRuntime().maxMemory() ;
		int cpu=Runtime.getRuntime().availableProcessors() ;
		float fPercent=fFreeMemory/fTotalMemory*100;
		
		
		EnvServlet env=new EnvServlet();
		env.setHashtable();
		String str="";
		str+="服务器操作系统："+env.queryHashtable("os.name")+" "+env.queryHashtable("os.version")+" "+env.queryHashtable("sun.os.patch.level")+"\n\r";
		str+=" 服务器操作系统类型："+env.queryHashtable("os.arch")+"\n\r";
		str+=" 服务器操作系统模式："+env.queryHashtable("sun.arch.data.model")+"位\n\r";
		str+=" 服务器所在地区："+env.queryHashtable("user.country")+"\n\r";
		str+=" 服务器语言："+env.queryHashtable("user.language")+"\n\r";
		str+=" 服务器时区："+env.queryHashtable("user.timezone")+"\n\r";
		str+=" 服务器时间："+new java.util.Date()+"\n\r"; 
		str+=" 当前用户："+env.queryHashtable("user.name")+"\n\r";
		str+=" 用户目录："+env.queryHashtable("user.dir")+"\n\r";	
		str+=" JAVA运行环境名称："+env.queryHashtable("java.runtime.name")+" "+env.queryHashtable("java.runtime.version")+"\n\r";
		str+=" JAVA虚拟机剩余内存："+fFreeMemory/1024/1024+"M"+"\n\r";
		str+=" JAVA虚拟机分配内存："+fTotalMemory/1024/1024+"M "+"\n\r";
		str+=" Java虚拟机试图使用的最大内存："+fmaxMemory/1024/1024+"M "+"\n\r";
		str+=" Java虚拟机可用CPU的数目："+cpu+"\n\r";
		str+=" java.home："+env.queryHashtable("java.home")+"\n\r";
		return str;
	}
	
	public static void main(String[] a){
		System.out.println(EvnUtil.jvmInfo());
	}

	
	
	static class EnvServlet
	{
		public long timeUse=0;
		public Hashtable htParam=new Hashtable();
		private Hashtable htShowMsg=new Hashtable();
		public void setHashtable()
		{
			Properties me=System.getProperties();
			Enumeration em=me.propertyNames();
			while(em.hasMoreElements())
			{
				String strKey=(String)em.nextElement();
				String strValue=me.getProperty(strKey);
				htParam.put(strKey,strValue);
			}
		}	
		public void getHashtable(String strQuery)
		{
			Enumeration em=htParam.keys();
			while(em.hasMoreElements())
			{
				String strKey=(String)em.nextElement();
				String strValue=new String();
				if(strKey.indexOf(strQuery,0)>=0)
				{
					strValue=(String)htParam.get(strKey);
					htShowMsg.put(strKey,strValue);
				}
			}
		}
		public String queryHashtable(String strKey)
		{
			strKey=(String)htParam.get(strKey);
			return strKey;
		}
		public long test_int()
		{
			long timeStart = System.currentTimeMillis();
			int i=0;
			while(i<3000000)i++;
			long timeEnd = System.currentTimeMillis();
			long timeUse=timeEnd-timeStart;
			return timeUse;
		}
		public long test_sqrt()
		{
			long timeStart = System.currentTimeMillis();
			int i=0;
			double db=(double)new Random().nextInt(1000);
			while(i<200000){db=Math.sqrt(db);i++;}
			long timeEnd = System.currentTimeMillis();
			long timeUse=timeEnd-timeStart;
			return timeUse;
		}
	}
}
