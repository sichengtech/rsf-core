package com.hc360.rsf.remoting.heartbeat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.ChannelPool;
import com.hc360.rsf.remoting.MockChannel;
import com.hc360.rsf.remoting.exchange.support.Request;

/**
 * 测试HeartbeatTask
 * 
 * @author zhaolei 2012-5-23
 */
public class HeartBeatTaskTest {

    private MockChannel channel = new MockChannel();
    private HeartbeatTask task;
    
    @Before
    public void setup() throws Exception {
//    	Map<String, Channel> channelMap=new HashMap<String, Channel>();
//    	channelMap.put("", channel);
//    	Map<String, Channel> channelMapFail=new HashMap<String, Channel>();
    	
    	ChannelPool channelPool=new ChannelPool();
    	channelPool.putChannel("127.0.0.1", channel);
    	
        task = new HeartbeatTask(channelPool, 1000, 1000 * 3);
    }
    
    @Test
    public void test_heartBeat() throws Exception {
        channel.setAttribute(
        		ChannelPool.KEY_READ_TIMESTAMP, System.currentTimeMillis());
        channel.setAttribute(
        		ChannelPool.KEY_WRITE_TIMESTAMP, System.currentTimeMillis());
        Thread.sleep( 2000L );
        task.run();
        List<Object> objects = channel.getSentObjects();
        Assert.assertTrue(objects.size() > 0);
        Object obj = objects.get(0);
        Assert.assertTrue(obj instanceof Request);
        Request request = (Request)obj;
        Assert.assertTrue(request.isHeartbeat());
        Thread.sleep( 20000L );
    }
}
