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

	// ��չ���б����
	private ExpandableListView myMusicListView;

	// �ҵ������е����б�
	private ArrayList<HashMap<String, Object>> groupsList;
	// �ҵ������е����б�
	private List<List<HashMap<String, Object>>> childsList;
	// �ҵ������б��е�������
	public static MyMusicListAdapter myMusicListAdapter;

	private static Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_my_music_list);
		myMusicListView = getExpandableListView();
		Log.e("MyMusicListActivity", "�����ҵ������б�");
		context = this;

		groupsList = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> group1 = new HashMap<String, Object>();
		group1.put("name", "��ϲ��");
		HashMap<String, Object> group2 = new HashMap<String, Object>();
		group2.put("name", "����");
		HashMap<String, Object> group3 = new HashMap<String, Object>();
		group3.put("name", "ѧϰ");
		groupsList.add(group1);
		groupsList.add(group2);
		groupsList.add(group3);

		childsList = new ArrayList<List<HashMap<String, Object>>>();
		childsList.add(MyApplication.getFavoriteMusicList());
		childsList.add(MyApplication.getRelaxMusicList());
		childsList.add(MyApplication.getStudyMusicList());

		// ���ﲻ֪��Ϊʲô����Ҫ�ж�Ϊ�ղ�new����Ȼ�Ͳ���
		myMusicListAdapter = new MyMusicListAdapter(this, groupsList,
				childsList);
		Log.e("MyMusicListActivity", "=========����������");
		myMusicListView.setAdapter(myMusicListAdapter);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.e("MyMusicListActivity", "�ҵ������б��е�onResume");
		myMusicListAdapter.notifyDataSetChanged();
	}

	public static Context getContext() {
		return context;
	}

	/**
	 * �������ĳһ���б��е����б���Ŀ
	 */
	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		HashMap<String, Object> map = childsList.get(groupPosition).get(
				childPosition);
		if (map.get("name").equals(PlayService.name)
				&& MyApplication.playStatus == 1) {
			Toast.makeText(context, "���ڲ���...", 0).show();
		} else {
			boolean b = PlayUtils.addMusicToList(context, map,
					MyApplication.getPlayMusicList());
			if (b) {
				PlayMusicListActivity.myListAdapter.notifyDataSetChanged();
				PlayUtils.turnToPlay(map, context);
			} else {
				// �������ز�����
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
	 * ���·��ؼ�
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
	 * ���·��ؼ�����ѡ����Ƿ��˳�
	 */
	public void backDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage("ȷ���˳���");
		builder.setTitle("��ʾ");
		builder.setPositiveButton("ȷ��", new OnClickListener() {

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
						// �˳�ʱ����������Ϊnull,�������պ�̨�߳��ڽ���ʱ���������������������new����ԭ���˳�֮ǰ�Ǹ�����ô��Ӧ�б�������ʾ
						// ���ҵ������б���Ϊû����oncreate��������������Ϊ�ղ�new�����в���պ�̨�߳����´�Ҳ�ǻ��½��µ�
						PlayMusicListActivity.myListAdapter = null;
						AllMusicListActivity.listAdapter = null;
						DataUtils.upData_allmusicList();
						DataUtils.upData_otherLists();
						return null;
					}

				}.execute();
			}
		});
		builder.setNegativeButton("ȡ��", new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	/**
	 * �ҵ������б��������̳л�������չ�б�������
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
			Log.e("MyMusicListActivity", "================�ҵ������б���newһ��adapter");
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
		 * ��ȡ�ӿؼ�
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
		 * ���б���������Ƿ�ɾ�������б���
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
		 * ��ȡ��ؼ�
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
				// �鲥�Ű�ť�󶨼�����
				groupHolder.groupButton.setOnClickListener(new GroupOnClick(
						groupPosition));
			}

			return convertView;
		}

		/**
		 * ���б���������Ƿ���������б������б����ŵ�һ��
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
