/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.common.serialize.support.hsessian;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import com.hc360.rsf.common.io.UnsafeByteArrayInputStream;
import com.hc360.rsf.common.io.UnsafeByteArrayOutputStream;
import com.hc360.rsf.common.serialize.ObjectInput;
import com.hc360.rsf.common.serialize.ObjectOutput;
import com.hc360.rsf.common.serialize.support.hessian.Hessian2ObjectInput;
import com.hc360.rsf.common.serialize.support.hessian.Hessian2ObjectOutput;

/**
 * Hessian 序列化测试
 * 
 * @author zhaolei 2012-5-28
 */
public class HessianSerializeTest {
	static String obj_simple=null;//简单对象
	static String obj_big=null;
	static Map<String,Object> obj_complex=null;//复杂对象
	boolean compact=false;
	
	@BeforeClass
	public static void init(){
		obj_simple="abcdefghijklmnopqrstuvwxyz";
		
		StringBuilder sbl=new StringBuilder();
		for(int i=0;i<100;i++){
			sbl.append(obj_simple);
		}
		obj_big=sbl.toString();
		obj_complex=new HashMap<String,Object>();
		obj_complex.put("1", new Date());
		obj_complex.put("2", obj_simple);
		obj_complex.put("3", 1000L);
		obj_complex.put("4", 260.2589D);
		obj_complex.put("5", 3000);
		obj_complex.put("6", new int[10]);
		obj_complex.put("7", new byte[10]);
		obj_complex.put("8", new ArrayList());
	}
	
	@Test
	public void testA(){
		try {
			UnsafeByteArrayOutputStream os=new UnsafeByteArrayOutputStream();
			ObjectOutput out=new Hessian2ObjectOutput(os);
			out.writeObject(obj_simple);
			out.flushBuffer();
			
			byte[] javacodes=os.toByteArray();
			UnsafeByteArrayInputStream is=new UnsafeByteArrayInputStream(javacodes);
			ObjectInput in=new Hessian2ObjectInput(is);
			String rs=(String)in.readObject();
			
			Assert.assertEquals(rs, obj_simple);
			System.out.println("JavaSerialize,原值:"+obj_simple);
			System.out.println("JavaSerialize,新值:"+rs);
			System.out.print("JavaSerialize,结果长度:"+javacodes.length+",");
			for(byte b:javacodes){
				System.out.print(b);
			}
			System.out.println();
			System.out.println("-------------------");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testB(){
		try {
			UnsafeByteArrayOutputStream os=new UnsafeByteArrayOutputStream();
			ObjectOutput out=new Hessian2ObjectOutput(os);
			out.writeObject(obj_big);
			out.flushBuffer();
			
			byte[] javacodes=os.toByteArray();
			UnsafeByteArrayInputStream is=new UnsafeByteArrayInputStream(javacodes);
			ObjectInput in=new Hessian2ObjectInput(is);
			String rs=(String)in.readObject();
			
			Assert.assertEquals(rs, obj_big);
			System.out.println("JavaSerialize,原值:"+obj_big);
			System.out.println("JavaSerialize,新值:"+rs);
			System.out.print("JavaSerialize,结果长度:"+javacodes.length+",");
			for(byte b:javacodes){
				System.out.print(b);
			}
			System.out.println();
			System.out.println("-------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testC(){
		try {
			UnsafeByteArrayOutputStream os=new UnsafeByteArrayOutputStream();
			ObjectOutput out=new Hessian2ObjectOutput(os);
			out.writeObject(obj_complex);
			out.flushBuffer();
			
			byte[] javacodes=os.toByteArray();
			UnsafeByteArrayInputStream is=new UnsafeByteArrayInputStream(javacodes);
			ObjectInput in=new Hessian2ObjectInput(is);
			Map<String,Object> rs=(Map<String,Object>)in.readObject();
			
			Assert.assertEquals(rs.get("1"), obj_complex.get("1"));
			Assert.assertEquals(rs.get("2"), obj_complex.get("2"));
			Assert.assertEquals(rs.get("3"), obj_complex.get("3"));
			Assert.assertEquals(rs.get("4"), obj_complex.get("4"));
			Assert.assertEquals(rs.get("5"), obj_complex.get("5"));
			System.out.println("JavaSerialize,原值:"+obj_complex);
			System.out.println("JavaSerialize,新值:"+rs);
			for(byte b:javacodes){
				System.out.print(b);
			}
			System.out.println();
			System.out.println("-------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testD(){
		try {
			SubClass sc=new SubClass();
			UnsafeByteArrayOutputStream os=new UnsafeByteArrayOutputStream();
			ObjectOutput out=new Hessian2ObjectOutput(os);
			out.writeObject(sc);
			out.flushBuffer();
		
			byte[] javacodes=os.toByteArray();
			UnsafeByteArrayInputStream is=new UnsafeByteArrayInputStream(javacodes);
			ObjectInput in=new Hessian2ObjectInput(is);
			SubClass rs=(SubClass)in.readObject();
			
			System.out.println(rs.getName());//注意,这里应该输出SubClass ,但输出了Parent,是Hessian的BUG
			System.out.println(rs.isGood());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testE(){
		try {
			String[] str_arr=new String[]{"1","2"};
			UnsafeByteArrayOutputStream os=new UnsafeByteArrayOutputStream();
			ObjectOutput out=new Hessian2ObjectOutput(os);
			out.writeObject(str_arr);
			out.flushBuffer();
		
			byte[] javacodes=os.toByteArray();
			UnsafeByteArrayInputStream is=new UnsafeByteArrayInputStream(javacodes);
			ObjectInput in=new Hessian2ObjectInput(is);
			Object rs=in.readObject();
			
			System.out.println(rs.getClass());//??
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testF(){
		try {
			BigDecimal big=new BigDecimal(122254);
			UnsafeByteArrayOutputStream os=new UnsafeByteArrayOutputStream();
			ObjectOutput out=new Hessian2ObjectOutput(os);
			out.writeObject(big);
			out.flushBuffer();
			
			byte[] javacodes=os.toByteArray();
			UnsafeByteArrayInputStream is=new UnsafeByteArrayInputStream(javacodes);
			ObjectInput in=new Hessian2ObjectInput(is);
			Object rs=in.readObject();
			System.out.println(rs);//结果应该122254
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
