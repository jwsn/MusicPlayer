package com.example.musicplayer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.example.musicplayer.MusicManagerActivity.myHandler;
import com.example.musicplayer.lrc.LrcContent;
import com.example.musicplayer.lrc.LrcView;
import com.example.musicplayer.lrc.LrcView.OnLrcClickListener;
import com.example.musicplayer.lrc.LrcView.OnSeekToListener;
import com.example.musicplayer.myAdapter.MyListViewAdapter;
import com.example.musicplayer.myListener.listListenerImpl;
import com.example.musicplayer.myclass.songInfo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PlayingMusicActivity extends Activity {
	private static List<songInfo> musicList;
	MyListViewAdapter adapter;
	private ListView listView;
	private View playingListView;
	
	private Context context;
	
	private boolean playinglistscreen = false;
	private boolean lrcsize = false;
	
	private ImageButton pauseOrStart;
	private ImageButton next;
	private ImageButton previous;
	private ImageButton collectbutton;
	private ImageButton back_button;
	private ImageButton playingListButton;
	private ImageButton lrcsizeButton;
	
	boolean isPlaying = false;
	private int currentDuration;
	private String currentSongName;

	private TextView final_text_time;
	private TextView current_text_time;
	private TextView display_music_name;
	private TextView lrc_size;
	
	private SeekBar mLrcSeekBar;
	private static LrcView mLrcView;
	//LrcView fLrcView;
	private Toast mLrcToast;
	
	private SeekBar audioTrack;
	private myHandler handler;
	private Handler updateBarHandler;
	
	final String PLAY_SEARCH_SONG_ACTION = "com.android.example.select_search_song_play";
	final String PLAY_COMPLETE_ACTION = "current_song_play_complete";
	final String PLAY_NEXT_ACTION ="com.android.musicplayer.playNext";
	final String PLAY_PRE_ACTION ="com.android.musicplayer.playPre";
	final String UPDATE_FINAL_TIME = "com.android.musicplayer.update_final_time";
	final String SNED_CURRENT_SONG_NAME_ACTION = "com.android.musicplayer.sed_song_name";
	final String UPDATE_PLAYING_SONG_NAME = "com.android.musicplayer.update_song_name";
	final String PLAY_CURRENT_POSITION_SONG = "com.android.musicplayer.play_currentP_song";
	final String PLAY_STATE_CHANGE_ACTION = "play_state_change";
	//final String UPDATE_SONG_LRCLIST = "com.android.musicplayer.update_lrc_list";

	final int UPDATE_BAR = 1;
	final int PLAY_COMPLETE = 2;
	final int UPTATE_FINAL_TEXT_TIME = 3;
	final int UPDATE_CURRENT_SONG_NAME = 4;
	final int UPDATE_LRCVIEW = 5;
	final int LISTEN_PLAY_STATE = 6;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playing_music_activity);

		handler = new myHandler();
		
		initViews();
		
		context = this;
		listView= (ListView) this.findViewById(R.id.playingmusiclistview); 
		lrc_size = (TextView)findViewById(R.id.lrc_size_text);
		playingListView = (View) this.findViewById(R.id.playinglistLayout);
		playingListView.getBackground().setAlpha(150);
		musicList = MusicManagerActivity.getMusicList();
		
		/*实现弹出播放列表功能*/
		playingListButton = (ImageButton)findViewById(R.id.playing_list);
		playingListButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(playinglistscreen == true)
				{
					playingListView.setVisibility(View.GONE);
					playinglistscreen = false;
				}
				else
				{
					playingListView.setVisibility(View.VISIBLE);					
					adapter = new MyListViewAdapter(context,musicList,Color.BLUE,false);
					adapter.setOnClickListener(new MusicListItemClickListener());
					listView.setAdapter(adapter);
					playinglistscreen = true;
					mLrcSeekBar.setVisibility(View.GONE);
					lrc_size.setVisibility(View.GONE);
					lrcsize = false;
				}
			}
				
		});
		
		/*实现调整字体大小功能*/
		lrcsizeButton = (ImageButton)findViewById(R.id.lrc_size_button);
		lrcsizeButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(lrcsize == true)
				{
					mLrcSeekBar.setVisibility(View.GONE);
					lrc_size.setVisibility(View.GONE);
					lrcsize = false;
				}
				else
				{
					mLrcSeekBar.setVisibility(View.VISIBLE);
					lrc_size.setVisibility(View.VISIBLE);
					lrcsize = true;
					playingListView.setVisibility(View.GONE);
					playinglistscreen = false;
					mLrcSeekBar.setProgress((int) ((mLrcView.getmCurScalingFactor()-LrcView.MIN_SCALING_FACTOR)
							/(LrcView.MAX_SCALING_FACTOR-LrcView.MIN_SCALING_FACTOR) *100));
				}
			}
				
		});
		
		//song_lrc_text.setText(currentSongUrl);
		/*实现暂停和开始播放功能*/
		pauseOrStart = (ImageButton)findViewById(R.id.playing_pause_or_start);
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
						/*获取歌词*/
						mLrcView.setLrcContents(PlayMusicService.getLrcList());
