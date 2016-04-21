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
 * ��������
 * ��̨�������ݿ�
 * �������ݿ��е���������
 * �������ݿ������б�
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
		Log.e("StartActivity", "����StartActivity");
		context = this;
		resolver = getContentResolver();//��ContentProvider��أ����ڻ�ȡSD���е�����
		
		new AsyncTask<Void, Void, Void>() {
			/*�첽�����࣬�ڲ�ʵ�ֳ����ʼ��ʱ���������������б�
			���ز����б��ҵ���������������ղ��б�*/
			@Override
			protected Void doInBackground(Void... params) {
				SQLiteDatabase db = new DBHelper(context, "myData").getWritableDatabase();
			/*	����һ��DBHelper��̳�SQLiteOpenHelper�࣬����Ϊһ������Sqlite�������࣬�ṩ��������Ĺ���
				1��getReadableDatabase()��ֻ�����ݿ⣩/getWritableDatabase()����д���ݿ⣩���Ի��SQLiteDatabase����ͨ���ö�����Զ����ݿ���в�����
				2���ṩOnCreate()��onUpgrade()�����ص����������������ڴ������������ݿ�ʱ�������Լ��Ĳ���*/
				
				//������ݿ���û��������򴴽�
				db.execSQL("create table if not exists all_music (id integer primary key autoincrement not null, name varchar(50) , path varchar(100) , artist varchar(50))");
				db.execSQL("create table if not exists play_music (id integer primary key autoincrement not null, name varchar(50) , path varchar(100) , artist varchar(50))");
				db.execSQL("create table if not exists my_favorite_music (id integer primary key autoincrement not null, name varchar(50) , path varchar(100) , artist varchar(50))");
				db.execSQL("create table if not exists my_relax_music (id integer primary key autoincrement not null, name varchar(50) , path varchar(100) , artist varchar(50))");		
				db.execSQL("create table if not exists my_study_music (id integer primary key autoincrement not null, name varchar(50) , path varchar(100) , artist varchar(50))");	
				db.close();
				
				//�������ݿ��е��������ֵ����������б�
				DataUtils.get_AllMusicListFDB(context);
				if(MyApplication.getAllMusicList().isEmpty()){//�����ʱ���������б���ΪΪ�գ���ӱ���SD���������ֵ����ݿ�
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
