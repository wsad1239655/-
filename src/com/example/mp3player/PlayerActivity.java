package com.example.mp3player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import custom.LrcView;
import model.AppConstant;
import model.Mp3Info;
import utils.ImageUtils;
import utils.MediaUtils;

public class PlayerActivity extends Activity {
	
	private TextView musicTitle = null;
	private TextView musicArtist = null;
	private ImageButton reverseButton;
	private ImageButton playButton;
	private ImageButton shuffleButton;
	private ImageButton nextButton;
	private ImageButton queueButton;
	private SeekBar music_progressBar;
	private TextView currentProgress;
	private	TextView finalProgress;
	private ImageView titleBack;
	//滑动界面
	private View albumView,lyricView;
	private ViewPager viewPager;
	private List<View> viewList;
	
	public static LrcView lrcView; // 自定义歌词视图
	private ImageView playerMusicAlbum;	//音乐专辑封面
	
	private String title;       //歌曲标题  
	private String artist;      //歌曲艺术家  
	private String url;         //歌曲路径  
	private int listPosition = 0;   //播放歌曲在mp3Infos的位置  
	private int currentTime;    //当前歌曲播放时间  
	private int duration;       //歌曲长度  
	private int flag;           //播放标识  
	private String bigAlumUrl; //专辑大图；
	
	private int repeatState;  
	private final int isCurrentRepeat = 1; // 单曲循环  
	private final int isShuffle = 2;   	// 随机播放  
	private final int isNoneRepeat = 3;     // 顺序播放 
	private boolean isPlaying;              // 正在播放  
	private boolean isFirstTime = true;
	private boolean isPause;                // 暂停  
	
	private PlayerReceiver playerReceiver;
	
	private List<Mp3Info> mp3Infos;
	 
