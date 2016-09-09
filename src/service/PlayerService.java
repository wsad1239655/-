package service;

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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.TextView;
import model.AppConstant;
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
    
    
	
	//����Ҫ���͵�һЩAction  
    public static final String UPDATE_ACTION = "com.example.action.UPDATE_ACTION";  //���¶���  
    public static final String CTL_ACTION = "com.example.action.CTL_ACTION";        //���ƶ���  
    public static final String MUSIC_CURRENT = "com.example.action.MUSIC_CURRENT";  //��ǰ���ֲ���ʱ����¶���  
    public static final String MUSIC_DURATION = "com.example.action.MUSIC_DURATION";//�����ֳ��ȸ��¶��� 
    public static final String ISPLAYINT_ACTION = "com.example.action.ISPLAYINT_ACTION";//���²���ͼ��

    
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
		
		
		
		return super.onStartCommand(intent, flags, startId);
	}

	
	
	
	//��������
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
