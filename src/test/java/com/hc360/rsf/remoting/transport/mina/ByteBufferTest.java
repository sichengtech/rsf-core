package com.hc360.rsf.remoting.transport.mina;

import java.nio.ByteBuffer;

/**
 *  
 * 
 * @author zhaolei 2012-5-29
 */
public class ByteBufferTest {

	public static void main(String[] args) {
		ByteBuffer bb = ByteBuffer.allocate(10);//分配一个新的字节缓冲区。容量是10
		for (int i = 1; i < 9; i++) {
			bb.put((byte) i);//将给定的字节写入此缓冲区的当前位置,然后该位置递增。 
		}

		//position：表示Buffer中第一个可以被读取或写入的数据的位置,
		//每次调用put（）方法时,把写入的数据放到position位置,然后position的值就会加1
		System.out.println("pos:" + bb.position());
		//limit：表示buffer中第一个不可被读取或写入的数据的位置,也即停止位,数据操作到此为止
		System.out.println("limit:" + bb.limit());
		//capacity：初始化时调用allocate(int size)为buffer分配的空间大小,不可变
		System.out.println("cap:" + bb.capacity());

		bb.flip();
		System.out.println("/nafter flip");
		System.out.println("pos:" + bb.position());
		System.out.println("limit:" + bb.limit());
		System.out.println("cap:" + bb.capacity());

		bb.mark();
		System.out.println("/nafter mark");
		System.out.println("pos:" + bb.position());
		System.out.println("limit:" + bb.limit());
		System.out.println("cap:" + bb.capacity());

		bb.reset();
		System.out.println("/nafter reset");
		System.out.println("pos:" + bb.position());
		System.out.println("limit:" + bb.limit());
		System.out.println("cap:" + bb.capacity());

		bb.clear();
		System.out.println("/nafter clear");
		System.out.println("pos:" + bb.position());
		System.out.println("limit:" + bb.limit());
		System.out.println("cap:" + bb.capacity());

		bb.limit(1);
		bb.put((byte) 9);
		bb.put((byte) 10);// 超出limit范围,抛出java.nio.BufferOverflowException异常
		bb.put((byte) 11);
	}
}
