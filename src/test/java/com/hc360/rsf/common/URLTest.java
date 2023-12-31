package com.hc360.rsf.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;

import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.hc360.rsf.common.utils.CollectionUtils;

/**
 * URLTest
 */
public class URLTest {
    
    @Test
    public void test_valueOf_noProtocolAndHost() throws Exception {
        URL url = URL.valueOf("/context/path?version=1.0.0&application=morgan");
        assertNull(url.getProtocol());
        assertNull(url.getUsername());
        assertNull(url.getPassword());
        assertNull(url.getHost());
        assertEquals(0, url.getPort());
        assertEquals("context/path", url.getPath());
        assertEquals(2, url.getParameters().size());
        assertEquals("1.0.0", url.getParameter("version"));
        assertEquals("morgan", url.getParameter("application"));
        

        url = URL.valueOf("context/path?version=1.0.0&application=morgan");
        //Caution , parse as host
        assertNull(url.getProtocol());
        assertNull(url.getUsername());
        assertNull(url.getPassword());
        assertEquals("context", url.getHost());
        assertEquals(0, url.getPort());
        assertEquals("path", url.getPath());
        assertEquals(2, url.getParameters().size());
        assertEquals("1.0.0", url.getParameter("version"));
        assertEquals("morgan", url.getParameter("application"));
    }
	
    @Test
	public void test_valueOf_noProtocol() throws Exception {
		URL url = URL.valueOf("10.20.130.230");
		assertNull(url.getProtocol());
		assertNull(url.getUsername());
		assertNull(url.getPassword());
		assertEquals("10.20.130.230", url.getHost());
		assertEquals(0, url.getPort());
		assertEquals(null, url.getPath());
		assertEquals(0, url.getParameters().size());
		
		url = URL.valueOf("10.20.130.230:63634");
        assertNull(url.getProtocol());
        assertNull(url.getUsername());
        assertNull(url.getPassword());
        assertEquals("10.20.130.230", url.getHost());
        assertEquals(63634, url.getPort());
        assertEquals(null, url.getPath());
        assertEquals(0, url.getParameters().size());
        
        url = URL.valueOf("10.20.130.230/context/path");
        assertNull(url.getProtocol());
        assertNull(url.getUsername());
        assertNull(url.getPassword());
        assertEquals("10.20.130.230", url.getHost());
        assertEquals(0, url.getPort());
        assertEquals("context/path", url.getPath());
        assertEquals(0, url.getParameters().size());
        
        url = URL.valueOf("10.20.130.230:63634/context/path");
        assertNull(url.getProtocol());
        assertNull(url.getUsername());
        assertNull(url.getPassword());
        assertEquals("10.20.130.230", url.getHost());
        assertEquals(63634, url.getPort());
        assertEquals("context/path", url.getPath());
        assertEquals(0, url.getParameters().size());
        
        url = URL.valueOf("admin:hello1234@10.20.130.230:63634/context/path?version=1.0.0&application=morgan");
        assertNull(url.getProtocol());
        assertEquals("admin", url.getUsername());
        assertEquals("hello1234", url.getPassword());
        assertEquals("10.20.130.230", url.getHost());
        assertEquals(63634, url.getPort());
        assertEquals("context/path", url.getPath());
        assertEquals(2, url.getParameters().size());
        assertEquals("1.0.0", url.getParameter("version"));
        assertEquals("morgan", url.getParameter("application"));
	}
    
