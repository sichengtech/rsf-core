package com.hc360.rsf.remoting.transport.dispather;

import com.hc360.rsf.remoting.Channel;
import com.hc360.rsf.remoting.HandlerDelegate;
import com.hc360.rsf.rpc.protocol.codec.AbstractCodec;

/**
 * ChannelEventRunnable
 */
public class ChannelEventRunnable implements Runnable {
    private final HandlerDelegate handler;//这里的handler对象是HeaderExchangeHandler类型
    private final Channel channel;
    private final ChannelState state;
    private final Throwable exception;
    private final Object message;
    
    public ChannelEventRunnable(Channel channel, HandlerDelegate handler, ChannelState state) {
        this(channel, handler, state, null);
    }
    
    public ChannelEventRunnable(Channel channel, HandlerDelegate handler, ChannelState state, Object message) {
        this(channel, handler, state, message, null);
    }
    
    public ChannelEventRunnable(Channel channel, HandlerDelegate handler, ChannelState state, Throwable t) {
        this(channel, handler, state, null , t);
    }

    /**
     * 赵磊
     * 
     * @param channel
     * @param handler 这里的handler对象是HeaderExchangeHandler类型
     * @param state
     * @param message
     * @param exception
     */
    public ChannelEventRunnable(Channel channel, HandlerDelegate handler, ChannelState state, Object message, Throwable exception) {
        this.channel = channel;
        this.handler = handler;
        this.state = state;
        this.message = message;
        this.exception = exception;
    }
    
    /**
     * 在一个线程中,处理耗时的业务  --赵磊
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        switch (state) {
            case CONNECTED:
                try{
                    handler.connected(channel);
                }catch (Exception e) {
                    throw new RuntimeException("ChannelEventRunnable handle error,channel is "+channel,e);
                }
                break;
            case DISCONNECTED:
                try{
                    handler.disconnected(channel);
                }catch (Exception e) {
                    throw new RuntimeException("ChannelEventRunnable handle error,channel is "+channel,e);
                }
                break;
            case SENT: //数据发送
                try{
                    handler.sent(channel,message);
                }catch (Exception e) {
                    throw new RuntimeException("ChannelEventRunnable handle error,channel is "+channel+",message is "+ message,e);
                }
                
                break;
            case RECEIVED:  //数据到达, 这里的handler对象是HeaderExchangeHandler类型
                try{
                    handler.received(channel, message);
                }catch (Exception e) {
                	
                	// S端发生异常时，也要响应C端,不然C端只能等待超时了。
        			// S端发生的异常有两类
        			// 第一类是业务异常，第二类是RSF本身异常，如线程池耗尽等等。
        			// 本段代码，就是针对：第二类RSF本身异常 的处理，通知C端 S端发生异常了，不要再等待了。
        			AbstractCodec.errorProce_ClientNotWait(message, channel, e);
                    //throw new RuntimeException("ChannelEventRunnable handle error,channel is "+channel+",message is "+ message,e);
                }
                break;
            case CAUGHT:
                try{
                    handler.caught(channel, exception);
                }catch (Exception e) {
                    throw new RuntimeException("ChannelEventRunnable handle error,channel is "+channel +", message is: "+message+" exception is "+exception,e);
                }
                
                break;
                }
        }
    
    /**
     * ChannelState
     * 
     */
    public enum ChannelState{
        
        /**
         * CONNECTED
         */
        CONNECTED,
        
        /**
         * DISCONNECTED
         */
        DISCONNECTED,
        
        /**
         * SENT  数据发送
         */
        SENT,
        
        /**
         * RECEIVED  数据到达
         */
        RECEIVED,
        
        /**
         * CAUGHT
         */
        CAUGHT
    }

}
