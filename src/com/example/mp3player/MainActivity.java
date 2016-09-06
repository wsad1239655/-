package com.example.mp3player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import adapter.MusicListAdapter;
import android.R.integer;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import model.AppConstant;
import model.Mp3Info;
import mp3fragment.Localfragment;
import mp3fragment.NetFragment;
import utils.MediaUtils;

public class MainActivity extends Activity {
	
	private ListView mMusiclist;//定义ListView
	private MusicListAdapter listAdapter;//定义资源适配器
	private List<Mp3Info> mp3Infos = null;//
	private ImageButton reverseButton;//上一首
	private ImageButton repeatButton;//重复
	private ImageButton playButton;//播放
	private ImageButton shuffleButton;//随机
	private ImageButton nextButton;//下一首
	private ImageButton musicPlaying;//正在播放
	private TextView musicTitle;//歌名
	private TextView musicDuration;//时长
	private TextView localList;
	private TextView netList;
	
	private int repeatState;        //循环标识  
	private final int isCurrentRepeat = 1; // 单曲循环  
	private final int isAllRepeat = 2; // 全部循环  
	private final int isNoneRepeat = 3; // 无重复播放  
	
	private boolean isFirstTime = true;   
	private boolean isPlaying; // 正在播放  
	private boolean isPause; // 暂停  
	private boolean isNoneShuffle = true; // 顺序播放  
	private boolean isShuffle = false; // 随机播放  	      
	private int listPosition = 0;   //标识列表位置 
	private int currentTime;  //当前播放时间
	private int duration;  //歌曲长度
	private static final String fragment1Tag = "fragment1";
	private static final String fragment2Tag = "fragment2";
	
	FragmentManager manager;
	FragmentTransaction transaction;
	Fragment fragment1;
	Fragment fragment2;
	
	 //一系列动作  
    public static final String UPDATE_ACTION = "com.example.action.UPDATE_ACTION";  
    public static final String CTL_ACTION = "com.example.action.CTL_ACTION";  
    public static final String MUSIC_CURRENT = "com.example.action.MUSIC_CURRENT";  
    public static final String MUSIC_DURATION = "com.example.action.MUSIC_DURATION";  
    public static final String REPEAT_ACTION = "com.example.action.REPEAT_ACTION";  
    public static final String SHUFFLE_ACTION = "com.example.action.SHUFFLE_ACTION";
    public static final String LIST_ACTION = "com.example.action.LIST_ACTION";      //记录音乐播放列表
    public static final String ISPLAYINT_ACTION = "com.example.action.ISPLAYINT_ACTION";//更新播放图标

    
    private MyReceiver myReceiver;
    
  //创建一个数列记录播放位置
  	private List list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		list = new ArrayList<Integer>();
		mp3Infos = MediaUtils.getMp3Infos(MainActivity.this); // 需要获取SD卡权限，再获取歌曲对象集合
//		mMusiclist = (ListView) findViewById(R.id.music_list);
//		listAdapter = new MusicListAdapter(this, mp3Infos);
//		mMusiclist.setAdapter(listAdapter);
//		mMusiclist.setOnCreateContextMenuListener(new MusicListItemContextMenuListener());
//		mMusiclist.setOnItemClickListener(new MusicListItemClickListener());
		findViewById();
		setOnclickListioner();
				
		//动态注册广播监听器，监听service返回的消息
		myReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		 filter.addAction(UPDATE_ACTION);  
		 filter.addAction(MUSIC_CURRENT);  
		 filter.addAction(MUSIC_DURATION);  
		 filter.addAction(REPEAT_ACTION);  
		 filter.addAction(SHUFFLE_ACTION);  
		 filter.addAction(LIST_ACTION);
		 filter.addAction(ISPLAYINT_ACTION);
		 registerReceiver(myReceiver, filter);
		
