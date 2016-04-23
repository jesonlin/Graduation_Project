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
import android.util.Log;

import com.linjinzhong.musicplayer.R;
import com.linjinzhong.musicplayer.R.string;

/**
 * 处理图片的工具类
 * 
 * @author lin_j
 * 
 */
public class ImageUtils {

	private static String filename = null;

	private static final Uri albumArtUri = Uri
			.parse("content://media/external/audio/albumart");

	private static Map<String, String> imageMap = new HashMap<String, String>();

	private ImageUtils() {
	}

	/**
	 * 没用到 将inputstream流写入path中名字为filename
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
	 * 没用到 根据关键词下载
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

	/**
	 * 没用到
	 * 
	 * @param path
	 * @param keyword
	 * @return
	 */
	public static Map<String, String> generateImageFromSouGou(String path,
			String keyword) {
		InputStream is = getInputstreamByKeyWord(keyword);

		imageMap.put(keyword, filename);

		writeToLocal(is, path);

		return imageMap;
	}

	/**
	 * 没用到
	 * 
	 * @param keyword
	 * @return
	 */
	public static Map<String, String> generateImageFromSouGou(String keyword) {
		return generateImageFromSouGou("/", keyword);
	}

	/**
	 * 获取默认显示图片
	 * 
	 * @param context
	 * @param small
	 *            没用到
	 * @return Bitmap 对象图片
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
	 * 从MP3文件中获取专辑封面位图
	 * 
	 * @param context
	 * @param song_id
	 *            歌曲ID
	 * @param album_id
	 *            专辑ID
	 * @return Bitmap 图像对象
	 */
	public static Bitmap getArtworkFromFile(Context context, long song_id,
			long album_id) {
		Log.e("ImageUtils", "开始获取图片信息流");
		Bitmap bm = null;
		if (album_id < 0 && song_id < 0) {
			// 如果两个ID都小于0说明没有相应的歌曲
			Log.e("ImageUtils", "两个id都小于0:song_id=" + song_id + ",album_id="
					+ album_id);
			throw new IllegalArgumentException(
					"Must specify an album or a song id");
		}
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			FileDescriptor fd = null;
			if (album_id < 0) {
				// 如果专辑ID小于0即MP3文件内部专辑ID部分为空,则由歌曲ID获取
				Uri uri = Uri.parse("content://media/external/audio/media/"
						+ song_id + "/albumart");
				Log.e("ImageUtils", "uri_album_id小于0=" + uri);
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) {
					fd = pfd.getFileDescriptor();
				}
			} else {
				// 专辑ID大于0，则由专辑ID获取图片
				Uri uri = ContentUris.withAppendedId(albumArtUri, album_id);
				Log.e("ImageUtils", "uri_album_id大于0=" + uri);
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
	 * 最开始获取专辑图片的调用入口 获取专辑封面位图对象 其实主要从album_id为路径查找,uri格式如下
	 * uri=content://media/external/audio/albumart/"album_id"
	 * 
	 * @param context
	 * @param song_id
	 * @param album_id
	 * @param allowdefault
	 * @return
	 */
	public static Bitmap getArtwork(Context context, long song_id,
			long album_id, boolean allowdefault, boolean small) {
		Log.e("ImageUtils", "进入getArtwork:song_id=" + song_id + "album_id="
				+ album_id);
		if (album_id < 0) {
			if (song_id >= 0) {
				Bitmap bm = getArtworkFromFile(context, song_id, -1);
				if (bm != null) {
					Log.e("ImageUtils", "由歌曲ID获取得到专辑图片");
					return bm;
				}
			}
			if (allowdefault) {
				Log.e("ImageUtils", "song_id<0&&album_id<0,返回默认图片");
				return getDefaultArtwork(context, small);
			}
			return null;
		}
		Log.e("ImageUtils", "两个ID都大于0");
		ContentResolver resolver = context.getContentResolver();
		// 如果album_id与图片都不为空的话，则到此步获取图片
		Uri uri = ContentUris.withAppendedId(albumArtUri, album_id);
		Log.e("ImageUtils", "uri_261=" + uri);
		if (uri != null) {
			InputStream in = null;
			try {
				// 捕获该uri路径下是否存在图片，一旦到某行不存在就到catch那里
				in = resolver.openInputStream(uri);
				BitmapFactory.Options options = new BitmapFactory.Options();
				Log.e("ImageUtils", "size=" + options.inSampleSize);
				// 我们得到了缩放比例，现在开始读入Bitmap数据
				options.inJustDecodeBounds = false;
				options.inDither = false;
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;
				in = resolver.openInputStream(uri);
				return BitmapFactory.decodeStream(in, null, options);
			} catch (FileNotFoundException e) {
				// 捕获到异常
				Bitmap bm = getArtworkFromFile(context, song_id, album_id);
				Log.e("ImageUtils", "bm=" + bm);
				if (bm != null) {
					if (bm.getConfig() == null) {
						bm = bm.copy(Bitmap.Config.RGB_565, false);
						if (bm == null && allowdefault) {
							Log.e("ImageUtils", "316位置,返回默认图片");
							return getDefaultArtwork(context, small);
						}
					}
				} else if (allowdefault) {
					Log.e("ImageUtils", "323位置，返回默认图片");
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

	/**
	 * 图片缩放比例相关，没用上，有点问题
	 * 
	 * @param options
	 * @param target
	 * @return
	 */
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