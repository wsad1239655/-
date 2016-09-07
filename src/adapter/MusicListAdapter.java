package adapter;

import java.util.List;

import com.example.mp3player.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import model.Mp3Info;
import utils.MediaUtils;

public class MusicListAdapter extends BaseAdapter{

	private Context context;		//�����Ķ�������
	private List<Mp3Info> mp3Infos;	//���Mp3Info���õļ���
	private Mp3Info mp3Info;		//Mp3Info��������
	private int pos = -1;			//�б�λ��
	
	public MusicListAdapter(Context context,List<Mp3Info> mp3Infos){
		this.context = context;
		this.mp3Infos = mp3Infos;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mp3Infos.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		ViewHolder viewHolder = null;
		
		if(convertView == null){
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.music_list_item_layout, null);
			viewHolder.albumImage = (ImageView) convertView.findViewById(R.id.albumImage);
			viewHolder.musicArtist = (TextView) convertView.findViewById(R.id.music_artist);
			viewHolder.musicTitle = (TextView)convertView.findViewById(R.id.music_title);
			viewHolder.musicDuration = (TextView)convertView.findViewById(R.id.music_duration);
			convertView.setTag(viewHolder);
		}
		else{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		//���������ӣ����򱨴��޷���ȡ��Ϣ
		mp3Info = mp3Infos.get(position);
		
		viewHolder.musicTitle.setText(mp3Info.getTitle());//��ʾ����
		viewHolder.musicArtist.setText(mp3Info.getArtist());//��ʾ������
		viewHolder.musicDuration.setText(MediaUtils.formatTime(mp3Info.getDuration()));//��ʾʱ��

		 if(position == pos) {  
	            viewHolder.albumImage.setImageResource(R.drawable.item);  
	        } else {  
	            Bitmap bitmap = MediaUtils.getArtwork(context, mp3Info.getId(),mp3Info.getAlbumId(), true, true);  
	            viewHolder.albumImage.setImageBitmap(bitmap);  
	        }  
		
		return convertView;
	}
	
	public class ViewHolder {
		//���пؼ���������
		public ImageView albumImage;	//ר��ͼƬ
		public TextView musicTitle;		//���ֱ���
		public TextView musicDuration;	//����ʱ��
		public TextView musicArtist;	//����������
	}

}
