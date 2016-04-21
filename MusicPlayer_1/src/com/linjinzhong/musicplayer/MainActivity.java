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
 * 主界面，TabHost
 * 三个标签：我的音乐，播放列表，所有歌曲
 * 底部一个正在播放音乐的textView，点击进入正在播放界面
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
	
	// 定义数组来存放按钮图片
	private int mImageViewArray[] = { R.drawable.tabview_mymusic_select,R.drawable.tabview_playlist_select, R.drawable.tabview_allmusic_select };
	// 对应名称
	private int mTextViewArray[] = { R.string.text_mymusic, R.string.text_playlist,
				R.string.text_allmusic };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_main);
		Log.e("MainActivity", "进入MainActivity");
		context=MainActivity.this;
		footer=(TextView) findViewById(R.id.text_footer_playingMusic);
		// 实例化布局对象
		layoutInflater = LayoutInflater.from(this);
		
		manager=new LocalActivityManager(this, true);
		manager.dispatchCreate(savedInstanceState);
		
		//加载标签卡
		this.loadTabHost();
		//加载viewPager
		this.loadViewPager();
		//接下来执行getView方法，加载每个播放列表activity
		Log.e("MainActivity", "TabHost和ViewPager加载完成,进入设置当前Tab");
		//设置当前标签为中间的播放列表
		tabHost.setCurrentTab(1);
		Log.e("MainActivity", "当前Tab设置完成");
		//为底下正在播放的textView设置监听器,跳转到播放界面
		footer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity.context.startActivity(new Intent(MainActivity.this,PlayMusicActivity.class));
			}
		});
	}

	@Override
	protected void onResume() {
		Log.e("MainActivity", "进入MainActivity的onResum");
		if(MyApplication.playStatus!=0){
			footer.setText(PlayService.name);
		}
		super.onResume();
	}
	
	public View getView(String id,Intent intent){
		Log.e("MainActivity", "进入TabHost的getView方法");
		return manager.startActivity(id, intent).getDecorView();
	}
	
	/**
	 * 给Tab设置标签卡视图包含文字和图标
	 */
	public View getTabItemView(int index) {
		// 标签填充界面
		View view = layoutInflater.inflate(R.layout.layout_tab_view, null);

		ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
		TextView textView = (TextView) view.findViewById(R.id.textView);
		imageView.setImageResource(mImageViewArray[index]);
		textView.setText(getString(mTextViewArray[index]));
		return view;
	}
	
	/**加载每个标签页，含有标签切换监听器*/
 	public void loadTabHost(){
 		Log.e("MainActivity", "加载tabHost");
		tabHost=getTabHost();
		tabHost.addTab(tabHost.newTabSpec("myMusic").setIndicator(getTabItemView(0)).setContent(new Intent(context,MyMusicListActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("playingMusic").setIndicator(getTabItemView(1)).setContent(new Intent(context,PlayMusicListActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("allMusic").setIndicator(getTabItemView(2)).setContent(new Intent(context,AllMusicListActivity.class)));
//		tabHost.addTab(tabHost.newTabSpec("myMusic").setIndicator("我的音乐",getResources().getDrawable(R.drawable.tab_aixin)).setContent(new Intent(context,MyMusicListActivity.class)));
//		tabHost.addTab(tabHost.newTabSpec("playingMusic").setIndicator("播放列表",getResources().getDrawable(R.drawable.tab_bofang)).setContent(new Intent(context,PlayMusicListActivity.class)));
//		tabHost.addTab(tabHost.newTabSpec("allMusic").setIndicator("所有歌曲",getResources().getDrawable(R.drawable.ic_launcher)).setContent(new Intent(context,AllMusicListActivity.class)));
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

	/**加载标签页具体内容viewPager,并且绑定适配器和切换监听器*/
	public void loadViewPager(){
		Log.e("MainActivity", "加载viewPager");
		viewPager=(ViewPager) findViewById(R.id.viewPager);
		final ArrayList<View> list=new ArrayList<View>();
		list.add(getView("myMusic",new Intent(context,MyMusicListActivity.class)));
		list.add(getView("playingMusic", new Intent(context,PlayMusicListActivity.class)));
		list.add(getView("allMusic", new Intent(context,AllMusicListActivity.class)));
		
		/**
		 * 为标签页具体显示页面viewPager设置适配器,手势滑动切换
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
		 * 为viewPager绑定切换监听器,当viewPage变化时，设置tabhost
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
