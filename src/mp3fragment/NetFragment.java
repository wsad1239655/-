package mp3fragment;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.example.mp3player.MainActivity;
import com.example.mp3player.R;

import adapter.SearchResultListAdapter;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import model.AppConstant;
import model.Mp3Info;
import utils.FileUtils;
import utils.ImageUtils;
import utils.OnLoadSearchFinishListener;
import utils.SearchUtils;

public class NetFragment extends Fragment{

	private static View view;
	private ListView lvSearchReasult;

	private List<Mp3Info> listSearchResult;

	private ProgressDialog dialog;
	
	private MainActivity activity;
	private ImageButton playButton;//����
	private TextView musicTitle;//����
	private ImageView musicAblum;//ר��
	private ImageView playerMusicAlbum;//���Ž���ר����ͼ
	
	
	public static final String NET_MUSIC = "com.example.action.NET_MUSIC";//��������

	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		view = inflater.inflate(R.layout.net_fragment, container, false);
		//����MainActivity
		activity = (MainActivity)getActivity();
		ImageUtils.initImageLoader(activity);// ImageLoader��ʼ��
		init();
		//��ȡ���ؼ�
		musicTitle = (TextView)activity.findViewById(R.id.music_title);
		playButton = (ImageButton)activity.findViewById(R.id.play_music);
		musicAblum = (ImageView)activity.findViewById(R.id.music_album);
		
		return view;
		
	}
	

	
	
	
	
	//������ʼ��
	private void init() {
		listSearchResult = new ArrayList<Mp3Info>();
		dialog = new ProgressDialog(activity);
		dialog.setTitle("�����С�����");
		lvSearchReasult = (ListView)view.findViewById(R.id.lv_search_list);
		Button btSearch = (Button)view.findViewById(R.id.bt_online_search);
		final EditText edtKey = (EditText)view.findViewById(R.id.edt_search);
		btSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.show();// �������״̬����ʾ������
				//���������
				InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
				new Thread(new Runnable() {
					//�������֣��õ����������jsoup��ȡ����ID��ͨ��ID��ȡ������Ϣ������Ϣ����musicList
					@Override
					public void run() {
						SearchUtils.getIds(edtKey.getText().toString(),
								new OnLoadSearchFinishListener() {

									@Override
									public void onLoadSucess(
											List<Mp3Info> musicList) {
										dialog.dismiss();// ������ɣ�ȡ��������
										Message msg = new Message();
										msg.what = 0;
										mHandler.sendMessage(msg);
										listSearchResult = musicList;
									}
									@Override
									public void onLoadFiler() {
										dialog.dismiss();// ����ʧ�ܣ�ȡ��������
										Toast.makeText(activity,
												"����ʧ��", Toast.LENGTH_SHORT)
												.show();
									}
								});

					}
				}).start();
			}
		});
		
		//�����б�
		lvSearchReasult.setOnItemClickListener(new LvSearchReasultListener());
	}
	
	
	private class LvSearchReasultListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			play(position);
			
			String url = listSearchResult.get(position).getLrcUrl();
			String name = listSearchResult.get(position).getTitle();
			DownloadLrc downloadLrc = new DownloadLrc(url,name);
			Thread thread = new Thread(downloadLrc);
			thread.start();
	
			//���͹㲥�����õ��������б����������ר�����͵��߳�
			Intent intent2 = new Intent();
			intent2.setAction(NET_MUSIC);
			intent2.putExtra("listSearchResult", (Serializable)listSearchResult);
			intent2.putExtra("listPosition", position);
			intent2.putExtra("albumUrl", listSearchResult.get(position).getBigAlumUrl());
			activity.sendBroadcast(intent2);
		}
		
	}
	
	class DownloadLrc implements Runnable{
		
		private String url;
		private String fileName;
		public DownloadLrc(String url,String fileName) {
			this.url = url;
			this.fileName = fileName;
		}
		@Override
		public void run() {
			//���ظ��
			InputStream inputStream = null;
			try {
				FileUtils fileUtils = new FileUtils();
				URL url1 = new URL(url);
				HttpURLConnection urlConn = (HttpURLConnection)url1.openConnection();
				inputStream = urlConn.getInputStream();
				File resultFile = fileUtils.write2SDFromInput("/Download", fileName + ".lrc", inputStream);
			} catch (Exception e) {
				e.printStackTrace();
			}
			finally{
				try {
					inputStream.close();
				}
				
			catch (Exception e) {
					e.printStackTrace();
				}
			
			}
		}
		
	}
	
	
	
	
	//����
	public void play(int position){
		Mp3Info mp3Info = listSearchResult.get(position);
		//����ͼ��
		playButton.setImageResource(R.drawable.pause);
		musicTitle.setText(mp3Info.getTitle());		
		// ��ȡר��λͼ����ΪСͼ
		ImageUtils.disPlay(mp3Info.getSmallAlumUrl(), musicAblum);
		Intent intent = new Intent();
		intent.putExtra("url", mp3Info.getUrl());
		intent.putExtra("MSG", AppConstant.PlayerMsg.NET_MSG);
		intent.putExtra("listSearchResult", (Serializable)listSearchResult);
		intent.putExtra("current", position);
		intent.setPackage(activity.getPackageName());
		activity.startService(intent);
	}
	
	
	//���߳��еõ����������������Դ�������������͸����̴߳���
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				SearchResultListAdapter adapter = new SearchResultListAdapter(
						listSearchResult,activity);
				lvSearchReasult.setAdapter(adapter);
				break;
			}
		};
	};
	
	
	
}
