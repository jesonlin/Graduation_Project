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

	// 歌词对象
	private Lyric lrc = null;
	// 当前播放时间
	private long time = 0l;
	// 字体画笔
	// 当前歌词字体画笔
	private Paint fontPaint = null;
	private Paint lrcPaint = null;
	// 字体颜色
	private int fontColor = Color.WHITE;
	// 当前歌词字体颜色
	private int lrcColor = Color.YELLOW;
	// 字体大小
	private int fontSize = 60;

	/**
	 * 设置歌词对象
	 */
	public void setLyric(Lyric lrc) {
		this.lrc = lrc;
	}

	/**
	 * 设置当前时间
	 */
	public void setTime(long ms) {
		this.time = ms;
	}

	/**
	 * 设置歌词字体颜色
	 */
	public void setFontColor(int color) {
		this.fontColor = color;
	}

	/**
	 * 设置当前歌词字体颜色
	 */
	public void setLyricColor(int color) {
		this.lrcColor = color;
	}

	/**
	 * 设置字体大小
	 */
	public void setFontSize(int size) {
		this.fontSize = size;
	}

	/**
	 * 重绘视图
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
				// 获取当前要播放歌词的索引
				int cIndex = lrc.getIndex(time);
				// 计算绘制歌词y坐标
				int h = getHeight()
						/ 2
						- cIndex
						* fontSize
						* 3
						/ 2
						- (int) ((fontSize * 3 / 2) * (lrc.getOffset(time) / (float) lrc
								.getNextTime(time)));
				Long[] ts = lrc.getAllTimes();
				// 循环绘制每一行歌词，当前播放歌词特殊绘制,measure测量当前行的字符所占的屏幕宽度，每行起始位置为总宽度减去行所占宽度除以2
				for (Long l : ts) {
					// Log.e("LyricView",
					// "getMeasuredWidth()="+getMeasuredWidth()+"行长="+lrcPaint.measureText(lrc.get(l)));
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
