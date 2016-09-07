package utils;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.example.mp3player.R;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import model.Mp3Info;

public class MediaUtils {
	
	//��ȡר��ͼƬuri
	private static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");  
	//��ȡMP3�ļ�
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
	        String album = cursor.getString(cursor
	        		.getColumnIndex(MediaStore.Audio.Media.ALBUM));//��ȡר��ͼƬ
	        long albumId = cursor.getInt(cursor
	        		.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));//����ͼƬid
	        long duration = cursor.getLong(cursor  
	            .getColumnIndex(MediaStore.Audio.Media.DURATION));//ʱ��  
	        long size = cursor.getLong(cursor  
	            .getColumnIndex(MediaStore.Audio.Media.SIZE));  //�ļ���С  
	        String url = cursor.getString(cursor  
	            .getColumnIndex(MediaStore.Audio.Media.DATA)); //�ļ�·��  
	    int isMusic = cursor.getInt(cursor  
	        .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));//�Ƿ�Ϊ����  
	    if (isMusic != 0) {     //��������ӵ����ϵ���  
	        mp3Info.setId(id);  
	        mp3Info.setTitle(title);  
	        mp3Info.setArtist(artist);  
	        mp3Info.setDuration(duration);  
	        mp3Info.setAlbum(album);
	        mp3Info.setAlbumId(albumId);	   
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
	
	//��ȡר����Ĭ��ͼƬ,����ר��ͼƬ��״̬
	private static Bitmap getDefaulArtWork(Context context,boolean small){
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;//��ʲô��ʽ����bitmap
		if (small) {
			//��ȡͼƬ
			return BitmapFactory.decodeStream(context.getResources().openRawResource(R.drawable.music), null, options);
		}
		
		
		return BitmapFactory.decodeStream(context.getResources().openRawResource(R.drawable.music), null, options);
		
	}
	
	//��ȡmp3�Դ���ר��ͼƬ
	public static Bitmap getArtWorkFromFile(Context context,long songId,long albumId){
		Bitmap bitmap = null;
		if (songId<0 && albumId<0) {
			throw new IllegalArgumentException("Must specify an album or a song id");  
		}
		try {
			//ͼƬ�����Ż�����
			BitmapFactory.Options options = new BitmapFactory.Options();
			//IO��ȡ
			FileDescriptor fd = null;
			//��ȡMP3�ļ��Դ���ר��ͼƬ
			if (albumId <0) {
				Uri uri = Uri.parse("content://media/external/audio/media/"  
                        + songId + "/albumart");
				ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
				if (pfd != null) {
					fd = pfd.getFileDescriptor();
				}
			} 
			//��ȡ���ݿ���ר��ͼƬ��ID
			else {
				Uri uri = ContentUris.withAppendedId(albumArtUri, albumId);
				ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
				if(pfd != null) {  
                    fd = pfd.getFileDescriptor();  
                }  				
			}
			 //bitmapΪnull����ȡͼƬ������С����ʵ����ű�����
			 options.inSampleSize = 1;  	     
			 options.inJustDecodeBounds = true;  
			 BitmapFactory.decodeFileDescriptor(fd, null, options);  
			 //��ͼƬ�������ã���ȡ���ʵ�bitmap
			 options.inSampleSize = 100;  
			 options.inJustDecodeBounds = false;  
			 options.inDither = false;  
			 options.inPreferredConfig = Bitmap.Config.ARGB_8888;  
	              
			 //����options��������������Ҫ���ڴ�  
			 bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);  
			
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

		return bitmap;
		
	}
	
	
	//��ȡר��ͼƬ
	public static Bitmap getArtwork(Context context, long song_id, long album_id, boolean allowdefalut, boolean small){
		if(album_id < 0) {
			if(song_id < 0) {
				Bitmap bm = getArtWorkFromFile(context, song_id, -1);
				if(bm != null) {
					return bm;
				}
			}
			if(allowdefalut) {
				return getDefaulArtWork(context, small);
			}
			return null;
		}
		ContentResolver res = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(albumArtUri, album_id);  
		if(uri != null) {
			InputStream in = null;
			try {
				in = res.openInputStream(uri);
				BitmapFactory.Options options = new BitmapFactory.Options();
				//���ƶ�ԭʼ��С
				options.inSampleSize = 1;
				//ֻ���д�С�ж�
				options.inJustDecodeBounds = true;
				//���ô˷����õ�options�õ�ͼƬ�Ĵ�С
				BitmapFactory.decodeStream(in, null, options);
				//����ͼƬ
				if(small){
					options.inSampleSize = computeSampleSize(options, 40);
				} else{
					options.inSampleSize = computeSampleSize(options, 600);
				}
				// ���ǵõ������ű��������ڿ�ʼ��ʽ����Bitmap����
				options.inJustDecodeBounds = false;
				options.inDither = false;
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;
				in = res.openInputStream(uri);
				return BitmapFactory.decodeStream(in, null, options);
			} catch (FileNotFoundException e) {
				Bitmap bm = getArtWorkFromFile(context, song_id, album_id);
				if(bm != null) {
					if(bm.getConfig() == null) {
						bm = bm.copy(Bitmap.Config.RGB_565, false);
						if(bm == null && allowdefalut) {
							return getDefaulArtWork(context, small);
						}
					}
				} else if(allowdefalut) {
					bm = getDefaulArtWork(context, small);
				}
				return bm;
			} finally {
				try {
					if(in != null) {
						in.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	
	
	
	//ͼƬ���Ź���
	public static int computeSampleSize(Options options,int target){
		int w = options.outWidth;
		int h = options.outHeight;
		int candidateW = w/target;
		int candidateH = h/target;
		int candidate = Math.max(candidateW, candidateH);
		if (candidate == 0) {
			return 1;
		}
		if(candidate > 1) {  
            if((w > target) && (w / candidate) < target) {  
                candidate -= 1;  
            }  
        }  
        if(candidate > 1) {  
            if((h > target) && (h / candidate) < target) {  
                candidate -= 1;  
            }  
        }  
        return candidate;  
    }  
		
	
	
	//����ȡ��ʱ�䣬���з���ת��
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
