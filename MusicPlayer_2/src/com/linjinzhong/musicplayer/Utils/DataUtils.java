package com.linjinzhong.musicplayer.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;

import com.linjinzhong.musicplayer.AllMusicListActivity;
import com.linjinzhong.musicplayer.MainActivity;
import com.linjinzhong.musicplayer.MyMusicListActivity;
import com.linjinzhong.musicplayer.PlayMusicListActivity;
import com.linjinzhong.musicplayer.StartActivity;

/**
 * 数据库与本地播放器之间的操作 从数据库更新音乐到各个列表 从各个列表更新数据库
 * 
 * @author lin_j
 * 
 */
public class DataUtils {
	private static List<MusicDemo> musicList = new ArrayList<MusicDemo>();

	/** 从本地SD卡更新所有音乐到数据库中 */
	public static void update_AllMusicDataTDB(ContentResolver resolver,
			Context context) {
		Log.e("DataUtils", "本地SDka更新数据库中的所有音乐列表");
		/* 先创建一个临时音乐列表，从本地SD卡更新 */
		musicList.clear();// 先清空临时音乐列表
		// ContentResolver是通过ContentProvider来获取其他与应用程序共享的数据，这里是查询外部SD卡上的音频文件，返回Cursor对象
		Cursor cursor = resolver.query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		while (cursor.moveToNext()) {
			// 歌曲名称
			String name = cursor.getString(cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
			// 歌曲歌手名
			String artist = cursor.getString(cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
			// 歌曲文件的路径
			String path = cursor.getString(cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
			
			musicList.add(new MusicDemo(name, path, artist));
			System.out.println("===============SD-临时列表" + name + path + artist);
		}

		/* 再将该临时音乐列表更新数据库中的所有音乐 */
		DBHelper dbHelper = new DBHelper(context, "myData");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("delete from all_music");// 先清空就数据库中的所有音乐
		if (!musicList.isEmpty()) {// 如果SD卡上有音乐
			System.out.println("==============进入临时列表选项");
			for (Iterator<MusicDemo> iterator = musicList.iterator(); iterator
					.hasNext();) {
				MusicDemo demo = (MusicDemo) iterator.next();
				ContentValues values = new ContentValues();
				values.put("name", demo.getName());
				values.put("path", demo.getPath());
				values.put("artist", demo.getArtist());
				System.out.println("======临时列表-DB=====" + demo.getName()
						+ demo.getPath() + demo.getArtist());
				db.insert("all_music", null, values);
			}
		}
		db.close();
	}

	/**
	 * 从数据库加载所有音乐列表
	 */
	public static void get_AllMusicListFDB(Context context) {
		Log.e("DataUtils", "加载数据库中的所有音乐列表");
		if (!MyApplication.getAllMusicList().isEmpty()) {// 如果所有音乐列表不为空
			MyApplication.getAllMusicList().clear();
		}
		DBHelper dbHelper = new DBHelper(context, "myData");
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		// cursor为获取数据库myData中数据的迭代器
		Cursor cursor = db.rawQuery("select name,path,artist from all_music",
				null);
		while (cursor.moveToNext()) {// cursor往后移动直到最后一行
			String name = cursor.getString(cursor.getColumnIndex("name"));
			String path = cursor.getString(cursor.getColumnIndex("path"));
			String artist = cursor.getString(cursor.getColumnIndex("artist"));
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("name", name);
			map.put("path", path);
			map.put("artist", artist);
			MyApplication.getAllMusicList().add(map);
		}
		cursor.close();
		db.close();
	}

	/**
	 * 从数据库加载播放列表
	 */
	public static void get_PlayMusicListFDB() {
		if (!MyApplication.getPlayMusicList().isEmpty()) {
			MyApplication.getPlayMusicList().clear();// 不为空先清空
		}
		DBHelper dbHelper = new DBHelper(StartActivity.context, "myData");
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("Select name,path,artist from play_music",
				null);
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex("name"));
			String path = cursor.getString(cursor.getColumnIndex("path"));
			String artist = cursor.getString(cursor.getColumnIndex("artist"));
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("name", name);
			map.put("path", path);
			map.put("artist", artist);
			MyApplication.getPlayMusicList().add(map);
		}
		db.close();
	}

	/** 从数据库加载最喜欢列表 */
	public static void get_FavoriteMusicListFDB() {
		if (!MyApplication.getFavoriteMusicList().isEmpty()) {
			MyApplication.getFavoriteMusicList().clear();
		}
		DBHelper dbHelper = new DBHelper(StartActivity.context, "myData");
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"Select name,path,artist from my_favorite_music", null);
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex("name"));
			String path = cursor.getString(cursor.getColumnIndex("path"));
			String artist = cursor.getString(cursor.getColumnIndex("artist"));
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("name", name);
			map.put("path", path);
			map.put("artist", artist);
			MyApplication.getFavoriteMusicList().add(map);
		}
		db.close();
	}

	/** 从数据库加载放松列表 */
	public static void get_RelaxMusicListFD() {
		if (!MyApplication.getRelaxMusicList().isEmpty()) {
			MyApplication.getRelaxMusicList().clear();
		}
		DBHelper dbHelper = new DBHelper(StartActivity.context, "myData");
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"Select name,path,artist from my_relax_music", null);
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex("name"));
			String path = cursor.getString(cursor.getColumnIndex("path"));
			String artist = cursor.getString(cursor.getColumnIndex("artist"));
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("name", name);
			map.put("path", path);
			map.put("artist", artist);
			MyApplication.getRelaxMusicList().add(map);
		}
		db.close();
	}

	/** 从数据库加载学习列表 */
	public static void get_StudyMusicListFD() {
		if (!MyApplication.getStudyMusicList().isEmpty()) {
			MyApplication.getStudyMusicList().clear();
		}
		DBHelper dbHelper = new DBHelper(StartActivity.context, "myData");
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"Select name,path,artist from my_study_music", null);
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex("name"));
			String path = cursor.getString(cursor.getColumnIndex("path"));
			String artist = cursor.getString(cursor.getColumnIndex("artist"));
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("name", name);
			map.put("path", path);
			map.put("artist", artist);
			MyApplication.getStudyMusicList().add(map);
		}
		db.close();
	}

	/** 从数据库加载五个列表 */
	public static void get_otherListFDB() {
		Log.e("DataUtils", "加载数据库中的五个列表");
		get_AllMusicListFDB(StartActivity.context);
		get_PlayMusicListFDB();
		get_FavoriteMusicListFDB();
		get_RelaxMusicListFD();
		get_StudyMusicListFD();
	}

	/** 某个本地列表变化后更新数据库中相应列表 */
	public static void upData_Lists(Context context, String table_name) {
		Log.e("DataUtils", "更新数据库中的某个列表");
		List<HashMap<String, Object>> musicList = new ArrayList<HashMap<String, Object>>();
		if (table_name.equals("play_music")) {
			musicList = MyApplication.getPlayMusicList();
		} else if (table_name.equals("my_favorite_music")) {
			musicList = MyApplication.getFavoriteMusicList();
		} else if (table_name.equals("my_relax_music")) {
			musicList = MyApplication.getRelaxMusicList();
		} else if (table_name.equals("my_study_music")) {
			musicList = MyApplication.getStudyMusicList();
		}
		SQLiteDatabase db = new DBHelper(context, "myData")
				.getWritableDatabase();
		db.execSQL("delete from " + table_name);// 先找到变化的列表对应的数据库中的该列表
		db.execSQL("update sqlite_sequence SET seq = 0 where name = '"
				+ table_name + "'");// 将该列表的自增序号变成0也就是情况该列表的数据
		for (Iterator<HashMap<String, Object>> iterator = musicList.iterator(); iterator
				.hasNext();) {
			HashMap<String, Object> hashMap = (HashMap<String, Object>) iterator
					.next();
			ContentValues values = new ContentValues();
			values.put("name", (String) hashMap.get("name"));
			values.put("path", (String) hashMap.get("path"));
			values.put("artist", (String) hashMap.get("artist"));
			db.insert(table_name, null, values);// 从变化的本地列表中将一个个数据项重新插入数据库相应表中
		}
		db.close();
	}

	/** 更新数据库中其他四个列表 */
	public static void upData_otherLists() {
		Log.e("DataUtils", "更新数据库中的其余四个列表");
		upData_Lists(MainActivity.context, "play_music");
		upData_Lists(MainActivity.context, "my_favorite_music");
		upData_Lists(MainActivity.context, "my_relax_music");
		upData_Lists(MainActivity.context, "my_study_music");
	}

	/**
	 * 从SD卡删除某首歌后，更新某个列表及其控件
	 */
	public static void updata_list_when_SDdelete(int p,
			List<HashMap<String, Object>> list, HashMap<String, Object> map) {
		Log.e("DataUtils", "更新" + p + "列表" + "list.size()=" + list.size());
		if (!list.isEmpty()) {// 要更新的列表不为空
			Log.e("DataUtils", "进入循环判断1");
			for (int i = 0; i < list.size(); i++) {
				Log.e("DataUtils", "进入循环2");
				// 不同列表中的map不能直接==判断相等与否，即使里面的键值对内容都相等,用equal可以判断内容是否相等
				if (map.equals((HashMap<String, Object>) list.get(i))) {
					Log.e("DataUtils", "相等");
					list.remove(i);
					Log.e("DataUtils", "p=" + p + "i=" + i);
					switch (p) {
					case 1:
						AllMusicListActivity.listAdapter.notifyDataSetChanged();
						break;
					case 2:
						PlayMusicListActivity.myListAdapter
								.notifyDataSetChanged();
						break;
					default:
						MyMusicListActivity.myMusicListAdapter
								.notifyDataSetChanged();
						break;
					}
					break;
				}
			}
		}
	}

	/**
	 * 从SD卡删除某首歌后，更新所有的列表以及控件
	 */
	public static void updata_all_list_when_SDdelete(HashMap<String, Object> map) {
		Log.e("DataUtils", "从SD卡删除某首歌后，更新所有的列表以及控件");
		updata_list_when_SDdelete(1, MyApplication.getAllMusicList(), map);
		updata_list_when_SDdelete(2, MyApplication.getPlayMusicList(), map);
		updata_list_when_SDdelete(3, MyApplication.getFavoriteMusicList(), map);
		updata_list_when_SDdelete(4, MyApplication.getRelaxMusicList(), map);
		updata_list_when_SDdelete(5, MyApplication.getStudyMusicList(), map);
	}
}
