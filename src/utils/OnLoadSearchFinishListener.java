package utils;

import java.util.List;


import model.Mp3Info;

public interface OnLoadSearchFinishListener {
	void onLoadSucess(List<Mp3Info> musicList);

	void onLoadFiler();
}
