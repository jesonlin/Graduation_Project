package com.linjinzhong.musicplayer.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Path.Op;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.linjinzhong.musicplayer.R;
import com.linjinzhong.musicplayer.R.string;

/**
 * 处理下载图片的
 * 
 * @author lin_j
 * 
 */
public class ImageUtils {

	private static String filename = null;
	
	private static final Uri albumArtUri=Uri.parse("content://media/external/audio/albumart");

	private static Map<String, String> imageMap = new HashMap<String, String>();

	private ImageUtils() {
	}

	/**
	 * 将inputstream流写入path中名字为filename
	 * 
	 * @param is
	 *            输入流
	 * @param path
	 *            要存放的路径
	 * @param filename
	 *            文件名
	 */
	public static void writeToLocal(InputStream is, String path) {
		OutputStream os = null;
		try {
			os = new FileOutputStream(new File(path + filename));

			byte[] b = new byte[4096];

			int read = 0;

			while (-1 != (read = is.read(b, 0, 4096))) {
				os.write(b, 0, read);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * 根据关键词下载
	 * 
	 * @param keyword
	 * @return
	 */
	public static InputStream getInputstreamByKeyWord(String keyword) {

		InputStream iss = null;
		InputStream is = null;
		try {

			String url = "http://pic.sogou.com/pics?query="
					+ URLEncoder.encode(keyword, "GB2312");

			URL u = new URL(url);

			URLConnection uc = u.openConnection();

			is = uc.getInputStream();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "GB2312"));

			String buffer = null;

			StringBuilder sb = new StringBuilder();

			while (null != (buffer = reader.readLine())) {
				sb.append(buffer);
			}

			String content = sb.toString();
			int httpPoint = content.indexOf("src=\"http");

			int titlePoint = content.indexOf("title", httpPoint);

			String imageUrl = content.substring(httpPoint + 5, titlePoint - 2);

			filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

			URL imageHttpUrl = new URL(imageUrl);

			URLConnection imageUrlConnection = imageHttpUrl.openConnection();

			iss = imageUrlConnection.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return iss;
	}

	public static Map<String, String> generateImageFromSouGou(String path,
			String keyword) {
		InputStream is = getInputstreamByKeyWord(keyword);

		imageMap.put(keyword, filename);

		writeToLocal(is, path);

		return imageMap;
	}

	public static Map<String, String> generateImageFromSouGou(String keyword) {
		return generateImageFromSouGou("/", keyword);
	}

	/**
	 * 获取默认显示图片
	 * 
	 * @param context
	 * @param small
	 * @return
	 */
	public static Bitmap getDefaultArtwork(Context context, boolean small) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		if (small) {// 返回小图片,默认显示的那张图片
			return BitmapFactory.decodeStream(context.getResources()
					.openRawResource(R.drawable.play_image), null, options);
		}
		return BitmapFactory.decodeStream(context.getResources()
				.openRawResource(R.drawable.play_image), null, options);
	}

	/**
	 * 从文件中获取专辑封面位图
	 * 
	 * @param context
	 * @param song_id
	 * @param album_id
	 * @return
	 */
	public static Bitmap getArtworkFromFile(Context context, long song_id,
			long album_id) {
		Bitmap bm = null;
		if (album_id < 0 && song_id < 0) {
			throw new IllegalArgumentException(
					"Must specify an album or a song id");
		}
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			FileDescriptor fd = null;
			if (album_id < 0) {
				Uri uri = Uri.parse("content://media/external/audio/media/"
						+ song_id + "/albumart");
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) {
					fd = pfd.getFileDescriptor();
				}
			} else {
				Uri uri = ContentUris.withAppendedId(albumArtUri, album_id);
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) {
					fd = pfd.getFileDescriptor();
				}
			}
			options.inSampleSize = 1;
			// 只进行大小判断
			options.inJustDecodeBounds = true;
			// 调用此方法得到option得到图片大小
			BitmapFactory.decodeFileDescriptor(fd, null, options);
			// 需要调用computerSampleSize得到图片的缩放比例
			options.inSampleSize = 100;
			// 开始正式读入Bitmap数据
			options.inJustDecodeBounds = false;
			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;

			// 根据option参数，减少所需要的内存
			bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bm;
	}

	/**
	 * 获取专辑封面位图对象
	 * 
	 * @param context
	 * @param song_id
	 * @param album_id
	 * @param allowdefault
	 * @return
	 */
	public static Bitmap getArtwork(Context context, long song_id,
			long album_id, boolean allowdefault, boolean small) {
		if (album_id < 0) {
			if (song_id >= 0) {
				Bitmap bm = getArtworkFromFile(context, song_id, -1);
				if (bm != null) {
					return bm;
				}
			}
			if (allowdefault) {
				return getDefaultArtwork(context, small);
			}
			return null;
		}
		ContentResolver resolver = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(albumArtUri, album_id);
		if (uri != null) {
			InputStream in = null;
			try {
				in = resolver.openInputStream(uri);
				BitmapFactory.Options options = new BitmapFactory.Options();
				// 先制定原始大小
				options.inSampleSize = 1;
				// 只进行大小判断
				options.inJustDecodeBounds = true;
				// 调用此方法得到option得到图片的大小
				BitmapFactory.decodeStream(in, null, options);
				/*
				 * 我们的目标是在你N pixel的画面上显示。 所以需要调用computeSampleSize得到图片缩放的比例
				 * 这里的target为800是根据默认专辑图片大小决定的，800只是测试数字但是试验后发现完美的
				 */
				if (small) {
					options.inSampleSize = computeSampleSize(options, 40);
				} else {
					options.inSampleSize = computeSampleSize(options, 600);
				}
				// 我们得到了缩放比例，现在开始读入Bitmap数据
				options.inJustDecodeBounds = false;
				options.inDither = false;
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;
				in = resolver.openInputStream(uri);
				return BitmapFactory.decodeStream(in, null, options);
			} catch (FileNotFoundException e) {
				Bitmap bm = getArtworkFromFile(context, song_id, album_id);
				if (bm != null) {
					if (bm.getConfig() == null) {
						bm = bm.copy(Bitmap.Config.RGB_565, false);
						if (bm == null && allowdefault) {
							return getDefaultArtwork(context, small);
						}
					}
				} else if (allowdefault) {
					bm = getDefaultArtwork(context, small);
				}
				return bm;
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static int computeSampleSize(Options options, int target) {
		int w = options.outWidth;
		int h = options.outHeight;
		int candidateW = w / target;
		int candidateH = h / target;
		int candidate = Math.max(candidateW, candidateH);
		if (candidate == 0) {
			return 1;
		}
		if (candidate > 1) {
			if ((w > target) && (w / candidate) < target) {
				candidate -= 1;
			}
		}
		if (candidate > 1) {
			if ((h > target) && (h / candidate) < target) {
				candidate -= 1;
			}
		}
		return candidate;
	}
}