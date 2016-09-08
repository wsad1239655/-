package adapter;

import java.util.List;

import com.example.mp3player.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import model.Mp3Info;
import utils.ImageUtils;


public class SearchResultListAdapter extends BaseAdapter {
	private List<Mp3Info> list;

	private Context context;

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
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		holder = new ViewHolder();
		if (convertView == null) {
			convertView = View.inflate(context,
					com.example.mp3player.R.layout.item_online_search_list, null);
			holder.tvMusicName = (TextView) convertView
					.findViewById(R.id.tv_search_list_title);
			holder.tvMusicAritist = (TextView) convertView
					.findViewById(R.id.tv_search_list_airtist);
			holder.ivMusicImage = (ImageView) convertView
					.findViewById(R.id.iv_search_list);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Mp3Info music = list.get(position);
		holder.tvMusicName.setText(music.getTitle());
		holder.tvMusicAritist.setText(music.getArtist());
		ImageUtils.disPlay(music.getSmallAlumUrl(), holder.ivMusicImage);
		return convertView;
	}

	class ViewHolder {
		TextView tvMusicName, tvMusicAritist;
		ImageView ivMusicImage;
	}

}
