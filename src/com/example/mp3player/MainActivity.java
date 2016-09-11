package com.example.mp3player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import adapter.MusicListAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import custom.LrcView;
import model.AppConstant;
import model.Mp3Info;
import mp3fragment.Localfragment;
import mp3fragment.NetFragment;
import utils.MediaUtils;

public class MainActivity extends Activity {
	
	private List<Mp3Info> mp3Infos = null;//��������
	private ImageButton playButton;//����
	private ImageButton nextButton;//��һ��
	private ImageButton musicPlaying;//���ڲ���
	private ImageView musicablum;
	private TextView musicTitle;//����
	private TextView musicDuration;//ʱ��
	private TextView localList;
	private TextView netList;
	public static LrcView lrcView;

	private boolean isFirstTime = true;   
	private boolean isPlaying; // ���ڲ���  
	private boolean isPause; // ��ͣ  
	private int listPosition = 0;   //��ʶ�б�λ�� 
	private int currentTime;  //��ǰ����ʱ��
	private int duration;  //��������
	
	private static final String fragment1Tag = "fragment1";
	private static final String fragment2Tag = "fragment2";
	private FragmentManager manager;
	private FragmentTransaction transaction;
	private Fragment fragment1;
	private Fragment fragment2;
	
	private Mp3Info mp3Info;
	
	 //һϵ�ж���  
    public static final String UPDATE_ACTION = "com.example.action.UPDATE_ACTION";  
    public static final String CTL_ACTION = "com.example.action.CTL_ACTION";  
    public static final String MUSIC_CURRENT = "com.example.action.MUSIC_CURRENT";  
    public static final String MUSIC_DURATION = "com.example.action.MUSIC_DURATION";  
    public static final String REPEAT_ACTION = "com.example.action.REPEAT_ACTION";  
    public static final String SHUFFLE_ACTION = "com.example.action.SHUFFLE_ACTION";
    public static final String LIST_ACTION = "com.example.action.LIST_ACTION";      //��¼���ֲ����б�
    public static final String ISPLAYINT_ACTION = "com.example.action.ISPLAYINT_ACTION";//���²���ͼ��
    public static final String NET_MUSIC = "com.example.action.NET_MUSIC";//��������
    
    private MyReceiver myReceiver;
    
  //����һ�����м�¼����λ��
  	private List list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		list = new ArrayList<Integer>();
		mp3Infos = MediaUtils.getMp3Infos(MainActivity.this); 
//		mMusiclist = (ListView) findViewById(R.id.music_list);
//		listAdapter = new MusicListAdapter(this, mp3Infos);
//		mMusiclist.setAdapter(listAdapter);
//		mMusiclist.setOnCreateContextMenuListener(new MusicListItemContextMenuListener());
//		mMusiclist.setOnItemClickListener(new MusicListItemClickListener());
		findViewById();
		setOnclickListioner();
				
		//��̬ע��㲥������������service���ص���Ϣ
		myReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		 filter.addAction(UPDATE_ACTION);  
		 filter.addAction(MUSIC_CURRENT);  
		 filter.addAction(MUSIC_DURATION);  
		 filter.addAction(REPEAT_ACTION);  
		 filter.addAction(SHUFFLE_ACTION);  
		 filter.addAction(LIST_ACTION);
		 filter.addAction(ISPLAYINT_ACTION);
		 filter.addAction(NET_MUSIC);
		 registerReceiver(myReceiver, filter);
		
