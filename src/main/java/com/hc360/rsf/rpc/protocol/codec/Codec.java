package com.hc360.rsf.rpc.protocol.codec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.hc360.rsf.remoting.Channel;

/**
 * Codec. 
 * rsf协议核心接口
 * 
 */
public interface Codec {

	/**
	 * Need more input poison.
	 * 
	 * @see #decode(Channel, InputStream)
	 */
	Object NEED_MORE_INPUT = new Object();

    /**
     * Encode message.
     * 
     * @param channel channel.
     * @param output output stream.
     * @param message message.
     */
    void encode(Channel channel, OutputStream output, Object message) throws IOException;

	/**
	 * Decode message.
	 * 
	 * @see #NEED_MORE_INPUT
	 * @param channel channel.
	 * @param input input stream.
	 * @return message or <code>NEED_MORE_INPUT</code> poison.
	 */
	Object decode(Channel channel, InputStream input) throws IOException;
}
