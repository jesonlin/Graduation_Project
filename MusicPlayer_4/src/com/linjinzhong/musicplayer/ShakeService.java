package com.linjinzhong.musicplayer;

import com.linjinzhong.musicplayer.Utils.MusicDemo;
import com.linjinzhong.musicplayer.Utils.MyApplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

/**
 * ҡһҡ�и蹦�ܣ�Ϩ��״̬��Ҳ������ShakeListener����
 * 
 * @author lin_j
 * 
 */
public class ShakeService extends Service {

	public static ShakeListener shakeListener;
	private Vibrator vibrator;

	// ������������
	public static SensorManager sensorManager;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.e("ShakeService", "����ShakeService��onCreate����");
		vibrator = (Vibrator) getBaseContext().getSystemService(
				Context.VIBRATOR_SERVICE);
		shakeListener = new ShakeListener(getBaseContext());
		shakeListener.setOnShakelistener(new ShakeListener.OnShakeListener() {

			@Override
			public void onShake() {
				Log.e("ShakeService", "=================������ҡ��");
				// TODO Auto-generated method stub
				// ������ҡ�κ���ֹͣ����
				shakeListener.stop();
				// ��ʼ��
				Log.e("ShakeService", "��ʼ��");
				startVibrator();
				// ��ʼ�л�����
				Log.e("ShakeService", "��ʼ�л�����");
				startChangeMusic();
				// ������ʼ����
				shakeListener.start();
			}
		});
	}

	/**
	 * ��ʼ�𶯺���
	 */
	void startVibrator() {
		// ��500����
		Log.e("ShakeService", "��ʼ��");
		vibrator.vibrate(300);
	}

	/**
	 * �л���������
	 */
	void startChangeMusic() {
		Log.e("ShakeService", "ҡһҡ��ʼ�л�����");
		PlayMusicActivity.next(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		shakeListener.start();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		shakeListener.stop();
	}
}
