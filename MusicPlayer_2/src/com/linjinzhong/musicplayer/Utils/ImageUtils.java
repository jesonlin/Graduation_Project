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
 * ��������ͼƬ��
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
	 * ��inputstream��д��path������Ϊfilename
	 * 
	 * @param is
	 *            ������
	 * @param path
	 *            Ҫ��ŵ�·��
	 * @param filename
	 *            �ļ���
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
	 * ���ݹؼ�������
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
	 * ��ȡĬ����ʾͼƬ
	 * 
	 * @param context
	 * @param small
	 * @return
	 */
	public static Bitmap getDefaultArtwork(Context context, boolean small) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		if (small) {// ����СͼƬ,Ĭ����ʾ������ͼƬ
			return BitmapFactory.decodeStream(context.getResources()
					.openRawResource(R.drawable.play_image), null, options);
		}
		return BitmapFactory.decodeStream(context.getResources()
				.openRawResource(R.drawable.play_image), null, options);
	}

	/**
	 * ���ļ��л�ȡר������λͼ
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
			// ֻ���д�С�ж�
			options.inJustDecodeBounds = true;
			// ���ô˷����õ�option�õ�ͼƬ��С
			BitmapFactory.decodeFileDescriptor(fd, null, options);
			// ��Ҫ����computerSampleSize�õ�ͼƬ�����ű���
			options.inSampleSize = 100;
			// ��ʼ��ʽ����Bitmap����
			options.inJustDecodeBounds = false;
			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;

			// ����option��������������Ҫ���ڴ�
			bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bm;
	}

	/**
	 * ��ȡר������λͼ����
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
				// ���ƶ�ԭʼ��С
				options.inSampleSize = 1;
				// ֻ���д�С�ж�
				options.inJustDecodeBounds = true;
				// ���ô˷����õ�option�õ�ͼƬ�Ĵ�С
				BitmapFactory.decodeStream(in, null, options);
				/*
				 * ���ǵ�Ŀ��������N pixel�Ļ�������ʾ�� ������Ҫ����computeSampleSize�õ�ͼƬ���ŵı���
				 * �����targetΪ800�Ǹ���Ĭ��ר��ͼƬ��С�����ģ�800ֻ�ǲ������ֵ����������������
				 */
				if (small) {
					options.inSampleSize = computeSampleSize(options, 40);
				} else {
					options.inSampleSize = computeSampleSize(options, 600);
				}
				// ���ǵõ������ű��������ڿ�ʼ����Bitmap����
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