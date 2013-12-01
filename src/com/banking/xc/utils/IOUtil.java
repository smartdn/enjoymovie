package com.banking.xc.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.banking.xc.utils.HttpGroup.StopController;

public class IOUtil {
	
	private static int bufferSize = 16384;

	/**
	 * 读取为 string
	 */
	public static String readAsString(InputStream is, String encode)
			throws Exception {
		return readAsString(is, encode, null);
	}

	/**
	 * 读取为 string
	 */
	public static String readAsString(InputStream is, String encode,
			ProgressListener progressListener) throws Exception {
		try {
			byte[] data = readAsBytes(is, progressListener);
			return new String(data, encode);
		} catch (UnsupportedEncodingException e) {
			if (Log.V) {
				Log.v("HttpRequest", e.getMessage());
			}
			return null;
		}
	}

	/**
	 * 读取为 byte[]
	 */
	public static byte[] readAsBytes(InputStream is,
			ProgressListener progressListener) throws Exception {

		byte[] data = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			byte[] buf = new byte[bufferSize];
			int len = 0;
			int progress = 0;
			while ((len = is.read(buf)) != -1) {
				os.write(buf, 0, len);
				progress += len;
				if (null != progressListener) {
					progressListener.notify(len, progress);
				}
			}
			data = os.toByteArray();
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (null != os) {
					os.close();
				}
			} catch (Exception e) {
			}
		}
		return data;

	}

	/**
	 * 读取为 file
	 */
	public static void readAsFile(InputStream is, FileOutputStream os,
			ProgressListener progressListener, StopController stopController)
			throws Exception {

		try {
			byte[] buf = new byte[bufferSize];
			int len = 0;
			int progress = 0;
			while ((len = is.read(buf)) != -1 && !stopController.isStop()) {
				os.write(buf, 0, len);
				progress += len;
				if (null != progressListener) {
					progressListener.notify(len, progress);
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (null != os) {
					os.close();
				}
			} catch (Exception e) {
			}
		}

	}

	public interface ProgressListener {
		/**
		 * @author lijingzuo
		 * 
		 *         Time: 2011-3-22 下午02:26:16
		 * 
		 *         Name:
		 * 
		 *         Description:
		 * 
		 * @param incremental
		 *            增量
		 * @param cumulant
		 *            累计量
		 * 
		 */
		void notify(int incremental, int cumulant);
	}

}