	//创建一个数列记录播放位置
	private List list;
	  
	
	public static final String UPDATE_ACTION = "com.example.action.UPDATE_ACTION";  //更新动作  
	public static final String CTL_ACTION = "com.example.action.CTL_ACTION";        //控制动作  
	public static final String MUSIC_CURRENT = "com.example.action.MUSIC_CURRENT";  //音乐当前时间改变动作  
	public static final String MUSIC_DURATION = "com.example.action.MUSIC_DURATION";//音乐播放长度改变动作  
	public static final String MUSIC_PLAYING = "com.example.action.MUSIC_PLAYING";  //音乐正在播放动作  
	public static final String REPEAT_ACTION = "com.example.action.REPEAT_ACTION";  //音乐重复播放动作  
	public static final String SHUFFLE_ACTION = "com.example.action.SHUFFLE_ACTION";//音乐随机播放动作 
	public static final String LIST_ACTION = "com.example.action.LIST_ACTION";      //记录音乐播放列表
	public static final String ISPLAYINT_ACTION = "com.example.action.ISPLAYINT_ACTION";//更新播放图标
	public static final String SHOW_LRC = "com.example.action.SHOW_LRC";			//通知显示歌词
	
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_activity);
		
		ImageUtils.initImageLoader(this);// ImageLoader初始化
		//监听列表位置
		list = new ArrayList<Integer>();
		viewList = new ArrayList<View>();
		//初始化
		findViewById();
		
		repeatState = isNoneRepeat; // 初始状态为无重复播放状态
		
		
		
		setViewOnClickListener();
		mp3Infos = MediaUtils.getMp3Infos(PlayerActivity.this);		
		//监听动作
		playerReceiver = new PlayerReceiver();		
		IntentFilter filter = new IntentFilter();
		filter.addAction(UPDATE_ACTION);
		filter.addAction(MUSIC_DURATION);
		filter.addAction(MUSIC_CURRENT);
		filter.addAction(ISPLAYINT_ACTION);
		registerReceiver(playerReceiver, filter);
		//滑动界面
		PagerViewAdapter adapter = new PagerViewAdapter();	
		viewPager.setAdapter(adapter);
		

	}
	
	//滑动切换歌词与专辑页面
	private class PagerViewAdapter extends PagerAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return viewList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			super.destroyItem(container, position, object);
			
			container.removeView(viewList.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			// TODO Auto-generated method stub
			container.addView(viewList.get(position));
			
			
			return viewList.get(position);
		}
		
	}
	
	
	//定义控件
	private void findViewById(){
		musicTitle = (TextView)findViewById(R.id.musicTitle);
		musicArtist = (TextView)findViewById(R.id.musicArtist);
		reverseButton = (ImageButton)findViewById(R.id.reverse_music);
		playButton = (ImageButton)findViewById(R.id.play_music);
		nextButton = (ImageButton)findViewById(R.id.next_music);
		shuffleButton = (ImageButton)findViewById(R.id.shuffle_music);
		queueButton = (ImageButton)findViewById(R.id.play_queue);
		music_progressBar = (SeekBar)findViewById(R.id.audioTrack);
		currentProgress = (TextView)findViewById(R.id.current_progress);
		finalProgress = (TextView)findViewById(R.id.final_progress);
		titleBack = (ImageView)findViewById(R.id.titleBack);
		
		viewPager = (ViewPager)findViewById(R.id.viewpager);
		LayoutInflater inflater = getLayoutInflater();
		albumView = inflater.inflate(R.layout.music_album, null);
		lyricView = inflater.inflate(R.layout.music_lyric, null);
		viewList.add(albumView);
		viewList.add(lyricView);
		
		lrcView = (LrcView)lyricView.findViewById(R.id.lrcShowView);
		playerMusicAlbum = (ImageView)albumView.findViewById(R.id.iv_music_ablum);
		
	}
	//设置控件监听器
	private void setViewOnClickListener(){
		ViewOnClickListener clickListener = new ViewOnClickListener();
		reverseButton.setOnClickListener(clickListener);
		playButton.setOnClickListener(clickListener);
		shuffleButton.setOnClickListener(clickListener);
		nextButton.setOnClickListener(clickListener);
		titleBack.setOnClickListener(clickListener);
		music_progressBar.setOnSeekBarChangeListener(new SeekBarChangeListener());
	}
	
	//每次启动Activity都更新界面
	@Override
	protected void onResume() {
		super.onResume();
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		title = bundle.getString("title");
		artist = bundle.getString("artist");
		url = bundle.getString("url");
		listPosition = bundle.getInt("listPosition");
		flag = bundle.getInt("MSG");
		currentTime = bundle.getInt("currentTime");
		duration = bundle.getInt("duration");
		isPlaying = bundle.getBoolean("isPlaying");
		bigAlumUrl = bundle.getString("bigAlumUrl");
		
				
		//接收广播并判断播放状态，更改播放图标
		if (isPlaying) {
			playButton.setImageResource(R.drawable.pause);
			isPause = false;
		}
		else {
			playButton.setImageResource(R.drawable.play);
			isPlaying = false;
			isPause = true;
		}
		//判断是否第一次播放
		if (currentTime > 0) {
			isFirstTime = false;
		}
		//进入PlayerActivity，发送广播，更新歌词
		if (flag == AppConstant.PlayerMsg.PLAYING_MSG) { // 如果播放信息是正在播放
			mp3Infos = (List<Mp3Info>) bundle.getSerializable("listSearchResult");
			Intent intent1 = new Intent();
			intent1.setAction(SHOW_LRC);
			intent1.putExtra("listPosition", listPosition);
			intent1.putExtra("listSearchResult", (Serializable)mp3Infos);
			sendBroadcast(intent1);
		}
		
		
		
		initView();
				
	}
	
	//初始化界面
	private void initView(){
		musicTitle.setText(title);
		musicArtist.setText(artist);
		music_progressBar.setProgress(currentTime);
		music_progressBar.setMax(duration);
		finalProgress.setText(MediaUtils.formatTime(mp3Infos.get(listPosition).getDuration()));
		//设置专辑封面
		Mp3Info mp3Info = mp3Infos.get(listPosition);
		if (bigAlumUrl != null) {
			ImageUtils.disPlay(bigAlumUrl, playerMusicAlbum);
		}
		else{
			Bitmap bm = MediaUtils.getArtwork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, false);
			playerMusicAlbum.setImageBitmap(bm);
		}

	}
	//控件点击事件
	private class ViewOnClickListener implements OnClickListener{

		Intent intent = new Intent();
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.play_music:
				if(isFirstTime){
					isFirstTime = false;
					isPlaying = true;
					isPause = false;
					play(0);
					
					Intent intent = new Intent(ISPLAYINT_ACTION);
					intent.putExtra("isPlaying", true);
					sendBroadcast(intent);
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
						//图标更改标识
						intent = new Intent(ISPLAYINT_ACTION);
						intent.putExtra("isPlaying", false);
						sendBroadcast(intent);
					} 
					else if(isPause){
						playButton.setImageResource(R.drawable.pause);
						intent.setAction("com.example.MUSIC_SERVICE");
						intent.putExtra("MSG", AppConstant.PlayerMsg.CONTINUE_MSG);
						intent.setPackage(getPackageName());
						startService(intent);
						isPause = false;
						isPlaying = true;	
						
						intent = new Intent(ISPLAYINT_ACTION);
						intent.putExtra("isPlaying", true);
						sendBroadcast(intent);
					}
					
					
				}
			
				break;
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
			case R.id.reverse_music:
				playButton.setImageResource(R.drawable.pause);
				isFirstTime = false;
				isPlaying = true;
				isPause = false;
				reverse();
				intent = new Intent(LIST_ACTION);
				intent.putExtra("list", (Serializable)list);
				sendBroadcast(intent);
				break;
			
				
				
			case R.id.shuffle_music:
				//单曲循环
				if(repeatState == isNoneRepeat){
					repeat_one();
					repeatState = isCurrentRepeat;
					Toast.makeText(PlayerActivity.this, R.string.repeat_current, Toast.LENGTH_SHORT).show();
				}
				
				//随机播放
				else if(repeatState == isCurrentRepeat ){
					shuffle();
					repeatState = isShuffle;
					Toast.makeText(PlayerActivity.this, R.string.shuffle, Toast.LENGTH_SHORT).show();					
				}
				
				//顺序播放
				else if (repeatState == isShuffle) {
					repeat_none();
					repeatState = isNoneRepeat;
					Toast.makeText(PlayerActivity.this, R.string.repeat_none, Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.titleBack:
				PlayerActivity.this.finish();
				
				break;
			
			}
		}
		
	} 
	
	//播放音乐
	public void play(int position){
		playButton.setImageResource(R.drawable.pause);
		Mp3Info mp3Info = mp3Infos.get(position);
		musicTitle.setText(mp3Info.getTitle());
		Intent intent = new Intent();
		intent.putExtra("listPosition", 0);
		intent.putExtra("url", mp3Info.getUrl());
		intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);
		intent.setPackage(getPackageName());
		startService(intent);
	}
	
	//播放音乐
	private void play(){
		repeat_none();
		Intent intent = new Intent();
		intent.setAction("com.example.MUSIC_SERVICE");
		intent.putExtra("url", url);
		intent.putExtra("listPosition", listPosition);
		intent.putExtra("MSG", flag);
		intent.setPackage(getPackageName());
		startService(intent);
		
	}
	//单曲循环
	public void repeat_one(){
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("control", 1);
		sendBroadcast(intent);
	}
	//随机播放
	public void shuffle(){
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("control", 2);
		sendBroadcast(intent);
	}
	//顺序播放
	public void repeat_none(){
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("control", 3);
		sendBroadcast(intent);
	}
	
	//上一首
		public void reverse(){
			if (listPosition >  0) {
				listPosition--;
				list.add(listPosition);
				mp3Infos = MediaUtils.getMp3Infos(PlayerActivity.this); 
				Mp3Info mp3Info = mp3Infos.get(listPosition);
				Bitmap bm = MediaUtils.getArtwork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, false);
				playerMusicAlbum.setImageBitmap(bm);
				musicTitle.setText(mp3Info.getTitle());
				Intent intent = new Intent();
				intent.setAction("com.example.MUSIC_SERVICE");
				intent.putExtra("listPosition", listPosition);
				intent.putExtra("url", mp3Info.getUrl());
				intent.putExtra("MSG", AppConstant.PlayerMsg.REVERSE_MSG);
				intent.setPackage(getPackageName());//添加此步骤才可以启动服务
				startService(intent);
			}
			
		}
		//下一首
		public void next(){
			if (listPosition < mp3Infos.size() - 1) {
				listPosition++;
				list.add(listPosition);
				mp3Infos = MediaUtils.getMp3Infos(PlayerActivity.this);  
				Mp3Info mp3Info = mp3Infos.get(listPosition);
				Bitmap bm = MediaUtils.getArtwork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, false);
				playerMusicAlbum.setImageBitmap(bm);
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
	
	//接收service传回的广播信息
		public class PlayerReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(MUSIC_CURRENT)) {
				currentTime = intent.getIntExtra("currentTime", -1);
				currentProgress.setText(MediaUtils.formatTime(currentTime));
				music_progressBar.setProgress(currentTime);
			}
			else if (action.equals(MUSIC_DURATION)) {
				int duration = intent.getIntExtra("duration", -1);
				music_progressBar.setMax(duration);
				finalProgress.setText(MediaUtils.formatTime(duration));
				
				
			}
			else if (action.equals(UPDATE_ACTION)) {
				listPosition = intent.getIntExtra("current", -1);
				url = mp3Infos.get(listPosition).getUrl();
				if (listPosition >= 0) {
					musicTitle.setText(mp3Infos.get(listPosition).getTitle());
					musicArtist.setText(mp3Infos.get(listPosition).getArtist());
					
				} 
				if(listPosition == 0){
					finalProgress.setText(MediaUtils.formatTime(mp3Infos.get(listPosition).getDuration()));
					playButton.setImageResource(R.drawable.pause);
					isPause = true;
				}
						
			}
			
		
				
			
			
		}
			
		}
	
	 
	
	//监听seekbar,随着拖动而播放当前的进度
	private class SeekBarChangeListener implements OnSeekBarChangeListener{

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (fromUser) {
				
				audioTrackChange(progress);
				
				playButton.setImageResource(R.drawable.pause);
				isPause = false;
				isPlaying = true;
			}
			
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}
		
	}
	//播放进度条的时间
	private void audioTrackChange(int progress){
		Intent intent = new Intent();
		intent.setAction("com.example.MUSIC_SERVICE");
		intent.putExtra("url", url);
		intent.putExtra("listPosition", listPosition);
		intent.putExtra("MSG", AppConstant.PlayerMsg.PROGRESS_CHANGE);
		intent.putExtra("progress", progress);
		intent.setPackage(getPackageName());
		startService(intent);
		
	}
	
	
	

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(playerReceiver);
	}
	

}
