package com.linjinzhong.musicplayer;

import java.io.File;
import java.util.HashMap;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.linjinzhong.musicplayer.Utils.ImageUtils;
import com.linjinzhong.musicplayer.Utils.Lyric;
import com.linjinzhong.musicplayer.Utils.LyricView;
import com.linjinzhong.musicplayer.Utils.MyApplication;
import com.linjinzhong.musicplayer.gesture.GestureFragment;
import com.linjinzhong.musicplayer.gesture.GestureFragment.Gesture_Judge;

/***
 * 播放界面
 * 
 * @author lin_j
 * 
 */
public class PlayMusicActivity extends FragmentActivity implements
		GestureFragment.onGestureJugdeResultCallback {
	// 继承fragment实现手势检测fragment中的回调接口处理传回来的手势判断结果

	// fragment管理器和手势检测的fragment对象
	private FragmentManager fragmentManager = getSupportFragmentManager();
	private Fragment gestureFragment;

	// 音频管理
	private AudioManager amg;

	static TextView musicTextView;
	private Button previousButton;
	public static Button playButton;
	private Button nextButton;
	static Context context;
	public static SeekBar seekBar;
	static ImageView musicImage;

	static String name;
	static String path;
	static String artist;
	// 歌曲ID和专辑ID,凭此在本地SD卡音乐中找到对应歌曲专辑封面照片，内置在MP3歌曲里面
	static long song_id;
	static long album_id;

	static Handler handler;
	public static Runnable runnable;
	static boolean isChangeSeekbarP;

	public static ContentResolver resolver;
	static String SDRoot = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + File.separator;

	private static Lyric lrc;
	private static LyricView lrcView;
	private static long time = 0;
	private static RelativeLayout lrcLayout;
	private static String lrcPath;

	// 是否加载过改页面
	public static boolean isUploaded = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_play_music);
		context = this;

		// 手势检测的fragment对象
		gestureFragment = new GestureFragment();
		// 把手势检测的fragment添加到layout中的相关控件上
		fragmentManager.beginTransaction()
				.add(R.id.lrc_layout, gestureFragment, "content_player")
				.commit();
		amg = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);

		resolver = getContentResolver();
		isUploaded = true;

		// 音乐名字
		musicTextView = (TextView) findViewById(R.id.text_musicName);
		// 三个按钮
		previousButton = (Button) findViewById(R.id.button_previous);
		playButton = (Button) findViewById(R.id.button_play);
		nextButton = (Button) findViewById(R.id.button_next);
		// 进度条
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		// 音乐图片
		musicImage = (ImageView) findViewById(R.id.image_pic);
		// 歌词显示控件
		lrcLayout = (RelativeLayout) findViewById(R.id.lrc_layout);
		lrcView = new LyricView(this);
		lrcLayout.setGravity(Gravity.CENTER);
		lrcLayout.addView(lrcView);

		// 三个按钮绑定监听器
		previousButton.setOnClickListener(new ButtonListener());
		playButton.setOnClickListener(new ButtonListener());
		nextButton.setOnClickListener(new ButtonListener());

		// 进度条绑定监听器
		seekBar.setOnSeekBarChangeListener(new onSeekBarListener());

		createHandler();

		runnable = new Runnable() {
			// 播放过程中一些相关设置，包括拖动进度条后要来这执行相关变化
			public void run() {
				System.out.println(PlayService.player.getCurrentPosition());
				if (MyApplication.playStatus == 1) {
					seekBar.setMax(PlayService.player.getDuration());
					seekBar.setProgress(PlayService.player.getCurrentPosition());
					if (new File(lrcPath).exists()) {
						lrc = new Lyric(lrcPath);
						lrc.setMaxTime(PlayService.player.getDuration());
						lrcView.setLyric(lrc);
						time = PlayService.player.getCurrentPosition();
						lrcView.setTime(time);
						lrcView.postInvalidate();
						setTitle(Lyric.longToString((int) time) + "/"
								+ Lyric.longToString((int) lrc.getMaxTime()));
					}
				}
				if (!name.equals(PlayService.name)) {
					name = PlayService.name;
					path = PlayService.path;
					artist = PlayService.artist;
					song_id = PlayService.song_id;
					album_id = PlayService.album_id;
					musicTextView.setText(name);
					musicImage.setImageResource(R.drawable.play_image);
					new imageTask().execute();
					lrcPath = path.substring(0, path.length() - 4) + ".lrc";
				}
				handler.postDelayed(this, 50);
			}
		};

		/**
		 * 设置进入播放页面时候的一些属性
		 */
		if (MyApplication.getPlayMusicList().isEmpty()) {
			name = null;
			path = null;
			song_id = -1;
			album_id = -1;
		} else {
			// 播放列表不为空
			if (MyApplication.playStatus != 0) {
				// 当前为正在播放或者暂停状态
				name = PlayService.name;
				path = PlayService.path;
				artist = PlayService.artist;
				song_id = PlayService.song_id;
				album_id = PlayService.album_id;

				// 歌词路径
				lrcPath = path.substring(0, path.length() - 4) + ".lrc";
				musicTextView.setText(name);
				new imageTask().execute();

				if (MyApplication.playStatus == 1) {
					// 当前为播放状态
					handler.post(runnable);
					playButton
							.setBackgroundResource(R.drawable.pausemusic_select);
				}
				if (MyApplication.playStatus == 2) {
					// 当前位暂停状态
					playButton
							.setBackgroundResource(R.drawable.playmusic_select);
				}
				for (int i = 0; i < MyApplication.getPlayMusicList().size(); i++) {
					if (name.equals(MyApplication.getPlayMusicList().get(i)
							.get("name"))) {
						MyApplication.musicPosition = i;
						break;
					}
				}
			} else {
				// 停止状态，设置待播放歌曲为播放列表第一项
				HashMap<String, Object> map = MyApplication.getPlayMusicList()
						.get(0);
				name = (String) map.get("name");
				path = (String) map.get("path");
				artist = (String) map.get("artist");
				song_id = Long.valueOf((String) map.get("song_id"));
				album_id = Long.valueOf((String) map.get("album_id"));

				lrcPath = path.substring(0, path.length() - 4) + ".lrc";
			}
		}
	}

	/**
	 * 新建一个handler
	 */
	public static void createHandler() {
		if (handler == null) {
			handler = new Handler();
		}
	}

	/**
	 * 播放时候图片更改类
	 * 
	 * @author lin_j
	 * 
	 */
	public static class imageTask extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			Log.e("PlayMusicActivity", "进入图片设置:song_id=" + song_id
					+ ",album_id=" + album_id);
			// 从SD卡中加载图片并显示
			Bitmap image = ImageUtils.getArtwork(context, song_id, album_id,
					true, true);
			if (image == null) {
				Log.e("PlayMusicActivity", "图片信息流为空");
			}
			return image;
		}

		@Override
		protected void onPostExecute(Object result) {
			Log.e("PlayMusicActivity", "开始显示图片");
			musicImage.setImageBitmap((Bitmap) result);
			super.onPostExecute(result);
		}

	}

	@Override
	protected void onResume() {
		if (MyApplication.playStatus == 1) {
			handler.post(runnable);
		}
		super.onResume();
	}

	@Override
	protected void onStop() {
		handler.removeCallbacks(runnable);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		handler.removeCallbacks(runnable);
		super.onDestroy();
	}

	/**
	 * 得到需要播放的音乐信息，设置该音乐名字路径歌手等
	 * 
	 * @param position
	 */
	public static void getMusicInfo(int position) {
		HashMap<String, Object> map = MyApplication.getPlayMusicList().get(
				position);
		name = (String) map.get("name");
		path = (String) map.get("path");
		artist = (String) map.get("artist");
		song_id = Long.valueOf((String) map.get("song_id"));
		album_id = Long.valueOf((String) map.get("album_id"));

		lrcPath = path.substring(0, path.length() - 4) + ".lrc";
		System.out.println("------------lrcPath>" + lrcPath);
	}

	/**
	 * 设置菜单栏选项
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "停止");
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * 停止选项
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			PlayService.stop();
			handler.removeCallbacks(runnable);
			seekBar.setProgress(0);
			musicImage.setImageResource(R.drawable.play_image);
			playButton.setBackgroundResource(R.drawable.playmusic_select);
			MainActivity.footer.setText("正在播放的歌曲");
			break;
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	/**
	 * 播放，暂停，上一首，下一首按钮监听器
	 * 
	 * @author lin_j
	 */
	private class ButtonListener implements OnClickListener {

		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.button_previous:
				// 上一首按钮
				previous(context);
				break;
			case R.id.button_play:
				// 播放/暂停按钮
				playOrpause(context);
				break;
			case R.id.button_next:
				// 下一首按钮
				next(context);
				break;
			default:
				break;
			}
		}

	}

	/**
	 * 播放音乐
	 */
	public static void playMusic() {
		Intent intent = new Intent(context, PlayService.class);
		intent.putExtra("name", name);
		intent.putExtra("path", path);
		intent.putExtra("artist", artist);
		intent.putExtra("song_id", String.valueOf(song_id));
		intent.putExtra("album_id", String.valueOf(album_id));

		context.startService(intent);

		playButton.setBackgroundResource(R.drawable.pausemusic_select);
		musicTextView.setText(name);
		TextView footer = (TextView) MainActivity.footer;
		footer.setText(name);

		handler.post(runnable);
		// 更改图片
		new imageTask().execute();
	}

	/**
	 * 播放上一首
	 */
	public static void previous(Context context) {
		try {
			Thread.sleep(80);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		// 如果播放列表里有两首以上歌曲
		if ((MyApplication.getPlayMusicList() != null)
				&& (MyApplication.getPlayMusicList().size() > 1)) {
			handler.removeCallbacks(runnable);
			PlayService.stop();
			musicImage.setImageResource(R.drawable.play_image);
			MyApplication.musicPosition = MyApplication.musicPosition - 1 < 0 ? MyApplication
					.getPlayMusicList().size() - 1
					: MyApplication.musicPosition - 1;
			getMusicInfo(MyApplication.musicPosition);
			playMusic();
		} else {
			Toast.makeText(context, "没有上一首", 0).show();
		}
	}

	/**
	 * 播放下一首
	 */
	public static void next(Context context) {
		try {
			Thread.sleep(80);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		if ((MyApplication.getPlayMusicList() != null)
				&& (MyApplication.getPlayMusicList().size() > 1)) {
			handler.removeCallbacks(runnable);
			PlayService.stop();
			musicImage.setImageResource(R.drawable.play_image);
			MyApplication.musicPosition = (MyApplication.musicPosition + 1)
					% MyApplication.getPlayMusicList().size();
			getMusicInfo(MyApplication.musicPosition);
			playMusic();
		} else {
			Toast.makeText(context, "没有下一首", 0).show();
		}
	}

	/**
	 * 点击播放或者暂停按钮
	 */
	public static void playOrpause(Context context) {
		if (MyApplication.playStatus == 0) {
			// 停止状态
			if (name == null) {
				Toast.makeText(context, "没有歌曲", 0).show();
			} else {
				playMusic();
			}
		} else if (MyApplication.playStatus == 1) {
			// 播放状态，变成暂停
			handler.removeCallbacks(runnable);
			PlayService.pause();
			playButton.setBackgroundResource(R.drawable.playmusic_select);
		} else if (MyApplication.playStatus == 2) {
			// 暂停状态，继续播放
			PlayService.goon();
			handler.post(runnable);
			playButton.setBackgroundResource(R.drawable.pausemusic_select);
		}
	}

	/**
	 * PlayService中自动播放下一首歌曲用到的
	 * 
	 * @return map 下一首歌曲
	 */
	public static HashMap<String, Object> getNextInfo() {
		MyApplication.musicPosition = (MyApplication.musicPosition + 1)
				% MyApplication.getPlayMusicList().size();
		HashMap<String, Object> map = MyApplication.getPlayMusicList().get(
				MyApplication.musicPosition);
		return map;
	}

	/**
	 * 进度条监听器
	 */
	private class onSeekBarListener implements OnSeekBarChangeListener {

		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {

		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			if (MyApplication.playStatus != 0) {
				handler.removeCallbacks(runnable);
				isChangeSeekbarP = true;
			}
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
			if (isChangeSeekbarP) {
				PlayService.player.seekTo(seekBar.getProgress());
				handler.post(runnable);
				isChangeSeekbarP = false;
			}
		}

	}

	/**
	 * 手势检测fragment判断手势类型后通过回调接口传回手势类型到此
	 */
	@Override
	public void onGestureResult(Gesture_Judge result) {
		Log.i("getsture_result", result + "");
		switch (result) {
		case HG_RIGHT:
			next(context);
			break;
		case HG_LEFT:
			previous(context);
			break;
		case DOUBLE_CLICK:
			playOrpause(context);
			break;
		case VG_RIGHT_DOWN:
			// 右侧向下滑
			amg.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
			break;
		case VG_RIGHT_UP:
			amg.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
			// 右侧向上滑
			break;
		case VG_LEFT_DOWN:
			// 左侧向下滑
			Toast.makeText(context, "左侧向下滑", 0).show();
			break;
		case VG_LEFT_UP:
			// 左侧向下滑
			Toast.makeText(context, "左侧向上滑", 0).show();
			break;
		default:
			break;
		}
	}

}
