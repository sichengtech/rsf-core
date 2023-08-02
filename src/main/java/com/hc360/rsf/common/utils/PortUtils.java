/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.common.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 端口号处理工具
 * 
 * @author zhaolei 2012-9-20
 */
public class PortUtils {
	
	/**
	 * rsf.xml配置文件中，可以设置服务端监听的端口，支持以下几种形式
	 * port="63634" 设置单一端口
	 * port="63634-63600" 设置一段端口,两端包含，降序
	 * port="63600-63634" 设置一段端口,两端包含，升序
	 * port="63634,63634-63600,63631" 混合设置，会保证顺序，但不会排除重复端口
	 * 
	 * 可能会抛出NumberFormatException
	 * 
	 * @param configPortStr
	 */
	public static Integer[] analysis(String configPortStr){
		if(configPortStr==null){
			return null;
		}
		String[] arr=configPortStr.split(",");
		List<Integer> ports=new ArrayList<Integer>();
		for(int i=0;i<arr.length;i++){
			String p=arr[i];
			if(p!=null && !p.trim().equals("")){
				
				String[] part=p.split("-");
				if(part!=null){
					if(part.length==1){
						//是单个端口
						int pp=Integer.valueOf(part[0]);
						ports.add(pp);
					}else if(part.length==2){
						//是一段端口
						String p0=part[0];
						String p1=part[1];
						int i0=Integer.valueOf(p0);
						int i1=Integer.valueOf(p1);
						if(i0<i1){
							//两端包含，保持顺序
							for(int k=i0;k<=i1;k++){
								ports.add(k);
							}
						}else{
							//两端包含，保持顺序
							for(int k=i0;k>=i1;k--){
								ports.add(k);
							}
						}
					}else{
						//这种错误型试"63600-63634-63650"
						throw new RuntimeException("端口参数配置错误:"+p);
					}
				}
			}
		}
		return ports.toArray(new Integer[ports.size()]);
	}
}
