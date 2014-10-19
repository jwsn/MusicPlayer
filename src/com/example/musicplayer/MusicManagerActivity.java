package com.example.musicplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.example.musicplayer.interfa.Audio;
import com.example.musicplayer.lrc.LrcContent;
import com.example.musicplayer.lrc.LrcView;
import com.example.musicplayer.lrc.LrcView.OnLrcClickListener;
import com.example.musicplayer.myAdapter.MyGridViewAdapter;
import com.example.musicplayer.myAdapter.MyListViewAdapter;
import com.example.musicplayer.myListener.FloatViewTouchListener;
import com.example.musicplayer.myListener.SetAdapterListener;
import com.example.musicplayer.myListener.listListenerImpl;
import com.example.musicplayer.myclass.songInfo;
import com.igexin.sdk.PushManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;





public class MusicManagerActivity extends Activity implements SensorEventListener {

	//private Audio laudio = new LocalAudio(this);
	private static List<songInfo> musicList;
	private List<String> Items =  new ArrayList<String>();
	private ImageButton pauseOrStart;
	private ImageButton playModeButton;
	private ImageButton previous;
	private ImageButton next;
	private ImageButton playingButton;
	private ImageButton searchSongButton;
	private ImageButton HistoryButton;
	private ImageButton MyLocalMusicButton;
	private ImageButton MyCollectMusicButton;
	private TextView PlayingMusicName;
	private Context myContext;
	private int statusBarHeight;
	private boolean firstTime = true;
	private boolean isshownotification = true;
	
	
	SensorManager sensorManager = null;
//	private static ListView listView;
	//MyListViewAdapter adapter;
	//private static  ManagerSetAdapterLitener managerSetAdapterListener;
	final String TAG = "MUSIC";
	final String SHOW_LRC_ON_IDLE_ACTION = "show_lrc_on_idle";
	final String PLAY_COMPLETE_ACTION = "current_song_play_complete";
	final String PLAY_NEXT_ACTION ="com.android.musicplayer.playNext";
	final String PLAY_PRE_ACTION ="com.android.musicplayer.playPre";
	final String UPDATE_FINAL_TIME = "com.android.musicplayer.update_final_time";
    final String PAUSE_PLAY_SONG_ACTION = "com.android.example.select_song_pause";
    final String RESUME_PLAY_SONG_ACTION = "com.android.example.select_song_resume";
	final String PLAY_SEARCH_SONG_ACTION = "com.android.example.select_search_song_play";
	final String SNED_CURRENT_SONG_NAME_ACTION = "com.android.musicplayer.sed_song_name";
	final String UPDATE_PLAYING_SONG_NAME = "com.android.musicplayer.update_song_name";
	final String PLAY_CURRENT_POSITION_SONG = "com.android.musicplayer.play_currentP_song";
	final String CLOSE_LRC_ON_IDLE_ACTION = "close_lrc_on_idle_action";
	final String PLAY_STATE_CHANGE_ACTION = "play_state_change";
	final int local_music = 0;
	final int collect_music = 1;
	final int history_music = 2;
	final int search_music = 3;
	final int settings_button = 4;
	
	final String NORMAL_S = "normal";
	final String REPEAT_ONE_S = "repeat_one";
	final String REPEAT_ALL_S = "repeat_all";
	final String RANDOM_S = "random";
	final String SHAKE_SETING_ON = "on";
	final String LRC_SHOW_OFF = "off";
	
	private String playMode_S = NORMAL_S;
	private String shake_settings = SHAKE_SETING_ON;
	private String lrcshow = LRC_SHOW_OFF;
	
