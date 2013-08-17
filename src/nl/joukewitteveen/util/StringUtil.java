package nl.joukewitteveen.util;

import java.io.*;
import java.util.Enumeration;
import java.util.NoSuchElementException;

public class StringUtil {
	/* Initialize from a byte array that is possibly null */
	public static String nullIsEmpty(byte[] bytes) {
		if(bytes == null) {
			return new String();
		}
		return new String(bytes);
	}

	/* Format a positive floating point number using one decimal */
	public static String oneDecimal(float number) {
		int tenfold = (int) (number * 10 + 0.5f);
		return (tenfold / 10) + "." + (tenfold % 10);
	}

	/* Format a positive number using at least two digits */
	public static String twoDigits(int number) {
		if(number >= 10) {
			return Integer.toString(number);
		} else if(number > 0) {
			return "0" + number;
		}
		return "00";
	}

	/* Enumerate tab-separated fields per line */
	public static class StreamEnumeration implements Enumeration {
		private static final char EOS = (char) -1;
		private boolean moreElements, moreRows;
		private InputStreamReader source;

		public StreamEnumeration(InputStream stream) {
			source = new InputStreamReader(stream);
			moreElements = false;
			moreRows = true;
			skipToNextRow();
		}

		public void skipToNextRow() throws NoSuchElementException {
			if(!moreRows) {
				throw new NoSuchElementException("No more rows");
			}
			try {
				if(moreElements) {
					char c;
					do {
						c = (char) source.read();
					} while(c != '\n' && c != EOS);
					moreRows = (c != EOS);
				}
				moreElements = moreRows;
			} catch (IOException e) {
				AppLog.log("> " + e.getMessage());
				moreElements = false;
				moreRows = false;
			}
		}

		public boolean hasMoreElements() {
			return moreElements;
		}

		public Object nextElement() throws NoSuchElementException {
			if(!moreElements) {
				throw new NoSuchElementException("No more fields");
			}
			String field = "";
			try {
				char c;
				read:
					while(true) {
						c = (char) source.read();
						switch(c) {
						case EOS:
							moreRows = false;
						case '\n':
							moreElements = false;
						case '\t':
							break read;
						}
						field += c;
					}
			} catch (IOException e) {
				AppLog.log("> " + e.getMessage());
				moreElements = false;
				moreRows = false;
			}
			return field;
		}
	}
}
