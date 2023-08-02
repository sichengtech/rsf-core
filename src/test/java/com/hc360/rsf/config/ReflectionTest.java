/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config;

import java.io.Serializable;
import java.lang.reflect.Type;
import com.hc360.rsf.config.callback.CallBack;

/**
 * ReflectionTest
 * 
 * @author zhaolei 2012-5-10
 */
public class ReflectionTest {

	/**
	 * main
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		CallBack push = new CallBack() {
			public Object call(Serializable data) {
				System.out.println(data);
				return null;
			}
		};

		Class<?> clazz = push.getClass();
		// 返回表示某些接口的 Type,这些接口由此对象所表示的类或接口直接实现。
		Type[] bbb = clazz.getGenericInterfaces();
		System.out.println("返回表示某些接口的 Type,这些接口由此对象所表示的类或接口直接实现。");
		for(Type c:bbb){
			System.out.println(((Class<?>)c).getName());
		}

		// 返回表示此 Class 所表示的实体（类、接口、基本类型或 void）的直接超类的 Type。
		Type ttt = clazz.getGenericSuperclass();
		System.out.println("返回表示此 Class 所表示的实体（类、接口、基本类型或 void）的直接超类的 Type。");
		System.out.println(((Class<?>)ttt).getName());

		// 确定此对象所表示的类或接口实现的接口。
		Class<?>[] aaa = clazz.getInterfaces();
		System.out.println(" 确定此对象所表示的类或接口实现的接口。");
		for(Class<?> c:aaa){
			System.out.println(c.getName());
		}

		// 判定指定的 Object 是否与此 Class 所表示的对象赋值兼容。
		boolean b1 = CallBack.class.isInstance(push);
		System.out.println("判定指定的 Object 是否与此 Class 所表示的对象赋值兼容。");
		System.out.println(b1);
		
		// 判定指定的 Class 对象是否表示一个接口类型。
		boolean b2 = clazz.isInterface();
		System.out.println("判定指定的 Class 对象是否表示一个接口类型。");
		System.out.println(b2);
	}
}
