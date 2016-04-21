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
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;

import com.linjinzhong.musicplayer.MainActivity;
import com.linjinzhong.musicplayer.PlayMusicListActivity;
import com.linjinzhong.musicplayer.PlayService;
import com.linjinzhong.musicplayer.R;

/** ��Բ��ŵ�λ����ĳЩ���������򲥷��б���Ӹ��� ,��ʾ��ʵ� */
public class PlayUtils {

	/** ��ĳ���б����һ�׸��� */
	public static boolean addMusicToList(Context context,
			HashMap<String, Object> map, List<HashMap<String, Object>> toList) {
		if (new File((String) map.get("path")).exists()) {
			if (toList.contains(map)) {
				Toast.makeText(context, "�Ѵ����ڲ����б�", 0).show();
				return true;
			} else {
				toList.add(map);
				Toast.makeText(context, "��ӳɹ���", 0).show();
				return true;
			}
		} else {
			// SD���Л]�иĸ����ŷ���false
			return false;// SD���Л]�иĸ����ŷ���false
		}
	}

	/** ����ĳһ�׸��� */
	public static void turnToPlay(HashMap<String, Object> map, Context context) {
		String name = (String) map.get("name");
		String path = (String) map.get("path");
		String artist = (String) map.get("artist");
		if (new File(path).exists()) {// ��������ԓ����
			Intent intent = new Intent(context, PlayService.class);
			intent.putExtra("name", name);
			intent.putExtra("path", path);
			intent.putExtra("artist", artist);
			context.startService(intent);
			TextView footer = (TextView) MainActivity.footer;
			footer.setText(name);
		} else {
			// ���ظ����ѱ�ɾ��
			Toast.makeText(
					context,
					name
							+ context.getResources().getString(
									R.string.text_error), 0).show();
		}
	}

	/** ���ĳһ�б�����������б�,���Ҳ��Ÿ��б�ĵ�һ�� */
	public static void turnToPlay_List(Context context,
			List<HashMap<String, Object>> musicList) {
		if (!musicList.isEmpty()) {// �б�Ϊ��
			for (int i = 0; i < musicList.size(); i++) {
				HashMap<String, Object> map = musicList.get(i);
				if (new File((String) map.get("path")).exists()) {
					if (!MyApplication.getPlayMusicList().contains(map)) {
						MyApplication.getPlayMusicList().add(map);
					}
				} else {
					// ���������ѱ��h��
					DataUtils.updata_all_list_when_SDdelete(map);
					Toast.makeText(
							context,
							(String) map.get("name")
									+ context.getResources().getString(
											R.string.text_error), 0).show();
				}
			}
			// ֪ͨ�����б����
			PlayMusicListActivity.myListAdapter.notifyDataSetChanged();
			if (musicList.size() > 0) {// �ж��ղ��б���ĸ����Ƿ�����һ�׻��ڱ���
				HashMap<String, Object> map = musicList.get(0);
				turnToPlay(map, context);
			} else {
				Toast.makeText(MainActivity.context, "���б��е������Ѵ�SD��ɾ��", 0)
						.show();
			}
		} else {
			Toast.makeText(MainActivity.context, "���б���û������", 0).show();
		}
	}

	/**
	 * ���ݸ������ִ�����������ͼƬ
	 * 
	 * @param keyword
	 * @return iss
	 */
	public static InputStream getInputStreamByKeyWord(String keyword) {
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

			// filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

			URL imageHttpUrl = new URL(imageUrl);

			URLConnection imageUrlConnection = imageHttpUrl.openConnection();

			iss = imageUrlConnection.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return iss;
	}

	public static String SDCardRoot = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + File.separator;

	/**
	 *  ����SD���ϵ��ļ���·�� 
	 * @param dir �ļ�������
	 * @return	dirFile �ļ���·��
	 */
	public static File createSDDir(String dir) {
		File dirFile = new File(SDCardRoot + dir + File.separator);
		return dirFile;
	}

	/** 
	 * ����SD���ϵ��ļ�/
	 * @param fileName �ļ���:��������+.jpg
	 * @param dir	�ļ�������
	 * @return file
	 * @throws IOException
	 */
	public static File createFileInSDCard(String fileName, String dir)
			throws IOException {
		File file = new File(SDCardRoot + dir + File.separator + fileName);
		boolean b = file.createNewFile();
		return file;
	}

	/**
	 * �����ļ������֣��������֣�����ͼƬ������ͼƬ��Ϣ���浽����
	 * @param path �ļ������� :MusicPlayer
	 * @param fileName �ļ�������������+.jpg
	 * @param input ����ͼƬ��
	 * @return
	 */
	public static File write2SDFromInput(String path, String fileName,
			InputStream input) {

		File file = null;
		OutputStream output = null;
		try {
			//��������ͼƬ�ļ���
			createSDDir(path);
			file = createFileInSDCard(fileName, path);
			output = new FileOutputStream(file);
			
			byte buffer[] = new byte[4 * 1024];
			int temp;
			while ((temp = input.read(buffer)) != -1) {
				output.write(buffer, 0, temp);
			}
			output.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				output.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	/**
	 * ��������·���������֣����ظ��·��
	 * 
	 * @param context
	 * @param musicName
	 * @param musicPath
	 * @return lrcFilePath
	 */
	public static String getLrcFile(Context context,String musicName,String musicPath){
		String lrcFileName=musicName+".lrc";
		String lrcFilePath=musicPath.substring(0, musicPath.length()-(musicName.length()+4))+lrcFileName;
		if(new File(lrcFilePath).exists()){
			return lrcFilePath;
		}else{
			return null;
		}
	}
}
