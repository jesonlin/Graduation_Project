package com.linjinzhong.musicplayer.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Application;

/**获取本地播放器的五项列表*/
public class MyApplication extends Application{
	
	private static List<HashMap<String, Object>> playMusicList=new ArrayList<HashMap<String,Object>>();
	private static List<HashMap<String, Object>> allMusicList=new ArrayList<HashMap<String,Object>>();
	private static List<HashMap<String, Object>> favoriteMusicList=new ArrayList<HashMap<String,Object>>();
	private static List<HashMap<String, Object>> relaxMusicList=new ArrayList<HashMap<String,Object>>();
	private static List<HashMap<String, Object>> studyMusicList=new ArrayList<HashMap<String,Object>>();
	public static int playStatus;
	public static int musicPosition;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public static List<HashMap<String, Object>> getPlayMusicList() {
		return playMusicList;
	}

	public static List<HashMap<String, Object>> getAllMusicList() {
		return allMusicList;
	}

	public static List<HashMap<String, Object>> getFavoriteMusicList() {
		return favoriteMusicList;
	}

	public static List<HashMap<String, Object>> getRelaxMusicList() {
		return relaxMusicList;
	}

	public static List<HashMap<String, Object>> getStudyMusicList() {
		return studyMusicList;
	}
	
	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}
}
