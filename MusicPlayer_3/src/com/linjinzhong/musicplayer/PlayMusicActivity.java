package com.linjinzhong.musicplayer;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import com.linjinzhong.musicplayer.Utils.ImageUtils;
import com.linjinzhong.musicplayer.Utils.Lyric;
import com.linjinzhong.musicplayer.Utils.LyricView;
import com.linjinzhong.musicplayer.Utils.MyApplication;
import com.linjinzhong.musicplayer.Utils.PlayUtils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/***
 * ���Ż
 * 
 * @author lin_j
 * 
 */
public class PlayMusicActivity extends Activity {

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
	// ����ID��ר��ID,ƾ���ڱ���SD���������ҵ���Ӧ����ר��������Ƭ��������MP3��������
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_play_music);
		context = this;
		resolver = getContentResolver();

		// ��������
		musicTextView = (TextView) findViewById(R.id.text_musicName);
		// ������ť
		previousButton = (Button) findViewById(R.id.button_previous);
		playButton = (Button) findViewById(R.id.button_play);
		nextButton = (Button) findViewById(R.id.button_next);
		// ������
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		// ����ͼƬ
		musicImage = (ImageView) findViewById(R.id.image_pic);
		// �����ʾ�ؼ�
		lrcLayout = (RelativeLayout) findViewById(R.id.lrc_layout);
		lrcView = new LyricView(this);
		lrcLayout.setGravity(Gravity.CENTER);
		lrcLayout.addView(lrcView);

		// ������ť�󶨼�����
		previousButton.setOnClickListener(new ButtonListener());
		playButton.setOnClickListener(new ButtonListener());
		nextButton.setOnClickListener(new ButtonListener());

		// �������󶨼�����
		seekBar.setOnSeekBarChangeListener(new onSeekBarListener());

		handler = new Handler();
		runnable = new Runnable() {
			// ���Ź�����һЩ������ã������϶���������Ҫ����ִ����ر仯
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
		 * ���ý��벥��ҳ��ʱ���һЩ����
		 */
		if (MyApplication.getPlayMusicList().isEmpty()) {
			name = null;
			path = null;
			song_id = -1;
			album_id = -1;
		} else {
			// �����б�Ϊ��
			if (MyApplication.playStatus != 0) {
				// ��ǰΪ���ڲ��Ż�����ͣ״̬
				name = PlayService.name;
				path = PlayService.path;
				artist = PlayService.artist;
				song_id = PlayService.song_id;
				album_id = PlayService.album_id;

				// ���·��
				lrcPath = path.substring(0, path.length() - 4) + ".lrc";
				musicTextView.setText(name);
				new imageTask().execute();

				if (MyApplication.playStatus == 1) {
					// ��ǰΪ����״̬
					handler.post(runnable);
					playButton
							.setBackgroundResource(R.drawable.pausemusic_select);
				}
				if (MyApplication.playStatus == 2) {
					// ��ǰλ��ͣ״̬
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
				// ֹͣ״̬�����ô����Ÿ���Ϊ�����б��һ��
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
	 * ����ʱ��ͼƬ������
	 * 
	 * @author lin_j
	 * 
	 */
	public static class imageTask extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			Log.e("PlayMusicActivity", "����ͼƬ����:song_id=" + song_id
					+ ",album_id=" + album_id);
			// ��SD���м���ͼƬ����ʾ
			Bitmap image = ImageUtils.getArtwork(context, song_id, album_id,
					true, true);
			if (image == null) {
				Log.e("PlayMusicActivity", "ͼƬ��Ϣ��Ϊ��");
			}
			return image;
		}

		@Override
		protected void onPostExecute(Object result) {
			Log.e("PlayMusicActivity", "��ʼ��ʾͼƬ");
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
	 * �õ���Ҫ���ŵ�������Ϣ�����ø���������·�����ֵ�
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
	 * ���ò˵���ѡ��
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "ֹͣ");
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * ֹͣѡ��
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
			MainActivity.footer.setText("���ڲ��ŵĸ���");
			break;
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	/**
	 * ���ţ���ͣ����һ�ף���һ�װ�ť������
	 * 
	 * @author lin_j
	 */
	private class ButtonListener implements OnClickListener {

		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.button_previous:
				// ��һ�װ�ť
				try {
					Thread.sleep(80);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				// ��������б������������ϸ���
				if (MyApplication.getPlayMusicList().size() > 1) {
					handler.removeCallbacks(runnable);
					PlayService.stop();
					musicImage.setImageResource(R.drawable.play_image);
					previous();
				} else {
					Toast.makeText(context, "û����һ��", 0).show();
				}
				break;
			case R.id.button_play:
				// ����/��ͣ��ť
				if (MyApplication.playStatus == 0) {
					// ֹͣ״̬
					if (name == null) {
						Toast.makeText(context, "û�и���", 0).show();
					} else {
						playMusic();
					}
				} else if (MyApplication.playStatus == 1) {
					// ����״̬�������ͣ
					handler.removeCallbacks(runnable);
					PlayService.pause();
					playButton
							.setBackgroundResource(R.drawable.playmusic_select);
				} else if (MyApplication.playStatus == 2) {
					// ��ͣ״̬����������
					PlayService.goon();
					handler.post(runnable);
					playButton
							.setBackgroundResource(R.drawable.pausemusic_select);
				}
				break;
			case R.id.button_next:
				// ��һ�װ�ť
				try {
					Thread.sleep(80);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

				if (MyApplication.getPlayMusicList().size() > 1) {
					handler.removeCallbacks(runnable);
					PlayService.stop();
					musicImage.setImageResource(R.drawable.play_image);
					next();
				} else {
					Toast.makeText(context, "û����һ��", 0).show();
				}
				break;
			default:
				break;
			}
		}

	}

	/**
	 * ��������
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
		// ����ͼƬ
		new imageTask().execute();
	}

	/**
	 * ������һ��
	 */
	public static void previous() {
		MyApplication.musicPosition = MyApplication.musicPosition - 1 < 0 ? MyApplication
				.getPlayMusicList().size() - 1
				: MyApplication.musicPosition - 1;
		getMusicInfo(MyApplication.musicPosition);
		playMusic();
	}

	/**
	 * ������һ��
	 */
	public static void next() {
		MyApplication.musicPosition = (MyApplication.musicPosition + 1)
				% MyApplication.getPlayMusicList().size();
		getMusicInfo(MyApplication.musicPosition);
		playMusic();
	}

	/**
	 * PlayService���Զ�������һ�׸����õ���
	 * 
	 * @return map ��һ�׸���
	 */
	public static HashMap<String, Object> getNextInfo() {
		MyApplication.musicPosition = (MyApplication.musicPosition + 1)
				% MyApplication.getPlayMusicList().size();
		HashMap<String, Object> map = MyApplication.getPlayMusicList().get(
				MyApplication.musicPosition);
		return map;
	}

	/**
	 * ������������
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

}
