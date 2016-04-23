package com.linjinzhong.musicplayer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.linjinzhong.musicplayer.Utils.MyApplication;
import com.linjinzhong.musicplayer.Utils.PlayUtils;

/**
 * ҡһҡ�и蹦�ܣ�Ϩ��״̬��Ҳ������ShakeListener����
 * 
 * @author lin_j
 * 
 */
public class ShakeService extends Service {

	public static ShakeListener shakeListener;
	private Vibrator vibrator;
	private static final int Interval_Time = 1000;
	private long lastTime = 0;

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

		// ҡһҡ����
		shakeListener.setOnShakelistener(new ShakeListener.OnShakeListener() {

			@Override
			public void onShake() {
				Log.e("ShakeService", "=================������ҡ��");
				// TODO Auto-generated method stub
				// ������ҡ�κ���ֹͣ����
				shakeListener.stop();
				if (System.currentTimeMillis() - lastTime > Interval_Time) {
					// ��ʼ��
					startVibrator();
					// ��ʼ�л�����
					startChangeMusic();
				}
				lastTime = System.currentTimeMillis();
				// ������ʼ����
				shakeListener.start();
			}
		});
		// ���ϵ��·�ת��Ļ
		shakeListener
				.setOnSilencelistener(new ShakeListener.OnSilenceListener() {

					@Override
					public void onSilence() {
						Log.e("ShakeService", "��Ļ���ϵ��·�ת��");
						if (MyApplication.playStatus == 1) {
							PlayService.pause();
						}
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
		if (MyApplication.getPlayMusicList() != null) {
			// �����б�Ϊ��
			if (MyApplication.getPlayMusicList().size() > 1) {
				// �����б������Ŀ����1
				PlayUtils.turnToPlay(
						MyApplication.getPlayMusicList().get(
								(MyApplication.musicPosition + 1)
										% MyApplication.getPlayMusicList()
												.size()), this);
			} else if (MyApplication.getPlayMusicList().size() == 1) {
				// �����б������ĿΪ1
				PlayUtils.turnToPlay(MyApplication.getPlayMusicList().get(0),
						this);
			} else {
				Toast.makeText(this, "��ǰ�����б�Ϊ�գ�", 0).show();
			}
		}
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