		 //����fragment1����ʾ�б���
		 manager = getFragmentManager();
		 transaction = manager.beginTransaction();
		 fragment1 = manager.findFragmentByTag(fragment1Tag);
		 fragment1 = new Localfragment();
		 transaction.add(R.id.fragment_layout, fragment1, fragment1Tag);
		 transaction.commit();
		 

	}
	
	//����view��Id
	private void findViewById() {
		playButton = (ImageButton)findViewById(R.id.play_music);
		nextButton = (ImageButton)findViewById(R.id.next_music);
		musicPlaying = (ImageButton)findViewById(R.id.playing);
		musicTitle = (TextView)findViewById(R.id.music_title);
		musicDuration = (TextView)findViewById(R.id.music_duration);
		localList = (TextView)findViewById(R.id.locallist);
		netList = (TextView)findViewById(R.id.netlist);		
		musicablum = (ImageView)findViewById(R.id.music_album);
	
	}
	//ע�������
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
	//���������
	private class ViewClickLintener implements OnClickListener{
		
		Intent intent = new Intent();
		@Override
		public void onClick(View v) {
			manager = getFragmentManager();
			transaction = manager.beginTransaction();
			fragment1 = manager.findFragmentByTag(fragment1Tag);
			fragment2 = manager.findFragmentByTag(fragment2Tag);
		
			switch (v.getId()) {
			
			
			//��������	
			case R.id.play_music:
				if (currentTime > 0) {
					isFirstTime = false;
				}
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
			
			//��һ��
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
			//���ڲ���
			case R.id.playing:
				startPlayerActivity();
				 break;  
				//���ڲ���
			case R.id.music_title:
				startPlayerActivity();
				 break;  
				//���ڲ���
			case R.id.music_duration:
				startPlayerActivity();
				 break;  
				 
			case R.id.locallist:
				//����fragment2����ʾfragment1
				if (fragment2 != null) {
					
					transaction.hide(fragment2);				
				}
				transaction.show(fragment1);				
				transaction.commit();				
				break;	 
				
			case R.id.netlist:
				//����fragment1����ʾfragment2
				transaction.hide(fragment1);
				if (fragment2 == null) {
					fragment2 = new NetFragment();
					transaction.add(R.id.fragment_layout, fragment2, fragment2Tag);
				} else {
					transaction.show(fragment2);
				}
				//�������
				transaction.commit();
				break;
				
			}
			
		}
		
	}
	
	//����PlayerActivity
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
	
//	//�����б�
//	private class MusicListItemClickListener implements OnItemClickListener{
//	
//		//����
//		Intent intent = new Intent();
//		@Override
//		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//			
//			//�������ı䵱ǰ�������ֵ�λ��
//			listPosition = position;
//			//���뵱ǰ�Ĳ���λ�����б�
//			list.add(listPosition);
//			//��һ�ε���б�ʱ�������֣��ڶ������ж��Ƿ���ͬһ��λ�ã����в�������ͣ
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
	
//	//���б����View���Ӳ˵���
//	private class  MusicListItemContextMenuListener implements OnCreateContextMenuListener{
//
//		@Override
//		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//			
//		}
//		
//	}
//	
	
//	
//	//������View�Ĳ˵���
//	public void MisicListItemDialog(){
//			
//	}
//	
	//��һ��
	public void next(){
		if (listPosition < mp3Infos.size() - 1) {
			listPosition++;
			//���뵱ǰ�Ĳ���λ�����б�
			mp3Infos = MediaUtils.getMp3Infos(MainActivity.this); 
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
	//����
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

	
	
	
	//����һ���㲥������������service���صĹ㲥
	public class MyReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			//���ò��ŵ�ʱ��
			if (action.equals(MUSIC_CURRENT)) {
				currentTime = intent.getIntExtra("currentTime", -1);
				musicDuration.setText(MediaUtils.formatTime(currentTime));
				
				
			}
			//���÷��ظ�������
			else if (action.equals(MUSIC_DURATION)) {
				duration = intent.getIntExtra("duration", -1);
			}
			//�����һ����һ�׷��صĸ���λ��
			else if (action.equals(UPDATE_ACTION)) {
				listPosition = intent.getIntExtra("current", -1);
				if (listPosition >= 0 && listPosition <= mp3Infos.size()-1) {
					musicTitle.setText(mp3Infos.get(listPosition).getTitle());
					Mp3Info mp3Info = mp3Infos.get(listPosition);
					Bitmap bitmap = MediaUtils.getArtwork(MainActivity.this, mp3Info.getId(),
							mp3Info.getAlbumId(), true, true);// ��ȡר��λͼ����ΪСͼ
					musicablum.setImageBitmap(bitmap); // ������ʾר��ͼƬ
				}
				
			}
			
			//����fragment�Ĳ����б��벥��λ��
			else if(action.equals(LIST_ACTION)){
				list = (List) intent.getSerializableExtra("list");
				listPosition = intent.getIntExtra("listPosition", -1);
				mp3Infos = MediaUtils.getMp3Infos(MainActivity.this); 
								
				if (list.size()>0) {
					isFirstTime = false;
					isPlaying = true;
					isPause = false;
				}
				
			}
			//���Ĳ���ͼ��
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
			//�õ����������б�
			else if (action.equals(NET_MUSIC)) {
				mp3Infos = (List<Mp3Info>) intent.getSerializableExtra("listSearchResult");
				listPosition = intent.getIntExtra("listPosition", -1);
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

