package com.hc360.rsf.common.io;

public interface ClassDescriptorMapper
{
	/**
	 * get Class-Descriptor by index.
	 * 
	 * @param index index.
	 * @return string.
	 */
	String getDescriptor(int index);

	/**
	 * get Class-Descriptor index
	 * 
	 * @param desc Class-Descriptor
	 * @return index.
	 */
	int getDescriptorIndex(String desc);
}
