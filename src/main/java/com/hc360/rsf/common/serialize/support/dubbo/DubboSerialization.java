
package com.hc360.rsf.common.serialize.support.dubbo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.hc360.rsf.common.URL;
import com.hc360.rsf.common.serialize.ObjectInput;
import com.hc360.rsf.common.serialize.ObjectOutput;
import com.hc360.rsf.common.serialize.Serialization;

/**
 * 
 */
public class DubboSerialization implements Serialization {

    public byte getContentTypeId() {
        return 1;
    }

    public String getContentType() {
        return "x-application/dubbo";
    }

    public ObjectOutput serialize(URL url, OutputStream out) throws IOException {
        return new GenericObjectOutput(out);
    }

    public ObjectInput deserialize(URL url, InputStream is) throws IOException {
        return new GenericObjectInput(is);
    }

}
