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

/** 针对播放单位进行某些操作，如向播放列表添加歌曲 ,显示歌词等 */
public class PlayUtils {

	/** 向某個列表添加一首歌曲 */
	public static boolean addMusicToList(Context context,
			HashMap<String, Object> map, List<HashMap<String, Object>> toList) {
		if (new File((String) map.get("path")).exists()) {
			if (toList.contains(map)) {
				Toast.makeText(context, "已存在于播放列表", 0).show();
				return true;
			} else {
				toList.add(map);
				Toast.makeText(context, "添加成功！", 0).show();
				return true;
			}
		} else {
			// SD卡中沒有改歌曲才返回false
			return false;// SD卡中沒有改歌曲才返回false
		}
	}

	/** 播放某一首歌曲 */
	public static void turnToPlay(HashMap<String, Object> map, Context context) {
		String name = (String) map.get("name");
		String path = (String) map.get("path");
		String artist = (String) map.get("artist");
		if (new File(path).exists()) {// 若本地有該歌曲
			Intent intent = new Intent(context, PlayService.class);
			intent.putExtra("name", name);
			intent.putExtra("path", path);
			intent.putExtra("artist", artist);
			context.startService(intent);
			TextView footer = (TextView) MainActivity.footer;
			footer.setText(name);
		} else {
			// 本地歌曲已被删除
			Toast.makeText(
					context,
					name
							+ context.getResources().getString(
									R.string.text_error), 0).show();
		}
	}

	/** 添加某一列表歌曲到播放列表,并且播放该列表的第一首 */
	public static void turnToPlay_List(Context context,
			List<HashMap<String, Object>> musicList) {
		if (!musicList.isEmpty()) {// 列表不为空
			for (int i = 0; i < musicList.size(); i++) {
				HashMap<String, Object> map = musicList.get(i);
				if (new File((String) map.get("path")).exists()) {
					if (!MyApplication.getPlayMusicList().contains(map)) {
						MyApplication.getPlayMusicList().add(map);
					}
				} else {
					// 歌曲本地已被刪除
					DataUtils.updata_all_list_when_SDdelete(map);
					Toast.makeText(
							context,
							(String) map.get("name")
									+ context.getResources().getString(
											R.string.text_error), 0).show();
				}
			}
			// 通知播放列表更新
			PlayMusicListActivity.myListAdapter.notifyDataSetChanged();
			if (musicList.size() > 0) {// 判断收藏列表里的歌曲是否至少一首还在本地
				HashMap<String, Object> map = musicList.get(0);
				turnToPlay(map, context);
			} else {
				Toast.makeText(MainActivity.context, "该列表中的音乐已从SD卡删除", 0)
						.show();
			}
		} else {
			Toast.makeText(MainActivity.context, "该列表中没有音乐", 0).show();
		}
	}

	/**
	 * 根据歌手名字从网络上下载图片
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
	 *  创建SD卡上的文件夹路径 
	 * @param dir 文件夹名字
	 * @return	dirFile 文件夹路径
	 */
	public static File createSDDir(String dir) {
		File dirFile = new File(SDCardRoot + dir + File.separator);
		return dirFile;
	}

	/** 
	 * 创建SD卡上的文件/
	 * @param fileName 文件名:歌手名字+.jpg
	 * @param dir	文件夹名字
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
	 * 根据文件夹名字，歌手名字，歌手图片流，将图片信息保存到本地
	 * @param path 文件夹名字 :MusicPlayer
	 * @param fileName 文件名：歌手名字+.jpg
	 * @param input 歌手图片流
	 * @return
	 */
	public static File write2SDFromInput(String path, String fileName,
			InputStream input) {

		File file = null;
		OutputStream output = null;
		try {
			//创建本地图片文件夹
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
	 * 根据音乐路径歌曲名字，返回歌词路径
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
