package com.linjinzhong.musicplayer.Utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	/** �ڶ����DBHelper���������󣬾Ϳ���ʹ������ഴ��Sqlite���ݿ⣬�������ݿ���в����ˣ� */

	private static final int VERSION = 1;

	// �ù��췽������ȱ��
	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	public DBHelper(Context context, String name, int version) {
		this(context, name, null, version);
	}

	public DBHelper(Context context, String name) {
		this(context, name, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// �ú������ڵ�һ�δ������ݿ��ʱ��ִ��,ʵ�������ڵ�һ�εõ�SQLiteDatabse�����ʱ�򣬲Ż�����������
		System.out.println("Create a Database!!!");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		super.onOpen(db);
	}
}
