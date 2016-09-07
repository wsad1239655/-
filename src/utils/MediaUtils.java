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
	
	//获取专辑图片uri
	private static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");  
	//获取MP3文件
	public static List<Mp3Info> getMp3Infos(Context context) {  
		
		//查询数据库，读取SD卡里的MP3文件，参数为数据库默认储存音频文件的地址。
	    Cursor cursor = context.getContentResolver().query(
	        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,  
	        MediaStore.Audio.Media.DEFAULT_SORT_ORDER);  
	  
	    //创建Mp3Info对象列表
	    List<Mp3Info> mp3Infos = new ArrayList<Mp3Info>(); 
	    
	    //遍历查询到的MP3Info对象，将相应的属性设置给mp3对象
	    for (int i = 0; i < cursor.getCount(); i++) {  
	        Mp3Info mp3Info = new Mp3Info();  
	        cursor.moveToNext();  
	        long id = cursor.getLong(cursor  
	            .getColumnIndex(MediaStore.Audio.Media._ID));   //音乐id  
	        String title = cursor.getString((cursor   
	            .getColumnIndex(MediaStore.Audio.Media.TITLE)));//音乐标题  
	        String artist = cursor.getString(cursor  
	            .getColumnIndex(MediaStore.Audio.Media.ARTIST));//艺术家  
	        String album = cursor.getString(cursor
	        		.getColumnIndex(MediaStore.Audio.Media.ALBUM));//获取专辑图片
	        long albumId = cursor.getInt(cursor
	        		.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));//整列图片id
	        long duration = cursor.getLong(cursor  
	            .getColumnIndex(MediaStore.Audio.Media.DURATION));//时长  
	        long size = cursor.getLong(cursor  
	            .getColumnIndex(MediaStore.Audio.Media.SIZE));  //文件大小  
	        String url = cursor.getString(cursor  
	            .getColumnIndex(MediaStore.Audio.Media.DATA)); //文件路径  
	    int isMusic = cursor.getInt(cursor  
	        .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));//是否为音乐  
	    if (isMusic != 0) {     //把音乐添加到集合当中  
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
	
	//获取专辑的默认图片,即无专辑图片的状态
	private static Bitmap getDefaulArtWork(Context context,boolean small){
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;//以什么方式解码bitmap
		if (small) {
			//获取图片
			return BitmapFactory.decodeStream(context.getResources().openRawResource(R.drawable.music), null, options);
		}
		
		
		return BitmapFactory.decodeStream(context.getResources().openRawResource(R.drawable.music), null, options);
		
	}
	
	//获取mp3自带的专辑图片
	public static Bitmap getArtWorkFromFile(Context context,long songId,long albumId){
		Bitmap bitmap = null;
		if (songId<0 && albumId<0) {
			throw new IllegalArgumentException("Must specify an album or a song id");  
		}
		try {
			//图片参数优化管理
			BitmapFactory.Options options = new BitmapFactory.Options();
			//IO读取
			FileDescriptor fd = null;
			//获取MP3文件自带的专辑图片
			if (albumId <0) {
				Uri uri = Uri.parse("content://media/external/audio/media/"  
                        + songId + "/albumart");
				ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
				if (pfd != null) {
					fd = pfd.getFileDescriptor();
				}
			} 
			//获取数据库存放专辑图片的ID
			else {
				Uri uri = ContentUris.withAppendedId(albumArtUri, albumId);
				ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
				if(pfd != null) {  
                    fd = pfd.getFileDescriptor();  
                }  				
			}
			 //bitmap为null，获取图片比例大小与合适的缩放比例。
			 options.inSampleSize = 1;  	     
			 options.inJustDecodeBounds = true;  
			 BitmapFactory.decodeFileDescriptor(fd, null, options);  
			 //对图片进行设置，获取合适的bitmap
			 options.inSampleSize = 100;  
			 options.inJustDecodeBounds = false;  
			 options.inDither = false;  
			 options.inPreferredConfig = Bitmap.Config.ARGB_8888;  
	              
			 //根据options参数，减少所需要的内存  
			 bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);  
			
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

		return bitmap;
		
	}
	
	
	//获取专辑图片
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
				//先制定原始大小
				options.inSampleSize = 1;
				//只进行大小判断
				options.inJustDecodeBounds = true;
				//调用此方法得到options得到图片的大小
				BitmapFactory.decodeStream(in, null, options);
				//缩放图片
				if(small){
					options.inSampleSize = computeSampleSize(options, 40);
				} else{
					options.inSampleSize = computeSampleSize(options, 600);
				}
				// 我们得到了缩放比例，现在开始正式读入Bitmap数据
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
	
	
	
	
	//图片缩放功能
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
		
	
	
	//将获取的时间，进行分秒转换
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
