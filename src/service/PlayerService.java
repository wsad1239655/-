package service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.example.mp3player.MainActivity;
import com.example.mp3player.PlayerActivity;
import com.example.mp3player.R;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.animation.AnimationUtils;
import android.webkit.WebView.FindListener;
import custom.LrcProcess;
import custom.LrcView;
import model.AppConstant;
import model.LrcInfo;
import model.Mp3Info;
import utils.MediaUtils;

public class PlayerService extends Service{
	
	private MediaPlayer mediaPlayer; // 媒体播放器对象  
    private String path;            // 音乐文件路径  
    private int msg;  
    private boolean isPause;        // 暂停状态  
    private int current = 0;        // 记录当前正在播放的音乐  
    private List<Mp3Info> mp3Infos;   //存放Mp3Info对象的集合  
    private int status = 3;         //播放状态，默认为顺序播放  
    private MusicReceiver musicReceiver;  //自定义广播接收器  
    private int currentTime;        //当前播放进度  
    private int duration;           //播放长度  
	private LrcProcess mLrcProcess;	//歌词处理
	private List<LrcInfo> lrcList = new ArrayList<LrcInfo>(); //存放歌词列表对象
	private int index = 0;			//歌词检索值
	private String lrcUrl;
	private int flag = 0;
    
    
	
