
package com.hc360.rsf.common.serialize.support.java;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;

/**
 * 压缩
 * Compacted java object output stream.
 * 
 */

public class CompactedObjectOutputStream extends ObjectOutputStream
{
	public CompactedObjectOutputStream(OutputStream out) throws IOException
	{
		super(out);
	}

	@Override
	protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException
	{
		Class<?> clazz = desc.forClass();
		if( clazz.isPrimitive() || clazz.isArray() )
		{
			write(0);
			super.writeClassDescriptor(desc);
		}
		else
		{
			write(1);
			writeUTF(desc.getName());
		}
	}
}
