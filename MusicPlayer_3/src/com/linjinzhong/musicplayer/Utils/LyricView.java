package com.linjinzhong.musicplayer.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

public class LyricView extends View {

	public LyricView(Context context) {
		super(context);
	}

	// ��ʶ���
	private Lyric lrc = null;
	// ��ǰ����ʱ��
	private long time = 0l;
	// ���廭��
	// ��ǰ������廭��
	private Paint fontPaint = null;
	private Paint lrcPaint = null;
	// ������ɫ
	private int fontColor = Color.WHITE;
	// ��ǰ���������ɫ
	private int lrcColor = Color.YELLOW;
	// �����С
	private int fontSize = 60;

	/**
	 * ���ø�ʶ���
	 */
	public void setLyric(Lyric lrc) {
		this.lrc = lrc;
	}

	/**
	 * ���õ�ǰʱ��
	 */
	public void setTime(long ms) {
		this.time = ms;
	}

	/**
	 * ���ø��������ɫ
	 */
	public void setFontColor(int color) {
		this.fontColor = color;
	}

	/**
	 * ���õ�ǰ���������ɫ
	 */
	public void setLyricColor(int color) {
		this.lrcColor = color;
	}

	/**
	 * ���������С
	 */
	public void setFontSize(int size) {
		this.fontSize = size;
	}

	/**
	 * �ػ���ͼ
	 */
	@Override
	protected void onDraw(Canvas c) {
		super.onDraw(c);
		if (lrc != null) {
			try {
				if (fontPaint == null) {
					fontPaint = new Paint();
				}
				if (lrcPaint == null) {
					lrcPaint = new Paint();
				}
				fontPaint.setColor(fontColor);
				fontPaint.setTextSize(fontSize);
				lrcPaint.setColor(lrcColor);
				lrcPaint.setTextSize(fontSize);
				// ��ȡ��ǰҪ���Ÿ�ʵ�����
				int cIndex = lrc.getIndex(time);
				// ������Ƹ��y����
				int h = getHeight()
						/ 2
						- cIndex
						* fontSize
						* 3
						/ 2
						- (int) ((fontSize * 3 / 2) * (lrc.getOffset(time) / (float) lrc
								.getNextTime(time)));
				Long[] ts = lrc.getAllTimes();
				// ѭ������ÿһ�и�ʣ���ǰ���Ÿ���������,measure������ǰ�е��ַ���ռ����Ļ��ȣ�ÿ����ʼλ��Ϊ�ܿ�ȼ�ȥ����ռ��ȳ���2
				for (Long l : ts) {
					// Log.e("LyricView",
					// "getMeasuredWidth()="+getMeasuredWidth()+"�г�="+lrcPaint.measureText(lrc.get(l)));
					c.drawText(lrc.get(l), (getMeasuredWidth() - lrcPaint
							.measureText(lrc.get(l))) / 2, h,
							lrc.getIndex(l) == cIndex ? lrcPaint : fontPaint);
					h += fontSize * 3 / 2;
				}
			} catch (Exception e) {
			}
		}
	}
}
