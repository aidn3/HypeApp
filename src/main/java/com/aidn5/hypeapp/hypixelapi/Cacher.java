package com.aidn5.hypeapp.hypixelapi;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

abstract class Cacher {
	private final Context context;

	/**
	 * The path to the cache folder
	 * <p>
	 * Folder will be in the private cache folder with the name of {@link Cacher}
	 * /data/data/com.example.abc/cache/Cacher/
	 */
	private final String cacheFolder;

	protected Cacher(Context context) {
		this.context = context;
		this.cacheFolder = context.getCacheDir().getAbsolutePath() + "/" + getClass().getSimpleName() + "/";
	}

	/**
	 * check whether the Cacher is ready to save(...) and get(...) data.
	 * When not, Create folder for the cache
	 *
	 * @return TRUE on success. FALSE on failure
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean initSuccess(String cacheFolder) {
		File file = new File(cacheFolder);

		return (file.exists()) || file.mkdirs();
	}

	/**
	 * Save data as cache
	 *
	 * @param key    the name of data.
	 * @param string the data to be saved
	 * @return TRUE on success
	 */
	protected final boolean saveCache(String key, String string) {
		if (!initSuccess(cacheFolder)) return false;

		OutputStream out = null;
		try {
			String file = cacheFolder + key + ".cache";
			out = new FileOutputStream(file);
			out.write(string.getBytes());

			out.close();
		} catch (IOException e) {
			try {
				if (out != null) out.close();
			} catch (Exception ignored) {
			}

			return false;
		}

		return true;
	}

	/**
	 * @param key the name of the data
	 * @return <b>{@link DataHolder}</b> if there is data. <b>NULL</b> when there is no data,
	 * not isReady() or can not get the data
	 */
	protected final DataHolder getCache(String key) {
		if (!initSuccess(cacheFolder)) return null;

		FileInputStream fis = null;
		try {
			File file = new File(cacheFolder + key + ".cache");
			if (!file.exists() || !file.isFile()) return null;

			long lastModified = file.lastModified();

			fis = new FileInputStream(file);

			byte[] data = new byte[(int) file.length()];
			//noinspection ResultOfMethodCallIgnored
			fis.read(data);

			fis.close();

			//noinspection CharsetObjectCanBeUsed
			return new DataHolder(new String(data, "UTF-8"), lastModified);
		} catch (Exception e) {
			e.printStackTrace();

			try {
				if (fis != null) fis.close();
			} catch (Exception ignored) {
			}

		}

		return null;
	}

	final class DataHolder {
		protected final String data;
		protected final long dataOld;

		private DataHolder(String data, long dataOld) {
			this.data = data;
			this.dataOld = dataOld;
		}
	}
}
