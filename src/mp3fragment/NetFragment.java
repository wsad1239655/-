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
	private ImageButton playButton;//播放
	private TextView musicTitle;//歌名
	private ImageView musicAblum;//专辑
	private ImageView playerMusicAlbum;//播放界面专辑大图
	
	
	public static final String NET_MUSIC = "com.example.action.NET_MUSIC";//网络音乐

	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		view = inflater.inflate(R.layout.net_fragment, container, false);
		//接收MainActivity
		activity = (MainActivity)getActivity();
		ImageUtils.initImageLoader(activity);// ImageLoader初始化
		init();
		//获取父控件
		musicTitle = (TextView)activity.findViewById(R.id.music_title);
		playButton = (ImageButton)activity.findViewById(R.id.play_music);
		musicAblum = (ImageView)activity.findViewById(R.id.music_album);
		
		return view;
		
	}
	

	
	
	
	
	//搜索初始化
	private void init() {
		listSearchResult = new ArrayList<Mp3Info>();
		dialog = new ProgressDialog(activity);
		dialog.setTitle("加载中。。。");
		lvSearchReasult = (ListView)view.findViewById(R.id.lv_search_list);
		Button btSearch = (Button)view.findViewById(R.id.bt_online_search);
		final EditText edtKey = (EditText)view.findViewById(R.id.edt_search);
		btSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.show();// 进入加载状态，显示进度条
				//隐藏软键盘
				InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
				new Thread(new Runnable() {
					//输入文字，得到搜索结果，jsoup获取歌曲ID，通过ID获取歌曲信息，将信息放入musicList
					@Override
					public void run() {
						SearchUtils.getIds(edtKey.getText().toString(),
								new OnLoadSearchFinishListener() {

									@Override
									public void onLoadSucess(
											List<Mp3Info> musicList) {
										dialog.dismiss();// 加载完成，取消进度条
										Message msg = new Message();
										msg.what = 0;
										mHandler.sendMessage(msg);
										listSearchResult = musicList;
									}
									@Override
									public void onLoadFiler() {
										dialog.dismiss();// 加载失败，取消进度条
										Toast.makeText(activity,
												"加载失败", Toast.LENGTH_SHORT)
												.show();
									}
								});

					}
				}).start();
			}
		});
		
		//监听列表
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
	
			//发送广播，将得到的音乐列表搜索结果与专辑发送到线程
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
			//下载歌词
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
	
	
	
	
	//播放
	public void play(int position){
		Mp3Info mp3Info = listSearchResult.get(position);
		//更改图标
		playButton.setImageResource(R.drawable.pause);
		musicTitle.setText(mp3Info.getTitle());		
		// 获取专辑位图对象，为小图
		ImageUtils.disPlay(mp3Info.getSmallAlumUrl(), musicAblum);
		Intent intent = new Intent();
		intent.putExtra("url", mp3Info.getUrl());
		intent.putExtra("MSG", AppConstant.PlayerMsg.NET_MSG);
		intent.putExtra("listSearchResult", (Serializable)listSearchResult);
		intent.putExtra("current", position);
		intent.setPackage(activity.getPackageName());
		activity.startService(intent);
	}
	
	
	//将线程中得到的搜索结果设置资源适配器，并传送给主线程处理
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
