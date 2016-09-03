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
	
	 private String title;       //��������  
	 private String artist;      //����������  
	 private String url;         //����·��  
	 private int listPosition;   //���Ÿ�����mp3Infos��λ��  
	 private int currentTime;    //��ǰ��������ʱ��  
	 private int duration;       //��������  
	 private int flag;           //���ű�ʶ  
	 
	 private int repeatState;  
	 private final int isCurrentRepeat = 1; // ����ѭ��  
	 private final int isAllRepeat = 2;      // ȫ��ѭ��  
	 private final int isNoneRepeat = 3;     // ���ظ�����  
	 private boolean isPlaying;              // ���ڲ���  
	 private boolean isPause;                // ��ͣ  
	 private boolean isNoneShuffle;           // ˳�򲥷�  
	 private boolean isShuffle;          // ������� 
	    
	private List<Mp3Info> mp3Infos;
	
	 public static final String UPDATE_ACTION = "com.example.action.UPDATE_ACTION";  //���¶���  
	 public static final String CTL_ACTION = "com.example.action.CTL_ACTION";        //���ƶ���  
	 public static final String MUSIC_CURRENT = "com.example.action.MUSIC_CURRENT";  //���ֵ�ǰʱ��ı䶯��  
	 public static final String MUSIC_DURATION = "com.example.action.MUSIC_DURATION";//���ֲ��ų��ȸı䶯��  
	 public static final String MUSIC_PLAYING = "com.example.action.MUSIC_PLAYING";  //�������ڲ��Ŷ���  
	 public static final String REPEAT_ACTION = "com.example.action.REPEAT_ACTION";  //�����ظ����Ŷ���  
	 public static final String SHUFFLE_ACTION = "com.example.action.SHUFFLE_ACTION";//����������Ŷ���  
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_activity);
		
		findViewById();
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
		initView();
	}
	
	//��ʼ������
	private void initView(){
		musicTitle.setText(title);
		musicArtist.setText(artist);
		music_progressBar.setProgress(currentTime);
		music_progressBar.setMax(duration);
	
		playButton.setImageResource(R.drawable.pause);
		isPlaying = true;
		isPause = false;
	}
	//�ؼ�����¼�
	private class ViewOnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.play_music:
				
				
				break;

			
			}
		}
		
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
		
	}
	//����ѭ��
	public void repeat_one(){
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("control", 1);
		sendBroadcast(intent);
	}
	//�������
	public void shuffleMusic(){
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
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	

}