	boolean isPlaying = true;
	public static int currentPosition; 
	private myHandler handler;
	private String lastUrl;
	private String lastExitSongName;
	private String lastExitPlayMode;
	enum PlayMode{NORMAL,REPEAT_ONE,REPEAT_ALL,RANDOM};
	PlayMode playMode;
	private long exitTime = 0;
	public static boolean systemExit = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_musicmain);

		GridView gridview = (GridView) findViewById(R.id.gridview);  
		gridview.setAdapter(new MyGridViewAdapter(this));
		//gridview.setSelector(new ColorDrawable(Color.TRANSPARENT)); 
		gridview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int itemid,long arg3) {
			   //arg1是当前item的view，通过它可以获得该项中的各个组件。 
			   //arg2是当前item的ID。这个id根据你在适配器中的写法可以自己定义。 
			   //arg3是当前的item在listView中的相对位置！ 
				Intent intent = new Intent();
				intent.putExtra("isplaying",isPlaying);
				switch(itemid){
				case local_music:
					intent.setClass(MusicManagerActivity.this, MyLocalMusicListActivity.class);
					startActivityForResult(intent,4);
					break;
				case collect_music:
					intent.setClass(MusicManagerActivity.this, MyCollectMusicListActivity.class);
					startActivityForResult(intent,4);
					break;
				case history_music:
					intent.setClass(MusicManagerActivity.this, PlayHistoryListActivity.class);
					startActivity(intent);
					break;
				case search_music:
					intent.setClass(MusicManagerActivity.this, SearchSongActivity.class);
					startActivity(intent);
					break;
				case settings_button:
					intent.setClass(MusicManagerActivity.this, SettingsActivity.class);
					intent.putExtra("shakingsetings",shake_settings);
					intent.putExtra("lrcshow",lrcshow);
					startActivityForResult(intent,4);
					break;
				default:
					break;
				}

		    }
			});
		
		
		Intent intent = getIntent();
		isPlaying = intent.getBooleanExtra("isplaying",false);
		
		Bundle b = intent.getExtras();
		lastExitSongName = b.getString("exitMusicName");
		lastExitPlayMode =  b.getString("exitPlayMode");
		shake_settings = b.getString("shakeseting");
		lrcshow = b.getString("lrcshow");
		if(lastExitPlayMode == null){
			lastExitPlayMode = NORMAL_S;
		}
		if(shake_settings == null){
			
			shake_settings = "off";
		}
		if(lrcshow == null){
			
			lrcshow = "off";
		}else if(lrcshow.equals("on")){
			lrc_show = true;
		}
		musicList = AminationActivity.getMyLocalMusicList();
		
		
		PlayingMusicName = (TextView)findViewById(R.id.song_name_text);
		
		if(lastExitSongName !=null)
		{
			PlayingMusicName.setText(lastExitSongName);
		}
		
		for(int i=0;i<musicList.size();i++)
		{
			if(musicList.get(i).getName().equals(lastExitSongName))
			{
				currentPosition = i;
				break;
			}
		}
		
	//	managerSetAdapterListener = new ManagerSetAdapterLitener();
		myContext = this;
		/*实现暂停和开始播放功能*/
		pauseOrStart = (ImageButton)findViewById(R.id.pause_or_start);
		pauseOrStart.setOnClickListener(new OnClickListener(){
			private boolean firstTime = true;
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isPlaying == true)
				{
					pauseOrStart.setImageResource(R.drawable.play_button);
					PlayMusicService.pauseMusic();
					isPlaying = false;
					firstTime = false;
				}
				else
				{
					
					pauseOrStart.setImageResource(R.drawable.pause_button);
					if(firstTime == true)
					{
						firstTime = false;
						isPlaying = true;
						play(currentPosition);
						return;
					}
					PlayMusicService.resumeMusic();
					isPlaying = true;
				}
			}
				
		});
		
		
		/*用于设置播放歌曲的模式*/
		playModeButton = (ImageButton)findViewById(R.id.playmode);
		if(lastExitPlayMode.equals(REPEAT_ONE_S))
		{
			playMode = PlayMode.REPEAT_ONE;
			playModeButton.setImageResource(R.drawable.playmode_repeate_single);
			playMode_S = REPEAT_ONE_S;
		}
		else if (lastExitPlayMode.equals(REPEAT_ALL_S))
		{
			playMode = PlayMode.REPEAT_ALL;
			playModeButton.setImageResource(R.drawable.playmode_repeate_all);
			playMode_S = REPEAT_ALL_S;
		}
		else if(lastExitPlayMode.equals(RANDOM_S))
		{
			playMode = PlayMode.RANDOM;
			playMode_S = RANDOM_S;
			playModeButton.setImageResource(R.drawable.playmode_repeate_random);
		}
		else
		{	playMode_S = NORMAL_S;
			playMode = PlayMode.NORMAL;
		}
			
		
		playModeButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch(playMode)
				{
					case NORMAL:
						playMode = PlayMode.REPEAT_ONE;
						playMode_S = REPEAT_ONE_S;
						playModeButton.setImageResource(R.drawable.playmode_repeate_single);
						Toast.makeText(MusicManagerActivity.this,"单曲循环！",Toast.LENGTH_SHORT).show();
						break;
					case REPEAT_ONE:
						playMode = PlayMode.REPEAT_ALL;
						playMode_S = REPEAT_ALL_S;
						playModeButton.setImageResource(R.drawable.playmode_repeate_all);
						Toast.makeText(MusicManagerActivity.this,"列表循环！",Toast.LENGTH_SHORT).show();
						break;
					case REPEAT_ALL:
						playMode = PlayMode.RANDOM;
						playMode_S = RANDOM_S;
						playModeButton.setImageResource(R.drawable.playmode_repeate_random);
						Toast.makeText(MusicManagerActivity.this,"随机播放！！",Toast.LENGTH_SHORT).show();
						break;
					case RANDOM:
						playMode = PlayMode.NORMAL;
						playMode_S = NORMAL_S;
						playModeButton.setImageResource(R.drawable.playmode_normal);
						Toast.makeText(MusicManagerActivity.this,"顺序播放！",Toast.LENGTH_SHORT).show();
						break;
				
				}
			}
			
		});
		
		
		
		/*实现播放上一首歌曲的功能*/
		previous = (ImageButton)findViewById(R.id.previous);
		previous.setOnTouchListener(new View.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					//previous.setImageResource(R.drawable.pre_button_press);
					return true;
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					//previous.setImageResource(R.drawable.pre_button);
					playPrevious();
					return true;
				}
				return false;
			
			}
			
			
		});
		
		
		/*实现播放下一首歌曲的功能*/
		next = (ImageButton)findViewById(R.id.next);
		next.setOnTouchListener(new View.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					//next.setImageResource(R.drawable.next_button_press);
					return true;
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					//next.setImageResource(R.drawable.next_button);
					playNext();
					return true;
				}
				return false;
			
			}
			
			
		});
		
		
		/*实现跳转到播放界面的功能*/
		playingButton = (ImageButton)findViewById(R.id.playingbutton);
		playingButton.setOnTouchListener(new View.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					playingButton.setImageResource(R.drawable.playing_button_press);
					return true;
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					playingButton.setImageResource(R.drawable.playing_button);
					Intent intent = new Intent();
					intent.putExtra("isplaying",isPlaying);
				//	intent.putExtra("currentSongName",((songInfo) listView.getItemAtPosition(currentPosition)).getName());
					intent.putExtra("currentSongName",((songInfo) musicList.get(currentPosition)).getName());
					intent.setClass(MusicManagerActivity.this, PlayingMusicActivity.class);
					startActivityForResult(intent,3);

					return true;
				}
				return false;
			
			}
			
			
		});
		

