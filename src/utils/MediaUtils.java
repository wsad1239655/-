package utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import model.Mp3Info;

public class MediaUtils {
	
	public static List<Mp3Info> getMp3Infos(Context context) {  
		
		//��ѯ���ݿ⣬��ȡSD�����MP3�ļ�������Ϊ���ݿ�Ĭ�ϴ�����Ƶ�ļ��ĵ�ַ��
	    Cursor cursor = context.getContentResolver().query(
	        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,  
	        MediaStore.Audio.Media.DEFAULT_SORT_ORDER);  
	  
	    //����Mp3Info�����б�
	    List<Mp3Info> mp3Infos = new ArrayList<Mp3Info>(); 
	    
	    //������ѯ����MP3Info���󣬽���Ӧ���������ø�mp3����
	    for (int i = 0; i < cursor.getCount(); i++) {  
	        Mp3Info mp3Info = new Mp3Info();  
	        cursor.moveToNext();  
	        long id = cursor.getLong(cursor  
	            .getColumnIndex(MediaStore.Audio.Media._ID));   //����id  
	        String title = cursor.getString((cursor   
	            .getColumnIndex(MediaStore.Audio.Media.TITLE)));//���ֱ���  
	        String artist = cursor.getString(cursor  
	            .getColumnIndex(MediaStore.Audio.Media.ARTIST));//������  
	        long duration = cursor.getLong(cursor  
	            .getColumnIndex(MediaStore.Audio.Media.DURATION));//ʱ��  
	        long size = cursor.getLong(cursor  
	            .getColumnIndex(MediaStore.Audio.Media.SIZE));  //�ļ���С  
	        String url = cursor.getString(cursor  
	            .getColumnIndex(MediaStore.Audio.Media.DATA)); //�ļ�·��  
	    int isMusic = cursor.getInt(cursor  
	        .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));//�Ƿ�Ϊ����  
	    if (isMusic != 0) {     //ֻ��������ӵ����ϵ���  
	        mp3Info.setId(id);  
	        mp3Info.setTitle(title);  
	        mp3Info.setArtist(artist);  
	        mp3Info.setDuration(duration);  
	        mp3Info.setSize(size);  
	        mp3Info.setUrl(url);  
	        mp3Infos.add(mp3Info);  
	        }  
	    }
	    
	    if(cursor != null){
	    	cursor.close();
	    }
	    
	return mp3Infos;  
	}  
	
	public static String formatTime(long time){
		
		String min = time/1000/60 + "";
		String sec = time%(1000 * 60) + "";
		
		if(min.length() < 2){
			min = "0" + time/1000/60 + "";
		}
		else{
			
			min = time/1000/60 + "";
		}
		
		
		if(sec.length() == 4){
			sec = "0" + time%(1000 * 60) + "";
		}
		else if (sec.length() == 3) {
			sec = "00" + time%(1000 * 60) + "";
		}
		else if (sec.length() == 2) {
			sec = "000" + time%(1000 * 60) + "";
		}
		else if (sec.length() == 1) {
			sec = "0000" + time%(1000 * 60) + "";
		}
		
		return min + ":" + sec.trim().substring(0,2);		
	}

}
