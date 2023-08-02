package com.hc360.rsf.common;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Version 判断rsf jar包的版本
 * 
 * 判断逻辑：
 * 首先查找META-INF\MANIFEST.MF规范中的版本号，每次RSF发布新版本都会修改MANIFEST.MF中的：Implementation-Version: 1.1.0 (废弃)
 * 如果MANIFEST.MF规范中没有版本号,则查找RSF的jar包文件名中包含的版本号，如RSF-1.0.12.jar。
 * 如果Jar包文件名中不包含版本号，则了RSF中Constants类中默认值。
 */
public final class Version {

	////////////////////////////////////////////////////////////////
	//每次发布新版本，请修改这里的版本号。同时还要修改MANIFEST.MF文件
	////////////////////////////////////////////////////////////////
	private static final String defaultVersion="1.3.6";

    private static final Logger LOGGER = LoggerFactory.getLogger(Version.class);

    private static final String VERSION = getVersion(Version.class, defaultVersion);


    static {
        // 检查是否存在重复的jar包
    	Version.checkDuplicate(Version.class);
	}
    
    private Version() {}

    public static String getVersion(){
    	return VERSION;
    }
    
    
//    private static boolean hasResource(String path) {
//        try {
//            return Version.class.getClassLoader().getResource(path) != null;
//        } catch (Throwable t) {
//            return false;
//        }
//    }
    
    private static String getVersion(Class<?> cls, String defaultVersion) {
        try {
            // 首先查找MANIFEST.MF规范中的版本号
//            String version = cls.getPackage().getImplementationVersion();
//            if (version == null || version.length() == 0) {
//                version = cls.getPackage().getSpecificationVersion();
//            }
//            if (version == null || version.length() == 0) {
        	String version = null;
                // 如果规范中没有版本号,基于jar包名获取版本号
                String file = cls.getProtectionDomain().getCodeSource().getLocation().getFile();
                if (file != null && file.length() > 0 && file.endsWith(".jar")) {
                    file = file.substring(0, file.length() - 4);
                    int i = file.lastIndexOf('/');
                    if (i >= 0) {
                        file = file.substring(i + 1);
                    }
                    i = file.indexOf('-');
                    if (i >= 0) {
                        file = file.substring(i + 1);
                    }
                    while (file.length() > 0 && ! Character.isDigit(file.charAt(0))) {
                        i = file.indexOf('-');
                        if (i >= 0) {
                            file = file.substring(i + 1);
                        } else {
                            break;
                        }
                    }
                    version = file;
                }
//            }
            // 返回版本号,如果为空返回缺省版本号
            return version == null || version.length() == 0 ? defaultVersion : version;
        } catch (Throwable e) { // 防御性容错
            // 忽略异常,返回缺省版本号
            LOGGER.error(e.getMessage(), e);
            return defaultVersion;
        }
    }

	public static void checkDuplicate(Class<?> cls) {
		checkDuplicate(cls.getName().replace('.', '/') + ".class");
	}

	public static void checkDuplicate(String path) {
		try {
			// 在ClassPath搜文件
			Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(path);
			Set<String> files = new HashSet<String>();
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				if (url != null) {
					String file = url.getFile();
					if (file != null && file.length() > 0) {
						files.add(file);
					}
				}
			}
			// 如果有多个,就表示重复
			if (files.size() > 1) {
				String msg="RSF框架jar包重复。发现重复的类 " + path + "在" + files.size() + "个jar包中 " + files;
				String out=msg+"\r\n\t"+msg+"\r\n\t"+msg+"\r\n\t"+msg+"\r\n\t"+msg;
				LOGGER.error(out);
				System.out.println(out);
			}
		} catch (Throwable e) { // 防御性容错
			LOGGER.error(e.getMessage(), e);
		}
	}
}
