package service;

import java.util.ArrayList;
import java.util.List;

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
	
	private MediaPlayer mediaPlayer; // ý�岥��������  
    private String path;            // �����ļ�·��  
    private int msg;  
    private boolean isPause;        // ��ͣ״̬  
    private int current = 0;        // ��¼��ǰ���ڲ��ŵ�����  
    private List<Mp3Info> mp3Infos;   //���Mp3Info����ļ���  
    private int status = 3;         //����״̬��Ĭ��Ϊ˳�򲥷�  
    private MusicReceiver musicReceiver;  //�Զ���㲥������  
    private int currentTime;        //��ǰ���Ž���  
    private int duration;           //���ų���  
	private LrcProcess mLrcProcess;	//��ʴ���
	private List<LrcInfo> lrcList = new ArrayList<LrcInfo>(); //��Ÿ���б����
	private int index = 0;			//��ʼ���ֵ
    
    
	
	//����Ҫ���͵�һЩAction  
    public static final String UPDATE_ACTION = "com.example.action.UPDATE_ACTION";  //���¶���  
    public static final String CTL_ACTION = "com.example.action.CTL_ACTION";        //���ƶ���  
    public static final String MUSIC_CURRENT = "com.example.action.MUSIC_CURRENT";  //��ǰ���ֲ���ʱ����¶���  
    public static final String MUSIC_DURATION = "com.example.action.MUSIC_DURATION";//�����ֳ��ȸ��¶��� 
    public static final String ISPLAYINT_ACTION = "com.example.action.ISPLAYINT_ACTION";//���²���ͼ��
	public static final String SHOW_LRC = "com.example.action.SHOW_LRC";			//֪ͨ��ʾ���

    
    private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if (msg.what==1) {
				if (mediaPlayer!=null) {
					currentTime = mediaPlayer.getCurrentPosition(); // ��ȡ��ǰ���ֲ��ŵ�λ��  
					Intent intent = new Intent();  
					intent.setAction(MUSIC_CURRENT);  
					intent.putExtra("currentTime", currentTime);  
					sendBroadcast(intent); // ��Activity���͹㲥  
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
		
		mediaPlayer = new MediaPlayer();  
        mp3Infos = MediaUtils.getMp3Infos(PlayerService.this);  
          
        
        // �������ֲ������ʱ�ļ����� 
           
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {  
  
            @Override  
            public void onCompletion(MediaPlayer mp) {  
                if (status == 1) { // ����ѭ��  
                    mediaPlayer.start();  
                } else if (status == 2) { // ȫ��ѭ��  
                    current++;  
                    if(current > mp3Infos.size() - 1) {  //��Ϊ��һ�׵�λ�ü�������  
                        current = 0;  
                    }  
                    Intent sendIntent = new Intent(UPDATE_ACTION);  
                    sendIntent.putExtra("current", current);  
                    // ���͹㲥������Activity����е�BroadcastReceiver���յ�  
                    sendBroadcast(sendIntent);  
                    path = mp3Infos.get(current).getUrl();  
                    play(0);  
                } else if (status == 3) { // ˳�򲥷�  
                    current++;  //��һ��λ��  
                    if (current <= mp3Infos.size() - 1) {  
                        Intent sendIntent = new Intent(UPDATE_ACTION);  
                        sendIntent.putExtra("current", current);  
                        // ���͹㲥������Activity����е�BroadcastReceiver���յ�  
                        sendBroadcast(sendIntent);  
                        path = mp3Infos.get(current).getUrl();  
                        play(0);  
                    }else {  
                        mediaPlayer.seekTo(0);  
                        current = 0;  
                        Intent sendIntent = new Intent(UPDATE_ACTION);  
                        sendIntent.putExtra("current", current);  
                        // ���͹㲥������Activity����е�BroadcastReceiver���յ�  
                        sendBroadcast(sendIntent);  
                    }  
                } else if(status == 4) {    //�������  
                    current = getRandomIndex(mp3Infos.size() - 1);  
                    System.out.println("currentIndex ->" + current);  
                    Intent sendIntent = new Intent(UPDATE_ACTION);  
                    sendIntent.putExtra("current", current);  
                    // ���͹㲥������Activity����е�BroadcastReceiver���յ�  
                    sendBroadcast(sendIntent);  
                    path = mp3Infos.get(current).getUrl();  
                    play(0);  
                }  
            }  
        });  
  
        musicReceiver = new MusicReceiver();  
        IntentFilter filter = new IntentFilter();  
        filter.addAction(CTL_ACTION);  
        filter.addAction(SHOW_LRC);
        registerReceiver(musicReceiver, filter);  
	}
	
	//����һ�������ȡ�ķ���
	protected int getRandomIndex(int end) {  
        int index = (int) (Math.random() * end);  
        return index;  
    }  

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {	
		
		path = intent.getStringExtra("url");
		current = intent.getIntExtra("listPosition", -1);
		msg = intent.getIntExtra("MSG", 0);
		//δ�Ż����̹߳������׿��������̳߳�
		if (msg == AppConstant.PlayerMsg.PLAY_MSG) {		
			play(0);
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
		}
		else if (msg == AppConstant.PlayerMsg.NEXT_MSG) {
			next();
		}
		else if (msg == AppConstant.PlayerMsg.PROGRESS_CHANGE) {
			currentTime = intent.getIntExtra("progress", -1);
			play(currentTime);
		}
		else if (msg == AppConstant.PlayerMsg.PLAYING_MSG) {
			handler.sendEmptyMessage(1);
		}
		else if (msg == AppConstant.PlayerMsg.NET_MSG) {
			new Thread(new Runnable() {
				
				public void run() {
					// TODO Auto-generated method stub
					play(0);
				}
			}).start();
			
		}
		
		
		
		return super.onStartCommand(intent, flags, startId);
	}

	
	
	
	//��������
	private void play(int currentTime){
		try {
			
			if (PlayerActivity.lrcView != null) {
				initLrc();
			}

			mediaPlayer.reset();
			mediaPlayer.setDataSource(path);
			mediaPlayer.prepare();
			mediaPlayer.setOnPreparedListener(new PreparedListener(currentTime));
			
			
			handler.sendEmptyMessage(1);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//��ͣ����
	private void pause(){
		if (mediaPlayer != null && mediaPlayer.isPlaying()){
			mediaPlayer.pause();
			isPause = true;
		}
	}
	//��������
	private void resume(){
		if (isPause) {
			mediaPlayer.start();
			isPause = false;
		}
	
	}
	//��һ��
	private void reverse(){
		Intent sendIntent = new Intent(UPDATE_ACTION);  
		sendIntent.putExtra("current", current);  
		// ���͹㲥������Activity����е�BroadcastReceiver���յ�  
		sendBroadcast(sendIntent);  
		play(0);  
	}
	//��һ��
	private void next(){
		Intent sendIntent = new Intent(UPDATE_ACTION);  
        sendIntent.putExtra("current", current);  
        // ���͹㲥������Activity����е�BroadcastReceiver���յ�  
        sendBroadcast(sendIntent);  
        play(0);
	}
	//ֹͣ����
	private void stop(){
		 if (mediaPlayer != null) {  
	            mediaPlayer.stop();  
	            try {  
	                mediaPlayer.prepare(); // �ڵ���stop�������Ҫ�ٴ�ͨ��start���в���,��Ҫ֮ǰ����prepare����  
	            } catch (Exception e) {  
	                e.printStackTrace();  
	            }  
	        }
		
	}
	
	//����һ�����������ж��Ƿ��ͷ��ʼ���ţ������뵱ǰ����ʱ�䣬���ʱ��㿪ʼ���ţ��������ֳ��ȷ��ͻ�������
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
			
			//�������ĳ��ȷ��ͳ�ȥ
			Intent intent = new Intent();
			intent.setAction(MUSIC_DURATION);
			duration = mediaPlayer.getDuration();
			intent.putExtra("duration", duration);
			sendBroadcast(intent);
			
		}
	
	}
	
	
	/**
	 * ��ʼ���������
	 */
	public void initLrc(){	
		
		mLrcProcess = new LrcProcess();
		//��ȡ����ļ�
		mLrcProcess.readLRC(mp3Infos.get(current).getUrl());	
		//���ش����ĸ���ļ�
		lrcList = mLrcProcess.getLrcList();		
		PlayerActivity.lrcView.setmLrcList(lrcList);
		//�л���������ʾ���
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
	 * ����ʱ���ȡ�����ʾ������ֵ
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
                status = 1; // ������״̬��Ϊ1��ʾ������ѭ��  
                break;  
            case 2:  
                status = 2; //������״̬��Ϊ2��ʾ��ȫ��ѭ��  
                break;  
            case 3:  
                status = 3; //������״̬��Ϊ3��ʾ��˳�򲥷�  
                break;  
            case 4:  
                status = 4; //������״̬��Ϊ4��ʾ���������  
                break;
			}
			
			String action = intent.getAction();
			if(action.equals(SHOW_LRC)){
				current = intent.getIntExtra("listPosition", -1);
				initLrc();
			}
			
		}
		
	}
	
	
	
	//�������˳������MP3������
	@Override
	public void onDestroy() {
		  if (mediaPlayer != null) {  
	            mediaPlayer.stop();  
	            mediaPlayer.release();  
	            mediaPlayer = null;  
	        }  
	}

}
