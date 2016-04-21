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
 * ���ݿ��뱾�ز�����֮��Ĳ��� �����ݿ�������ֵ������б� �Ӹ����б�������ݿ�
 * 
 * @author lin_j
 * 
 */
public class DataUtils {
	private static List<MusicDemo> musicList = new ArrayList<MusicDemo>();

	/** �ӱ���SD�������������ֵ����ݿ��� */
	public static void update_AllMusicDataTDB(ContentResolver resolver,
			Context context) {
		Log.e("DataUtils", "����SDka�������ݿ��е����������б�");
		/* �ȴ���һ����ʱ�����б��ӱ���SD������ */
		musicList.clear();// �������ʱ�����б�
		// ContentResolver��ͨ��ContentProvider����ȡ������Ӧ�ó���������ݣ������ǲ�ѯ�ⲿSD���ϵ���Ƶ�ļ�������Cursor����
		Cursor cursor = resolver.query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		while (cursor.moveToNext()) {
			// ��������
			String name = cursor.getString(cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
			// ����������
			String artist = cursor.getString(cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
			// �����ļ���·��
			String path = cursor.getString(cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
			
			musicList.add(new MusicDemo(name, path, artist));
			System.out.println("===============SD-��ʱ�б�" + name + path + artist);
		}

		/* �ٽ�����ʱ�����б�������ݿ��е��������� */
		DBHelper dbHelper = new DBHelper(context, "myData");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("delete from all_music");// ����վ����ݿ��е���������
		if (!musicList.isEmpty()) {// ���SD����������
			System.out.println("==============������ʱ�б�ѡ��");
			for (Iterator<MusicDemo> iterator = musicList.iterator(); iterator
					.hasNext();) {
				MusicDemo demo = (MusicDemo) iterator.next();
				ContentValues values = new ContentValues();
				values.put("name", demo.getName());
				values.put("path", demo.getPath());
				values.put("artist", demo.getArtist());
				System.out.println("======��ʱ�б�-DB=====" + demo.getName()
						+ demo.getPath() + demo.getArtist());
				db.insert("all_music", null, values);
			}
		}
		db.close();
	}

	/**
	 * �����ݿ�������������б�
	 */
	public static void get_AllMusicListFDB(Context context) {
		Log.e("DataUtils", "�������ݿ��е����������б�");
		if (!MyApplication.getAllMusicList().isEmpty()) {// ������������б�Ϊ��
			MyApplication.getAllMusicList().clear();
		}
		DBHelper dbHelper = new DBHelper(context, "myData");
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		// cursorΪ��ȡ���ݿ�myData�����ݵĵ�����
		Cursor cursor = db.rawQuery("select name,path,artist from all_music",
				null);
		while (cursor.moveToNext()) {// cursor�����ƶ�ֱ�����һ��
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
	 * �����ݿ���ز����б�
	 */
	public static void get_PlayMusicListFDB() {
		if (!MyApplication.getPlayMusicList().isEmpty()) {
			MyApplication.getPlayMusicList().clear();// ��Ϊ�������
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

	/** �����ݿ������ϲ���б� */
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

	/** �����ݿ���ط����б� */
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

	/** �����ݿ����ѧϰ�б� */
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

	/** �����ݿ��������б� */
	public static void get_otherListFDB() {
		Log.e("DataUtils", "�������ݿ��е�����б�");
		get_AllMusicListFDB(StartActivity.context);
		get_PlayMusicListFDB();
		get_FavoriteMusicListFDB();
		get_RelaxMusicListFD();
		get_StudyMusicListFD();
	}

	/** ĳ�������б�仯��������ݿ�����Ӧ�б� */
	public static void upData_Lists(Context context, String table_name) {
		Log.e("DataUtils", "�������ݿ��е�ĳ���б�");
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
		db.execSQL("delete from " + table_name);// ���ҵ��仯���б��Ӧ�����ݿ��еĸ��б�
		db.execSQL("update sqlite_sequence SET seq = 0 where name = '"
				+ table_name + "'");// �����б��������ű��0Ҳ����������б������
		for (Iterator<HashMap<String, Object>> iterator = musicList.iterator(); iterator
				.hasNext();) {
			HashMap<String, Object> hashMap = (HashMap<String, Object>) iterator
					.next();
			ContentValues values = new ContentValues();
			values.put("name", (String) hashMap.get("name"));
			values.put("path", (String) hashMap.get("path"));
			values.put("artist", (String) hashMap.get("artist"));
			db.insert(table_name, null, values);// �ӱ仯�ı����б��н�һ�������������²������ݿ���Ӧ����
		}
		db.close();
	}

	/** �������ݿ��������ĸ��б� */
	public static void upData_otherLists() {
		Log.e("DataUtils", "�������ݿ��е������ĸ��б�");
		upData_Lists(MainActivity.context, "play_music");
		upData_Lists(MainActivity.context, "my_favorite_music");
		upData_Lists(MainActivity.context, "my_relax_music");
		upData_Lists(MainActivity.context, "my_study_music");
	}

	/**
	 * ��SD��ɾ��ĳ�׸�󣬸���ĳ���б���ؼ�
	 */
	public static void updata_list_when_SDdelete(int p,
			List<HashMap<String, Object>> list, HashMap<String, Object> map) {
		Log.e("DataUtils", "����" + p + "�б�" + "list.size()=" + list.size());
		if (!list.isEmpty()) {// Ҫ���µ��б�Ϊ��
			Log.e("DataUtils", "����ѭ���ж�1");
			for (int i = 0; i < list.size(); i++) {
				Log.e("DataUtils", "����ѭ��2");
				// ��ͬ�б��е�map����ֱ��==�ж������񣬼�ʹ����ļ�ֵ�����ݶ����,��equal�����ж������Ƿ����
				if (map.equals((HashMap<String, Object>) list.get(i))) {
					Log.e("DataUtils", "���");
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
	 * ��SD��ɾ��ĳ�׸�󣬸������е��б��Լ��ؼ�
	 */
	public static void updata_all_list_when_SDdelete(HashMap<String, Object> map) {
		Log.e("DataUtils", "��SD��ɾ��ĳ�׸�󣬸������е��б��Լ��ؼ�");
		updata_list_when_SDdelete(1, MyApplication.getAllMusicList(), map);
		updata_list_when_SDdelete(2, MyApplication.getPlayMusicList(), map);
		updata_list_when_SDdelete(3, MyApplication.getFavoriteMusicList(), map);
		updata_list_when_SDdelete(4, MyApplication.getRelaxMusicList(), map);
		updata_list_when_SDdelete(5, MyApplication.getStudyMusicList(), map);
	}
}
