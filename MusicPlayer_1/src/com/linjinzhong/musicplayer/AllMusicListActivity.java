package com.linjinzhong.musicplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
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

import com.linjinzhong.musicplayer.PlayMusicListActivity.MyListAdapter;
import com.linjinzhong.musicplayer.Utils.DataUtils;
import com.linjinzhong.musicplayer.Utils.MyApplication;
import com.linjinzhong.musicplayer.Utils.PlayUtils;

/**
 * ���������б� *
 * 
 * @author lin_j
 * 
 */
public class AllMusicListActivity extends ListActivity {

	private ListView listView;
	public static MyListAdapter listAdapter;
	public static Context context;
	private ContentResolver resolver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_all_music_list);
		getWindow().setBackgroundDrawable(null);
		listView = (ListView) findViewById(android.R.id.list);
		Log.e("AllMusicListActivity", "�������������б�");
		context = this;
		resolver = getContentResolver();

		if (listAdapter == null) {
			listAdapter = new MyListAdapter(this,
					MyApplication.getAllMusicList());
			listView.setAdapter(listAdapter);
		}

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				HashMap<String, Object> map = MyApplication.getAllMusicList()
						.get(position);
				if (map.get("name").equals(PlayService.name)
						&& MyApplication.playStatus == 1) {
					Toast.makeText(context, "���ڲ���...", 0).show();
				} else {
					boolean b = PlayUtils.addMusicToList(context, map,
							MyApplication.getPlayMusicList());
					if (b) {
						// ����ĸ�����������²����б�����
						PlayMusicListActivity.myListAdapter
								.notifyDataSetChanged();
						PlayUtils.turnToPlay(map, context);
					} else {
						// �����ļ����ڱ��ر�ɾ���������������б���ɾ���ü�¼��֪ͨ�б����
						DataUtils.updata_all_list_when_SDdelete(map);
						Toast.makeText(
								context,
								(String) map.get("name")
										+ context.getResources().getString(
												R.string.text_error), 0).show();
					}
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.e("AllMusicListActivity", "����AllMusicListActivity");
//		if (listAdapter == null) {
//			listAdapter = new MyListAdapter(this,
//					MyApplication.getAllMusicList());
//			listView.setAdapter(listAdapter);
//		}
		// ������ʾ��ҳ��ʱ������˳�����û�������̨ʱ��Ҫ������֪ͨ�б����
		listAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "����ȫ��");
		menu.add(0, 1, 1, "��������");
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * �˵�ѡ��
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case 0:
			PlayUtils.turnToPlay_List(context, MyApplication.getAllMusicList());
			break;
		case 1:
			new AllMusic_List_asyncTask(listAdapter).execute();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * �����I�Ƿ���
	 */
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			backDialog();
		}
		return false;
	}

	/**
	 * ���ؼ����󵯴�
	 */
	public void backDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage("ȷ���˳���");
		builder.setTitle("��ʾ");
		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				((Activity) context).finish();
				new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						if (MyApplication.playStatus != 0) {
							PlayService.stop();
							// PlayService.player.release();
						}
						DataUtils.upData_otherLists();
						return null;
					}

				}.execute();

			}
		});
		builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}

		});

		builder.create().show();
	}

	/**
	 * ���и����б�������
	 * 
	 * @author lin_j
	 * 
	 */
	public class MyListAdapter extends BaseAdapter {

		private class buttonViewHolder {
			TextView musicName;
			ImageButton add;
			ImageButton collect;
		}

		private ArrayList<HashMap<String, Object>> musicList;
		private LayoutInflater mInflater;
		private Context mContext;
		private buttonViewHolder holder;

		public MyListAdapter(Context context,
				List<HashMap<String, Object>> appList) {
			musicList = (ArrayList<HashMap<String, Object>>) appList;
			mContext = context;
			mInflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			return musicList.size();
		}

		public Object getItem(int position) {
			return musicList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public void removeItem(int position) {
			musicList.remove(position);
			this.notifyDataSetChanged();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView != null) {
				holder = (buttonViewHolder) convertView.getTag();
			} else {
				convertView = mInflater.inflate(
						R.layout.layout_all_music_list_item, null);
				holder = new buttonViewHolder();
				holder.musicName = (TextView) convertView
						.findViewById(R.id.text_musicName);
				holder.add = (ImageButton) convertView
						.findViewById(R.id.button_add);
				holder.collect = (ImageButton) convertView
						.findViewById(R.id.button_collect);
				convertView.setTag(holder);
			}

			HashMap<String, Object> iteminfo = musicList.get(position);

			if (iteminfo != null) {
				String aname = (String) iteminfo.get("name");
				holder.musicName.setText(aname);
				holder.add.setOnClickListener(new addOnClickListener(position));
				holder.collect.setOnClickListener(new collectOnClickListener(
						position));
			}
			return convertView;
		}

		/**
		 * ����������Ӹ��������������б�
		 * 
		 * @author lin_j
		 * 
		 */
		class addOnClickListener implements OnClickListener {

			private int position;

			public addOnClickListener(int position) {
				this.position = position;
			}

			@Override
			public void onClick(View v) {
				HashMap<String, Object> map = MyApplication.getAllMusicList()
						.get(position);
				boolean b = PlayUtils.addMusicToList(context, map,
						MyApplication.getPlayMusicList());
				if (b) {
					PlayMusicListActivity.myListAdapter.notifyDataSetChanged();
				} else {
					DataUtils.updata_all_list_when_SDdelete(map);
					Toast.makeText(
							context,
							(String) map.get("name")
									+ context.getResources().getString(
											R.string.text_error), 0).show();
				}
			}
		}

		/**
		 * ���������ռ��������ҵ������б�
		 * 
		 * @author lin_j
		 * 
		 */
		class collectOnClickListener implements OnClickListener {
			int position;

			public collectOnClickListener(int position) {
				this.position = position;
			}

			public void onClick(View v) {
				Builder builder = new AlertDialog.Builder(context);
				builder.setIcon(R.drawable.collect);
				builder.setTitle("����ѡ���ղ��б�");
				builder.setItems(new String[] { "��ϲ��", "����", "ѧϰ" },
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								HashMap<String, Object> map = MyApplication
										.getAllMusicList().get(position);
								boolean b;
								switch (which) {
								case 0:
									b = PlayUtils.addMusicToList(context, map,
											MyApplication
													.getFavoriteMusicList());
									break;
								case 1:
									b = PlayUtils.addMusicToList(context, map,
											MyApplication.getRelaxMusicList());
									break;
								case 2:
									b = PlayUtils.addMusicToList(context, map,
											MyApplication.getStudyMusicList());
									break;
								default:
									b = false;
									break;
								}
								if (b) {
									MyMusicListActivity.myMusicListAdapter
											.notifyDataSetChanged();
								} else {
									DataUtils
											.updata_all_list_when_SDdelete(map);
									Toast.makeText(
											context,
											(String) map.get("name")
													+ context
															.getResources()
															.getString(
																	R.string.text_error),
											0).show();
								}
							}
						});
				builder.create().show();
			}
		}
	}

	/**
	 * ���±�����������
	 * 
	 * @author lin_j
	 * 
	 */
	public class AllMusic_List_asyncTask extends AsyncTask<String, Void, Void> {
		private ProgressDialog progressDialog;
		private MyListAdapter listAdapter;

		public AllMusic_List_asyncTask(MyListAdapter listAdapter) {
			this.listAdapter = listAdapter;
			progressDialog = new ProgressDialog(context);
		}

		@Override
		protected void onPreExecute() {
			progressDialog.setMessage("ɨ����������...");
			progressDialog.setCancelable(false);
			progressDialog.setIndeterminate(false);
			progressDialog.show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(String... params) {
			DataUtils.update_AllMusicDataTDB(resolver, context);
			DataUtils.get_AllMusicListFDB(context);
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {

		}

		@Override
		protected void onPostExecute(Void result) {
			listAdapter.notifyDataSetChanged();
			Toast.makeText(context,
					"������" + MyApplication.getAllMusicList().size() + "�׸���", 0)
					.show();
			progressDialog.dismiss();
		}
	}
}
