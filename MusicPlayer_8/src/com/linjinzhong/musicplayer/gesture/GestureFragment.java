package com.linjinzhong.musicplayer.gesture;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import com.linjinzhong.musicplayer.R;
import com.linjinzhong.musicplayer.Utils.MyApplication;

/**
 * ���Ʋ�������
 * 
 * @author jeson
 * 
 */
public class GestureFragment extends Fragment {
	private final boolean isDebug = true;
	private GestureDetector gestureDetector;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_gesture, container, false);
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// ���Ʋ��� ��� ����
		// view.getWidth(); oncreate onresume , ��ȷ���ؼ��Ŀ�ߵĻ���listview,scrollview��,
		// 0;
		
		// ��Ļ��С���󣬳�ʼ��
		cHolder = new CHolder(ScreenUtils.getScreenWidth(MyApplication
				.getContext()), ScreenUtils.getScreenHeight(MyApplication
				.getContext()));
		
		// ���Ƽ����,�������Ƽ�����
		gestureDetector = new GestureDetector(getActivity(),
				new gestureListener());

		//��ͼ���ÿɵ��
		view.setClickable(true);
		//��ͼ���õ��������һ�е���¼��������Ƽ��������ʼִ�����Ƽ���
		view.setOnTouchListener(new OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});
	};

	/**
	 * ���Ƽ�����
	 * ˫�����Ѿ������¼�����
	 * @author jeson
	 * 
	 */
	private class gestureListener extends
			GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDoubleTap(MotionEvent e) { // ˫��
			//�жϻص��ӿ��Ƿ�������Ȼ�󴫽�˫���¼�
			if (getActivity() instanceof onGestureJugdeResultCallback) {
				((onGestureJugdeResultCallback) getActivity())
						.onGestureResult(Gesture_Judge.DOUBLE_CLICK);
			}
			return super.onDoubleTap(e);
		}

		/**
		 * �������û����´������������ƶ����ɿ���
		 * ��1��MotionEvent ACTION_DOWN, ���ACTION_MOVE, 1��ACTION_UP����
		 * e1����1��ACTION_DOWN MotionEvent
		 * e2�����һ��ACTION_MOVE MotionEvent
		 * velocityX��X���ϵ��ƶ��ٶȣ�����/��
		 * velocityY��Y���ϵ��ƶ��ٶȣ�����/��
		 */
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			//��ָ������ʼ��
			float startX = e1.getX();
			float startY = e1.getY();
			float endX = e2.getX();
			float endY = e2.getY();

			if (isDebug) {
				Log.v("tag", "startX = " + startX + " ,startY = " + startY);
				Log.v("tag", "endX = " + endX + " ,endY = " + endY);
			}

			//ʹ�ûص��ӿڴ�����ʼĩ���жϳ�����������
			if (getActivity() instanceof onGestureJugdeResultCallback) {
				((onGestureJugdeResultCallback) getActivity())
						.onGestureResult(judgeTypeOfGesture(startX, startY,
								endX, endY));
			}

			return super.onFling(e1, e2, velocityX, velocityY);
		}
	}

	private CHolder cHolder;

	/**
	 * ��Ļ�����߶�
	 * ������ˮƽ�ƶ���ֱ�ƶ�������,�Լ��ƶ��������ޣ���ֱ�����������Ҳ��
	 * @author jeson
	 * 
	 */
	private class CHolder {
		private int allWidth, allHeight;

		public CHolder(int screenWidth, int screenHeight) {
			this.allWidth = screenWidth;
			this.allHeight = screenHeight;

			hgRangeOfHorizontal = allWidth / 4;
			hgRangeOfVertical = allHeight / 6;

			vgRangeOfHorizontal = allWidth / 6;
			vgRangeOfVertical = allHeight / 8;
		}

		// ˮƽ����
		public int hgRangeOfHorizontal; //ˮƽ�ƶ���������
		public int hgRangeOfVertical; // ����ƫ��ֵ����

		//��ֱ����
		public int vgRangeOfHorizontal; // ˮƽƫ������
		public int vgRangeOfVertical; // �����ƶ���������

		// ��ֱ����֮���Ҳ����֣�right(true);left(false)
		public boolean isVGRight(float startX) {
			return startX > allWidth / 2 ? true : false;
		}
	}

	/**
	 * �����ж��࣬�������յ�λ���ж�
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 * @return ��������
	 */
	private Gesture_Judge judgeTypeOfGesture(float startX, float startY,
			float endX, float endY) {
		if (Math.abs(endY - startY) < cHolder.hgRangeOfVertical) { // ˮƽ����
			if (endX - startX > cHolder.hgRangeOfHorizontal) {//���һ���
				return Gesture_Judge.HG_RIGHT;
			} else if (startX - endX > cHolder.hgRangeOfHorizontal) {//���󻬶�
				return Gesture_Judge.HG_LEFT;
			} else {//����
				return Gesture_Judge.NONE;
			}
		}

		if (Math.abs(endX - startX) < cHolder.vgRangeOfHorizontal) { // ��ֱ����
			if (endY - startY > cHolder.vgRangeOfVertical) { // ���»���
				return cHolder.isVGRight(startX) ? Gesture_Judge.VG_RIGHT_DOWN
						: Gesture_Judge.VG_LEFT_DOWN;
			} else if (startY - endY > cHolder.vgRangeOfVertical) {//���ϻ���
				return cHolder.isVGRight(startX) ? Gesture_Judge.VG_RIGHT_UP
						: Gesture_Judge.VG_LEFT_UP;
			} else {
				return Gesture_Judge.NONE;//����
			}
		}

		return Gesture_Judge.NONE; //����
	}

	/**
	 * �������࣬ö��
	 * @author jeson
	 *
	 */
	public enum Gesture_Judge {
		/** �����ڶ��µ����� */
		NONE(-1),

		/** ˮƽ�� */
		HG_LEFT(0),
		/** ˮƽ�һ� */
		HG_RIGHT(1),

		/** ��ֱ���ϻ� */
		VG_LEFT_UP(2),
		/** ��ֱ���»� */
		VG_LEFT_DOWN(3),

		/** ��ֱ�ҷ��ϻ� */
		VG_RIGHT_UP(4),
		/** ��ֱ�ҷ��»� */
		VG_RIGHT_DOWN(5),

		/** ˫�� */
		DOUBLE_CLICK(6);

		private final int value;

		//������������
		public int getValue() {
			return value;
		}
		//������������
		private Gesture_Judge(int value) {
			this.value = value;
		}
	}

	/**
	 * ���Ƽ�������ص��ӿ�
	 * @author jeson
	 *
	 */
	public interface onGestureJugdeResultCallback {
		void onGestureResult(Gesture_Judge result);
	}

}
