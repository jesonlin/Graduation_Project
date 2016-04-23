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
 * 摇一摇切歌功能，熄屏状态下也可以与ShakeListener合用
 * 
 * @author lin_j
 * 
 */
public class ShakeService extends Service {

	public static ShakeListener shakeListener;
	private Vibrator vibrator;
	private static final int Interval_Time = 1000;
	private long lastTime = 0;

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

		// 摇一摇监听
		shakeListener.setOnShakelistener(new ShakeListener.OnShakeListener() {

			@Override
			public void onShake() {
				Log.e("ShakeService", "=================监听到摇晃");
				// TODO Auto-generated method stub
				// 监听到摇晃后先停止监听
				shakeListener.stop();
				if (System.currentTimeMillis() - lastTime > Interval_Time) {
					// 开始震动
					startVibrator();
					// 开始切换歌曲
					startChangeMusic();
				}
				lastTime = System.currentTimeMillis();
				// 继续开始监听
				shakeListener.start();
			}
		});
		// 从上到下翻转屏幕
		shakeListener
				.setOnSilencelistener(new ShakeListener.OnSilenceListener() {

					@Override
					public void onSilence() {
						Log.e("ShakeService", "屏幕从上到下翻转了");
						if (MyApplication.playStatus == 1) {
							PlayService.pause();
						}
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
		if (MyApplication.getPlayMusicList() != null) {
			// 播放列表不为空
			if (MyApplication.getPlayMusicList().size() > 1) {
				// 播放列表歌曲数目大于1
				PlayUtils.turnToPlay(
						MyApplication.getPlayMusicList().get(
								(MyApplication.musicPosition + 1)
										% MyApplication.getPlayMusicList()
												.size()), this);
			} else if (MyApplication.getPlayMusicList().size() == 1) {
				// 播放列表歌曲数目为1
				PlayUtils.turnToPlay(MyApplication.getPlayMusicList().get(0),
						this);
			} else {
				Toast.makeText(this, "当前播放列表为空！", 0).show();
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
