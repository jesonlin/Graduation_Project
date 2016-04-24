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
 * ����ͼƬ�Ĺ�����
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
	 * û�õ� ��inputstream��д��path������Ϊfilename
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
	 * û�õ� ���ݹؼ�������
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
	 * û�õ�
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
	 * û�õ�
	 * 
	 * @param keyword
	 * @return
	 */
	public static Map<String, String> generateImageFromSouGou(String keyword) {
		return generateImageFromSouGou("/", keyword);
	}

	/**
	 * ��ȡĬ����ʾͼƬ
	 * 
	 * @param context
	 * @param small
	 *            û�õ�
	 * @return Bitmap ����ͼƬ
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
	 * ��MP3�ļ��л�ȡר������λͼ
	 * 
	 * @param context
	 * @param song_id
	 *            ����ID
	 * @param album_id
	 *            ר��ID
	 * @return Bitmap ͼ�����
	 */
	public static Bitmap getArtworkFromFile(Context context, long song_id,
			long album_id) {
		Log.e("ImageUtils", "��ʼ��ȡͼƬ��Ϣ��");
		Bitmap bm = null;
		if (album_id < 0 && song_id < 0) {
			// �������ID��С��0˵��û����Ӧ�ĸ���
			Log.e("ImageUtils", "����id��С��0:song_id=" + song_id + ",album_id="
					+ album_id);
			throw new IllegalArgumentException(
					"Must specify an album or a song id");
		}
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			FileDescriptor fd = null;
			if (album_id < 0) {
				// ���ר��IDС��0��MP3�ļ��ڲ�ר��ID����Ϊ��,���ɸ���ID��ȡ
				Uri uri = Uri.parse("content://media/external/audio/media/"
						+ song_id + "/albumart");
				Log.e("ImageUtils", "uri_album_idС��0=" + uri);
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) {
					fd = pfd.getFileDescriptor();
				}
			} else {
				// ר��ID����0������ר��ID��ȡͼƬ
				Uri uri = ContentUris.withAppendedId(albumArtUri, album_id);
				Log.e("ImageUtils", "uri_album_id����0=" + uri);
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
	 * �ʼ��ȡר��ͼƬ�ĵ������ ��ȡר������λͼ���� ��ʵ��Ҫ��album_idΪ·������,uri��ʽ����
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
		Log.e("ImageUtils", "����getArtwork:song_id=" + song_id + "album_id="
				+ album_id);
		if (album_id < 0) {
			if (song_id >= 0) {
				Bitmap bm = getArtworkFromFile(context, song_id, -1);
				if (bm != null) {
					Log.e("ImageUtils", "�ɸ���ID��ȡ�õ�ר��ͼƬ");
					return bm;
				}
			}
			if (allowdefault) {
				Log.e("ImageUtils", "song_id<0&&album_id<0,����Ĭ��ͼƬ");
				return getDefaultArtwork(context, small);
			}
			return null;
		}
		Log.e("ImageUtils", "����ID������0");
		ContentResolver resolver = context.getContentResolver();
		// ���album_id��ͼƬ����Ϊ�յĻ����򵽴˲���ȡͼƬ
		Uri uri = ContentUris.withAppendedId(albumArtUri, album_id);
		Log.e("ImageUtils", "uri_261=" + uri);
		if (uri != null) {
			InputStream in = null;
			try {
				// �����uri·�����Ƿ����ͼƬ��һ����ĳ�в����ھ͵�catch����
				in = resolver.openInputStream(uri);
				BitmapFactory.Options options = new BitmapFactory.Options();
				Log.e("ImageUtils", "size=" + options.inSampleSize);
				// ���ǵõ������ű��������ڿ�ʼ����Bitmap����
				options.inJustDecodeBounds = false;
				options.inDither = false;
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;
				in = resolver.openInputStream(uri);
				return BitmapFactory.decodeStream(in, null, options);
			} catch (FileNotFoundException e) {
				// �����쳣
				Bitmap bm = getArtworkFromFile(context, song_id, album_id);
				Log.e("ImageUtils", "bm=" + bm);
				if (bm != null) {
					if (bm.getConfig() == null) {
						bm = bm.copy(Bitmap.Config.RGB_565, false);
						if (bm == null && allowdefault) {
							Log.e("ImageUtils", "316λ��,����Ĭ��ͼƬ");
							return getDefaultArtwork(context, small);
						}
					}
				} else if (allowdefault) {
					Log.e("ImageUtils", "323λ�ã�����Ĭ��ͼƬ");
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
	 * ͼƬ���ű�����أ�û���ϣ��е�����
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