	//服务要发送的一些Action  
    public static final String UPDATE_ACTION = "com.example.action.UPDATE_ACTION";  //更新动作  
    public static final String CTL_ACTION = "com.example.action.CTL_ACTION";        //控制动作  
    public static final String MUSIC_CURRENT = "com.example.action.MUSIC_CURRENT";  //当前音乐播放时间更新动作  
    public static final String MUSIC_DURATION = "com.example.action.MUSIC_DURATION";//新音乐长度更新动作 
    public static final String ISPLAYINT_ACTION = "com.example.action.ISPLAYINT_ACTION";//更新播放图标
	public static final String SHOW_LRC = "com.example.action.SHOW_LRC";			//通知显示歌词
	public static final String NET_MUSIC = "com.example.action.NET_MUSIC";//网络音乐

    
    private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if (msg.what==1) {
				if (mediaPlayer!=null) {
					currentTime = mediaPlayer.getCurrentPosition(); // 获取当前音乐播放的位置  
					Intent intent = new Intent();  
					intent.setAction(MUSIC_CURRENT);  
					intent.putExtra("currentTime", currentTime);  
					sendBroadcast(intent); // 给Activity发送广播  
					handler.sendEmptyMessageDelayed(1, 1000);  
					
				}
			}
			super.handleMessage(msg);
		}
    	
    };
    
    
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		//初始化参数
		mediaPlayer = new MediaPlayer();  
        mp3Infos = MediaUtils.getMp3Infos(PlayerService.this);  
          
        //注册监听器
        musicReceiver = new MusicReceiver();  
        IntentFilter filter = new IntentFilter();  
        filter.addAction(CTL_ACTION);  
        filter.addAction(SHOW_LRC);
        filter.addAction(NET_MUSIC);
        registerReceiver(musicReceiver, filter);  
        
        // 设置音乐播放完成时的监听器 
           
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {  
  
            @Override  
            public void onCompletion(MediaPlayer mp) {  
                if (status == 1) { // 单曲循环  
                    mediaPlayer.start();  
                }
                
                else if(status == 2) {    //随机播放  
            	current = getRandomIndex(mp3Infos.size() - 1);  
            	System.out.println("currentIndex ->" + current);  
            	Intent sendIntent = new Intent(UPDATE_ACTION);  
            	sendIntent.putExtra("current", current);  
            	// 发送广播，将被Activity组件中的BroadcastReceiver接收到  
            	sendBroadcast(sendIntent);  
            	path = mp3Infos.get(current).getUrl();  
            	play(0);  
                }  
                else if (status == 3) { // 顺序播放  
                    current++;  //下一首位置  
                    if (current <= mp3Infos.size() - 1) {  
                        Intent sendIntent = new Intent(UPDATE_ACTION);  
                        sendIntent.putExtra("current", current);  
                        // 发送广播，将被Activity组件中的BroadcastReceiver接收到  
                        sendBroadcast(sendIntent);  
                        path = mp3Infos.get(current).getUrl();  
                        play(0);  
                    }else {  
                        mediaPlayer.seekTo(0);  
                        current = 0;  
                        Intent sendIntent = new Intent(UPDATE_ACTION);  
                        sendIntent.putExtra("current", current);  
                        // 发送广播，将被Activity组件中的BroadcastReceiver接收到  
                        sendBroadcast(sendIntent);  
                    }  
                 }
            }  
        });  
          
        
	}
	
	//定义一个随机抽取的方法
	protected int getRandomIndex(int end) {  
        int index = (int) (Math.random() * end);  
        return index;  
    }  

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {	
		mp3Infos = MediaUtils.getMp3Infos(PlayerService.this);
		path = intent.getStringExtra("url");
		current = intent.getIntExtra("listPosition", -1);
		msg = intent.getIntExtra("MSG", 0);
		
		//未优化，线程未释放
		if (msg == AppConstant.PlayerMsg.PLAY_MSG) {	
			play(0);
			if (PlayerActivity.lrcView != null) {
				lrcUrl = mp3Infos.get(current).getUrl().replace(".mp3", ".lrc");
				initLrc(lrcUrl);
				flag = 0;
			}
		}
		else if (msg == AppConstant.PlayerMsg.PAUSE_MSG) {
			pause();		
		}
		else if (msg == AppConstant.PlayerMsg.STOP_MSG) {
			stop();
		}
		else if (msg == AppConstant.PlayerMsg.CONTINUE_MSG) {
			resume();
		}
		else if (msg == AppConstant.PlayerMsg.REVERSE_MSG) {
			reverse();
			if (PlayerActivity.lrcView != null) {
				lrcUrl = mp3Infos.get(current).getUrl().replace(".mp3", ".lrc");
				initLrc(lrcUrl);
				flag = 0;
			}
		}
		else if (msg == AppConstant.PlayerMsg.NEXT_MSG) {
			next();
			if (PlayerActivity.lrcView != null) {
				lrcUrl = mp3Infos.get(current).getUrl().replace(".mp3", ".lrc");
				initLrc(lrcUrl);
				flag = 0;
			}
		}
		else if (msg == AppConstant.PlayerMsg.PROGRESS_CHANGE) {
			currentTime = intent.getIntExtra("progress", -1);
			play(currentTime);
		}
		else if (msg == AppConstant.PlayerMsg.PLAYING_MSG) {
			handler.sendEmptyMessage(1);
		}
		else if (msg == AppConstant.PlayerMsg.NET_MSG) {
			mp3Infos = (List<Mp3Info>) intent.getSerializableExtra("listSearchResult");
			current = intent.getIntExtra("current", -1);
			new Thread(new Runnable() {
				
				public void run() {
					play(0);
					flag = 1;
					if (PlayerActivity.lrcView != null) {
					lrcUrl = Environment.getExternalStorageDirectory() + "/Download" + File.separator + mp3Infos.get(current).getTitle() + ".lrc";
					initLrc(lrcUrl);
					}
				}
			}).start();
			
		}
		
		
		
		return super.onStartCommand(intent, flags, startId);
	}

	
	
	
	//播放音乐
	private void play(int currentTime){
		try {
			
			mediaPlayer.reset();
			mediaPlayer.setDataSource(path);
			mediaPlayer.prepare();
			mediaPlayer.setOnPreparedListener(new PreparedListener(currentTime));
			
			handler.sendEmptyMessage(1);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//暂停播放
	private void pause(){
		if (mediaPlayer != null && mediaPlayer.isPlaying()){
			mediaPlayer.pause();
			isPause = true;
		}
	}
	//继续播放
	private void resume(){
		if (isPause) {
			mediaPlayer.start();
			isPause = false;
		}
	
	}
	//上一首
	private void reverse(){
		Intent sendIntent = new Intent(UPDATE_ACTION);  
		sendIntent.putExtra("current", current);  
		// 发送广播，将被Activity组件中的BroadcastReceiver接收到  
		sendBroadcast(sendIntent);  
		play(0);  
	}
	//下一首
	private void next(){
		 if (status == 1) { // 单曲循环  
			 play(0);
         }
         
         else if(status == 2) {    //随机播放  
     	current = getRandomIndex(mp3Infos.size() - 1);  
     	System.out.println("currentIndex ->" + current);  
     	Intent sendIntent = new Intent(UPDATE_ACTION);  
     	sendIntent.putExtra("current", current);  
     	// 发送广播，将被Activity组件中的BroadcastReceiver接收到  
     	sendBroadcast(sendIntent);  
     	path = mp3Infos.get(current).getUrl();  
     	play(0);  
         }  
         else if (status == 3) { // 顺序播放  
             if (current <= mp3Infos.size() - 1) {  
                 Intent sendIntent = new Intent(UPDATE_ACTION);  
                 sendIntent.putExtra("current", current);  
                 // 发送广播，将被Activity组件中的BroadcastReceiver接收到  
                 sendBroadcast(sendIntent);  
                 path = mp3Infos.get(current).getUrl();  
                 play(0);  
             }else {  
                 mediaPlayer.seekTo(0);  
                 current = 0;  
                 Intent sendIntent = new Intent(UPDATE_ACTION);  
                 sendIntent.putExtra("current", current);  
                 // 发送广播，将被Activity组件中的BroadcastReceiver接收到  
                 sendBroadcast(sendIntent);  
             }  
          }
		
		Intent sendIntent = new Intent(UPDATE_ACTION);  
        sendIntent.putExtra("current", current);  
        // 发送广播，将被Activity组件中的BroadcastReceiver接收到  
        sendBroadcast(sendIntent);  
	}
	//停止播放
	private void stop(){
		 if (mediaPlayer != null) {  
	            mediaPlayer.stop();  
	            try {  
	                mediaPlayer.prepare(); // 在调用stop后如果需要再次通过start进行播放,需要之前调用prepare函数  
	            } catch (Exception e) {  
	                e.printStackTrace();  
	            }  
	        }
		
	}
	
	//定义一个监听器，判断是否从头开始播放，若传入当前播放时间，则从时间点开始播放，并将音乐长度发送回主界面
	private final class PreparedListener implements OnPreparedListener{
		
		private int currentTime;
		
		public PreparedListener(int currentTime) {
			this.currentTime = currentTime;
		}

		@Override
		public void onPrepared(MediaPlayer mp) {
			mediaPlayer.start();
			
			if(currentTime > 0){
				mediaPlayer.seekTo(currentTime);
			}
			
			//将歌曲的长度发送出去
			Intent intent = new Intent();
			intent.setAction(MUSIC_DURATION);
			duration = mediaPlayer.getDuration();
			intent.putExtra("duration", duration);
			sendBroadcast(intent);
			
		}
	
	}
	
	
	/**
	 * 初始化歌词配置
	 */
	public void initLrc(String url){	
		mLrcProcess = new LrcProcess();
		//读取歌词文件
		mLrcProcess.readLRC(url);	
		//传回处理后的歌词文件
		lrcList = mLrcProcess.getLrcList();		
		PlayerActivity.lrcView.setmLrcList(lrcList);
		//切换带动画显示歌词
		PlayerActivity.lrcView.setAnimation(AnimationUtils.loadAnimation(PlayerService.this,R.anim.alpha));
		handler.post(mRunnable);
	}
	
	Runnable mRunnable = new Runnable() {
		
		@Override
		public void run() {
			PlayerActivity.lrcView.setIndex(lrcIndex());
			PlayerActivity.lrcView.invalidate();
			handler.postDelayed(mRunnable, 100);
		}
	};
	
	/**
	 * 根据时间获取歌词显示的索引值
	 * @return
	 */
	public int lrcIndex() {
		if(mediaPlayer.isPlaying()) {
			currentTime = mediaPlayer.getCurrentPosition();
			duration = mediaPlayer.getDuration();
		}
		if(currentTime < duration) {
			for (int i = 0; i < lrcList.size(); i++) {
				if (i < lrcList.size() - 1) {
					if (currentTime < lrcList.get(i).getLrcTime() && i == 0) {
						index = i;
					}
					if (currentTime > lrcList.get(i).getLrcTime()
							&& currentTime < lrcList.get(i + 1).getLrcTime()) {
						index = i;
					}
				}
				if (i == lrcList.size() - 1
						&& currentTime > lrcList.get(i).getLrcTime()) {
					index = i;
				}
			}
		}
		return index;
	}
	
	
	public class MusicReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			int control = intent.getIntExtra("control", -1);
			switch (control) {
			case 1:  
                status = 1; // 将播放状态置为1表示：单曲循环  
                break;  
			case 2:  
				status = 2; //将播放状态置为2表示：随机播放  
				break;
            case 3:  
                status = 3; //将播放状态置为3表示：顺序播放  
                break;  
			}
			
			String action = intent.getAction();
			Bundle bundle = intent.getExtras();
			if(action.equals(SHOW_LRC)){
				if (flag == 0) {
					mp3Infos = MediaUtils.getMp3Infos(PlayerService.this);  
					lrcUrl = mp3Infos.get(current).getUrl().replace(".mp3", ".lrc");
					initLrc(lrcUrl);
				} else {
					mp3Infos = (List<Mp3Info>)bundle.getSerializable("listSearchResult");
					lrcUrl = Environment.getExternalStorageDirectory() + "/Download" + File.separator + mp3Infos.get(current).getTitle() + ".lrc";
					initLrc(lrcUrl);
				}
				
			}
			
			
		}
		
	}
	
	
	
	//当程序退出，清空MP3播放器
	@Override
	public void onDestroy() {
		  if (mediaPlayer != null) {  
	            mediaPlayer.stop();  
	            mediaPlayer.release();  
	            mediaPlayer = null;  
	        }  
	}

}
