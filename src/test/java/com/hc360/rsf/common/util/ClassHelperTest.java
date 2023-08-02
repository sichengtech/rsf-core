/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.common.util;

import org.junit.Test;

/**
 * 类加载器测试 
 * 
 * @author zhaolei 2012-5-18
 */
public class ClassHelperTest {
	
	/**
	 * 打印出类加载器 
	 */
	@Test
	public void printClassLoader(){
		 ClassLoader loader = ClassHelperTest.class.getClassLoader(); 
	        while (loader != null) { 
	            System.out.println(loader.toString()); 
	            loader = loader.getParent(); 
	        } 
	}

}
