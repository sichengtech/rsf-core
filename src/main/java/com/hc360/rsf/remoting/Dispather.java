package com.hc360.rsf.remoting;

import com.hc360.rsf.common.URL;

/**
 * Dispather
 */
public interface Dispather {

    /**
     * dispath.
     * 
     * @param handler
     * @param url
     * @return channel handler
     */
	HandlerDelegate dispath(HandlerDelegate handler, URL url);

}