		 //创建fragment1并显示列表布局
		 manager = getFragmentManager();
		 transaction = manager.beginTransaction();
		 fragment1 = manager.findFragmentByTag(fragment1Tag);
		 fragment1 = new Localfragment();
		 transaction.add(R.id.fragment_layout, fragment1, fragment1Tag);
		 transaction.addToBackStack(null);			 
		 transaction.commit();
		 
	}
	
	//定义view的Id
	private void findViewById() {
		playButton = (ImageButton)findViewById(R.id.play_music);
		nextButton = (ImageButton)findViewById(R.id.next_music);
		musicPlaying = (ImageButton)findViewById(R.id.playing);
		musicTitle = (TextView)findViewById(R.id.music_title);
		musicDuration = (TextView)findViewById(R.id.music_duration);
		localList = (TextView)findViewById(R.id.locallist);
		netList = (TextView)findViewById(R.id.netlist);		
	}
	//注册监听器
	private void setOnclickListioner(){
		ViewClickLintener viewClickLintener = new ViewClickLintener();
		playButton.setOnClickListener(viewClickLintener);
		nextButton.setOnClickListener(viewClickLintener);
		musicPlaying.setOnClickListener(viewClickLintener);
		musicDuration.setOnClickListener(viewClickLintener);
		musicTitle.setOnClickListener(viewClickLintener);
		localList.setOnClickListener(viewClickLintener);
		netList.setOnClickListener(viewClickLintener);

	}
	//定义监听器
	private class ViewClickLintener implements OnClickListener{
		
		Intent intent = new Intent();
		@Override
		public void onClick(View v) {
			manager = getFragmentManager();
			transaction = manager.beginTransaction();
			fragment1 = manager.findFragmentByTag(fragment1Tag);
			fragment2 = manager.findFragmentByTag(fragment2Tag);
		
			switch (v.getId()) {
			
			
			//播放音乐	
			case R.id.play_music:
				if(isFirstTime){
					isFirstTime = false;
					isPlaying = true;
					isPause = false;
					play(0);
				}
				else{
					if (isPlaying) {
						playButton.setImageResource(R.drawable.play);
						intent.setAction("com.example.MUSIC_SERVICE");
						intent.putExtra("MSG", AppConstant.PlayerMsg.PAUSE_MSG);
						intent.setPackage(getPackageName());
						startService(intent);
						isPlaying = false;
						isPause = true;
						
						
					} 
					else if(isPause){
						playButton.setImageResource(R.drawable.pause);
						intent.setAction("com.example.MUSIC_SERVICE");
						intent.putExtra("MSG", AppConstant.PlayerMsg.CONTINUE_MSG);
						intent.setPackage(getPackageName());
						startService(intent);
						isPause = false;
						isPlaying = true;
						
					
					}
				}
				
				break;
			
			//下一首
			case R.id.next_music:
				playButton.setImageResource(R.drawable.pause);
				isFirstTime = false;
				isPlaying = true;
				isPause = false;
				next();
				
				intent = new Intent(LIST_ACTION);
				intent.putExtra("list", (Serializable)list);
				sendBroadcast(intent);
				break;
			//正在播放
			case R.id.playing:
				startPlayerActivity();
				 break;  
				//正在播放
			case R.id.music_title:
				startPlayerActivity();
				 break;  
				//正在播放
			case R.id.music_duration:
				startPlayerActivity();
				 break;  
				 
			case R.id.locallist:
				//隐藏fragment2，显示fragment1
				transaction.hide(fragment2);				
				transaction.show(fragment1);				
				transaction.commit();				
				break;	 
				
			case R.id.netlist:
				//隐藏fragment1，显示fragment2
				transaction.hide(fragment1);
				if (fragment2 == null) {
					fragment2 = new NetFragment();
					transaction.add(R.id.fragment_layout, fragment2, fragment2Tag);
					transaction.addToBackStack(null);
				} else {
					transaction.show(fragment2);
				}
				//结束语句
				transaction.commit();
				break;
				
			}
			
		}
		
	}
	
	//启动PlayerActivity
	private void startPlayerActivity(){
		 Mp3Info mp3Info = mp3Infos.get(listPosition);  
		 Intent intent = new Intent(MainActivity.this, PlayerActivity.class);  
		 intent.putExtra("title", mp3Info.getTitle());     
		 intent.putExtra("url", mp3Info.getUrl());  
		 intent.putExtra("artist", mp3Info.getArtist());  
		 intent.putExtra("listPosition", listPosition);  
		 intent.putExtra("currentTime", currentTime);  
		 intent.putExtra("duration", duration);  
		 intent.putExtra("MSG", AppConstant.PlayerMsg.PLAYING_MSG);
		 if (isPlaying) {
			intent.putExtra("isPlaying", true);
		} else {
			intent.putExtra("isPlaying", false);
		}
		 startActivity(intent);  
	}
	
