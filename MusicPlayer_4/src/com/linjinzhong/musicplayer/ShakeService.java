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
 * 摇一摇切歌功能，熄屏状态下也可以与ShakeListener合用
 * 
 * @author lin_j
 * 
 */
public class ShakeService extends Service {

	public static ShakeListener shakeListener;
	private Vibrator vibrator;

	// 传感器管理器
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
		Log.e("ShakeService", "进入ShakeService的onCreate方法");
		vibrator = (Vibrator) getBaseContext().getSystemService(
				Context.VIBRATOR_SERVICE);
		shakeListener = new ShakeListener(getBaseContext());
		shakeListener.setOnShakelistener(new ShakeListener.OnShakeListener() {

			@Override
			public void onShake() {
				Log.e("ShakeService", "=================监听到摇晃");
				// TODO Auto-generated method stub
				// 监听到摇晃后先停止监听
				shakeListener.stop();
				// 开始震动
				Log.e("ShakeService", "开始震动");
				startVibrator();
				// 开始切换歌曲
				Log.e("ShakeService", "开始切换歌曲");
				startChangeMusic();
				// 继续开始监听
				shakeListener.start();
			}
		});
	}

	/**
	 * 开始震动函数
	 */
	void startVibrator() {
		// 震动500毫秒
		Log.e("ShakeService", "开始震动");
		vibrator.vibrate(300);
	}

	/**
	 * 切换歌曲功能
	 */
	void startChangeMusic() {
		Log.e("ShakeService", "摇一摇后开始切换歌曲");
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
