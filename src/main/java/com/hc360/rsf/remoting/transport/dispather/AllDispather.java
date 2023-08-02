package com.hc360.rsf.remoting.transport.dispather;

import com.hc360.rsf.common.URL;
import com.hc360.rsf.remoting.Dispather;
import com.hc360.rsf.remoting.HandlerDelegate;

/**
 * 默认的线程池配置
 * 
 */
public class AllDispather implements Dispather {
    public static final String NAME = "all";
    public HandlerDelegate dispath(HandlerDelegate handler, URL url) {
        return new AllChannelHandler(handler, url);
    }
}
