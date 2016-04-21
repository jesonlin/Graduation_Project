package com.linjinzhong.musicplayer;

import com.linjinzhong.musicplayer.Utils.DBHelper;
import com.linjinzhong.musicplayer.Utils.DataUtils;
import com.linjinzhong.musicplayer.Utils.MyApplication;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts.Data;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

/**
 * 启动界面
 * 后台创建数据库
 * 加载数据库中的所有音乐
 * 加载数据库其余列表
 * @author lin_j
 *
 */
public class StartActivity extends ActionBarActivity {
	
	public static Context context;
	private ContentResolver resolver;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_start);
		Log.e("StartActivity", "进入StartActivity");
		context = this;
		resolver = getContentResolver();//与ContentProvider相关，用于获取SD卡中的内容
		
		new AsyncTask<Void, Void, Void>() {
			/*异步操作类，内部实现程序初始化时候，搜索本地音乐列表，
			加载播放列表，我的音乐里面的三个收藏列表*/
			@Override
			protected Void doInBackground(Void... params) {
				SQLiteDatabase db = new DBHelper(context, "myData").getWritableDatabase();
			/*	创建一个DBHelper类继承SQLiteOpenHelper类，它作为一个访问Sqlite的助手类，提供了两方面的功能
				1：getReadableDatabase()（只读数据库）/getWritableDatabase()（可写数据库）可以获得SQLiteDatabase对象，通过该对象可以对数据库进行操作；
				2：提供OnCreate()和onUpgrade()两个回调函数，允许我们在创建和升级数据库时，进行自己的操作*/
				
				//如果数据库中没有那五项，则创建
				db.execSQL("create table if not exists all_music (id integer primary key autoincrement not null, name varchar(50) , path varchar(100) , artist varchar(50))");
				db.execSQL("create table if not exists play_music (id integer primary key autoincrement not null, name varchar(50) , path varchar(100) , artist varchar(50))");
				db.execSQL("create table if not exists my_favorite_music (id integer primary key autoincrement not null, name varchar(50) , path varchar(100) , artist varchar(50))");
				db.execSQL("create table if not exists my_relax_music (id integer primary key autoincrement not null, name varchar(50) , path varchar(100) , artist varchar(50))");		
				db.execSQL("create table if not exists my_study_music (id integer primary key autoincrement not null, name varchar(50) , path varchar(100) , artist varchar(50))");	
				db.close();
				
				//加载数据库中的所有音乐到所有音乐列表
				DataUtils.get_AllMusicListFDB(context);
				if(MyApplication.getAllMusicList().isEmpty()){//如果此时所有音乐列表仍为为空，则从本地SD卡更新音乐到数据库
					DataUtils.update_AllMusicDataTDB(resolver, context);
				//	DataUtils.get_AllMusicListFDB(context);
				}
				DataUtils.get_otherListFDB();
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				// TODO Auto-generated method stub
				startActivity(new Intent(context,MainActivity.class));
				finish();
				super.onPostExecute(result);
			}
			
		}.execute();
	}
}
