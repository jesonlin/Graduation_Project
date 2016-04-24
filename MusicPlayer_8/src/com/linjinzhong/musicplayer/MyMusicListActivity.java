package com.linjinzhong.musicplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ExpandableListActivity;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.linjinzhong.musicplayer.Utils.DataUtils;
import com.linjinzhong.musicplayer.Utils.MyApplication;
import com.linjinzhong.musicplayer.Utils.PlayUtils;

public class MyMusicListActivity extends ExpandableListActivity {

	// 可展开列表组件
	private ExpandableListView myMusicListView;

	// 我的音乐中的组列表
	private ArrayList<HashMap<String, Object>> groupsList;
	// 我的音乐中的子列表
	private List<List<HashMap<String, Object>>> childsList;
	// 我的音乐列表中的适配器
	public static MyMusicListAdapter myMusicListAdapter;

	private static Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_my_music_list);
		myMusicListView = getExpandableListView();
		Log.e("MyMusicListActivity", "进入我的音乐列表");
		context = this;

		groupsList = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> group1 = new HashMap<String, Object>();
		group1.put("name", "最喜欢");
		HashMap<String, Object> group2 = new HashMap<String, Object>();
		group2.put("name", "放松");
		HashMap<String, Object> group3 = new HashMap<String, Object>();
		group3.put("name", "学习");
		groupsList.add(group1);
		groupsList.add(group2);
		groupsList.add(group3);

		childsList = new ArrayList<List<HashMap<String, Object>>>();
		childsList.add(MyApplication.getFavoriteMusicList());
		childsList.add(MyApplication.getRelaxMusicList());
		childsList.add(MyApplication.getStudyMusicList());

		// 这里不知道为什么不需要判断为空才new，不然就不行
		myMusicListAdapter = new MyMusicListAdapter(this, groupsList,
				childsList);
		Log.e("MyMusicListActivity", "=========设置适配器");
		myMusicListView.setAdapter(myMusicListAdapter);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.e("MyMusicListActivity", "我的音乐列表中的onResume");
		myMusicListAdapter.notifyDataSetChanged();
	}

	public static Context getContext() {
		return context;
	}

	/**
	 * 点击播放某一组列表中的子列表项目
	 */
	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		HashMap<String, Object> map = childsList.get(groupPosition).get(
				childPosition);
		if (map.get("name").equals(PlayService.name)
				&& MyApplication.playStatus == 1) {
			Toast.makeText(context, "正在播放...", 0).show();
		} else {
			boolean b = PlayUtils.addMusicToList(context, map,
					MyApplication.getPlayMusicList());
			if (b) {
				PlayMusicListActivity.myListAdapter.notifyDataSetChanged();
				PlayUtils.turnToPlay(map, context);
			} else {
				// 歌曲本地不存在
				DataUtils.updata_all_list_when_SDdelete(map);
				Toast.makeText(
						context,
						(String) map.get("name")
								+ context.getResources().getString(
										R.string.text_error), 0).show();
			}
		}

		return super.onChildClick(parent, v, groupPosition, childPosition, id);
	}

	/**
	 * 按下返回键
	 */
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			backDialog();
		}
		return false;
	};

	@Override
	protected void onDestroy() {
		DataUtils.upData_otherLists();
		super.onDestroy();
	}

	/**
	 * 按下返回键弹出选项框是否退出
	 */
	public void backDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage("确认退出吗？");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				((Activity) context).finish();
				new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						if (MyApplication.playStatus != 0) {
							PlayService.stop();
							context.stopService(new Intent(
									MainActivity.context, ShakeService.class));
						}
						// 退出时设置适配器为null,如果不清空后台线程在进入时候这个适配器将不会重新new而是原来退出之前那个，那么相应列表将不会显示
						// 而我的音乐列表因为没有在oncreate里面设置适配器为空才new，所有不清空后台线程重新打开也是会新建新的
						PlayMusicListActivity.myListAdapter = null;
						AllMusicListActivity.listAdapter = null;
						DataUtils.upData_allmusicList();
						DataUtils.upData_otherLists();
						return null;
					}

				}.execute();
			}
		});
		builder.setNegativeButton("取消", new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	/**
	 * 我的音乐列表适配器继承基础可扩展列表适配器
	 * 
	 * @author lin_j
	 * 
	 */
	public class MyMusicListAdapter extends BaseExpandableListAdapter {

		private Context context;
		private List<HashMap<String, Object>> groups;
		private List<List<HashMap<String, Object>>> childs;
		private childViewsHolder childHolder;
		private GroupViewsHolder groupHolder;

		public MyMusicListAdapter(Context context,
				List<HashMap<String, Object>> groups,
				List<List<HashMap<String, Object>>> childs) {
			Log.e("MyMusicListActivity", "================我的音乐列表里new一个adapter");
			this.context = context;
			this.groups = groups;
			this.childs = childs;
		}

		private class childViewsHolder {
			TextView childText;
			ImageButton childButton;
		}

		public Object getChild(int arg0, int arg1) {
			return childs.get(arg0).get(arg1);
		}

		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		public void removeChildItem(int groupPosition, int childPosition) {
			childs.get(groupPosition).remove(childPosition);
			this.notifyDataSetChanged();
		}

		/**
		 * 获取子控件
		 */
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			if (convertView != null) {
				childHolder = (childViewsHolder) convertView.getTag();
			} else {
				childHolder = new childViewsHolder();
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.layout_my_music_child,
						null);
				childHolder.childText = (TextView) convertView
						.findViewById(R.id.text_childItem);
				childHolder.childButton = (ImageButton) convertView
						.findViewById(R.id.button_deleteChildItem);
				convertView.setTag(childHolder);
			}

			HashMap<String, Object> childItem = childs.get(groupPosition).get(
					childPosition);
			if (childItem != null) {
				String name = (String) childItem.get("name");
				childHolder.childText.setText(name);
				childHolder.childButton
						.setOnClickListener(new childItemOnClick(groupPosition,
								childPosition));
			}
			return convertView;
		}

		/**
		 * 子列表监听器，是否删除该子列表项
		 * 
		 * @author lin_j
		 * 
		 */
		public class childItemOnClick implements
				android.view.View.OnClickListener {

			private int groupPosition;
			private int childPosition;

			public childItemOnClick(int groupPosition, int childposition) {
				this.groupPosition = groupPosition;
				this.childPosition = childposition;
			}

			public void onClick(View v) {
				removeChildItem(groupPosition, childPosition);
			}

		}

		public int getChildrenCount(int groupPosition) {
			return childs.get(groupPosition).size();
		}

		public Object getGroup(int groupPosition) {
			return groups.get(groupPosition);

		}

		public int getGroupCount() {
			return groups.size();
		}

		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		private class GroupViewsHolder {
			TextView groupText;
			ImageButton groupButton;
		}

		/**
		 * 获取组控件
		 */
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			if (convertView != null) {
				groupHolder = (GroupViewsHolder) convertView.getTag();
			} else {
				groupHolder = new GroupViewsHolder();
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.layout_my_music_groups,
						null);
				groupHolder.groupText = (TextView) convertView
						.findViewById(R.id.text_groupItem);
				groupHolder.groupButton = (ImageButton) convertView
						.findViewById(R.id.button_playAll);
				convertView.setTag(groupHolder);
			}

			HashMap<String, Object> map = groups.get(groupPosition);
			if (map != null) {
				String name = (String) map.get("name");
				groupHolder.groupText.setText(name);
				// 组播放按钮绑定监听器
				groupHolder.groupButton.setOnClickListener(new GroupOnClick(
						groupPosition));
			}

			return convertView;
		}

		/**
		 * 组列表监听器，是否添加整组列表到播放列表并播放第一首
		 * 
		 * @author lin_j
		 * 
		 */
		class GroupOnClick implements View.OnClickListener {

			private int position;

			public GroupOnClick(int position) {
				this.position = position;
			}

			public void onClick(View v) {
				switch (position) {
				case 0:
					PlayUtils.turnToPlay_List(context,
							MyApplication.getFavoriteMusicList());
					break;
				case 1:
					PlayUtils.turnToPlay_List(context,
							MyApplication.getRelaxMusicList());
					break;
				case 2:
					PlayUtils.turnToPlay_List(context,
							MyApplication.getStudyMusicList());
					break;
				default:
					break;
				}
			}
		}

		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return true;
		}

	}
}