    @Test
    public void test_valueOf_noHost() throws Exception {
        URL url = URL.valueOf("file:///home/user1/router.js");
        assertEquals("file", url.getProtocol());
        assertNull(url.getUsername());
        assertNull(url.getPassword());
        assertNull(url.getHost());
        assertEquals(0, url.getPort());
        assertEquals("home/user1/router.js", url.getPath());
        assertEquals(0, url.getParameters().size());
        
        // Caution!! 
        url = URL.valueOf("file://home/user1/router.js");
        //                      ^^ only tow slash!
        assertEquals("file", url.getProtocol());
        assertNull(url.getUsername());
        assertNull(url.getPassword());
        assertEquals("home", url.getHost());
        assertEquals(0, url.getPort());
        assertEquals("user1/router.js", url.getPath());
        assertEquals(0, url.getParameters().size());
        

        url = URL.valueOf("file:/home/user1/router.js");
        assertEquals("file", url.getProtocol());
        assertNull(url.getUsername());
        assertNull(url.getPassword());
        assertNull(url.getHost());
        assertEquals(0, url.getPort());
        assertEquals("home/user1/router.js", url.getPath());
        assertEquals(0, url.getParameters().size());
    
        url = URL.valueOf("file:///d:/home/user1/router.js");
        assertEquals("file", url.getProtocol());
        assertNull(url.getUsername());
        assertNull(url.getPassword());
        assertNull(url.getHost());
        assertEquals(0, url.getPort());
        assertEquals("d:/home/user1/router.js", url.getPath());
        assertEquals(0, url.getParameters().size());
        
        url = URL.valueOf("file:///home/user1/router.js?p1=v1&p2=v2");
        assertEquals("file", url.getProtocol());
        assertNull(url.getUsername());
        assertNull(url.getPassword());
        assertNull(url.getHost());
        assertEquals(0, url.getPort());
        assertEquals("home/user1/router.js", url.getPath());
        assertEquals(2, url.getParameters().size());
        Map<String, String> params = new HashMap<String, String>();
        params.put("p1", "v1");
        params.put("p2", "v2");
        assertEquals(params, url.getParameters());
        
        url = URL.valueOf("file:/home/user1/router.js?p1=v1&p2=v2");
        assertEquals("file", url.getProtocol());
        assertNull(url.getUsername());
        assertNull(url.getPassword());
        assertNull(url.getHost());
        assertEquals(0, url.getPort());
        assertEquals("home/user1/router.js", url.getPath());
        assertEquals(2, url.getParameters().size());
        params = new HashMap<String, String>();
        params.put("p1", "v1");
        params.put("p2", "v2");
        assertEquals(params, url.getParameters());
    }
    
    @Test
    public void test_valueOf_WithProtocolHost() throws Exception {
        URL url = URL.valueOf("rsf://10.20.130.230");
        assertEquals("rsf", url.getProtocol());
        assertNull(url.getUsername());
        assertNull(url.getPassword());
        assertEquals("10.20.130.230", url.getHost());
        assertEquals(0, url.getPort());
        assertEquals(null, url.getPath());
        assertEquals(0, url.getParameters().size());
        
        url = URL.valueOf("rsf://10.20.130.230:63634/context/path");
        assertEquals("rsf", url.getProtocol());
        assertNull(url.getUsername());
        assertNull(url.getPassword());
        assertEquals("10.20.130.230", url.getHost());
        assertEquals(63634, url.getPort());
        assertEquals("context/path", url.getPath());
        assertEquals(0, url.getParameters().size());
        
        url = URL.valueOf("rsf://admin:hello1234@10.20.130.230:63634");
        assertEquals("rsf", url.getProtocol());
        assertEquals("admin", url.getUsername());
        assertEquals("hello1234", url.getPassword());
        assertEquals("10.20.130.230", url.getHost());
        assertEquals(63634, url.getPort());
        assertEquals(null, url.getPath());
        assertEquals(0, url.getParameters().size());
        
        url = URL.valueOf("rsf://admin:hello1234@10.20.130.230:63634?version=1.0.0");
        assertEquals("rsf", url.getProtocol());
        assertEquals("admin", url.getUsername());
        assertEquals("hello1234", url.getPassword());
        assertEquals("10.20.130.230", url.getHost());
        assertEquals(63634, url.getPort());
        assertEquals(null, url.getPath());
        assertEquals(1, url.getParameters().size());
        assertEquals("1.0.0", url.getParameter("version"));
        
        url = URL.valueOf("rsf://admin:hello1234@10.20.130.230:63634/context/path?version=1.0.0&application=morgan");
        assertEquals("rsf", url.getProtocol());
        assertEquals("admin", url.getUsername());
        assertEquals("hello1234", url.getPassword());
        assertEquals("10.20.130.230", url.getHost());
        assertEquals(63634, url.getPort());
        assertEquals("context/path", url.getPath());
        assertEquals(2, url.getParameters().size());
        assertEquals("1.0.0", url.getParameter("version"));
        assertEquals("morgan", url.getParameter("application"));
        
        url = URL.valueOf("rsf://admin:hello1234@10.20.130.230:63634/context/path?version=1.0.0&application=morgan&noValue");
        assertEquals("rsf", url.getProtocol());
        assertEquals("admin", url.getUsername());
        assertEquals("hello1234", url.getPassword());
        assertEquals("10.20.130.230", url.getHost());
        assertEquals(63634, url.getPort());
        assertEquals("context/path", url.getPath());
        assertEquals(3, url.getParameters().size());
        assertEquals("1.0.0", url.getParameter("version"));
        assertEquals("morgan", url.getParameter("application"));
        assertEquals("noValue", url.getParameter("noValue"));
    }

