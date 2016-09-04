package com.example.mp3player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
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
	
	 private String title;       //��������  
	 private String artist;      //����������  
	 private String url;         //����·��  
	 private int listPosition = 0;   //���Ÿ�����mp3Infos��λ��  
	 private int currentTime;    //��ǰ��������ʱ��  
	 private int duration;       //��������  
	 private int flag;           //���ű�ʶ  
	 
	 private int repeatState;  
	 private final int isCurrentRepeat = 1; // ����ѭ��  
	 private final int isAllRepeat = 2;      // ȫ��ѭ��  
	 private final int isNoneRepeat = 3;     // ���ظ�����  
	 private boolean isPlaying;              // ���ڲ���  
	 private boolean isFirstTime = true;
	 private boolean isPause;                // ��ͣ  
	 private boolean isNoneShuffle;           // ˳�򲥷�  
	 private boolean isShuffle;          // ������� 
	 
	 private PlayerReceiver playerReceiver;
	    
	 private List<Mp3Info> mp3Infos;
	 
	//����һ�����м�¼����λ��
	  List list;
	  
	
	 public static final String UPDATE_ACTION = "com.example.action.UPDATE_ACTION";  //���¶���  
	 public static final String CTL_ACTION = "com.example.action.CTL_ACTION";        //���ƶ���  
	 public static final String MUSIC_CURRENT = "com.example.action.MUSIC_CURRENT";  //���ֵ�ǰʱ��ı䶯��  
	 public static final String MUSIC_DURATION = "com.example.action.MUSIC_DURATION";//���ֲ��ų��ȸı䶯��  
	 public static final String MUSIC_PLAYING = "com.example.action.MUSIC_PLAYING";  //�������ڲ��Ŷ���  
	 public static final String REPEAT_ACTION = "com.example.action.REPEAT_ACTION";  //�����ظ����Ŷ���  
	 public static final String SHUFFLE_ACTION = "com.example.action.SHUFFLE_ACTION";//����������Ŷ��� 
	 public static final String LIST_ACTION = "com.example.action.LIST_ACTION";      //��¼���ֲ����б�
	 public static final String ISPLAYINT_ACTION = "com.example.action.ISPLAYINT_ACTION";//���²���ͼ��

	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_activity);
		
		list = new ArrayList();
		findViewById();
		setViewOnClickListener();
		mp3Infos = MediaUtils.getMp3Infos(PlayerActivity.this);
		playerReceiver = new PlayerReceiver();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(UPDATE_ACTION);
		filter.addAction(MUSIC_DURATION);
		filter.addAction(MUSIC_CURRENT);
		filter.addAction(ISPLAYINT_ACTION);
		registerReceiver(playerReceiver, filter);
		
	}
	
	//����ؼ�
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
	//���ÿؼ�������
	private void setViewOnClickListener(){
		ViewOnClickListener clickListener = new ViewOnClickListener();
		reverseButton.setOnClickListener(clickListener);
		repeatButton.setOnClickListener(clickListener);
		playButton.setOnClickListener(clickListener);
		shuffleButton.setOnClickListener(clickListener);
		nextButton.setOnClickListener(clickListener);
	}
	
	//ÿ������Activity�����½���
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
		
		if (isPlaying) {
			playButton.setImageResource(R.drawable.pause);
		}
		else {
			playButton.setImageResource(R.drawable.play);
			
		}
		
		
		if (currentTime > 0) {
			isFirstTime = false;
			isPlaying = true;
			isPause = false;
		}
		
		initView();
		
		
	}
	
	//��ʼ������
	private void initView(){
		musicTitle.setText(title);
		musicArtist.setText(artist);
		music_progressBar.setProgress(currentTime);
		music_progressBar.setMax(duration);
		finalProgress.setText(MediaUtils.formatTime(mp3Infos.get(listPosition).getDuration()));

	}
	//�ؼ�����¼�
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
						//ͼ����ı�ʶ
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
				//�ظ�����
			case R.id.repeat_music:
				//����ѭ��
				if(repeatState == isNoneRepeat){
					repeat_one();
					shuffleButton.setClickable(false);
					Toast.makeText(PlayerActivity.this, R.string.repeat_current, Toast.LENGTH_SHORT).show();
				}
				//ȫ��ѭ��
				else if (repeatState == isCurrentRepeat) {
					repeat_all();
					shuffleButton.setClickable(false);
					Toast.makeText(PlayerActivity.this, R.string.repeat_all, Toast.LENGTH_SHORT).show();  
				}
				//˳�򲥷�
				else if (repeatState == isAllRepeat) {
					repeat_none();
					shuffleButton.setClickable(true);
					Toast.makeText(PlayerActivity.this, R.string.repeat_none, Toast.LENGTH_SHORT).show();
				}
				
				
				break;
				
				//�������
			case R.id.shuffle_music:
				if(isNoneShuffle){
					Toast.makeText(PlayerActivity.this, R.string.shuffle, Toast.LENGTH_SHORT).show();
					isNoneShuffle = false;
					isShuffle = true;
					shuffle();
					repeatButton.setClickable(false);
					
				}
				else if (isShuffle) {
					Toast.makeText(PlayerActivity.this, R.string.shuffle_none, Toast.LENGTH_SHORT).show();
					isNoneShuffle = true;
					isShuffle = false;
					repeatButton.setClickable(true);
				}
				break;

			
			}
		}
		
	} 
	
	//��������
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
	
	//��������
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
	//˳�򲥷�
	public void repeat_none(){
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("control", 3);
		sendBroadcast(intent);
	}
	//����ѭ��
	public void repeat_one(){
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("control", 1);
		sendBroadcast(intent);
	}
	//�������
	public void shuffle(){
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("contril", 4);
		sendBroadcast(intent);
	}
	//ȫ��ѭ��
	public void repeat_all(){
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("control", 2);
		sendBroadcast(intent);
	}
	//��һ��
		public void reverse(){
			if (listPosition >  0) {
				listPosition--;
				list.add(listPosition);
				Mp3Info mp3Info = mp3Infos.get(listPosition);
				musicTitle.setText(mp3Info.getTitle());
				Intent intent = new Intent();
				intent.setAction("com.example.MUSIC_SERVICE");
				intent.putExtra("listPosition", listPosition);
				intent.putExtra("url", mp3Info.getUrl());
				intent.putExtra("MSG", AppConstant.PlayerMsg.REVERSE_MSG);
				intent.setPackage(getPackageName());//��Ӵ˲���ſ�����������
				startService(intent);
			}
			
		}
		//��һ��
		public void next(){
			if (listPosition < mp3Infos.size() - 1) {
				listPosition++;
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
	
	//����service���صĹ㲥��Ϣ
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
			//���Ĳ���ͼ��
			else if(action.equals(ISPLAYINT_ACTION)){
				isPlaying = intent.getBooleanExtra("isPlaying", true);
				if (isPlaying) {
					playButton.setImageResource(R.drawable.pause);
				} else {
					playButton.setImageResource(R.drawable.play);
					isPause = true;
				}
			}
			
		}
			
		}
	
	 
	
	//����seekbar,�����϶������ŵ�ǰ�Ľ���
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
	//���Ž�������ʱ��
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
		unregisterReceiver(playerReceiver);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	

}
