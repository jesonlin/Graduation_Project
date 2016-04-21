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
 * 播放音乐服务
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
			Toast.makeText(PlayMusicActivity.context, "正在播放", 0).show();
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
		// 播放器绑定播放是否完成监听器
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
					System.out.println("播放有错误！");
				}
			}
		});
	}

	/** 根据当前name,path,artist播放歌曲,并设置底部正在播放的歌曲名以及当前播放到播放列表的哪一首 */
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

	// 0停止，1播放，2暂停
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
