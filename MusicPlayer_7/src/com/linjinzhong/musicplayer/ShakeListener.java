package com.linjinzhong.musicplayer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.util.Log;

public class ShakeListener implements SensorEventListener {

	// 速度阈值
	private static final int SPEED_SHAKEHOLD = 200;
	// 检测时间间隔
	private static final int UPDATA_INTERVAL_TIME = 450;
	// 传感器管理器
	private SensorManager sensorManager;
	// 加速度传感器
	private Sensor sensor;
	// 重力感应监听器
	private OnShakeListener onShakeListener;
	private OnSilenceListener onSilenceListener;
	// 上下文
	private Context context;
	// 手机上一个位置时重力感应坐标
	private float lastX;
	private float lastY;
	private float lastZ;
	// 上次检测时间
	private long lastUpdataTime;

	// 翻转静音相关参数
	private static final float CRITICAL_DOWN_ANGLE = -5.0f;
	private static final float CRITICAL_UP_ANGLE = 5.0f;
	private static final int Z_ORIENTATION = 2;
	private int mReverseDownFlg = -1;
	private int previousMuteMode = -1;

	/**
	 * 构造函数
	 * 
	 * @param context
	 */
	public ShakeListener(Context context) {
		super();
		Log.e("ShakeListener", "new一个ShakeListener");
		this.context = context;
		start();
	}

	/**
	 * 初始化
	 */
	public void start() {
		// 获取传感器管理对象
		sensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		if (sensorManager != null) {
			// 获取加速度传感器
			sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}
		if (sensor != null) {
			// 注册加速器，并且设置速率
			Log.e("ShakeListener", "注册加速器成功");
			sensorManager.registerListener(this, sensor,
					sensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	/**
	 * 设置重力感应器对象
	 * 
	 * @param listener
	 */
	public void setOnShakelistener(OnShakeListener listener) {
		onShakeListener = listener;
	}

	public void setOnSilencelistener(OnSilenceListener listener) {
		onSilenceListener = listener;
	}

	/*
	 * 停止后注销监听器
	 */
	public void stop() {
		sensorManager.unregisterListener(this);
	}

	/**
	 * 重力感应变化监听
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		// 当前时间
		long currentUpdataTime = System.currentTimeMillis();
		// 时间间隔
		long timeInterval = currentUpdataTime - lastUpdataTime;

		Log.e("ShakeListener", "x=" + event.values[0] + ", y="
				+ event.values[1] + ", z=" + event.values[2]);
		if (event.values[2] >= CRITICAL_UP_ANGLE) {
			mReverseDownFlg = 0;
		} else if (event.values[2] <= CRITICAL_DOWN_ANGLE
				&& mReverseDownFlg == 0) {
			mReverseDownFlg = 1;// 屏幕翻转向下
		}

		if (mReverseDownFlg == 1) {// 屏幕从上到下翻转
			onSilenceListener.onSilence();
			return;// 返回，后面切换歌曲不执行
		}

		if (timeInterval < UPDATA_INTERVAL_TIME) {
			// 如果时间间隔小于规定值，忽略,直接返回
			return;
		}

		lastUpdataTime = currentUpdataTime;

		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];

		float deltaX = x - lastX;
		float deltaY = y - lastY;
		float deltaZ = z - lastZ;

		lastX = x;
		lastY = y;
		lastZ = z;

		double speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ
				* deltaZ)
				/ timeInterval * 10000;
		Log.e("ShakeListener", "speed=============" + speed);
		if (speed > SPEED_SHAKEHOLD) {
			// 如果算出来的加速度值大于规定值，则开始摇晃
			onShakeListener.onShake();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	/**
	 * 摇晃监听接口 回调函数接口
	 * 
	 * @author lin_j
	 * 
	 */
	public interface OnShakeListener {
		public void onShake();
	}

	/**
	 * 翻转监听接口，回调函数接口
	 * 
	 * @author lin_j
	 * 
	 */
	public interface OnSilenceListener {
		public void onSilence();
	}
}
