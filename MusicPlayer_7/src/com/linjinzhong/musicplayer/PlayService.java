package com.linjinzhong.musicplayer;

import java.io.IOException;
import java.util.HashMap;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;
import android.widget.Toast;

import com.linjinzhong.musicplayer.Utils.MyApplication;

/**
 * �������ַ���
 * 
 * @author lin_j
 * 
 */
public class PlayService extends Service {

	public static MediaPlayer player;
	public static String name;
	static String path;
	static String artist;
	static long song_id;
	static long album_id;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		if (player == null) {
			player = new MediaPlayer();
		}
		super.onCreate();
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if (name == intent.getStringExtra("name")) {
			Toast.makeText(PlayMusicActivity.context, "���ڲ���", 0).show();
		} else {
			name = intent.getStringExtra("name");
			path = intent.getStringExtra("path");
			artist = intent.getStringExtra("artist");
			song_id = Long.valueOf(intent.getStringExtra("song_id"));
			album_id = Long.valueOf(intent.getStringExtra("album_id"));
			try {
				play();
			} catch (IllegalArgumentException e) {
				// TODO: handle exception
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO: handle exception
				e.printStackTrace();
			} catch (IOException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		// �������󶨲����Ƿ���ɼ�����
		player.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				player.reset();
				MyApplication.playStatus = 0;

				MyApplication.musicPosition = (MyApplication.musicPosition + 1)
						% MyApplication.getPlayMusicList().size();
				HashMap<String, Object> map = MyApplication.getPlayMusicList()
						.get(MyApplication.musicPosition);
				path = (String) map.get("path");
				name = (String) map.get("name");
				artist = (String) map.get("artist");
				song_id = Long.valueOf((String) map.get("song_id"));
				album_id = Long.valueOf((String) map.get("album_id"));

				try {
					play();
				} catch (Exception e) {
					System.out.println("�����д���");
				}
			}
		});
	}

	/** ���ݵ�ǰname,path,artist���Ÿ���,�����õײ����ڲ��ŵĸ������Լ���ǰ���ŵ������б����һ�� */
	void play() throws IllegalArgumentException, IllegalStateException,
			IOException {
		player.reset();
		player.setDataSource(path);
		player.prepareAsync();
		player.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				// TODO Auto-generated method stub
				if (player != null) {
					player.start();
					MyApplication.playStatus = 1;
					for (int i = 0; i < MyApplication.getPlayMusicList().size(); i++) {
						if (name.equals(MyApplication.getPlayMusicList().get(i)
								.get("name"))) {
							MyApplication.musicPosition = i;
							break;
						}
					}
					MainActivity.footer.setText(name);
					MyApplication.playStatus = 1;
				}
			}
		});
	}

	// 0ֹͣ��1���ţ�2��ͣ
	public static void pause() {
		if (MyApplication.playStatus == 1) {
			player.pause();
			MyApplication.playStatus = 2;
		}
	}

	public static void goon() {
		if (MyApplication.playStatus != 0) {
			player.start();
			MyApplication.playStatus = 1;

		}
	}

	public static void stop() {
		if (MyApplication.playStatus != 0) {
			player.stop();
			MyApplication.playStatus = 0;
		}
	}
}
