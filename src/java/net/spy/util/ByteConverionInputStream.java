// Copyright (c) 2003  Dustin Sallings <dustin@spy.net>

package net.spy.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Super class for all ascii decoding input streams.
 */
public abstract class ByteConverionInputStream extends FilterInputStream {

	/**
	 * Get an instance of ByteConverionInputStream.
	 */
	public ByteConverionInputStream(InputStream is) {
		super(is);
	}

	/**
	 * Marking and resetting are not supported in this filter by default.
	 */
	@Override
	public boolean markSupported() {
		return(false);
	}

	/**
	 * Reads up to len bytes of data from this input stream into an array
	 * of bytes.  See the FilterInputStream documentation for more details.
	 *
	 * @see FilterInputStream
	 */
	@Override
	public int read(byte data[], int offset, int len) throws IOException {
		byte[] tmpbuf=new byte[len];

		int lastread=0;
		int bytesread=0;

		for(bytesread=0; bytesread<len && lastread>=0; bytesread++) {
			lastread=read();
			if(lastread>=0) {
				tmpbuf[bytesread]=(byte)lastread;
			} else {
				bytesread--;
			}
		}

		System.arraycopy(tmpbuf, 0, data, offset, bytesread);

		return(bytesread);
	}

}
