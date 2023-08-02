package com.hc360.rsf.common.util;
import com.hc360.rsf.common.utils.NetUtils;

import junit.framework.Assert;
import junit.framework.TestCase;

public class NetUtilsTest extends TestCase {
	
	public void testValidAddress() throws Exception {
		Assert.assertTrue(NetUtils.isValidAddress("10.20.130.230:20880"));
		Assert.assertFalse(NetUtils.isValidAddress("10.20.130.230"));
		Assert.assertFalse(NetUtils.isValidAddress("10.20.130.230:666666"));
	}

}
