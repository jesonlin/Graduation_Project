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
 * 手势操作区域
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
		// 手势操作 宽高 传参
		// view.getWidth(); oncreate onresume , 不确定控件的宽高的话（listview,scrollview）,
		// 0;
		
		// 屏幕大小对象，初始化
		cHolder = new CHolder(ScreenUtils.getScreenWidth(MyApplication
				.getContext()), ScreenUtils.getScreenHeight(MyApplication
				.getContext()));
		
		// 手势检测器,传进手势监听器
		gestureDetector = new GestureDetector(getActivity(),
				new gestureListener());

		//视图设置可点击
		view.setClickable(true);
		//视图设置点击监听，一有点击事件启动手势检测器，开始执行手势监听
		view.setOnTouchListener(new OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});
	};

	/**
	 * 手势监听器
	 * 双击，已经滑动事件监听
	 * @author jeson
	 * 
	 */
	private class gestureListener extends
			GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDoubleTap(MotionEvent e) { // 双击
			//判断回调接口是否正常，然后传进双击事件
			if (getActivity() instanceof onGestureJugdeResultCallback) {
				((onGestureJugdeResultCallback) getActivity())
						.onGestureResult(Gesture_Judge.DOUBLE_CLICK);
			}
			return super.onDoubleTap(e);
		}

		/**
		 * 滑屏，用户按下触摸屏、快速移动后松开，
		 * 由1个MotionEvent ACTION_DOWN, 多个ACTION_MOVE, 1个ACTION_UP触发
		 * e1：第1个ACTION_DOWN MotionEvent
		 * e2：最后一个ACTION_MOVE MotionEvent
		 * velocityX：X轴上的移动速度，像素/秒
		 * velocityY：Y轴上的移动速度，像素/秒
		 */
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			//手指滑动起始点
			float startX = e1.getX();
			float startY = e1.getY();
			float endX = e2.getX();
			float endY = e2.getY();

			if (isDebug) {
				Log.v("tag", "startX = " + startX + " ,startY = " + startY);
				Log.v("tag", "endX = " + endX + " ,endY = " + endY);
			}

			//使用回调接口传进由始末点判断出的手势类型
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
	 * 屏幕宽度与高度
	 * 里面有水平移动垂直移动的上限,以及移动距离下限，垂直手势所有左右侧等
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

		// 水平手势
		public int hgRangeOfHorizontal; //水平移动距离下限
		public int hgRangeOfVertical; // 纵向偏移值上限

		//垂直手势
		public int vgRangeOfHorizontal; // 水平偏移上限
		public int vgRangeOfVertical; // 纵向移动距离下限

		// 垂直手势之左右侧区分，right(true);left(false)
		public boolean isVGRight(float startX) {
			return startX > allWidth / 2 ? true : false;
		}
	}

	/**
	 * 手势判断类，由起点和终点位置判断
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 * @return 手势类型
	 */
	private Gesture_Judge judgeTypeOfGesture(float startX, float startY,
			float endX, float endY) {
		if (Math.abs(endY - startY) < cHolder.hgRangeOfVertical) { // 水平手势
			if (endX - startX > cHolder.hgRangeOfHorizontal) {//向右滑动
				return Gesture_Judge.HG_RIGHT;
			} else if (startX - endX > cHolder.hgRangeOfHorizontal) {//向左滑动
				return Gesture_Judge.HG_LEFT;
			} else {//其他
				return Gesture_Judge.NONE;
			}
		}

		if (Math.abs(endX - startX) < cHolder.vgRangeOfHorizontal) { // 垂直手势
			if (endY - startY > cHolder.vgRangeOfVertical) { // 向下滑动
				return cHolder.isVGRight(startX) ? Gesture_Judge.VG_RIGHT_DOWN
						: Gesture_Judge.VG_LEFT_DOWN;
			} else if (startY - endY > cHolder.vgRangeOfVertical) {//向上滑动
				return cHolder.isVGRight(startX) ? Gesture_Judge.VG_RIGHT_UP
						: Gesture_Judge.VG_LEFT_UP;
			} else {
				return Gesture_Judge.NONE;//其他
			}
		}

		return Gesture_Judge.NONE; //其他
	}

	/**
	 * 手势种类，枚举
	 * @author jeson
	 *
	 */
	public enum Gesture_Judge {
		/** 不属于定下的手势 */
		NONE(-1),

		/** 水平左滑 */
		HG_LEFT(0),
		/** 水平右滑 */
		HG_RIGHT(1),

		/** 垂直左方上滑 */
		VG_LEFT_UP(2),
		/** 垂直左方下滑 */
		VG_LEFT_DOWN(3),

		/** 垂直右方上滑 */
		VG_RIGHT_UP(4),
		/** 垂直右方下滑 */
		VG_RIGHT_DOWN(5),

		/** 双击 */
		DOUBLE_CLICK(6);

		private final int value;

		//返回手势类型
		public int getValue() {
			return value;
		}
		//设置手势类型
		private Gesture_Judge(int value) {
			this.value = value;
		}
	}

	/**
	 * 手势监听结果回调接口
	 * @author jeson
	 *
	 */
	public interface onGestureJugdeResultCallback {
		void onGestureResult(Gesture_Judge result);
	}

}