/*		
		searchSongButton = (ImageButton)findViewById(R.id.search_song_button);
		searchSongButton.setOnTouchListener(new View.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					searchSongButton.setImageResource(R.drawable.search_button_press);
					return true;
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					searchSongButton.setImageResource(R.drawable.search_button);
					Intent intent = new Intent();
					intent.putExtra("isplaying",isPlaying);
					intent.setClass(MusicManagerActivity.this, SearchSongActivity.class);
					startActivity(intent);
					return true;
				}
				return false;
			}
			
			
		});
		

		HistoryButton = (ImageButton)findViewById(R.id.main_history_button);
		HistoryButton.setOnTouchListener(new View.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					HistoryButton.setImageResource(R.drawable.history_button_press);
					return true;
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					HistoryButton.setImageResource(R.drawable.history_button);
					Intent intent = new Intent();
					intent.putExtra("isplaying",isPlaying);
					intent.setClass(MusicManagerActivity.this, PlayHistoryListActivity.class);
					startActivity(intent);
					return true;
				}
				return false;
			}
			
			
		});
		
		MyLocalMusicButton = (ImageButton)findViewById(R.id.my_local_music);
		MyLocalMusicButton.setOnTouchListener(new View.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					MyLocalMusicButton.setImageResource(R.drawable.my_local_music_button_press);
					return true;
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					MyLocalMusicButton.setImageResource(R.drawable.my_local_music_button);
					Intent intent = new Intent();
					intent.putExtra("isplaying",isPlaying);
					intent.setClass(MusicManagerActivity.this, MyLocalMusicListActivity.class);
					startActivityForResult(intent,3);
					return true;
				}
				return false;
			}
			
			
		});
		
		MyCollectMusicButton = (ImageButton)findViewById(R.id.my_collect_music);
		MyCollectMusicButton.setOnTouchListener(new View.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					MyCollectMusicButton.setImageResource(R.drawable.my_collect_button_press);
					return true;
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					MyCollectMusicButton.setImageResource(R.drawable.my_collect_button);
					Intent intent = new Intent();
					intent.putExtra("isplaying",isPlaying);
					intent.setClass(MusicManagerActivity.this, MyCollectMusicListActivity.class);
					startActivityForResult(intent,3);
					return true;
				}
				return false;
			}
			
			
		});*/
		
		/*获取到歌曲列表，并且放到adapter里面，注册监听器，用于监听是否点击了list的item*/
		//listView= (ListView) this.findViewById(R.id.mylistview);  
		//adapter = new MyListViewAdapter(this,musicList,false);
		//adapter.setOnClickListener(new MusicListItemClickListener());
		//listView.setAdapter(adapter); 
		
		View v = findViewById(R.id.ButtonLayout);//找到你要设透明背景的layout 的id
		v.getBackground().setAlpha(180);
		
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);  
		sensorManager.registerListener(this,  
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),  
				SensorManager.SENSOR_DELAY_NORMAL);  
		if(isshownotification){
			isshownotification = false;
		    showNotification();
		}
	
		handler = new myHandler();
		myhandler = new mySysHandler();
		
		IntentFilter filter = new IntentFilter();  
		filter.addAction(PLAY_COMPLETE_ACTION);
		filter.addAction(PLAY_NEXT_ACTION);
		filter.addAction(PLAY_PRE_ACTION);
		filter.addAction(PLAY_SEARCH_SONG_ACTION);
		filter.addAction(PAUSE_PLAY_SONG_ACTION);
		filter.addAction(RESUME_PLAY_SONG_ACTION);
		filter.addAction(PLAY_CURRENT_POSITION_SONG);
		filter.addAction(SHOW_LRC_ON_IDLE_ACTION);
		filter.addAction(PLAY_STATE_CHANGE_ACTION);
		filter.addAction("android.intent.action.HEADSET_PLUG");
		registerReceiver(new MusicListReceiver(),filter);
		
    	fLrcView = new LrcView(getApplicationContext());
    	fLrcView.setLrcScalingFactor(1.0f);
    	fLrcView.isFloatWindow = true;
    	fLrcView.setLrcTextColor(Color.YELLOW,Color.RED);
    	
		SystemThread sysThread  = new SystemThread();
		sysThread.start();
	//	fLrcView = AminationActivity.getFloatLrcView();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		switch(resultCode){
		case 1:
		case 2:
		case 3:
			isPlaying = data.getBooleanExtra("isplayingornot",false);
			if(isPlaying){
				pauseOrStart.setImageResource(R.drawable.pause_button);
				
			}else{
				pauseOrStart.setImageResource(R.drawable.play_button);
			}
			break;
		case 4:
			shake_settings = data.getStringExtra("shakingsetings");
			lrcshow = data.getStringExtra("lrcshow");
			break;
		
		}
		
		
	}
		
	
	public static boolean isAppFront(Context context) 
	{
		 ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		 List<RunningAppProcessInfo> rapis = am.getRunningAppProcesses();
		 for (RunningAppProcessInfo rapi : rapis) 
		 {
			 if (rapi.processName.equals(context.getPackageName())) 
			 {
				 if (rapi.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) 
				 {
					 	return true;
				 }
			 }
		 }
		 	return false;
	}

	////////////start: 定义浮窗的变量///////////////
	mySysHandler myhandler;
	final int APPforegroud =1;
	final int APPnotforegroud =2;
	boolean lrc_show = false;
	boolean show_button_on_idle = true;
	final int UPDATELRC = 3;
	private static LrcView fLrcView;
	RelativeLayout floatLayout;
	LinearLayout playingbuttonlayout;
	WindowManager wm;
	WindowManager.LayoutParams wmParams;
	ImageButton floatPreButton;
	ImageButton floatPlayOrStopButton;
	ImageButton floatNextButton;
	ImageButton floatViewDeleteButton;
	ImageButton floatchangeLrcColorButton;
	private int floatViewY = 0;
	private boolean clickMyFloatButton = false;
	private int floatViewLrcColor = Color.BLUE;
	////////////////end: 定义浮窗的变量/////////////////
	

	public class SystemThread extends Thread
	{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			int lastState = APPforegroud;
			long lastStateTime = 0;  
			while(MusicManagerActivity.systemExit == false)
			{
				
				if(lrc_show == true)
				{
					if(isAppFront(myContext))
					{
						if(lastState != APPforegroud)
						{
							Message msg = new Message();
							msg.what = APPforegroud;
							myhandler.sendMessage(msg);
							lastState = APPforegroud;
							Log.d("ForeGroundTest","APPnotforegroud  to fore");
						}

					}
					else 
					{
						if(lastState != APPnotforegroud)
						{
							
							Message msg = new Message();
							msg.what = APPnotforegroud;
							myhandler.sendMessage(msg);
						//	clickMyFloatButton = false;
							lastState = APPnotforegroud;
							Log.d("ForeGroundTest","APPforegroud to notfore");

						}
					}

					
					Message msg = new Message();
					msg.what = UPDATELRC;
					myhandler.sendMessage(msg);
				}
	

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	
		}
		
	}
	public static void setFLrcViewList( List<LrcContent> lrcList)
	{
		if(fLrcView!=null)
			fLrcView.setLrcContents(lrcList);
	}
	
	
	public class mySysHandler extends Handler
	{
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub

			//playNext();
			switch(msg.what)
			{
			case APPforegroud:
				if(clickMyFloatButton == false)
				{
					WindowManager wm = (WindowManager)getApplicationContext().getSystemService("window");
					if(floatLayout!=null)
					{
						show_button_on_idle = true;
						if(floatLayout.getParent()!=null)
							wm.removeView(floatLayout);
					}
				}
				break;
			case APPnotforegroud:
				if(clickMyFloatButton == false)
				{
			    	wm = (WindowManager)getApplicationContext().getSystemService("window");
					wmParams = new WindowManager.LayoutParams();
				//	wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
					wmParams.type = 2003;
					wmParams.format = 1;
					wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;;
					wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
					wmParams.height =200;
					wmParams.gravity=Gravity.LEFT|Gravity.TOP;   //这个表示坐标从左上角开始计算
					wmParams.y = floatViewY;
			
					/*
			    	if(fLrcView!=null)
			    	{
			    		if(fLrcView.getParent()!=null)
			    			return;
						
			    		final DBHelper data=new DBHelper(myContext);
					    SQLiteDatabase db=data.getReadableDatabase();
					    Cursor c = db.query("musicwordfontsize", null, null, null, null, null, null);
					    String scale="0.5";
					    while(c.moveToNext())
					    {
					    	 scale = c.getString(0);
					    } 
				    	Log.i("fontscale:","get scale value is"+scale);
				    	float s = Float.parseFloat(scale);
				    	fLrcView.setLrcScalingFactor(s);
			    		wmParams.height = fLrcView.getTextSize()*3;
			    		wm.addView(fLrcView,wmParams);
			    		fLrcView.setWmAndParams(wm,wmParams);
			    		return;
			    	}
	
			    	fLrcView = MusicManagerActivity.getFloatLrcView();
			    	if(fLrcView!=null)
			    		fLrcView.setLrcContents(PlayMusicService.getLrcList());
			    	wmParams.height = fLrcView.getTextSize()*3;
					wm.addView(fLrcView,wmParams);
					fLrcView.setWmAndParams(wm,wmParams);*/
					LayoutInflater inflater = LayoutInflater.from(getApplication());
					floatLayout = (RelativeLayout)inflater.inflate(R.layout.floatlrcviewlayout,null);
	
					LrcView myfloatview = (LrcView)floatLayout.findViewById(R.id.floatview_song_lrc_text);
					
					playingbuttonlayout = (LinearLayout)floatLayout.findViewById(R.id.floatview_Playing_Button_Layout);
					
					floatPreButton = (ImageButton)floatLayout.findViewById(R.id.floatview_playing_previous);
					floatPreButton.setOnTouchListener(new View.OnTouchListener(){

						@Override
						public boolean onTouch(View v, MotionEvent event) {
							// TODO Auto-generated method stub
							if(event.getAction()==MotionEvent.ACTION_DOWN)
							{
								//previous.setImageResource(R.drawable.pre_button_press);
								return true;
							}
							else if(event.getAction()==MotionEvent.ACTION_UP)
							{
								//previous.setImageResource(R.drawable.pre_button);
								playPrevious();
								clickMyFloatButton = true;
								Intent intent= new Intent();
								intent.setAction(PLAY_STATE_CHANGE_ACTION);
								intent.putExtra("playstate",isPlaying);
								sendBroadcast(intent);
								return true;
							}
							return true;
						
						}
						
					});
					
					
					floatPlayOrStopButton = (ImageButton)floatLayout.findViewById(R.id.floatview_playing_pause_or_start);
					if(isPlaying == true)
					{
						floatPlayOrStopButton.setImageResource(R.drawable.pause_button);
					}
					else
					{
						floatPlayOrStopButton.setImageResource(R.drawable.play_button);
					}
					floatPlayOrStopButton.setOnClickListener(new OnClickListener(){
					
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							if(isPlaying == true)
							{
								floatPlayOrStopButton.setImageResource(R.drawable.play_button);
								PlayMusicService.pauseMusic();
								isPlaying = false;
								
							}
							else
							{
								
								floatPlayOrStopButton.setImageResource(R.drawable.pause_button);
								PlayMusicService.resumeMusic();
								isPlaying = true;
							}
							Intent intent= new Intent();
							intent.setAction(PLAY_STATE_CHANGE_ACTION);
							intent.putExtra("playstate",isPlaying);
							sendBroadcast(intent);
						}
							
					});
					floatNextButton = (ImageButton)floatLayout.findViewById(R.id.floatview_playing_next);
					floatNextButton.setOnTouchListener(new View.OnTouchListener(){

						@Override
						public boolean onTouch(View v, MotionEvent event) {
							// TODO Auto-generated method stub
							if(event.getAction()==MotionEvent.ACTION_DOWN)
							{
								return true;
							}
							else if(event.getAction()==MotionEvent.ACTION_UP)
							{
								playNext();
								clickMyFloatButton = true;
								Intent intent= new Intent();
								intent.setAction(PLAY_STATE_CHANGE_ACTION);
								intent.putExtra("playstate",isPlaying);
								sendBroadcast(intent);
								return true;
							}
							return true;
						
						}
						
						
					});
					

					
					floatchangeLrcColorButton = (ImageButton)floatLayout.findViewById(R.id.floatview_changelrccolor_button);
					floatchangeLrcColorButton.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							if(fLrcView!=null)
							{
								switch(floatViewLrcColor)
								{
									case Color.BLUE:
										floatViewLrcColor = Color.RED;
										break;
									case Color.RED:
										floatViewLrcColor = Color.YELLOW;
										break;
									case Color.YELLOW:
										floatViewLrcColor = Color.GREEN;
										break;
									case Color.GREEN:
										floatViewLrcColor = Color.BLACK;
										break;
									case Color.BLACK:
										floatViewLrcColor = Color.WHITE;
										break;
									default:
										floatViewLrcColor = Color.BLUE;
										break;
								}
								fLrcView.setLrcTextColor(floatViewLrcColor,floatViewLrcColor-0x1234);
								fLrcView.invalidate();
							}
						}
						
					});
					
					floatViewDeleteButton = (ImageButton)floatLayout.findViewById(R.id.floatview_delete_button);
					floatViewDeleteButton.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							WindowManager wm = (WindowManager)getApplicationContext().getSystemService("window");
							if(floatLayout!=null)
							{
								if(floatLayout.getParent()!=null)
									wm.removeView(floatLayout);
								show_button_on_idle = true;
								lrcshow = "off";
								lrc_show = false;
								ContentValues values=new ContentValues();
								
								values.put("shakeseting", shake_settings);
								values.put("lrcshow", lrcshow);
								DBHelper data=new DBHelper(MusicManagerActivity.this);
								SQLiteDatabase db=data.getWritableDatabase();
								db.insert("settings", null, values);
								
								Intent intent= new Intent();
								intent.setAction(CLOSE_LRC_ON_IDLE_ACTION);
								intent.putExtra("lrcshow",lrcshow);
								sendBroadcast(intent);
							}
						}
						
					});
					
					playingbuttonlayout.setVisibility(View.GONE);
					
					/*
					if(!show_button_on_idle){
					    floatPreButton.setVisibility(View.VISIBLE);
					    floatPlayOrStopButton.setVisibility(View.VISIBLE);
					    floatNextButton.setVisibility(View.VISIBLE);
					    floatViewDeleteButton.setVisibility(View.VISIBLE);
					}else{
					    floatPreButton.setVisibility(View.GONE);
					    floatPlayOrStopButton.setVisibility(View.GONE);
					    floatNextButton.setVisibility(View.GONE);
					    floatViewDeleteButton.setVisibility(View.GONE);
					}*/
					myfloatview.isFloatWindow = true;
					fLrcView = myfloatview;
			    	if(fLrcView!=null)
			    	{
			    		fLrcView.setLrcContents(PlayMusicService.getLrcList());
			    		final DBHelper data=new DBHelper(myContext);
					    SQLiteDatabase db=data.getReadableDatabase();
					    Cursor c = db.query("musicwordfontsize", null, null, null, null, null, null);
					    String scale="0.5";
					    while(c.moveToNext())
					    {
					    	 scale = c.getString(0);
					    } 
				    	Log.i("fontscale:","get scale value is"+scale);
				    	float s = Float.parseFloat(scale);
				    	fLrcView.setLrcScalingFactor(s);
				    	
				    	FloatViewTouchListener floatViewTouchListener = new FloatViewTouchListener(){
				    		private int y;
				    		private int buttonlayoutheight = 2*58 ;
							@Override
							public boolean onTouchDown(View v, MotionEvent event) {
								// TODO Auto-generated method stub
								Log.d("eventType:","onTouchDown  event type ="+event.getAction());
								if(show_button_on_idle){
									show_button_on_idle = false;
									playingbuttonlayout.setVisibility(View.VISIBLE);

								}else{
									show_button_on_idle = true;
									playingbuttonlayout.setVisibility(View.GONE);

								}
								/*获取状态栏高度*/
								Rect frame = new Rect();
								getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
								statusBarHeight = frame.top;
								
								y = (int)event.getY();
								return true;
							}

							@Override
							public boolean onTouchMove(View v, MotionEvent event) {
								// TODO Auto-generated method stub
								Log.d("eventType:","onTouchMove  event type ="+event.getAction());
								if(show_button_on_idle){
								    floatViewY=wmParams.y = (int) event.getRawY()-buttonlayoutheight-y-statusBarHeight;
								}else{
									floatViewY=wmParams.y = (int) event.getRawY()-y-statusBarHeight;
								}
				                wm.updateViewLayout(floatLayout, wmParams); 
								return false;
							}

							@Override
							public boolean onTouchUp(View v, MotionEvent event) {
								// TODO Auto-generated method stub
								Log.d("eventType:","onTouchUp  event type ="+event.getAction());
								return true;
							}
				    		
				    	};
				    	fLrcView.setLrcTextColor(floatViewLrcColor,Color.WHITE);
				    	fLrcView.setFloatViewTouchListener(floatViewTouchListener);
				    	wmParams.height = fLrcView.getTextSize()*3+60;
						wm.addView(floatLayout,wmParams);
			    	}

				}
				else
					clickMyFloatButton = false;
				break;
				
			case UPDATELRC:
				if(fLrcView!=null)
					fLrcView.seekTo(PlayMusicService.getCurrentPlayPosition(), true,false);
				}
		}
		
	}
	
	public static LrcView getFloatLrcView()
	{
		return fLrcView;
	}

	public static List<songInfo> getMusicList()
	{
		return musicList;
	}
	
	public static void setMusicList(List<songInfo> SetmusicList , int position)
	{
		musicList = SetmusicList;
		currentPosition = position;
	}
	
	/*
	public static String getCurrentSongName()
	{
		if(musicList!=null)
		{
			if(currentPosition >=musicList.size())
				currentPosition = musicList.size() -1;
			return (musicList.get(currentPosition)).getName();
		}
		else
			return "没有播放歌曲";
	}*/
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
	    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
	        if((System.currentTimeMillis()-exitTime) > 2000){  
	            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();                                
	            exitTime = System.currentTimeMillis();  
	         /*   
	            String ns = Context.NOTIFICATION_SERVICE;

		        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		        int icon = R.drawable.zhuomian;
		        CharSequence tickerText = "我的通知栏标题";
		        long when = System.currentTimeMillis();
		        Notification notification = new Notification(icon, tickerText, when);
		        Context context = getApplicationContext();

		        CharSequence contentTitle = "我的通知栏标展开标题";

		        CharSequence contentText = "我的通知栏展开详细内容";

		        Intent notificationIntent = new Intent(this, PlayingMusicActivity.class);

		        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,

		                notificationIntent, 0);

		        notification.setLatestEventInfo(context, contentTitle, contentText,

		                contentIntent);

		         

		        //用mNotificationManager的notify方法通知用户生成标题栏消息通知

		        mNotificationManager.notify(1, notification);*/
	            
	        } else {
	            finish();
	    		Intent intent = new Intent("com.angel.Android.MUSIC");
	    		intent.setClass(MusicManagerActivity.this,PlayMusicService.class);
	    		stopService(intent);
	    		
	    		ContentValues values=new ContentValues();
	    		
	    		//values.put("lastmusicname", getCurrentSongName());
	    		values.put("lastmusicname", MusicManagerActivity.getMusicList().get(MusicManagerActivity.currentPosition).getName());
	    		values.put("playmode",playMode_S);
	    		DBHelper data=new DBHelper(MusicManagerActivity.this);
	    		SQLiteDatabase db=data.getWritableDatabase();
	    		db.insert("exitinfo", null, values);
	    		quitnotification();
	    		systemExit = true;
	            System.exit(0);
	        }
	        return true;   
	        /*
	         * moveTaskToBack(false);
			return true;

	         * */
	    }
	    /*
	    else if(keyCode == KeyEvent.KEYCODE_HOME&&event.getAction() == KeyEvent.ACTION_UP)
	    {
	    	String ns = Context.NOTIFICATION_SERVICE;

	        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
	        int icon = R.drawable.zhuomian;
	        CharSequence tickerText = "我的通知栏标题";
	        long when = System.currentTimeMillis();
	        Notification notification = new Notification(icon, tickerText, when);
	        Context context = getApplicationContext();

	        CharSequence contentTitle = "我的通知栏标展开标题";

	        CharSequence contentText = "我的通知栏展开详细内容";

	        Intent notificationIntent = new Intent(this, AminationActivity.class);

	        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,

	                notificationIntent, 0);

	        notification.setLatestEventInfo(context, contentTitle, contentText,

	                contentIntent);

	         

	        //用mNotificationManager的notify方法通知用户生成标题栏消息通知

	        mNotificationManager.notify(1, notification);
	        
	    }*/

		
		return super.onKeyUp(keyCode, event);
	}
	
	
	/*
	public class MusicListItemClickListener implements listListenerImpl
	{
		private boolean resume = false;
		@Override
		public void onItemClick(int position) {
			// TODO Auto-generated method stub
		//	play(position);
			if(adapter!=null)
			{
				String url = adapter.getCurrentSelectUrl();
			//	adapter.setClickState(false);
		
				if(url.equals(lastUrl))
				{	
					if(resume == false)
					{
						resume = true;
						isPlaying = false;
						PlayMusicService.pauseMusic();
						pauseOrStart.setImageResource(R.drawable.play_40);
					}
					else 
					{
						resume = false;
						isPlaying = true;
						PlayMusicService.resumeMusic();
						pauseOrStart.setImageResource(R.drawable.stop);
					}
				}
				else
				{
					lastUrl = url;
					resume = false;
					play(url);
				}

			}
		}
		
		
	}
	
	*/
	public void showNotification()
	{
    	String ns = Context.NOTIFICATION_SERVICE;

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
        int icon = R.drawable.zhuomian;
        CharSequence tickerText = "我的通知栏标题";
        long when = System.currentTimeMillis();
        Notification notification = new Notification(icon, tickerText, when);
        Context context = getApplicationContext();

        CharSequence contentTitle = "小黄人音乐播放器";

        CharSequence contentText = "正在播放：";

        //Intent notificationIntent = new Intent(this, AminationActivity.class);
        Intent notificationIntent = new Intent(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.setClass(this, MusicManagerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        Context mContext = getApplicationContext();



        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,

                notificationIntent, 0);

        notification.setLatestEventInfo(context, contentTitle, contentText,

                contentIntent);

         

        //用mNotificationManager的notify方法通知用户生成标题栏消息通知

        mNotificationManager.notify(1, notification);
        
    }
	public void quitnotification()
	{
	    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);   
        nm.cancel(1);
	}
	
	/*此函数用于播放歌曲*/
	public void play(int position )
	{
		//songInfo selectSong = (songInfo) listView.getItemAtPosition(position);
		Log.i("MYTAG","Manager before songinfo getcurrentPosition = "+MusicManagerActivity.currentPosition);
		currentPosition = position;
		songInfo selectSong = (songInfo) musicList.get(currentPosition);
		if(selectSong == null)
			return;

		String playSong = selectSong.getPath()+ selectSong.getName();
		Intent intent = new Intent("com.angel.Android.MUSIC");
		intent.putExtra("selectSong", selectSong.getPath());
		intent.setClass(MusicManagerActivity.this,PlayMusicService.class);
		startService(intent);
		pauseOrStart.setImageResource(R.drawable.pause_button);
		isPlaying = true;
		
		lastUrl = selectSong.getPath();

		PlayingMusicName.setText(selectSong.getName());
		
		Log.i("MYTAG","Manager getcurrentPosition = "+MusicManagerActivity.currentPosition);
		
		Intent intent1= new Intent();
		intent1.setAction(UPDATE_PLAYING_SONG_NAME);
		sendBroadcast(intent1);
		/*
		Intent intent2 = new Intent();
		//intent2.putExtra("currentSongName",((songInfo) listView.getItemAtPosition(currentPosition)).getName());
		intent2.putExtra("currentSongName",selectSong.getName());
		intent2.setAction(SNED_CURRENT_SONG_NAME_ACTION);
		sendBroadcast(intent2);*/
		
		savehistory(selectSong.getId(),selectSong.getName(),selectSong.getPath());
		

	}

	public void play(String url)
	{
		Intent intent = new Intent("com.angel.Android.MUSIC");
		intent.setClass(MusicManagerActivity.this,PlayMusicService.class);
		intent.putExtra("selectSong", url);
		startService(intent);
		pauseOrStart.setImageResource(R.drawable.pause_button);
		isPlaying = true;
		for(int i=0;i<musicList.size(); i++)
		{
			if(url.equals(musicList.get(i).getPath()))
			{
				currentPosition = i;
				break;
			}
		}
		lastUrl = musicList.get(currentPosition).getPath();
		PlayingMusicName.setText(musicList.get(currentPosition).getName());
		
		Log.i("MYTAG","Manager getcurrentPosition = "+MusicManagerActivity.currentPosition);
		
		Intent intent1= new Intent();
		intent1.setAction(UPDATE_PLAYING_SONG_NAME);
		sendBroadcast(intent1);
		
		
		/*
		Intent intent2 = new Intent();
		//intent2.putExtra("currentSongName",((songInfo) listView.getItemAtPosition(currentPosition)).getName());
		intent2.putExtra("currentSongName",musicList.get(currentPosition).getName());
		intent2.setAction(SNED_CURRENT_SONG_NAME_ACTION);
		sendBroadcast(intent2);*/
		savehistory(musicList.get(currentPosition).getId(),musicList.get(currentPosition).getName(),musicList.get(currentPosition).getPath());
	}


	/*此函数用于保存播放历史*/
	public void savehistory(Long ID, String name, String Path)
	{		
  		ContentValues values=new ContentValues();
  		values.put("id", ID);
		values.put("musicname", name);
		values.put("path", Path);
		DBHelper plane=new DBHelper(MusicManagerActivity.this,"PlayHistory");
		SQLiteDatabase db=plane.getWritableDatabase();
		
		Cursor c = db.query("history", null, null, null, null, null, null);
		if (c.moveToFirst()) {
			for (int i = 0; i < c.getCount(); i++) {
				c.moveToPosition(i);
				if(name.equals(c.getString(1)))
				{
					db.delete("history", "musicname=?", new String[]{name});
				}
			}
		}
		db.insert("history", null, values);
		db.close();
	}
	
	/*此函数用于播放前一首歌曲*/
	public void playPrevious()
	{
		if(musicList == null)
			return;
		int p = 0; 
		switch(playMode)
		{
			case NORMAL:
				p = currentPosition - 1;
				break;
			case REPEAT_ONE:
				p = currentPosition;
				break;
			case REPEAT_ALL:
				p =currentPosition -1;
				if(p == -1)
					p = musicList.size()-1;
				break;
			case RANDOM:
				Random random = new Random();
				int randomPosition = (random.nextInt())%(musicList.size());
				if(randomPosition<0)
					randomPosition=0-randomPosition;
				p = randomPosition;
				break;
		
		}
		if(p<0)
		{
			Message msg = new Message();
			msg.obj = "已经是第一首了 !";
			handler.sendMessage(msg);
			return;
			//Toast.makeText(MusicMainActivity.this,"已经是第一首了 !",Toast.LENGTH_SHORT).show();
		}
		else
		{
			currentPosition = p;
			play(p);
		}
	//	adapter.setLastSelectPosition(currentPosition);
		Intent intent= new Intent();
		intent.setAction(UPDATE_FINAL_TIME);
		sendBroadcast(intent);
		Intent intent2 = new Intent();
	//	intent2.putExtra("currentSongName",((songInfo) listView.getItemAtPosition(currentPosition)).getName());
		intent2.putExtra("currentSongName",((songInfo) musicList.get(currentPosition)).getName());
		intent2.setAction(UPDATE_PLAYING_SONG_NAME);
		sendBroadcast(intent2);	
	}
	
	
	/*此函数用于播放下一首歌曲*/
	public void playNext()
	{
		if(musicList == null)
			return;
		int p = 0; 
		switch(playMode)
		{
			case NORMAL:
				p = currentPosition+1;
				break;
			case REPEAT_ONE:
				p = currentPosition;
				break;
			case REPEAT_ALL:
				p =currentPosition+1;
				if(p == musicList.size())
					p = 0;
				break;
			case RANDOM:
				Random random = new Random();
				int randomPosition = (random.nextInt())%(musicList.size());
				if(randomPosition<0)
					randomPosition=0-randomPosition;
				p = randomPosition;
				break;
		
		}
		if(p>musicList.size()-1)
		{
			//Toast.makeText(MusicMainActivity.this,"已经是最后一首了 !",Toast.LENGTH_SHORT).show();
			Message msg = new Message();
			msg.obj = "已经是最后一首了 !";
			handler.sendMessage(msg);
			return;
		}
		else
		{
			currentPosition = p;
			play(p);
		}
	//	adapter.setLastSelectPosition(currentPosition);
		Intent intent= new Intent();
		intent.setAction(UPDATE_FINAL_TIME);
		sendBroadcast(intent);	
		
		Intent intent2 = new Intent();
		//intent2.putExtra("currentSongName",((songInfo) listView.getItemAtPosition(currentPosition)).getName());
		intent2.putExtra("currentSongName",((songInfo) musicList.get(currentPosition)).getName());
		intent2.setAction(UPDATE_PLAYING_SONG_NAME);
		sendBroadcast(intent2);	
	}
	

	/*
	public class musicPlayActivityThread implements Runnable
	{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(true)
			{
				
				if(PlayMusicService.completePlay == true)
				{
					PlayMusicService.completePlay = false;
					switch(playMode)
					{
						case NORMAL:
							playNext();
							break;
						case REPEAT_ONE:
							play(currentPosition);
							break;
						case REPEAT_ALL:
							currentPosition+=1;
							if(currentPosition == musicList.size())
								currentPosition = 0;
							play(currentPosition);
							break;
						case RANDOM:
							Random random = new Random();
							int randomPosition = (random.nextInt())%(musicList.size());
							play(randomPosition);
							break;
					
					}
					
				}
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
	}
	
*/
	public class myHandler extends Handler
	{
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			String str = (String)msg.obj;
			Toast.makeText(MusicManagerActivity.this,str,Toast.LENGTH_SHORT).show();
			//playNext();
		}
		
	}
	
	
	private int startTouchX;
	private int startTouchY;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction()==MotionEvent.ACTION_DOWN)
		{
			startTouchX = (int)event.getX();
			startTouchY = (int)event.getY();
			return true;
		}
		else if(event.getAction()==MotionEvent.ACTION_UP)
		{
			int moveX = (int)event.getX() - startTouchX ;
			int moveY = (int)event.getY() - startTouchY;
			if(Math.abs(moveX)<Math.abs(moveY))
			{
				if(Math.abs(moveY)<20)
					return super.onTouchEvent(event);
				
				if(moveY>0)
					touchMoveDown();
				else
					touchMoveUp();
				
				return true;
				
			}
			else
			{
				if(Math.abs(moveX)<20)
					return super.onTouchEvent(event);
				
				if(moveX>0)
					touchMoveRight();
				else
					touchMoveLeft();
				return true;
			}
		}
		return super.onTouchEvent(event);
	}
	
	private void touchMoveUp()
	{
		//Toast.makeText(PlayingMusicActivity.this,"向上滑动",Toast.LENGTH_SHORT).show();
	}
	private void touchMoveDown()
	{
		//Toast.makeText(PlayingMusicActivity.this,"向下滑动",Toast.LENGTH_SHORT).show();
	}
	private void touchMoveRight()
	{
		//Toast.makeText(PlayingMusicActivity.this,"向右滑动",Toast.LENGTH_SHORT).show();
	}
	private void touchMoveLeft()
	{
		//Toast.makeText(PlayingMusicActivity.this,"向左滑动",Toast.LENGTH_SHORT).show();
	}
	
	
	public class MusicListReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
		//	String broadcastMsg = arg1.getStringExtra("broadcastMsg");
			String myaction = arg1.getAction().toString();
			if(myaction.equals(PLAY_COMPLETE_ACTION))
			{
				switch(playMode)
				{
					case NORMAL:
						playNext();
						break;
					case REPEAT_ONE:
						play(currentPosition);
						break;
					case REPEAT_ALL:
						currentPosition+=1;
						if(currentPosition == musicList.size())
							currentPosition = 0;
						play(currentPosition);
						break;
					case RANDOM:
						Random random = new Random();
						int randomPosition = (random.nextInt())%(musicList.size());
						if(randomPosition<0)
							randomPosition=0-randomPosition;
						play(randomPosition);
						break;
				
				}
			}
			else if(myaction.equals(PLAY_NEXT_ACTION))
			{
				playNext();
			}
			else if(myaction.equals(PLAY_PRE_ACTION))
			{
				playPrevious();
			}
			else if(myaction.equals(PLAY_SEARCH_SONG_ACTION))
			{
				String url = arg1.getStringExtra("select_search_song");
				play(url);
			}
			else if(myaction.equals(PAUSE_PLAY_SONG_ACTION))
			{
				PlayMusicService.pauseMusic();
			}
			else if(myaction.equals(RESUME_PLAY_SONG_ACTION))
			{
				PlayMusicService.resumeMusic();
			}
			else if(myaction.equals(PLAY_CURRENT_POSITION_SONG))
			{
				play(currentPosition);
				Intent intent= new Intent();
				intent.setAction(UPDATE_FINAL_TIME);
				sendBroadcast(intent);
			}
			else if(myaction.equals("android.intent.action.HEADSET_PLUG"))
			{
				if(arg1.hasExtra("state"))
				{
					if(arg1.getIntExtra("state",0) ==0)
					{
						Toast.makeText(arg0, "headset not connected", Toast.LENGTH_LONG).show();
						pauseOrStart.setImageResource(R.drawable.play_button);
						PlayMusicService.pauseMusic();
						isPlaying = false;
						firstTime = false;

					}
					else if(arg1.getIntExtra("state",0)==1)
					{
						pauseOrStart.setImageResource(R.drawable.pause_button);
						if(firstTime == true)
						{
							firstTime = false;
							isPlaying = true;
							play(currentPosition);
							return;
						}
						PlayMusicService.resumeMusic();
						isPlaying = true;
					}
				}
			}
			else if(myaction.equals(SHOW_LRC_ON_IDLE_ACTION))
			{
				String showlrc = arg1.getStringExtra("show_lrc_or_not");
				if(showlrc.equals("on")){
					lrc_show = true;
				}else {
					lrc_show = false;
				}
			}
			else if(myaction.equals(PLAY_STATE_CHANGE_ACTION))
			{
				Boolean playstate = arg1.getBooleanExtra("playstate",false);
				if(playstate){
					pauseOrStart.setImageResource(R.drawable.pause_button);
					if(floatPlayOrStopButton != null)
					    floatPlayOrStopButton.setImageResource(R.drawable.pause_button);
				}else{
					pauseOrStart.setImageResource(R.drawable.play_button);
					if(floatPlayOrStopButton != null)
					    floatPlayOrStopButton.setImageResource(R.drawable.play_button);
				}
				isPlaying = playstate;
				
			}
			
		}

	}


	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		//do nothing
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		int sensorType = event.sensor.getType();
		float[] values = event.values;
		if (shake_settings.equals(SHAKE_SETING_ON) && (Math.abs(values[0]) > 18 || Math.abs(values[1]) > 18 || Math.abs(values[2]) > 18)) {
			playNext();			
		}
	}
	
//	LrcView fLrcView;
	/*
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		WindowManager wm = (WindowManager)getApplicationContext().getSystemService("window");
		if(fLrcView!=null)
		{
			if(fLrcView.getParent()!=null)
				wm.removeView(fLrcView);
		}
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onStop();
    	WindowManager wm = (WindowManager)getApplicationContext().getSystemService("window");
		WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
		//wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
		wmParams.type = 2003;
		wmParams.format = 1;
		wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;;
		wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
	//	wmParams.height =200;
		wmParams.gravity=Gravity.LEFT|Gravity.TOP;   
		wmParams.y = 0;
    	if(fLrcView!=null)
    	{
    		wmParams.height = fLrcView.getTextSize()*3;
    		wm.addView(fLrcView,wmParams);
    		fLrcView.setWmAndParams(wm,wmParams);
    		return;
    	}

    	fLrcView = AminationActivity.getFloatLrcView();
    	if(fLrcView!=null)
    		fLrcView.setLrcContents(PlayMusicService.getLrcList());
    	wmParams.height = fLrcView.getTextSize()*3;
		wm.addView(fLrcView,wmParams);
		fLrcView.setWmAndParams(wm,wmParams);
	}
	*/

}