//						if(fLrcView!=null)
//							fLrcView.setLrcContents(PlayMusicService.getLrcList());
						firstTime = false;
						Intent intent= new Intent();
						intent.setAction(PLAY_CURRENT_POSITION_SONG);
						sendBroadcast(intent);
						isPlaying = true;
						return;
					}
					isPlaying = true;
					PlayMusicService.resumeMusic();
				}
			}
				
		});
		
		/*实现返回播放列表界面的功能*/
		back_button = (ImageButton)findViewById(R.id.backbutton);
		//back_button.getBackground().setAlpha(150);
		back_button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				Boolean aa = isPlaying;
				intent.putExtra("isplayingornot",isPlaying);
				setResult(2,intent);
                finish();
			}
				
		});
		
		/*实现播放下一首歌曲的功能*/
		next = (ImageButton)findViewById(R.id.playing_next);
		next.setOnTouchListener(new View.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					next.setImageResource(R.drawable.next_button_press);
					return true;
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					/*获取歌词*/

					//mLrcView.setLrcContents(PlayMusicService.getLrcList());
					next.setImageResource(R.drawable.next_button);
					Intent intent= new Intent();
					intent.setAction(PLAY_NEXT_ACTION);
					sendBroadcast(intent);	

					return true;
				}
				return false;
			
			}
			
			
		});
		
		/*实现播放上一首歌曲的功能*/
		previous = (ImageButton)findViewById(R.id.playing_previous);
		previous.setOnTouchListener(new View.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					previous.setImageResource(R.drawable.pre_button_press);
					return true;
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					/*获取歌词*/
					//mLrcView.setLrcContents(PlayMusicService.getLrcList());
					
					previous.setImageResource(R.drawable.pre_button);
					Intent intent= new Intent();
					intent.setAction(PLAY_PRE_ACTION);
					sendBroadcast(intent);

					return true;
				}
				return false;
			
			}
			
			
		});
		
		/*实现收藏歌曲的功能*/
		collectbutton = (ImageButton)findViewById(R.id.my_collect);
		collectbutton.setOnTouchListener(new View.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					//collectbutton.setImageResource(R.drawable.collected);
					return true;
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					SaveCollectSongThread thread = new SaveCollectSongThread();
					thread.saveSongName = currentSongName;
					thread.context = PlayingMusicActivity.this;
					thread.start();
					Toast.makeText(PlayingMusicActivity.this,"歌曲收藏成功",Toast.LENGTH_SHORT).show();
					return true;
				}
				return false;
			
			}
			
			
		});
		
		
		Intent intent =  getIntent();
		isPlaying = intent.getBooleanExtra("isplaying",false);
		if(isPlaying)
		{
			pauseOrStart.setImageResource(R.drawable.pause_button);
		}
		else
		{
			pauseOrStart.setImageResource(R.drawable.play_button);
		}
			
		currentSongName = intent.getStringExtra("currentSongName");

		display_music_name = (TextView)findViewById(R.id.music_name_text);
		display_music_name.setText(currentSongName);
		
		currentDuration = PlayMusicService.getDuration();
		final_text_time = (TextView)findViewById(R.id.final_time_text);
		String time = TimeformatChange(currentDuration);
		final_text_time.setText(time);
		
		current_text_time = (TextView)findViewById(R.id.current_time_text);
		current_text_time.setText("0:00");

		
		/*播放界面进度条的控制*/
		audioTrack = (SeekBar)findViewById(R.id.audioTrack);
		audioTrack.setOnSeekBarChangeListener(onSeekBarChangeListener);
		
		/*
		audioTrack.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				//PlayMusicService.seekTo(progress);
	
				mLrcView.seekTo(progress, true,fromUser);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
		});*/
		
		
		/*创建一个handler，用于更新UI的操作*/
		updateBarHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch(msg.what)
				{
					case UPDATE_BAR:
						int position = PlayMusicService.getCurrentPlayPosition();
						int total = PlayMusicService.getDuration();
						int max = audioTrack.getMax();
						if(position>=0 && total!=0)
						{
							audioTrack.setProgress(position*max/total);
						}
						
						String time = TimeformatChange(position);
						current_text_time.setText(time);
					break;
					
					case PLAY_COMPLETE:
						currentDuration = PlayMusicService.getDuration();
						String time1 = TimeformatChange(currentDuration);
						final_text_time.setText(time1);
						mLrcView.setLrcContents(PlayMusicService.getLrcList());
//						if(fLrcView!=null)
//							fLrcView.setLrcContents(PlayMusicService.getLrcList());
						break;
						
					case UPTATE_FINAL_TEXT_TIME:
						currentDuration = PlayMusicService.getDuration();
						String time_final = TimeformatChange(currentDuration);
						final_text_time.setText(time_final);
						break;
					case UPDATE_CURRENT_SONG_NAME:
						Bundle b = msg.getData();
						String name = b.getString("songName");
						//song_lrc_text.setText(name);
						break;
					case UPDATE_LRCVIEW:
						if(isPlaying && PlayMusicService.isPause == false)
						{
							//mLrcView.setLrcContents(PlayMusicService.getLrcList());
							//song_lrc_text.setLrcList(PlayMusicService.getLrcList());
							//song_lrc_text.setIndex(PlayMusicService.lrcIndex());
							//song_lrc_text.invalidate();
							//mLrcView.setIndex(PlayMusicService.lrcIndex());
						//	mLrcView.setLrcContents(PlayMusicService.getLrcList());
//							if(fLrcView!=null)
//								fLrcView.seekTo(PlayMusicService.getCurrentPlayPosition(), true,false);
							mLrcView.seekTo(PlayMusicService.getCurrentPlayPosition(), true,false);
							
							
						}
						break;
					case LISTEN_PLAY_STATE:
						if(isPlaying){
							
							//listenLrcHandler();
						}
						break;
					default:
						break;
					
				}
			}
			
		};
		
		//View v = findViewById(R.id.playingButtonLayout);//找到你要设透明背景的layout 的id
		//v.getBackground().setAlpha(100);
		
		updateBarThread ubt = new updateBarThread();
		(new Thread(ubt)).start();
		
		/*广播的动态注册*/
		IntentFilter filter = new IntentFilter();  
		filter.addAction(PLAY_COMPLETE_ACTION);
		filter.addAction(UPDATE_FINAL_TIME);
		filter.addAction(SNED_CURRENT_SONG_NAME_ACTION);
		filter.addAction(UPDATE_PLAYING_SONG_NAME);
		filter.addAction(PLAY_STATE_CHANGE_ACTION);
		//filter.addAction(UPDATE_SONG_LRCLIST);
		registerReceiver(new PlayingMusicReceiver(),filter); 
		
		