    @Test
    public void test_valueOf_Exception_noProtocol() throws Exception {
        try {
            URL.valueOf("://1.2.3.4:8080/path");
            fail();
        } catch (IllegalStateException expected) {
            assertEquals("url missing protocol: \"://1.2.3.4:8080/path\"", expected.getMessage());
        }
    }

    @Test
    public void test_getAddress() throws Exception {
        URL url1 = URL.valueOf("rsf://admin:hello1234@10.20.130.230:63634/context/path?version=1.0.0&application=morgan");
        assertEquals("10.20.130.230:63634", url1.getAddress());
    }
    
    @Test
	public void test_getAbsolutePath() throws Exception {
	    URL url = new URL("p1", "1.2.2.2",  33);
	    assertEquals(null, url.getAbsolutePath());
	    
	    url = new URL("file", null, 90, "/home/user1/route.js");
        assertEquals("/home/user1/route.js", url.getAbsolutePath());
	}
	
	@Test
    public void test_equals() throws Exception {
        URL url1 = URL.valueOf("rsf://admin:hello1234@10.20.130.230:63634/context/path?version=1.0.0&application=morgan");
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("version", "1.0.0");
        params.put("application", "morgan");
        URL url2 = new URL("rsf", "admin", "hello1234", "10.20.130.230", 63634, "context/path", params);
        
        assertEquals(url1, url2);
    }
	
	@Test
	public void test_toString() throws Exception {
	    URL url1 = URL.valueOf("rsf://admin:hello1234@10.20.130.230:63634/context/path?version=1.0.0&application=morgan");
	    assertThat(url1.toString(), anyOf(
	            equalTo("rsf://10.20.130.230:63634/context/path?version=1.0.0&application=morgan"),
	            equalTo("rsf://10.20.130.230:63634/context/path?application=morgan&version=1.0.0"))
	            );
	}
	
	@Test
	public void test_toFullString() throws Exception {
        URL url1 = URL.valueOf("rsf://admin:hello1234@10.20.130.230:63634/context/path?version=1.0.0&application=morgan");
        assertThat(url1.toFullString(), anyOf(
                equalTo("rsf://admin:hello1234@10.20.130.230:63634/context/path?version=1.0.0&application=morgan"),
                equalTo("rsf://admin:hello1234@10.20.130.230:63634/context/path?application=morgan&version=1.0.0"))
                );
	}
	
	@Test
    public void test_set_methods() throws Exception {
	    URL url = URL.valueOf("rsf://admin:hello1234@10.20.130.230:63634/context/path?version=1.0.0&application=morgan");
	    
	    url = url.setHost("host");
	    
        assertEquals("rsf", url.getProtocol());
        assertEquals("admin", url.getUsername());
        assertEquals("hello1234", url.getPassword());
        assertEquals("host", url.getHost());
        assertEquals(63634, url.getPort());
        assertEquals("context/path", url.getPath());
        assertEquals(2, url.getParameters().size());
        assertEquals("1.0.0", url.getParameter("version"));
        assertEquals("morgan", url.getParameter("application"));
        
        url = url.setPort(1);
        
        assertEquals("rsf", url.getProtocol());
        assertEquals("admin", url.getUsername());
        assertEquals("hello1234", url.getPassword());
        assertEquals("host", url.getHost());
        assertEquals(1, url.getPort());
        assertEquals("context/path", url.getPath());
        assertEquals(2, url.getParameters().size());
        assertEquals("1.0.0", url.getParameter("version"));
        assertEquals("morgan", url.getParameter("application"));
        
        url = url.setPath("path");
        
        assertEquals("rsf", url.getProtocol());
        assertEquals("admin", url.getUsername());
        assertEquals("hello1234", url.getPassword());
        assertEquals("host", url.getHost());
        assertEquals(1, url.getPort());
        assertEquals("path", url.getPath());
        assertEquals(2, url.getParameters().size());
        assertEquals("1.0.0", url.getParameter("version"));
        assertEquals("morgan", url.getParameter("application"));
	    
        url = url.setProtocol("protocol");
        
        assertEquals("protocol", url.getProtocol());
        assertEquals("admin", url.getUsername());
        assertEquals("hello1234", url.getPassword());
        assertEquals("host", url.getHost());
        assertEquals(1, url.getPort());
        assertEquals("path", url.getPath());
        assertEquals(2, url.getParameters().size());
        assertEquals("1.0.0", url.getParameter("version"));
        assertEquals("morgan", url.getParameter("application"));
        
        url = url.setUsername("username");
        
        assertEquals("protocol", url.getProtocol());
        assertEquals("username", url.getUsername());
        assertEquals("hello1234", url.getPassword());
        assertEquals("host", url.getHost());
        assertEquals(1, url.getPort());
        assertEquals("path", url.getPath());
        assertEquals(2, url.getParameters().size());
        assertEquals("1.0.0", url.getParameter("version"));
        assertEquals("morgan", url.getParameter("application"));
        
        url = url.setPassword("password");
        
        assertEquals("protocol", url.getProtocol());
        assertEquals("username", url.getUsername());
        assertEquals("password", url.getPassword());
        assertEquals("host", url.getHost());
        assertEquals(1, url.getPort());
        assertEquals("path", url.getPath());
        assertEquals(2, url.getParameters().size());
        assertEquals("1.0.0", url.getParameter("version"));
        assertEquals("morgan", url.getParameter("application"));
	}
	
