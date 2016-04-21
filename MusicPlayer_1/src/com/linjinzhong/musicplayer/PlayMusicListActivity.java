package com.linjinzhong.musicplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.linjinzhong.musicplayer.Utils.DataUtils;
import com.linjinzhong.musicplayer.Utils.MyApplication;
import com.linjinzhong.musicplayer.Utils.PlayUtils;

/**
 * 播放音乐列表
 * @author lin_j
 *
 */
public class PlayMusicListActivity extends Activity {
	
	public static ListView playMusicList;
	public static MyListAdapter myListAdapter;
	public static Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_play_music_list);
		Log.e("PlayMusicListActivity", "进入播放音乐列表");
		context = this;
		
		playMusicList = (ListView) findViewById(R.id.playMusicList);

		Log.e("PlayMusicListActivity", "PlayMusicList:"+MyApplication.getPlayMusicList());
		
		if(myListAdapter==null){
			//不能重复新建，之前有可能两次进入oncreate方法，所有这里要加判断是否已经不为空
			myListAdapter = new MyListAdapter(this, MyApplication.getPlayMusicList(), new int[] { R.id.text_playMusicName, R.id.button_delete });
			playMusicList.setAdapter(myListAdapter);
		}
	
		/**
		 * 播放音乐列表项点击监听
		 */
		playMusicList.setOnItemClickListener(new OnItemClickListener() {

			
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				HashMap<String, Object> map = MyApplication.getPlayMusicList().get(position);
				if (map.get("name").equals(PlayService.name) && MyApplication.playStatus == 1) {
					Toast.makeText(context, "正在播放...", 0).show();
				}else{
					if (new File((String) map.get("path")).exists()) {// 若本地有該歌曲
						PlayUtils.turnToPlay(map, context);
					}else{
						Log.e("PlayMusicListActivity", "本地歌曲不存在，开始修改列表和控件");
						DataUtils.updata_all_list_when_SDdelete(map);
						Toast.makeText(context,(String) map.get("name") + context.getResources().getString(R.string.text_error), 0).show();
					}
				}
			}
		});
	}
	
	@Override
	protected void onRestart() {
		Log.e("PlayMusicListActivity", "进入播放音乐列表的onRestart");
		myListAdapter.notifyDataSetChanged();
		super.onRestart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.e("PlayMusicListActivity", "PlayMusicListActivity的onResume");
		Log.e("PlayMusicListActivity", "onResume()中=PlayMusicList().size()="+MyApplication.getPlayMusicList().size());
//		if(myListAdapter==null){
			//不能重复新建，之前有可能两次进入oncreate方法，所有这里要加判断是否已经不为空
//			myListAdapter = new MyListAdapter(this, MyApplication.getPlayMusicList(), new int[] { R.id.text_playMusicName, R.id.button_delete });
//			playMusicList.setAdapter(myListAdapter);
//		}
		myListAdapter.notifyDataSetChanged();
//		myListAdapter.notifyDataSetInvalidated();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, R.string.text_deletePlayList);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 *清空列表
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 0) {
			new emptyPlayMusicListTask(myListAdapter).execute();
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 清空播放音乐列表方法
	 * @author lin_j
	 *
	 */
	public class emptyPlayMusicListTask extends AsyncTask<Void, Void, Void> {

		MyListAdapter myListAdapter;

		public emptyPlayMusicListTask(MyListAdapter myListAdapter) {
			this.myListAdapter = myListAdapter;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			MyApplication.getPlayMusicList().clear();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			myListAdapter.notifyDataSetChanged();
			PlayService.stop();
			MainActivity.footer.setText("正在播放的歌曲");
			super.onPostExecute(result);
		}

	}

	/**
	 * 返回键
	 */
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			backDialog();
		}
		return false;
	};

	/**
	 * 返回键按下后跳出是否退出选项框
	 */
	public void backDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage("确认退出吗？");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

		
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				((Activity) context).finish();
				new AsyncTask<Void, Void, Void>(){

					@Override
					protected Void doInBackground(Void... params) {
						if (MyApplication.playStatus != 0) {
							PlayService.stop();
//							PlayService.player.release();
						}
						//退出时更新数据库上所有列表除所有音乐列表
						DataUtils.upData_otherLists();
						return null;
					}
					
				}.execute();
			}
		}); 
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	public class MyListAdapter extends BaseAdapter{

		public class buttonViewHolder {
			TextView musicName;
			ImageButton delete;
		}

		private ArrayList<HashMap<String, Object>> musicList;
		private LayoutInflater mInflater;
		private Context mContext;
		private int[] valueViewID;
		private buttonViewHolder holder;
		
		public MyListAdapter(Context context, List<HashMap<String, Object>> appList, int[] to) {
			Log.e("PlayMusicActivity", "进入MyListAdapter");
			musicList = (ArrayList<HashMap<String, Object>>) appList;
			mContext = context;
			mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			valueViewID = to;
		}
		
		@Override
		public int getCount() {
			Log.e("PlayMusicActivity", "MyListAdapter中的getCount==playMusicListview.size()=="+musicList.size());
			return musicList.size();
		}

		@Override
		public Object getItem(int position) {
			return musicList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public void removeItem(int position) {
			if (musicList.get(position).get("name").equals(PlayService.name)) {
				PlayService.stop();
				TextView footer = (TextView) MainActivity.footer;
				footer.setText("正在播放的歌曲");
			}
			musicList.remove(position);
			this.notifyDataSetChanged();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.e("PlayMusicListActivity", "进入getview");
			//super.getItemViewType(position);
			if (convertView != null) {
				holder = (buttonViewHolder) convertView.getTag();
			} else {
				convertView = mInflater.inflate(R.layout.layout_play_music_list_item, null);
				holder = new buttonViewHolder();
				holder.musicName = (TextView) convertView.findViewById(valueViewID[0]);
				holder.delete = (ImageButton) convertView.findViewById(valueViewID[1]);
				convertView.setTag(holder);
			}

			HashMap<String, Object> itemInfo = MyApplication.getPlayMusicList().get(position);

			if (itemInfo != null) {
				String aname = (String) itemInfo.get("name");
				holder.musicName.setText(aname);
				holder.delete.setOnClickListener(new deleOnClickListener(position));
			}
			Log.e("PlayMusicListActivity", "getview中每一行name="+itemInfo.get("name"));
			return convertView;
		}
	
		class deleOnClickListener implements OnClickListener{

			private int position;

			public deleOnClickListener(int position) {
				this.position = position;
			}
			public void onClick(View v) {
				removeItem(position);
			}
		}
	}
}
