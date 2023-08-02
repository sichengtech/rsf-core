/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.common.util;

import junit.framework.Assert;

import org.junit.Test;

import com.hc360.rsf.common.utils.PortUtils;

/**
 * 端口号处理工具的测试
 * 
 * @author zhaolei 2012-9-20
 */
public class PortUtilsTest {
	/**
	 * rsf.xml配置文件中，可以设置服务端监听的端口，支持以下几种形式
	 * port="63634" 设置单一端口
	 * port="63634-63600" 设置一段端口，降序
	 * port="63600-63634" 设置一段端口，升序
	 * port="63634,63634-63600,63631" 混合设置，不会排除重复端口
	 * 
	 */
	@Test
	public void test1(){
		Integer[] rs=PortUtils.analysis("63634");
		StringBuilder sbl=new StringBuilder();
		sbl.append("test1:");
		for(Integer i:rs){
			sbl.append(i);
			sbl.append(",");
		}
		System.out.println(sbl.toString());
		Assert.assertEquals(sbl.toString(), "test1:63634,");
	}
	@Test
	public void test2(){
		Integer[] rs=PortUtils.analysis("63634-63600");
		StringBuilder sbl=new StringBuilder();
		sbl.append("test2:");
		for(Integer i:rs){
			sbl.append(i);
			sbl.append(",");
		}
		System.out.println(sbl.toString());
		Assert.assertEquals(sbl.toString(), "test2:63634,63633,63632,63631,63630,63629,63628,63627,63626,63625,63624,63623,63622,63621,63620,63619,63618,63617,63616,63615,63614,63613,63612,63611,63610,63609,63608,63607,63606,63605,63604,63603,63602,63601,63600,");
	}
	@Test
	public void test3(){
		Integer[] rs=PortUtils.analysis("63600-63634");
		StringBuilder sbl=new StringBuilder();
		sbl.append("test3:");
		for(Integer i:rs){
			sbl.append(i);
			sbl.append(",");
		}
		System.out.println(sbl.toString());
		Assert.assertEquals(sbl.toString(), "test3:63600,63601,63602,63603,63604,63605,63606,63607,63608,63609,63610,63611,63612,63613,63614,63615,63616,63617,63618,63619,63620,63621,63622,63623,63624,63625,63626,63627,63628,63629,63630,63631,63632,63633,63634,");
	}
	@Test
	public void test4(){
		Integer[] rs=PortUtils.analysis("63634,63634-63600,63631");
		StringBuilder sbl=new StringBuilder();
		sbl.append("test4:");
		for(Integer i:rs){
			sbl.append(i);
			sbl.append(",");
		}
		System.out.println(sbl.toString());
		Assert.assertEquals(sbl.toString(), "test4:63634,63634,63633,63632,63631,63630,63629,63628,63627,63626,63625,63624,63623,63622,63621,63620,63619,63618,63617,63616,63615,63614,63613,63612,63611,63610,63609,63608,63607,63606,63605,63604,63603,63602,63601,63600,63631,");
	}
	/**
	 * 要报错，就达到目的了
	 */
	@Test
	public void test5(){
		System.out.print("test5:");
		try{
			PortUtils.analysis("63600-63634-63650");
		}catch(Exception e){
			System.out.println("要报错，就达到目的了");
			//e.printStackTrace();
		}
	}
	/**
	 * 要报错，就达到目的了
	 */
	@Test
	public void test6(){
		System.out.print("test6:");
		try{
			PortUtils.analysis("63634,63634-aaa63600,63631");
		}catch(Exception e){
			System.out.println("要报错，就达到目的了");
			//e.printStackTrace();
		}
	}
	/**
	 * 
	 */
	@Test
	public void test7(){
		Integer[] rs=PortUtils.analysis("63634-");
		StringBuilder sbl=new StringBuilder();
		sbl.append("test7:");
		for(Integer i:rs){
			sbl.append(i);
			sbl.append(",");
		}
		System.out.println(sbl.toString());
		Assert.assertEquals(sbl.toString(),"test7:63634,");
	}
}