//	//监听列表
//	private class MusicListItemClickListener implements OnItemClickListener{
//	
//		//创建
//		Intent intent = new Intent();
//		@Override
//		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//			
//			//监听并改变当前播放音乐的位置
//			listPosition = position;
//			//放入当前的播放位置在列表
//			list.add(listPosition);
//			//第一次点击列表时播放音乐，第二次则判断是否点击同一个位置，进行播放与暂停
//		 if(list.size() > 1){
//			 if (list.get(list.size()-2) == (Object)listPosition) {
//				 if (isPlaying) {
//						playButton.setImageResource(R.drawable.play);
//						intent.setAction("com.example.MUSIC_SERVICE");
//						intent.putExtra("MSG", AppConstant.PlayerMsg.PAUSE_MSG);
//						intent.setPackage(getPackageName());
//						startService(intent);
//						isPlaying = false;
//						isPause = true;
//					} 
//					else if(isPause){
//						playButton.setImageResource(R.drawable.pause);
//						intent.setAction("com.example.MUSIC_SERVICE");
//						intent.putExtra("MSG", AppConstant.PlayerMsg.CONTINUE_MSG);
//						intent.setPackage(getPackageName());
//						startService(intent);
//						isPause = false;
//						isPlaying = true;
//					}
//			}
//			 else{
//				 play(listPosition);
//			 }
//		 }
//		 else{
//			 isFirstTime = false;
//			 isPlaying = true;
//			 isPause = false;
//			 play(listPosition);
//		 }
//			
//			
//		}
//		
//	}
	
	//给列表的子View增加菜单项
	private class  MusicListItemContextMenuListener implements OnCreateContextMenuListener{

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			
		}
		
	}
	
	//下一首
	public void next(){
		if (listPosition < mp3Infos.size() - 1) {
			listPosition++;
			//放入当前的播放位置在列表
			list.add(listPosition);
			Mp3Info mp3Info = mp3Infos.get(listPosition);
			musicTitle.setText(mp3Info.getTitle());
			Intent intent = new Intent();
			intent.setAction("com.example.MUSIC_SERVICE");
			intent.putExtra("listPosition", listPosition);
			intent.putExtra("url", mp3Info.getUrl());
			intent.putExtra("MSG", AppConstant.PlayerMsg.NEXT_MSG);
			intent.setPackage(getPackageName());
			startService(intent);
		} 
		
	}
	//播放
	public void play(int listposition){
		playButton.setImageResource(R.drawable.pause);
		Mp3Info mp3Info = mp3Infos.get(listposition);
		musicTitle.setText(mp3Info.getTitle());
		Intent intent = new Intent();
		intent.putExtra("listPosition", 0);
		intent.putExtra("url", mp3Info.getUrl());
		intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);
		intent.setPackage(getPackageName());
		startService(intent);
	}

	
	//正在播放的音乐
	public void playMusic(int listposition){
		if(mp3Infos != null){
			Mp3Info mp3Info = mp3Infos.get(listposition);
			musicTitle.setText(mp3Info.getTitle());
			Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
			intent.putExtra("title", mp3Info.getTitle());
			intent.putExtra("url", mp3Info.getUrl());
			intent.putExtra("artist", mp3Info.getArtist());
			intent.putExtra("listPosition", listPosition);  
			intent.putExtra("currentTime", currentTime);  
			intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);  
            startActivity(intent);  
		}
		
	}
	
	//设置子View的菜单项
	public void MisicListItemDialog(){
			
	}
	
	
	
	//定义一个广播接收器，接收service返回的广播
	public class MyReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			//设置播放的时间
			if (action.equals(MUSIC_CURRENT)) {
				currentTime = intent.getIntExtra("currentTime", -1);
				musicDuration.setText(MediaUtils.formatTime(currentTime));
				
				
			}
			//设置返回歌曲长度
			else if (action.equals(MUSIC_DURATION)) {
				duration = intent.getIntExtra("duration", -1);
			}
			//点击上一首下一首返回的歌曲位置
			else if (action.equals(UPDATE_ACTION)) {
				listPosition = intent.getIntExtra("current", -1);
				if (listPosition >= 0) {
					musicTitle.setText(mp3Infos.get(listPosition).getTitle());
				}
				
			}
			
			//监听fragment的播放列表与播放位置
			else if(action.equals(LIST_ACTION)){
				list = (List) intent.getSerializableExtra("list");
				listPosition = intent.getIntExtra("listPosition", -1);
				if (list.size()>0) {
					isFirstTime = false;
					isPlaying = true;
					isPause = false;
				}
				
			}
			//更改播放图标
			else if(action.equals(ISPLAYINT_ACTION)){
				isPlaying = intent.getBooleanExtra("isPlaying", true);
				if (isPlaying) {
					playButton.setImageResource(R.drawable.pause);
				} else {
					playButton.setImageResource(R.drawable.play);
					isPlaying = false;
					isPause = true;
					
				}
			}
			
			
		}
		
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

