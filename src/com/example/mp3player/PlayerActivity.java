package com.example.mp3player;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioTrack;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import model.AppConstant;
import model.Mp3Info;
import utils.MediaUtils;

public class PlayerActivity extends Activity {
	
	private TextView musicTitle = null;
	private TextView musicArtist = null;
	private ImageButton reverseButton;
	private ImageButton repeatButton;
	private ImageButton playButton;
	private ImageButton shuffleButton;
	private ImageButton nextButton;
	private ImageButton searchButton;
	private ImageButton queueButton;
	private SeekBar music_progressBar;
	private TextView currentProgress;
	private	TextView finalProgress;
	
	 private String title;       //歌曲标题  
	 private String artist;      //歌曲艺术家  
	 private String url;         //歌曲路径  
	 private int listPosition;   //播放歌曲在mp3Infos的位置  
	 private int currentTime;    //当前歌曲播放时间  
	 private int duration;       //歌曲长度  
	 private int flag;           //播放标识  
	 
	 private int repeatState;  
	 private final int isCurrentRepeat = 1; // 单曲循环  
	 private final int isAllRepeat = 2;      // 全部循环  
	 private final int isNoneRepeat = 3;     // 无重复播放  
	 private boolean isPlaying;              // 正在播放  
	 private boolean isPause;                // 暂停  
	 private boolean isNoneShuffle;           // 顺序播放  
	 private boolean isShuffle;          // 随机播放 
	    
	private List<Mp3Info> mp3Infos;
	
	 public static final String UPDATE_ACTION = "com.example.action.UPDATE_ACTION";  //更新动作  
	 public static final String CTL_ACTION = "com.example.action.CTL_ACTION";        //控制动作  
	 public static final String MUSIC_CURRENT = "com.example.action.MUSIC_CURRENT";  //音乐当前时间改变动作  
	 public static final String MUSIC_DURATION = "com.example.action.MUSIC_DURATION";//音乐播放长度改变动作  
	 public static final String MUSIC_PLAYING = "com.example.action.MUSIC_PLAYING";  //音乐正在播放动作  
	 public static final String REPEAT_ACTION = "com.example.action.REPEAT_ACTION";  //音乐重复播放动作  
	 public static final String SHUFFLE_ACTION = "com.example.action.SHUFFLE_ACTION";//音乐随机播放动作  
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_activity);
		
		findViewById();
	}
	
	//定义控件
	private void findViewById(){
		musicTitle = (TextView)findViewById(R.id.musicTitle);
		musicArtist = (TextView)findViewById(R.id.musicArtist);
		reverseButton = (ImageButton)findViewById(R.id.reverse_music);
		repeatButton = (ImageButton)findViewById(R.id.repeat_music);
		playButton = (ImageButton)findViewById(R.id.play_music);
		shuffleButton = (ImageButton)findViewById(R.id.shuffle_music);
		nextButton = (ImageButton)findViewById(R.id.next_music);
		searchButton = (ImageButton)findViewById(R.id.search_music);
		queueButton = (ImageButton)findViewById(R.id.play_queue);
		music_progressBar = (SeekBar)findViewById(R.id.audioTrack);
		currentProgress = (TextView)findViewById(R.id.current_progress);
		finalProgress = (TextView)findViewById(R.id.final_progress);
	}
	//设置控件监听器
	private void setViewOnClickListener(){
		
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
		initView();
	}
	
	//初始化界面
	private void initView(){
		musicTitle.setText(title);
		musicArtist.setText(artist);
		music_progressBar.setProgress(currentTime);
		music_progressBar.setMax(duration);
	
		playButton.setImageResource(R.drawable.pause);
		isPlaying = true;
		isPause = false;
	}
	//控件点击事件
	private class ViewOnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.play_music:
				
				
				break;

			
			}
		}
		
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
	//顺序播放
	public void repeat_none(){
		
	}
	//单曲循环
	public void repeat_one(){
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("control", 1);
		sendBroadcast(intent);
	}
	//随机播放
	public void shuffleMusic(){
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("contril", 4);
		sendBroadcast(intent);
	}
	//全部循环
	public void repeat_all(){
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("control", 2);
		sendBroadcast(intent);
	}
	
	
	
	
	
	//监听seekbar,随着拖动而播放当前的进度
	private class SeekBarChangeListener implements OnSeekBarChangeListener{

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if(fromUser){
				audioTrackChange(progress);
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
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	

}