//		fLrcView = AminationActivity.getFloatLrcView();
	}
	
	
	public static void setmLrcViewList(List<LrcContent> lrcList)
	{
		if(mLrcView!=null)
			mLrcView.setLrcContents(lrcList);
	}
	public class MusicListItemClickListener implements listListenerImpl
	{
		@Override
		public void onItemClick(int position) {
			// TODO Auto-generated method stub

			String url = adapter.getCurrentSelectUrl();
			Intent intent= new Intent();
			intent.setAction(PLAY_SEARCH_SONG_ACTION);
			intent.putExtra("select_search_song",url );
			sendBroadcast(intent);
			isPlaying = true;
			pauseOrStart.setImageResource(R.drawable.pause_button);
			if(mLrcView!=null)
				mLrcView.setLrcContents(PlayMusicService.getLrcList());
//			if(fLrcView!=null)
//				fLrcView.setLrcContents(PlayMusicService.getLrcList());

		}

		@Override
		public void onItemLongClick(int position) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTouchUp() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTouchDown() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTouchRight() {
			// TODO Auto-generated method stub
			touchMoveRight();
		}

		@Override
		public void onTouchLeft() {
			// TODO Auto-generated method stub
			touchMoveLeft();
		}
		
	}
	/*
	public class CollectSongThread extends Thread
	{
		public String saveSongName;
		@Override
		public void run() {
			// TODO Auto-generated method stub
			savehistory(saveSongName);
		}
		
	}*/
	
	private void initViews() {
		// TODO Auto-generated method stub
		mLrcView = (LrcView)findViewById(R.id.song_lrc_text);
		mLrcView.setOnSeekToListener(onSeekToListener);
		mLrcView.setOnLrcClickListener(onLrcClickListener);
		/*获取歌词*/
		mLrcView.setLrcContents(PlayMusicService.getLrcList());
		mLrcSeekBar = (SeekBar) findViewById(R.id.include_lrc_seekbar);
		mLrcSeekBar.setMax(100);
		mLrcSeekBar.setProgress((int) ((mLrcView.getmCurScalingFactor()-LrcView.MIN_SCALING_FACTOR)
				/(LrcView.MAX_SCALING_FACTOR-LrcView.MIN_SCALING_FACTOR) *100));
		mLrcSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		
		final DBHelper data=new DBHelper(this);
	    SQLiteDatabase db=data.getReadableDatabase();
	    Cursor c = db.query("musicwordfontsize", null, null, null, null, null, null);
	    String scale="0.5";
	    while(c.moveToNext())
	    {
	    	 scale = c.getString(0);
	    } 
    	Log.i("fontscale:","get scale value is"+scale);
    	float s = Float.parseFloat(scale);
    	mLrcView.setLrcScalingFactor(s);
    	
    	


	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Intent intent= new Intent();
		intent.setAction(PLAY_STATE_CHANGE_ACTION);
		intent.putExtra("playstate",isPlaying);
		sendBroadcast(intent);
	}
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
	/*
	@Override
	public boolean moveTaskToBack(boolean nonRoot) {
		// TODO Auto-generated method stub
    	fLrcView = new LrcView(getApplicationContext());
    	fLrcView.setLrcContents(PlayMusicService.getLrcList());
    	//fLrcView.setLrcScalingFactor(s);
    	fLrcView.setOnSeekToListener(onSeekToListener);
    	fLrcView.isFloatWindow = true;
    	fLrcView.setmCurColorForHightLightLrc(Color.BLACK);
    	fLrcView.setDisableTouch(true);
    	WindowManager wm = (WindowManager)getApplicationContext().getSystemService("window");
		WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
		wmParams.type = 2002;
		wmParams.format = 1;
		wmParams.flags = 40;
		wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.height = 400;
		wmParams.x = 10;
		//wmParams.y = 200;
		wm.addView(fLrcView,wmParams);
		return super.moveTaskToBack(nonRoot);
	}*/

