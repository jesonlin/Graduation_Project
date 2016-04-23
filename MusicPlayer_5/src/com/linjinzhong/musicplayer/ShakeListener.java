package com.linjinzhong.musicplayer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class ShakeListener implements SensorEventListener {

	// �ٶ���ֵ
	private static final int SPEED_SHAKEHOLD = 200;
	// ���ʱ����
	private static final int UPDATA_INTERVAL_TIME = 450;
	// ������������
	private SensorManager sensorManager;
	// ���ٶȴ�����
	private Sensor sensor;
	// ������Ӧ������
	private OnShakeListener onShakeListener;
	// ������
	private Context context;
	// �ֻ���һ��λ��ʱ������Ӧ����
	private float lastX;
	private float lastY;
	private float lastZ;
	// �ϴμ��ʱ��
	private long lastUpdataTime;

	/**
	 * ���캯��
	 * 
	 * @param context
	 */
	public ShakeListener(Context context) {
		super();
		Log.e("ShakeListener", "newһ��ShakeListener");
		this.context = context;
		start();
	}

	/**
	 * ��ʼ��
	 */
	public void start() {
		// ��ȡ�������������
		sensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		if (sensorManager != null) {
			// ��ȡ���ٶȴ�����
			sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}
		if (sensor != null) {
			// ע���������������������
			Log.e("ShakeListener", "ע��������ɹ�");
			sensorManager.registerListener(this, sensor,
					sensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	/**
	 * ����������Ӧ������
	 * 
	 * @param listener
	 */
	public void setOnShakelistener(OnShakeListener listener) {
		onShakeListener = listener;
	}

	/*
	 * ֹͣ��ע��������
	 */
	public void stop() {
		sensorManager.unregisterListener(this);
	}

	/**
	 * ������Ӧ�仯����
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		// ��ǰʱ��
		long currentUpdataTime = System.currentTimeMillis();
		// ʱ����
		long timeInterval = currentUpdataTime - lastUpdataTime;

		if (timeInterval < UPDATA_INTERVAL_TIME) {
			// ���ʱ����С�ڹ涨ֵ������,ֱ�ӷ���
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
			// ���������ļ��ٶ�ֵ���ڹ涨ֵ����ʼҡ��
			onShakeListener.onShake();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	/**
	 * ҡ�μ����ӿ� �ص������ӿ�
	 * 
	 * @author lin_j
	 * 
	 */
	public interface OnShakeListener {
		public void onShake();
	}

}
