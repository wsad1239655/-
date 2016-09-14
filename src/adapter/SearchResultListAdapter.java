package adapter;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.example.mp3player.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import model.Mp3Info;
import utils.FileUtils;
import utils.ImageUtils;


public class SearchResultListAdapter extends BaseAdapter {
	private List<Mp3Info> list;
	private Context context;
	private PopupWindow mPopWindow;//下拉更多菜单
	private int flag = 0;//标记popupwindow的消失

	//传入MP3info列表
	public SearchResultListAdapter(List<Mp3Info> list, Context c) {
		super();
		this.list = list;
		this.context = c;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		holder = new ViewHolder();
		
		if (convertView == null) {
			convertView = View.inflate(context,com.example.mp3player.R.layout.item_online_search_list, null);
			holder.tvMusicName = (TextView) convertView.findViewById(R.id.tv_search_list_title);
			holder.tvMusicAritist = (TextView) convertView.findViewById(R.id.tv_search_list_airtist);
			holder.ivMusicImage = (ImageView) convertView.findViewById(R.id.iv_search_list);
			holder.more = (ImageButton)convertView.findViewById(R.id.more);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Mp3Info music = list.get(position);
		holder.tvMusicName.setText(music.getTitle());
		holder.tvMusicAritist.setText(music.getArtist());
		ImageUtils.disPlay(music.getSmallAlumUrl(), holder.ivMusicImage);
		
		//点击更多按钮时候显示popupwindow，只能在adapter中设置
		holder.more.setOnClickListener(new moreOnclicListener(position));
		
		return convertView;
	}

	class ViewHolder {
		TextView tvMusicName, tvMusicAritist;
		ImageView ivMusicImage;
		ImageButton more;
	}
	
	//点击更多按钮的时候弹出菜单
	private class moreOnclicListener implements OnClickListener{
		private int position;
		public moreOnclicListener(int position){
			this.position = position;
		}
		
		public void onClick(View v) {
			if (flag == 0) {
				showPopupWindow(v,position); 
				flag = -1;
			}
			else{
				 mPopWindow.dismiss();  
				 flag = 0;
				 }
		}
		
	}
	
	
	//弹出的下载菜单，并获取位置
	private void showPopupWindow(View view,int position){
		View contentView = LayoutInflater.from(context).inflate(R.layout.download_menu, null);
		mPopWindow = new PopupWindow(contentView);
		mPopWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);  
		mPopWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT); 
		
		TextView view1 = (TextView)contentView.findViewById(R.id.download);
		view1.setOnClickListener(new moreMenuListener(position));
		
		mPopWindow.showAsDropDown(view);
		
	}
	//点击其中的按钮时，创建线程下载MP3文件
	private class moreMenuListener implements OnClickListener{
		private int position;
		public moreMenuListener(int position){
			this.position = position;
		}
		
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			case R.id.download:
				
                new Thread(new Runnable() {
                	
					public void run() {
					InputStream inputStream = null;
					try {
						String url = list.get(position).getUrl();
						String fileName = list.get(position).getTitle();
						FileUtils fileUtils = new FileUtils();
						URL url1 = new URL(url);
						HttpURLConnection urlConn = (HttpURLConnection)url1.openConnection();
						inputStream = urlConn.getInputStream();
						File resultFile = fileUtils.write2SDFromInput("/Download", fileName + ".mp3", inputStream);
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
				}).start();
                
                mPopWindow.dismiss(); 
				break;
			}
		}
		
	}
	
	
	
	

}