/*

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
    	WindowManager wm = (WindowManager)getApplicationContext().getSystemService("window");
		WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
	//	wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
		wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
		wmParams.format = 1;
		wmParams.flags = 40;
		wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.height =400;
    	if(fLrcView!=null)
    	{
    		wm.addView(fLrcView,wmParams);
    		fLrcView.setWmAndParams(wm,wmParams);
    		return;
    	}

    	fLrcView = AminationActivity.getFloatLrcView();
    	if(fLrcView!=null)
    		fLrcView.setLrcContents(PlayMusicService.getLrcList());
		wm.addView(fLrcView,wmParams);
		fLrcView.setWmAndParams(wm,wmParams);
	}
	
	


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		WindowManager wm = (WindowManager)getApplicationContext().getSystemService("window");
		if(fLrcView!=null)
		wm.removeView(fLrcView);
		
	}
*/








	OnLrcClickListener onLrcClickListener = new OnLrcClickListener() {
		@Override
		public void onClick() {
			//Toast.makeText(getApplicationContext(), "姝岃瘝琚偣鍑诲暒", Toast.LENGTH_SHORT).show();
		}
	};
	
	OnSeekToListener onSeekToListener = new OnSeekToListener() {

		@Override
		public void onSeekTo(int progress) {
			PlayMusicService.seekTo(progress);
			
		}
	};
	
	OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
		
			if(seekBar == audioTrack){
				
				int max = audioTrack.getMax();
				int total = PlayMusicService.getDuration();
				
				if(fromUser)
				{
					if(progress>=0 && total!=0)
					{
						PlayMusicService.seekTo(progress*total/max);
					}
				}
				/*歌词平滑向上滑动*/
				
				if(fromUser)
				{
					mLrcView.seekTo(progress*total/max, true,fromUser);
//					if(fLrcView!=null)
//					fLrcView.seekTo(progress*total/max, true,fromUser);
				}
				
				
			}else if(seekBar == mLrcSeekBar){
				
				float scalingFactor = LrcView.MIN_SCALING_FACTOR + progress*(LrcView.MAX_SCALING_FACTOR-LrcView.MIN_SCALING_FACTOR)/100;
				mLrcView.setLrcScalingFactor(scalingFactor);
//				if(fLrcView!=null)
//					fLrcView.setLrcScalingFactor(scalingFactor);
				ContentValues values=new ContentValues();
	    		values.put("fontSize",scalingFactor+"");
	    		DBHelper data=new DBHelper(PlayingMusicActivity.this);
	    		SQLiteDatabase db=data.getWritableDatabase();
	    		db.insert("musicwordfontsize", null, values);
				//showLrcToast((int)(scalingFactor*100)+"%");
	    		Log.i("fontscale:","save scale value is"+scalingFactor);
			}
		}

	};
	
	/**
	 * 将播放进度的毫米数转换成时间格式
	 * 如 3000 --> 00:03 
	 * @param progress
	 * @return
	 */
	private String formatTimeFromProgress(int progress){
		//总的秒数 
		int msecTotal = progress/1000;
		int min = msecTotal/60;
		int msec = msecTotal%60;
		String minStr = min < 10 ? "0"+min:""+min;
		String msecStr = msec < 10 ? "0"+msec:""+msec;
		return minStr+":"+msecStr;
	}
	
	/*
	private List<LrcContent> getLrcRows(){
		List<LrcContent> rows = null;
		InputStream is = getResources().openRawResource(R.raw.hs);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line ;
		StringBuffer sb = new StringBuffer();
		try {
			while((line = br.readLine()) != null){
				sb.append(line+"\n");
			}
			System.out.println(sb.toString());
			rows = DefaultLrcParser.getIstance().getLrcRows(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rows;
	}
	
	private TextView mPlayerToastTv;
	private void showPlayerToast(String text){
		if(mPlayerToast == null){
			mPlayerToast = new Toast(this);
			mPlayerToastTv = (TextView) LayoutInflater.from(this).inflate(R.layout.toast, null);
			mPlayerToast.setView(mPlayerToastTv);
			mPlayerToast.setDuration(Toast.LENGTH_SHORT);
		}
		mPlayerToastTv.setText(text);
		mPlayerToast.show();
	}*/
	
	/*
	private TextView mLrcToastTv;
	private void showLrcToast(String text){
		if(mLrcToast == null){
			mLrcToast = new Toast(this);
			mLrcToastTv = (TextView) LayoutInflater.from(this).inflate(R.layout.toast, null);
			mLrcToast.setView(mLrcToastTv);
			mLrcToast.setDuration(Toast.LENGTH_SHORT);
		}
		mLrcToastTv.setText(text);
		mLrcToast.show();
	}*/
	/*此函数用于保存播放历史*/
	/*
	public void savehistory(String name)
	{		
  		ContentValues values=new ContentValues();
		values.put("collectmusicname", name);
		
		DBHelper plane=new DBHelper(PlayingMusicActivity.this,"CollectMusiclist");
		SQLiteDatabase db=plane.getWritableDatabase();
		
		Cursor c = db.query("mycollect", null, null, null, null, null, null);
		if (c.moveToFirst()) {
			for (int i = 0; i < c.getCount(); i++) {
				c.moveToPosition(i);
				if(name.equals(c.getString(0)))
				{
					Message msg = new Message();
					msg.obj = "该歌曲已在收藏列表 !";
					handler.sendMessage(msg);
					return;
				}
			}
		}
		Message msg = new Message();
		msg.obj = "歌曲收藏成功 !";
		handler.sendMessage(msg);
		
		db.insert("mycollect", null, values);
		db.close();
	}
	*/
	/*此函数用于转换时间格式，因为我们获取到的时间是毫秒，这个函数可以把毫秒转换成分+秒的格式，即02:23这种*/
	public static String TimeformatChange(int msec)
	{
		String min = msec/(1000*60) +"";
		String sec = msec%(1000*60)+"";
		if(min.length()<2)
			min = "0"+msec/(1000*60);
		
		if (sec.length() == 4)  
			sec = "0" + (msec % (1000 * 60)) + "";  
	    else if (sec.length() == 3)  
	    	sec = "00" + (msec % (1000 * 60)) + "";  
	    else if (sec.length() == 2)   
	    	sec = "000" + (msec % (1000 * 60)) + "";  
		else if (sec.length() == 1)   
	        sec = "0000" + (msec % (1000 * 60)) + "";  
	   
		return min+":"+sec.trim().substring(0, 2);
		
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
		Toast.makeText(this,"向右滑动",Toast.LENGTH_SHORT).show();
		this.finish();
	}
	private void touchMoveLeft()
	{
		Toast.makeText(this,"向左滑动",Toast.LENGTH_SHORT).show();
		this.finish();
	}
	
	/*这个线程用于更新进度条*/
	public class updateBarThread implements Runnable
	{

		@Override
		public void run() {

			//if(isPlaying == true){
				//Message m = new Message();
				//m.what = LISTEN_PLAY_STATE;
				//updateBarHandler.sendMessage(m);
				
				//Message g = new Message();
				//g.what = UPDATE_LRCVIEW;
				//updateBarHandler.sendMessage(g);
			//}
			
			// TODO Auto-generated method stub
			while(MusicManagerActivity.systemExit == false)
			{
				if(updateBarHandler!=null)
				{
					Message msg = new Message();
					msg.what = UPDATE_BAR;
					updateBarHandler.sendMessage(msg);
				}
			
				
				if(isPlaying == true){
					
					Message g = new Message();
					g.what = UPDATE_LRCVIEW;
					updateBarHandler.sendMessage(g);

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
	
	
	/*用于接收广播的receiver*/
	public class PlayingMusicReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String myaction = intent.getAction().toString();
			if(myaction.equals(PLAY_COMPLETE_ACTION))
			{
				if(updateBarHandler!=null)
				{
					Message msg = new Message();
					msg.what = PLAY_COMPLETE;
					updateBarHandler.sendMessage(msg);
				}
			}
			
			else if(myaction.equals(UPDATE_FINAL_TIME))
			{
				if(updateBarHandler!=null)
				{
					Message msg = new Message();
					msg.what = UPTATE_FINAL_TEXT_TIME;
					updateBarHandler.sendMessage(msg);
				}
			}
			else if(myaction.equals(UPDATE_PLAYING_SONG_NAME))
			{
				//String songName = intent.getStringExtra("currentSongName");
				currentSongName = MusicManagerActivity.getMusicList().get(MusicManagerActivity.currentPosition).getName();
				//Bundle b = new Bundle();
				//b.putString("songName",currentSongName);
				//currentSongName = intent.getStringExtra("currentSongName");
				
				display_music_name.setText(currentSongName);
				if(mLrcView!=null)
					mLrcView.setLrcContents(PlayMusicService.getLrcList());
//				if(fLrcView!=null)
//					fLrcView.setLrcContents(PlayMusicService.getLrcList());
				
				//Message msg = new Message();
				//msg.setData(b);
				//msg.what = UPDATE_CURRENT_SONG_NAME;
				//updateBarHandler.sendMessage(msg);
			}
			else if(myaction.equals(PLAY_STATE_CHANGE_ACTION))
			{
				Boolean playstate = intent.getBooleanExtra("playstate",false);
				if(playstate){
					pauseOrStart.setImageResource(R.drawable.pause_button);
				}else{
					pauseOrStart.setImageResource(R.drawable.play_button);
				}
				isPlaying = playstate;
				
			}
			/*
			else if(myaction.equals(UPDATE_SONG_LRCLIST))
			{
				if(mLrcView!=null)
					mLrcView.setLrcContents(PlayMusicService.getLrcList());
			}*/
		}
		
	}
	/*显示提醒的handler*/
	public class myHandler extends Handler
	{
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			String str = (String)msg.obj;
			Toast.makeText(PlayingMusicActivity.this,str,Toast.LENGTH_SHORT).show();
			//playNext();
		}
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode,KeyEvent event){
		
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(playinglistscreen == true)
			{
				playingListView.setVisibility(View.GONE);
				playinglistscreen = false;
			}else if(lrcsize == true)
			{
				mLrcSeekBar.setVisibility(View.GONE);
				lrc_size.setVisibility(View.GONE);
				lrcsize = false;
			}else{
				Intent intent = new Intent();
				Boolean aa = isPlaying;
				intent.putExtra("isplayingornot",isPlaying);
				setResult(2,intent);
				finish();
			}
			return true;
		}
		else
		{
			return super.onKeyUp(keyCode, event);
		}
	}
	/*public void listenLrcHandler(){
		new Thread(new Runnable(){
			
			@Override
			public void run(){
				while(true)
				{
					if(updateBarHandler!=null)
					{
						Message msg = new Message();
						msg.what = UPDATE_LRCVIEW;
						updateBarHandler.sendMessage(msg);
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
						// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				//handler.postDelay(mRunnable, 100);
			}
		}).start();
	}*/
	
}