	@Test
    public void test_removeParameters() throws Exception {
	    URL url = URL.valueOf("rsf://admin:hello1234@10.20.130.230:63634/context/path?version=1.0.0&application=morgan&k1=v1&k2=v2");
	    
	    url = url.removeParameter("version");
        assertEquals("rsf", url.getProtocol());
        assertEquals("admin", url.getUsername());
        assertEquals("hello1234", url.getPassword());
        assertEquals("10.20.130.230", url.getHost());
        assertEquals(63634, url.getPort());
        assertEquals("context/path", url.getPath());
        assertEquals(3, url.getParameters().size());
        assertEquals("morgan", url.getParameter("application"));
        assertEquals("v1", url.getParameter("k1"));
        assertEquals("v2", url.getParameter("k2"));
        
        url = URL.valueOf("rsf://admin:hello1234@10.20.130.230:63634/context/path?version=1.0.0&application=morgan&k1=v1&k2=v2");
        url = url.removeParameters("version", "application");
        assertEquals("rsf", url.getProtocol());
        assertEquals("admin", url.getUsername());
        assertEquals("hello1234", url.getPassword());
        assertEquals("10.20.130.230", url.getHost());
        assertEquals(63634, url.getPort());
        assertEquals("context/path", url.getPath());
        assertEquals(2, url.getParameters().size());
        assertEquals("v1", url.getParameter("k1"));
        assertEquals("v2", url.getParameter("k2"));
        
        url = URL.valueOf("rsf://admin:hello1234@10.20.130.230:63634/context/path?version=1.0.0&application=morgan&k1=v1&k2=v2");
        url = url.removeParameters(Arrays.asList("version", "application"));
        assertEquals("rsf", url.getProtocol());
        assertEquals("admin", url.getUsername());
        assertEquals("hello1234", url.getPassword());
        assertEquals("10.20.130.230", url.getHost());
        assertEquals(63634, url.getPort());
        assertEquals("context/path", url.getPath());
        assertEquals(2, url.getParameters().size());
        assertEquals("v1", url.getParameter("k1"));
        assertEquals("v2", url.getParameter("k2"));
	}
	
