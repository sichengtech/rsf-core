package com.hc360.rsf.common.serialize;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import com.hc360.com.caucho.hessian.io.Hessian2Input;
import com.hc360.com.caucho.hessian.io.Hessian2Output;
import com.hc360.rsf.common.io.Bytes;
import com.hc360.rsf.common.io.UnsafeByteArrayInputStream;
import com.hc360.rsf.common.io.UnsafeByteArrayOutputStream;
import com.hc360.rsf.common.serialize.support.dubbo.Builder;
import com.hc360.rsf.common.serialize.support.dubbo.GenericObjectInput;
import com.hc360.rsf.common.serialize.support.dubbo.GenericObjectOutput;
import com.hc360.rsf.common.serialize.support.java.CompactedObjectInputStream;
import com.hc360.rsf.common.serialize.support.java.CompactedObjectOutputStream;


/**
 * 
 */
public class SerializationCompareTest
{
	@Test
	public void test_CompareSerializeLength() throws Exception
	{
		long[] data = new long[]{ -1l, 2l, 3l, 4l, 5l };
		ByteArrayOutputStream os;

		os = new ByteArrayOutputStream();
		ObjectOutputStream jos = new ObjectOutputStream(os);
		jos.writeObject(data);
		System.out.println("java:"+Bytes.bytes2hex(os.toByteArray())+":"+os.size());

		os = new ByteArrayOutputStream();
		CompactedObjectOutputStream oos = new CompactedObjectOutputStream(os);
		oos.writeObject(data);
		System.out.println("compacted java:"+Bytes.bytes2hex(os.toByteArray())+":"+os.size());

		os = new ByteArrayOutputStream();
		Hessian2Output h2o = new Hessian2Output(os);
		h2o.writeObject(data);
		h2o.flushBuffer();
		System.out.println("hessian:"+Bytes.bytes2hex(os.toByteArray())+":"+os.size());

		os = new ByteArrayOutputStream();
		ObjectOutput out=new GenericObjectOutput(os);
		out.writeObject(data);
		out.flushBuffer();
		System.out.println("dubbo:"+Bytes.bytes2hex(os.toByteArray())+":"+os.size());
	}

	@Test
	public void testBuilderPerm() throws Exception
	{
		Builder<Bean> bb = Builder.register(Bean.class);
		Bean bean = new Bean();
		int len = 0;
		long now = System.currentTimeMillis();
		for(int i=0;i<500;i++)
		{
			//ByteArrayOutputStream os = new ByteArrayOutputStream();
			UnsafeByteArrayOutputStream os=new UnsafeByteArrayOutputStream();
			bb.writeTo(bean, os);
			os.close();
			if( i == 0 )
				len = os.toByteArray().length;
			
//			ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
			UnsafeByteArrayInputStream is = new UnsafeByteArrayInputStream(os.toByteArray());
			Bean b = bb.parseFrom(is);
			assertEquals(b.getClass(), Bean.class);
		}
		System.out.println("Builder write and parse 500 times in " + (System.currentTimeMillis()-now)+"ms, size " + len);
	}
	
	@Test
	public void testDubboPerm() throws Exception
	{
		Bean bean = new Bean();
		int len = 0;
		long now = System.currentTimeMillis();
		for(int i=0;i<500;i++)
		{
			UnsafeByteArrayOutputStream os=new UnsafeByteArrayOutputStream();
			ObjectOutput out=new GenericObjectOutput(os);
			out.writeObject(bean);
			out.flushBuffer();
			
			byte[] javacodes=os.toByteArray();
			
			if( i == 0 )
				len = javacodes.length;
			
			UnsafeByteArrayInputStream is=new UnsafeByteArrayInputStream(javacodes);
			ObjectInput in=new GenericObjectInput(is);
			Bean rs=(Bean)in.readObject();
			
			assertEquals(rs.getClass(), Bean.class);
		}
		System.out.println("Dubbo write and parse 500 times in " + (System.currentTimeMillis()-now)+"ms, size " + len);
	}

