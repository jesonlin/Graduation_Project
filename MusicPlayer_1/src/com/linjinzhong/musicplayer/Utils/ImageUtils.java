package com.linjinzhong.musicplayer.Utils;

import java.io.BufferedReader;
import java.io.File;
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

/**
 * 处理下载图片的，没用到
 * @author lin_j
 *
 */
public class ImageUtils {

	private static String filename = null;

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

	public static InputStream getInputstreamByKeyWord(String keyword) {

		InputStream iss = null;
		InputStream is = null;
		try {

			String url = "http://pic.sogou.com/pics?query=" + URLEncoder.encode(keyword, "GB2312");

			URL u = new URL(url);

			URLConnection uc = u.openConnection();

			is = uc.getInputStream();

			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "GB2312"));

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

	public static Map<String, String> generateImageFromSouGou(String path, String keyword) {
		InputStream is = getInputstreamByKeyWord(keyword);

		imageMap.put(keyword, filename);

		writeToLocal(is, path);

		return imageMap;
	}

	public static Map<String, String> generateImageFromSouGou(String keyword) {
		return generateImageFromSouGou("/", keyword);
	}

}