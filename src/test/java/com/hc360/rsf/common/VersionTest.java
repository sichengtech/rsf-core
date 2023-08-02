/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.common;

import org.junit.Test;

/**
 * VersionTest
 * 
 * @author zhaolei 2012-6-14
 */
public class VersionTest {
	
	@Test
	public void test_getVersion(){
		String version=Version.getVersion();
		System.out.println("version="+version);
	}
	
	@Test
	public void test_checkDuplicate(){
		Version.checkDuplicate(Version.class);
	}
}
