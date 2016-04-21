package com.linjinzhong.musicplayer;

import java.util.ArrayList;

import android.app.LocalActivityManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.linjinzhong.musicplayer.Utils.MyApplication;

/**
 * �����棬TabHost
 * ������ǩ���ҵ����֣������б����и���
 * �ײ�һ�����ڲ������ֵ�textView������������ڲ��Ž���
 * @author lin_j
 *
 */
public class MainActivity extends TabActivity{
	
	public static Context context;
	private LocalActivityManager manager;
	private TabHost tabHost;
	private ViewPager viewPager;
	private LayoutInflater layoutInflater;
	public static TextView footer;
	
	// ������������Ű�ťͼƬ
	private int mImageViewArray[] = { R.drawable.tabview_mymusic_select,R.drawable.tabview_playlist_select, R.drawable.tabview_allmusic_select };
	// ��Ӧ����
	private int mTextViewArray[] = { R.string.text_mymusic, R.string.text_playlist,
				R.string.text_allmusic };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_main);
		Log.e("MainActivity", "����MainActivity");
		context=MainActivity.this;
		footer=(TextView) findViewById(R.id.text_footer_playingMusic);
		// ʵ�������ֶ���
		layoutInflater = LayoutInflater.from(this);
		
		manager=new LocalActivityManager(this, true);
		manager.dispatchCreate(savedInstanceState);
		
		//���ر�ǩ��
		this.loadTabHost();
		//����viewPager
		this.loadViewPager();
		//������ִ��getView����������ÿ�������б�activity
		Log.e("MainActivity", "TabHost��ViewPager�������,�������õ�ǰTab");
		//���õ�ǰ��ǩΪ�м�Ĳ����б�
		tabHost.setCurrentTab(1);
		Log.e("MainActivity", "��ǰTab�������");
		//Ϊ�������ڲ��ŵ�textView���ü�����,��ת�����Ž���
		footer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity.context.startActivity(new Intent(MainActivity.this,PlayMusicActivity.class));
			}
		});
	}

	@Override
	protected void onResume() {
		Log.e("MainActivity", "����MainActivity��onResum");
		if(MyApplication.playStatus!=0){
			footer.setText(PlayService.name);
		}
		super.onResume();
	}
	
	public View getView(String id,Intent intent){
		Log.e("MainActivity", "����TabHost��getView����");
		return manager.startActivity(id, intent).getDecorView();
	}
	
	/**
	 * ��Tab���ñ�ǩ����ͼ�������ֺ�ͼ��
	 */
	public View getTabItemView(int index) {
		// ��ǩ������
		View view = layoutInflater.inflate(R.layout.layout_tab_view, null);

		ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
		TextView textView = (TextView) view.findViewById(R.id.textView);
		imageView.setImageResource(mImageViewArray[index]);
		textView.setText(getString(mTextViewArray[index]));
		return view;
	}
	
	/**����ÿ����ǩҳ�����б�ǩ�л�������*/
 	public void loadTabHost(){
 		Log.e("MainActivity", "����tabHost");
		tabHost=getTabHost();
		tabHost.addTab(tabHost.newTabSpec("myMusic").setIndicator(getTabItemView(0)).setContent(new Intent(context,MyMusicListActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("playingMusic").setIndicator(getTabItemView(1)).setContent(new Intent(context,PlayMusicListActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("allMusic").setIndicator(getTabItemView(2)).setContent(new Intent(context,AllMusicListActivity.class)));
//		tabHost.addTab(tabHost.newTabSpec("myMusic").setIndicator("�ҵ�����",getResources().getDrawable(R.drawable.tab_aixin)).setContent(new Intent(context,MyMusicListActivity.class)));
//		tabHost.addTab(tabHost.newTabSpec("playingMusic").setIndicator("�����б�",getResources().getDrawable(R.drawable.tab_bofang)).setContent(new Intent(context,PlayMusicListActivity.class)));
//		tabHost.addTab(tabHost.newTabSpec("allMusic").setIndicator("���и���",getResources().getDrawable(R.drawable.ic_launcher)).setContent(new Intent(context,AllMusicListActivity.class)));
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				if(tabId.equals("myMusic")){
					viewPager.setCurrentItem(0);
				}
				if(tabId.equals("playingMusic")){
					viewPager.setCurrentItem(1);
				}
				if(tabId.equals("allMusic")){
					viewPager.setCurrentItem(2);
				}
			}
		});		
	}

	/**���ر�ǩҳ��������viewPager,���Ұ����������л�������*/
	public void loadViewPager(){
		Log.e("MainActivity", "����viewPager");
		viewPager=(ViewPager) findViewById(R.id.viewPager);
		final ArrayList<View> list=new ArrayList<View>();
		list.add(getView("myMusic",new Intent(context,MyMusicListActivity.class)));
		list.add(getView("playingMusic", new Intent(context,PlayMusicListActivity.class)));
		list.add(getView("allMusic", new Intent(context,AllMusicListActivity.class)));
		
		/**
		 * Ϊ��ǩҳ������ʾҳ��viewPager����������,���ƻ����л�
		 */
		viewPager.setAdapter(new PagerAdapter() {
			
			@Override
			public void destroyItem(View arg0, int arg1, Object arg2) {
				viewPager.removeView(list.get(arg1));
			}

			@Override
			public Object instantiateItem(View arg0, int arg1) {
				((ViewPager)arg0).addView(list.get(arg1));
				return list.get(arg1);
			}

			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0==arg1;
			}
			
			@Override
			public int getCount() {
				return list.size();
			}

			@Override
			public void finishUpdate(View arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void restoreState(Parcelable arg0, ClassLoader arg1) {
				// TODO Auto-generated method stub
			}

			@Override
			public Parcelable saveState() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void startUpdate(View arg0) {
				// TODO Auto-generated method stub
			}			
		});
		
		/**
		 * ΪviewPager���л�������,��viewPage�仯ʱ������tabhost
		 */
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				tabHost.setCurrentTab(arg0);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}
}