	@Test
    public void test_addParameters() throws Exception {
	    URL url = URL.valueOf("rsf://admin:hello1234@10.20.130.230:63634/context/path?application=morgan");
        url = url.addParameters(CollectionUtils.toStringMap("k1", "v1", "k2", "v2"));
        
        assertEquals("rsf", url.getProtocol());
        assertEquals("admin", url.getUsername());
        assertEquals("hello1234", url.getPassword());
        assertEquals("10.20.130.230", url.getHost());
        assertEquals(63634, url.getPort());
        assertEquals("context/path", url.getPath());
        assertEquals(3, url.getParameters().size());
        assertEquals("morgan", url.getParameter("application"));
        assertEquals("v1", url.getParameter("k1"));
        assertEquals("v2", url.getParameter("k2"));
        
        url = URL.valueOf("rsf://admin:hello1234@10.20.130.230:63634/context/path?application=morgan");
        url = url.addParameters("k1", "v1", "k2", "v2", "application", "xxx");
        
        assertEquals("rsf", url.getProtocol());
        assertEquals("admin", url.getUsername());
        assertEquals("hello1234", url.getPassword());
        assertEquals("10.20.130.230", url.getHost());
        assertEquals(63634, url.getPort());
        assertEquals("context/path", url.getPath());
        assertEquals(3, url.getParameters().size());
        assertEquals("xxx", url.getParameter("application"));
        assertEquals("v1", url.getParameter("k1"));
        assertEquals("v2", url.getParameter("k2"));
        
        url = URL.valueOf("rsf://admin:hello1234@10.20.130.230:63634/context/path?application=morgan");
        url = url.addParametersIfAbsent(CollectionUtils.toStringMap("k1", "v1", "k2", "v2", "application", "xxx"));
        
        assertEquals("rsf", url.getProtocol());
        assertEquals("admin", url.getUsername());
        assertEquals("hello1234", url.getPassword());
        assertEquals("10.20.130.230", url.getHost());
        assertEquals(63634, url.getPort());
        assertEquals("context/path", url.getPath());
        assertEquals(3, url.getParameters().size());
        assertEquals("morgan", url.getParameter("application"));
        assertEquals("v1", url.getParameter("k1"));
        assertEquals("v2", url.getParameter("k2"));
        
        url = URL.valueOf("rsf://admin:hello1234@10.20.130.230:63634/context/path?application=morgan");
        url = url.addParameter("k1", "v1");
        
        assertEquals("rsf", url.getProtocol());
        assertEquals("admin", url.getUsername());
        assertEquals("hello1234", url.getPassword());
        assertEquals("10.20.130.230", url.getHost());
        assertEquals(63634, url.getPort());
        assertEquals("context/path", url.getPath());
        assertEquals(2, url.getParameters().size());
        assertEquals("morgan", url.getParameter("application"));
        assertEquals("v1", url.getParameter("k1"));
        
        url = URL.valueOf("rsf://admin:hello1234@10.20.130.230:63634/context/path?application=morgan");
        url = url.addParameter("application", "xxx");
        
        assertEquals("rsf", url.getProtocol());
        assertEquals("admin", url.getUsername());
        assertEquals("hello1234", url.getPassword());
        assertEquals("10.20.130.230", url.getHost());
        assertEquals(63634, url.getPort());
        assertEquals("context/path", url.getPath());
        assertEquals(1, url.getParameters().size());
        assertEquals("xxx", url.getParameter("application"));
        
        url = URL.valueOf("rsf://admin:hello1234@10.20.130.230:63634/context/path?application=morgan");
        url = url.addParameterIfAbsent("application", "xxx");
        
        assertEquals("rsf", url.getProtocol());
        assertEquals("admin", url.getUsername());
        assertEquals("hello1234", url.getPassword());
        assertEquals("10.20.130.230", url.getHost());
        assertEquals(63634, url.getPort());
        assertEquals("context/path", url.getPath());
        assertEquals(1, url.getParameters().size());
        assertEquals("morgan", url.getParameter("application"));
	}
	
    @Test
	public void test_windowAbsolutePathBeginWithSlashIsValid() throws Exception {
	    final String osProperty = System.getProperties().getProperty("os.name");
	    if(!osProperty.toLowerCase().contains("windows")) return;
	    
	    System.out.println("Test Windows valid path string.");
	    
	    File f0 = new File("C:/Windows");
	    File f1 = new File("/C:/Windows");
	    
	    File f2 = new File("C:\\Windows");
	    File f3 = new File("/C:\\Windows");
	    File f4 = new File("\\C:\\Windows");
	    
	    assertEquals(f0, f1);
	    assertEquals(f0, f2);
	    assertEquals(f0, f3);
	    assertEquals(f0, f4);
	}
	
    @Test
    public void test_javaNetUrl() throws Exception {
        java.net.URL url = new java.net.URL("http://admin:hello1234@10.20.130.230:63634/context/path?version=1.0.0&application=morgan#anchor1");
        
        assertEquals("http", url.getProtocol());
        assertEquals("admin:hello1234", url.getUserInfo());
        assertEquals("10.20.130.230", url.getHost());
        assertEquals(63634, url.getPort());
        assertEquals("/context/path", url.getPath()); 
        assertEquals("version=1.0.0&application=morgan", url.getQuery());
        assertEquals("anchor1", url.getRef());
        
        assertEquals("admin:hello1234@10.20.130.230:63634", url.getAuthority());
        assertEquals("/context/path?version=1.0.0&application=morgan", url.getFile());
    }

    @Test
    public void test_Anyhost() throws Exception {
        URL url = URL.valueOf("rsf://0.0.0.0:63634");
        assertEquals("true", url.getParameter("anyhost"));
    }
    
    @Test
    public void test_Localhost() throws Exception {
        URL url = URL.valueOf("rsf://127.0.0.1:63634");
        assertEquals("true", url.getParameter("localhost"));
        
        url = URL.valueOf("rsf://localhost:63634");
        assertEquals("true", url.getParameter("localhost"));
    }
    
}