	@Test
	public void testH2oPerm() throws Exception
	{
		Bean bean = new Bean();
		int len = 0;
		long now = System.currentTimeMillis();
		for(int i=0;i<500;i++)
		{
			//ByteArrayOutputStream os = new ByteArrayOutputStream();
			UnsafeByteArrayOutputStream os=new UnsafeByteArrayOutputStream();
			Hessian2Output out = new Hessian2Output(os);
			out.writeObject(bean);
			out.flushBuffer();
			os.close();
			if( i == 0 )
				len = os.toByteArray().length;
//			ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
			UnsafeByteArrayInputStream is = new UnsafeByteArrayInputStream(os.toByteArray());
			Hessian2Input in = new Hessian2Input(is);
			assertEquals(in.readObject().getClass(), Bean.class);
		}
		System.out.println("Hessian2 write and parse 500 times in " + (System.currentTimeMillis()-now)+"ms, size " + len);
	}

	@Test
	public void testJavaOutputPerm() throws Exception
	{
		Bean bean = new Bean();
		int len = 0;
		long now = System.currentTimeMillis();
		for(int i=0;i<500;i++)
		{
			//ByteArrayOutputStream os = new ByteArrayOutputStream();
			UnsafeByteArrayOutputStream os=new UnsafeByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(os);
			out.writeObject(bean);
			os.close();
			if( i == 0 )
				len = os.toByteArray().length;
//			ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
			UnsafeByteArrayInputStream is = new UnsafeByteArrayInputStream(os.toByteArray());
			ObjectInputStream in = new ObjectInputStream(is);
			assertEquals(in.readObject().getClass(), Bean.class);
		}
		System.out.println("java write and parse 500 times in " + (System.currentTimeMillis()-now)+"ms, size " + len);
	}

	@Test
	public void testCompactedJavaOutputPerm() throws Exception
	{
		Bean bean = new Bean();
		int len = 0;
		long now = System.currentTimeMillis();
		for(int i=0;i<500;i++)
		{
			//ByteArrayOutputStream os = new ByteArrayOutputStream();
			UnsafeByteArrayOutputStream os=new UnsafeByteArrayOutputStream();
			CompactedObjectOutputStream out = new CompactedObjectOutputStream(os);
			out.writeObject(bean);
			os.close();
			if( i == 0 )
				len = os.toByteArray().length;
			//ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
			UnsafeByteArrayInputStream is = new UnsafeByteArrayInputStream(os.toByteArray());
			CompactedObjectInputStream in = new CompactedObjectInputStream(is);
			assertEquals(in.readObject().getClass(), Bean.class);
		}
		System.out.println("compacted java write and parse 500 times in " + (System.currentTimeMillis()-now)+"ms, size " + len);
	}

	public static enum EnumTest { READ, WRITE, CREATE, UNREGISTER };

	static class MyList<T> extends ArrayList<T>
	{
        private static final long serialVersionUID = 1L;
        
        private int code = 12345;
		private String id = "feedback";
	}

	static class MyMap<K, V> extends HashMap<K, V>
	{
        private static final long serialVersionUID = 1L;
        
        private int code = 12345;
		private String id = "feedback";
	}

	public static class Bean implements Serializable
	{
		private static final long serialVersionUID = 7737610585231102146L;

		public EnumTest ve = EnumTest.CREATE;

		public int vi = 0;
		public long vl = 100l;

		boolean b = true;
		boolean[] bs = {false, true};

		String s1 = "1234567890";
		String s2 = "1234567890一二三四五六七八九零";

		int i = 123123, ni = -12344, is[] = {1,2,3,4,-1,-2,-3,-4};
		short s = 12, ns = -76;
		double d = 12.345, nd = -12.345;
		long l = 1281447759383l, nl = -13445l;
		private ArrayList<Object> mylist = new ArrayList<Object>();
		{
			mylist.add(1);
			mylist.add("qianlei");
			mylist.add("qianlei");
			mylist.add("qianlei");
			mylist.add("qianlei");
		}
		private HashMap<Object, Object> mymap = new HashMap<Object, Object>();
		{
			mymap.put(1,2);
			mymap.put(2,"1234");
			mymap.put("2345",12938.122);
			mymap.put("2345",-1);
			mymap.put("2345",-1.20);
		}

		public ArrayList<Object> getMylist()
		{
			return mylist;
		}

		public void setMylist(ArrayList<Object> list)
		{
			mylist = list;
		}

		public HashMap<Object, Object> getMymap()
		{
			return mymap;
		}

		public void setMymap(HashMap<Object, Object> map)
		{
			mymap = map;
		}
	}
}
