package com.example.musicplayer;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.musicplayer.lrc.LrcContent;
import com.example.musicplayer.lrc.LrcHandler;
import com.example.musicplayer.lrc.LrcView;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

public class PlayMusicService extends Service{

	private static MediaPlayer mediaPlayer;
	
	private String selectSong;
	
	public static boolean isPause = false;
	private static boolean needResume = false;
	public boolean startingNew = false;  //这个变量用于判断是否正在启动一个新歌曲，因为当上一首歌没有播完的时候，点击list播放另外一首的时候，onCompletion回调函数也会调用
	
	public static boolean needSeekTo;
	public static int seekValue;
	public static int songDuration;
	public static int CurrentPlayPosition;

    private controlThread cthread  = null;
    
    final String PLAY_COMPLETE_ACTION = "current_song_play_complete";
    final String PAUSE_PLAY_SONG_ACTION = "com.android.example.select_song_pause";
    final String RESUME_PLAY_SONG_ACTION = "com.android.example.select_song_resume";
    
	private LrcHandler mLrcHandler;
	private static List<LrcContent> lrcList;
	
	private static int lrcList_index = 0;
	private static int mediaPlayer_currentTime;
	private static int mediaPlayer_duration;
	private String mediaPlayerSongPath;
	
	private final static String TAG = "PlayMusicService";
    
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public PlayMusicService()
	{
		super();
		cthread = new controlThread();
		cthread.start();
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(new OnCompletionListener(){

			@Override
			public void onCompletion(MediaPlayer arg0) {
				// TODO Auto-generated method stub
				if(startingNew == false)  //解决当点击list item切换歌曲的时候，直接播放完毕的bug，因为播放过程中切换歌曲，这个onCompletion会被调用，但是当前是不需要播放完毕这个广播的
				{
					Toast.makeText(PlayMusicService.this,"这首播放完毕!",Toast.LENGTH_SHORT).show();
					//completePlay = true;
					Intent intent= new Intent();
					intent.setAction(PLAY_COMPLETE_ACTION);
					intent.putExtra("broadcastMsg", "收到此广播请回复，谢谢！");
					sendBroadcast(intent);
				}
				else
				{
					startingNew = false;
				}
			}
			
		});
		
		/*
		  service 里面注册不了广播
		IntentFilter filter = new IntentFilter();  
		filter.addAction(PAUSE_PLAY_SONG_ACTION);
		filter.addAction(RESUME_PLAY_SONG_ACTION);
		PlayMusicServiceReceiver p = new PlayMusicServiceReceiver();
		registerReceiver(p,filter); */
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		
		if(mediaPlayer.isPlaying())
		{
			stop();
		}
		if(intent ==null)
			return -1;
			
		selectSong = intent.getStringExtra("selectSong");
		mediaPlayerSongPath = selectSong;
		mediaPlayer.reset();

		try {
			mediaPlayer.setDataSource(selectSong);
			mediaPlayer.prepare();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		isPause = false;
		needResume = false;
		needSeekTo = false;
		CurrentPlayPosition = 0;
		
		mediaPlayer.start();
		initLrc();
		MusicManagerActivity.setFLrcViewList(lrcList);
		PlayingMusicActivity.setmLrcViewList(lrcList);
		songDuration = mediaPlayer.getDuration();
		startingNew = true;
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	private void stop()
	{
		if(mediaPlayer!= null)
		{
			mediaPlayer.stop();
			try {
				mediaPlayer.prepare();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	private void pause()
	{
		if(mediaPlayer!=null&&mediaPlayer.isPlaying())
		{
			mediaPlayer.pause();

		}
		
	}
	
	private void rePlay()
	{
		if(mediaPlayer!=null&&!mediaPlayer.isPlaying())
		{
			
			mediaPlayer.start();

			
		}
		
	}
	
	public static void pauseMusic()
	{
		isPause = true;
		needResume = false;
	}
	
	public static void resumeMusic()
	{
		isPause = false;
		needResume = true;
	}
	
	public static int getDuration()
	{
		return songDuration;
	}
	
	public static int getCurrentPlayPosition()
	{
		return CurrentPlayPosition;
	}
	
	public static void seekTo(int value)
	{
		needSeekTo = true;
		seekValue = value;
	}
	
	public class controlThread extends Thread
	{
		private int Times =0;
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			while(MusicManagerActivity.systemExit == false)
			{
				if(mediaPlayer!=null)
				{
					if(isPause == true)
					{
						pause();
					}
					else if(needResume == true)
					{
						rePlay();
					}
					
					if(needSeekTo == true)
					{
						needSeekTo = false;
						mediaPlayer.seekTo(seekValue);
					}
					CurrentPlayPosition = mediaPlayer.getCurrentPosition();
					
					
					/*为了规避有时候切换歌曲的时候不会调用onCompletion回调函数*/
					if(startingNew == true && Times<10)
					{
						Times++;
					}
					else if(startingNew == true)
					{
						startingNew = false;
						Times = 0;
					}
						
				}
				
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}	
		}
			
	}

	
	public void initLrc(){
		
		Log.d(TAG, "initLrc()");
		
		mLrcHandler = LrcHandler.getInstance();
		//mLrcHandler.getLrcContent(mediaPlayerSongPath);
		lrcList = mLrcHandler.readLRC(mediaPlayerSongPath);
	}
	
	/*
	public static int lrcIndex(){
		//Log.d(TAG, "lrcIndex()");
		if(mediaPlayer == null){
			return 0;
		}

		if(mediaPlayer.isPlaying()){
			mediaPlayer_currentTime = mediaPlayer.getCurrentPosition();
			mediaPlayer_duration = mediaPlayer.getDuration();
			if(mediaPlayer_currentTime < mediaPlayer_duration){
				for(int i = 0; i < lrcList.size(); i++){
					if(i < lrcList.size() - 1){
						//if(mediaPlayer_currentTime < lrcList.get(i).getLrcTime() && i == 0){
							//lrcList_index = i;
						//}
						//if(mediaPlayer_currentTime > lrcList.get(i).getLrcTime() && mediaPlayer_currentTime < lrcList.get(i + 1).getLrcTime()){
							//lrcList_index = i;
						//}
					}
					//if(i == lrcList.size() - 1 && mediaPlayer_currentTime > lrcList.get(i).getLrcTime()){
						//lrcList_index = i;
					//}
				}
			}
		}
		return lrcList_index;
	}*/
	
	public static int lrcIndex(){
		//Log.d(TAG, "lrcIndex()");
		if(mediaPlayer == null){
			return 0;
		}

		if(mediaPlayer.isPlaying()){
			mediaPlayer_currentTime = mediaPlayer.getCurrentPosition();
			mediaPlayer_duration = mediaPlayer.getDuration();
			if(mediaPlayer_currentTime < mediaPlayer_duration){
				for(int i = 0; i < lrcList.size(); i++){
					if(i < lrcList.size() - 1){
						if(mediaPlayer_currentTime < lrcList.get(i).getTime() && i == 0){
							lrcList_index = i;
						}
						if(mediaPlayer_currentTime > lrcList.get(i).getTime() && mediaPlayer_currentTime < lrcList.get(i + 1).getTime()){
							lrcList_index = i;
						}
					}
					if(i == lrcList.size() - 1 && mediaPlayer_currentTime > lrcList.get(i).getTime()){
						lrcList_index = i;
					}
				}
			}
		}
		return lrcList_index;
	}
	
	
	public static void setlrcIndex(int index)
	{
		lrcList_index = index;
		//mediaPlayer_currentTime =  lrcList.get(index).getLrcTime();
		//mediaPlayer.seekTo(mediaPlayer_currentTime);
		
	}
	
	public static List<LrcContent> getLrcList()
	{	
		return lrcList;
	}

	/*
	public class PlayMusicServiceReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			String myaction = arg1.getAction().toString();
			if(myaction.equals(RESUME_PLAY_SONG_ACTION))
			{
				resumeMusic();
			}
			else if(myaction.equals(PAUSE_PLAY_SONG_ACTION))
			{
				pauseMusic();
			}
			
		}
		
	}*/
		
}




