package mp3fragment;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.example.mp3player.MainActivity;
import com.example.mp3player.R;

import adapter.MusicListAdapter;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import model.AppConstant;
import model.Mp3Info;
import utils.MediaUtils;

public class Localfragment extends Fragment{

	private View view;
	private ListView mMusiclist;//定义ListView
	private List<Mp3Info> mp3Infos = null;//
	private MusicListAdapter listAdapter;//定义资源适配器
	
	private ImageButton playButton;//播放
	private TextView musicTitle;//歌名
	private ImageView musicAblum;//专辑
	
	private boolean isFirstTime = true;   

	private boolean isPlaying; // 正在播放  
	private boolean isPause; // 暂停  
	private int listPosition = 0;   //标识列表位置 
	
	private MainActivity activity;
	//创建一个数列记录播放位置
  	private List list;
	
	 public static final String LIST_ACTION = "com.example.action.LIST_ACTION";      //记录音乐播放列表


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		view = inflater.inflate(R.layout.local_fragment, container, false);
		
		//接收MainActivity
		activity = ((MainActivity)getActivity());
		//获取父控件
		musicTitle = (TextView)activity.findViewById(R.id.music_title);
		playButton = (ImageButton)activity.findViewById(R.id.play_music);
		musicAblum = (ImageView)activity.findViewById(R.id.music_album);
		
		//接收列表
		list = new ArrayList<Integer>();
		// 需要获取SD卡权限，再获取歌曲对象集合
		mp3Infos = MediaUtils.getMp3Infos(activity); 
		//设置列表参数
		mMusiclist = (ListView)view.findViewById(R.id.localfragment_list);
		listAdapter = new MusicListAdapter(activity, mp3Infos);
		mMusiclist.setAdapter(listAdapter);
		mMusiclist.setOnItemClickListener(new MusicListItemClickListener());
		//接收广播
		IntentFilter filter = new IntentFilter();
		filter.addAction(LIST_ACTION);
		BroadcastReceiver localfragmentReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();			
				//监听fragment的播放列表与播放位置
				if(action.equals(LIST_ACTION)){
					list = (List) intent.getSerializableExtra("list");
				}
			}
		};
		
		activity.registerReceiver(localfragmentReceiver, filter);
		
		
		
	
		return view;	
		
	}



		//监听列表
		private class MusicListItemClickListener implements OnItemClickListener{
		
			//创建
			Intent intent = new Intent();
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				//监听并改变当前播放音乐的位置
				listPosition = position;
				//放入当前的播放位置在列表
				list.add(listPosition);
				//第一次点击列表时播放音乐，第二次则判断是否点击同一个位置，进行播放与暂停
			 if(list.size() > 1){
				 if (list.get(list.size()-2) == (Object)listPosition) {
					 if (isPlaying) {
							playButton.setImageResource(R.drawable.play);
							intent.setAction("com.example.MUSIC_SERVICE");
							intent.putExtra("MSG", AppConstant.PlayerMsg.PAUSE_MSG);
							intent.setPackage(activity.getPackageName());
							activity.startService(intent);
							isPlaying = false;
							isPause = true;
						} 
						else if(isPause){
							playButton.setImageResource(R.drawable.pause);
							intent.setAction("com.example.MUSIC_SERVICE");
							intent.putExtra("MSG", AppConstant.PlayerMsg.CONTINUE_MSG);
							intent.setPackage(activity.getPackageName());
							activity.startService(intent);
							isPause = false;
							isPlaying = true;
						}
				}
				 else{
					 play(listPosition);
				 }
			 }
			 else{
				 isFirstTime = false;
				 isPlaying = true;
				 isPause = false;
				 play(listPosition);
			 }
				
			 //发送播放列表的位置
			 intent = new Intent(LIST_ACTION);
			 intent.putExtra("list", (Serializable)list);
			 intent.putExtra("listPosition", listPosition);
			 activity.sendBroadcast(intent);
				
			}
			
		}
		
		
		//播放
		public void play(int position){
			playButton.setImageResource(R.drawable.pause);
			Mp3Info mp3Info = mp3Infos.get(position);
			musicTitle.setText(mp3Info.getTitle());
			// 获取专辑位图对象，为小图
			Bitmap bitmap = MediaUtils.getArtwork(activity, mp3Info.getId(),mp3Info.getAlbumId(), true, true);
			musicAblum.setImageBitmap(bitmap); // 这里显示专辑图片		
			Intent intent = new Intent();
			intent.putExtra("url", mp3Info.getUrl());
			intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);
			intent.putExtra("listPosition", position);
			intent.setPackage(activity.getPackageName());
			activity.startService(intent);
		}

	
		

	
}
