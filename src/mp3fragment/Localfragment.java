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
	private ListView mMusiclist;//����ListView
	private List<Mp3Info> mp3Infos = null;//
	private MusicListAdapter listAdapter;//������Դ������
	
	private ImageButton playButton;//����
	private TextView musicTitle;//����
	private ImageView musicAblum;//ר��
	
	private boolean isFirstTime = true;   

	private boolean isPlaying; // ���ڲ���  
	private boolean isPause; // ��ͣ  
	private int listPosition = 0;   //��ʶ�б�λ�� 
	
	private MainActivity activity;
	//����һ�����м�¼����λ��
  	private List list;
	
	 public static final String LIST_ACTION = "com.example.action.LIST_ACTION";      //��¼���ֲ����б�


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		view = inflater.inflate(R.layout.local_fragment, container, false);
		
		//����MainActivity
		activity = ((MainActivity)getActivity());
		//��ȡ���ؼ�
		musicTitle = (TextView)activity.findViewById(R.id.music_title);
		playButton = (ImageButton)activity.findViewById(R.id.play_music);
		musicAblum = (ImageView)activity.findViewById(R.id.music_album);
		
		//�����б�
		list = new ArrayList<Integer>();
		// ��Ҫ��ȡSD��Ȩ�ޣ��ٻ�ȡ�������󼯺�
		mp3Infos = MediaUtils.getMp3Infos(activity); 
		//�����б����
		mMusiclist = (ListView)view.findViewById(R.id.localfragment_list);
		listAdapter = new MusicListAdapter(activity, mp3Infos);
		mMusiclist.setAdapter(listAdapter);
		mMusiclist.setOnItemClickListener(new MusicListItemClickListener());
		//���չ㲥
		IntentFilter filter = new IntentFilter();
		filter.addAction(LIST_ACTION);
		BroadcastReceiver localfragmentReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();			
				//����fragment�Ĳ����б��벥��λ��
				if(action.equals(LIST_ACTION)){
					list = (List) intent.getSerializableExtra("list");
				}
			}
		};
		
		activity.registerReceiver(localfragmentReceiver, filter);
		
		
		
	
		return view;	
		
	}



		//�����б�
		private class MusicListItemClickListener implements OnItemClickListener{
		
			//����
			Intent intent = new Intent();
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				//�������ı䵱ǰ�������ֵ�λ��
				listPosition = position;
				//���뵱ǰ�Ĳ���λ�����б�
				list.add(listPosition);
				//��һ�ε���б�ʱ�������֣��ڶ������ж��Ƿ���ͬһ��λ�ã����в�������ͣ
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
				
			 //���Ͳ����б��λ��
			 intent = new Intent(LIST_ACTION);
			 intent.putExtra("list", (Serializable)list);
			 intent.putExtra("listPosition", listPosition);
			 activity.sendBroadcast(intent);
				
			}
			
		}
		
		
		//����
		public void play(int position){
			playButton.setImageResource(R.drawable.pause);
			Mp3Info mp3Info = mp3Infos.get(position);
			musicTitle.setText(mp3Info.getTitle());
			// ��ȡר��λͼ����ΪСͼ
			Bitmap bitmap = MediaUtils.getArtwork(activity, mp3Info.getId(),mp3Info.getAlbumId(), true, true);
			musicAblum.setImageBitmap(bitmap); // ������ʾר��ͼƬ		
			Intent intent = new Intent();
			intent.putExtra("url", mp3Info.getUrl());
			intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);
			intent.putExtra("listPosition", position);
			intent.setPackage(activity.getPackageName());
			activity.startService(intent);
		}

	
		

	
}
