/**
 * UnsafeStringReader.java   2012-5-10
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.common.io;

import java.io.IOException;
import java.io.Reader;

/**
 * Thread unsafed StringReader.
 * 
 */

public class UnsafeStringReader extends Reader
{
	private String mString;

	private int mPosition, mLimit, mMark;

	public UnsafeStringReader(String str)
	{
		mString = str;
		mLimit = str.length();
		mPosition = mMark = 0;
	}

	@Override
	public int read() throws IOException
	{
		ensureOpen();
		if( mPosition >= mLimit )
			return -1;

		return mString.charAt(mPosition++);
	}

	@Override
	public int read(char[] cs, int off, int len) throws IOException
	{
		ensureOpen();
		if( (off < 0) || (off > cs.length) || (len < 0) ||
				((off + len) > cs.length) || ((off + len) < 0) )
			throw new IndexOutOfBoundsException();

		if( len == 0 )
			return 0;

		if( mPosition >= mLimit )
			return -1;

		int n = Math.min(mLimit - mPosition, len);
		mString.getChars(mPosition, mPosition + n, cs, off);
		mPosition += n;
		return n;
	}

	public long skip(long ns) throws IOException
	{
		ensureOpen();
		if( mPosition >= mLimit )
			return 0;

		long n = Math.min(mLimit - mPosition, ns);
		n = Math.max(-mPosition, n);
		mPosition += n;
		return n;
	}

	public boolean ready() throws IOException
	{
		ensureOpen();
		return true;
	}

	@Override
	public boolean markSupported()
	{
		return true;
	}

	public void mark(int readAheadLimit) throws IOException
	{
		if( readAheadLimit < 0 )
			throw new IllegalArgumentException("Read-ahead limit < 0");

		ensureOpen();
		mMark = mPosition;
	}

	public void reset() throws IOException
	{
		ensureOpen();
		mPosition = mMark;
	}
 
	@Override
	public void close() throws IOException
	{
		mString = null;
	}

    private void ensureOpen() throws IOException
    {
    	if( mString == null )
    		throw new IOException("Stream closed");
	}
}
