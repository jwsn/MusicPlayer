package com.example.musicplayer;

import java.util.List;

import com.example.musicplayer.MusicManagerActivity.MusicListReceiver;
import com.example.musicplayer.interfa.Audio;
import com.example.musicplayer.lrc.LrcView;
import com.example.musicplayer.myAdapter.MyListViewAdapter;
import com.example.musicplayer.myListener.SetAdapterListener;
import com.example.musicplayer.myListener.listListenerImpl;
import com.example.musicplayer.myclass.songInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class MyLocalMusicListActivity extends Activity {

	private Audio laudio = new LocalAudio(this);
	private List<songInfo> localmusicList;

	private ImageButton pauseOrStart;
	private ImageButton next;
	private ImageButton previous;
	private ImageButton collectbutton;
	private ImageButton back_button;
	private ImageButton playingButton;
	private ImageButton next_page_button;
	private TextView mylocal_song_name_text;
	private TextView mylocal_name_text;
	//private int currentPosition=0;
	private int saveSongPosition;
	private ListView listView;
	MyListViewAdapter adapter;
	final String TAG = "MUSIC";

	final String PLAY_SEARCH_SONG_ACTION = "com.android.example.select_search_song_play";
	final String SNED_CURRENT_SONG_NAME_ACTION = "com.android.musicplayer.sed_song_name";
	final String PLAY_COMPLETE_ACTION = "current_song_play_complete";
	final String PLAY_NEXT_ACTION ="com.android.musicplayer.playNext";
	final String PLAY_PRE_ACTION ="com.android.musicplayer.playPre";
	final String UPDATE_FINAL_TIME = "com.android.musicplayer.update_final_time";
	final String UPDATE_PLAYING_SONG_NAME = "com.android.musicplayer.update_song_name";
	final String PLAY_CURRENT_POSITION_SONG = "com.android.musicplayer.play_currentP_song";
	final String PLAY_STATE_CHANGE_ACTION = "play_state_change";
	boolean isPlaying = true;
	enum PlayMode{NORMAL,REPEAT_ONE,REPEAT_ALL,RANDOM};
	PlayMode playMode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_local_music_list_activity);
		
		localmusicList = AminationActivity.getMyLocalMusicList();
		
		mylocal_song_name_text = (TextView)findViewById(R.id.mylocal_song_name_text);
		//mylocal_song_name_text.setText(MusicManagerActivity.getCurrentSongName());
		mylocal_song_name_text.setText(MusicManagerActivity.getMusicList().get(MusicManagerActivity.currentPosition).getName());
		mylocal_name_text = (TextView)findViewById(R.id.mylocal_name_text);
		mylocal_name_text.setText("我的本地歌曲");
		/*实现暂停和开始播放功能*/
		pauseOrStart = (ImageButton)findViewById(R.id.mylocal_pause_or_start);
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
				}
				else
				{
					pauseOrStart.setImageResource(R.drawable.pause_button);
					if(firstTime == true)
					{
						firstTime = false;
						Intent intent= new Intent();
						intent.setAction(PLAY_CURRENT_POSITION_SONG);
						sendBroadcast(intent);
						isPlaying = true;
						return;
					}
					PlayMusicService.resumeMusic();
					isPlaying = true;
				}
			}
				
		});
		/*实现返回播放列表界面的功能*/
		back_button = (ImageButton)findViewById(R.id.mylocal_backbutton);
		back_button.getBackground().setAlpha(150);
		back_button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				Boolean aa = isPlaying;
				intent.putExtra("isplayingornot",isPlaying);
				setResult(1,intent);
                finish();
			}
				
		});
		
		/*实现移动到我的收藏界面的功能*/
		next_page_button = (ImageButton)findViewById(R.id.mylocal_next_page_button);
		//back_button.getBackground().setAlpha(150);
		next_page_button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				touchMoveLeft();
			}
				
		});
		
		/*实现播放下一首歌曲的功能*/
		next = (ImageButton)findViewById(R.id.mylocal_next);
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
		previous = (ImageButton)findViewById(R.id.mylocal_previous);
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
					previous.setImageResource(R.drawable.pre_button);
					Intent intent= new Intent();
					intent.setAction(PLAY_PRE_ACTION);
					sendBroadcast(intent);
					return true;
				}
				return false;
			
			}
			
			
		});
		
		
		/*实现跳转到播放界面的功能*/
		playingButton = (ImageButton)findViewById(R.id.mylocal_playingbutton);
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
					//intent.putExtra("currentSongName",MusicManagerActivity.getCurrentSongName());//MusicManagerActivity.getMusicList().get(MusicManagerActivity.currentPosition).getName()
					intent.putExtra("currentSongName",MusicManagerActivity.getMusicList().get(MusicManagerActivity.currentPosition).getName());
					intent.setClass(MyLocalMusicListActivity.this, PlayingMusicActivity.class);
					startActivityForResult(intent,3);

					return true;
				}
				return false;
			
			}
			
			
		});
		

		View v = findViewById(R.id.mylocal_ButtonLayout);//找到你要设透明背景的layout 的id
		v.getBackground().setAlpha(150);
		
		Intent intent =  getIntent();
		isPlaying = intent.getBooleanExtra("isplaying",false);
		setPlayButton(isPlaying);

		/*获取到歌曲列表，并且放到adapter里面，注册监听器，用于监听是否点击了list的item*/
		listView= (ListView) this.findViewById(R.id.mylocalmusiclistview);  
		adapter = new MyListViewAdapter(this,localmusicList,Color.BLUE,false);
		adapter.setOnClickListener(new MusicListItemClickListener());
		listView.setAdapter(adapter); 
		
		listView.setOnTouchListener(new OnTouchListener(){
			
			
			boolean thisMove;
			private int start_x;
			private int start_y;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					start_x = (int)event.getX();
					start_y = (int)event.getY();
					Log.i("myList","listView Down");
					
					return false;
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					thisMove = false;
					Log.i("myList","listView UP");
					int moveX = (int)event.getX() - start_x ;
					int moveY = (int)event.getY() - start_y;
					if(Math.abs(moveX)<Math.abs(moveY))
					{
						/*
						if(Math.abs(moveY)<20)
							return false;
						
						if(moveY>0)
							touchMoveDown();
						else
							touchMoveUp();*/
						
						return false;
						
					}
					else
					{
						if(Math.abs(moveX)<20)
							return false;
						
						if(moveX>0)
							touchMoveRight();
						else
							touchMoveLeft();
						return true;
					}
				}
				
				else if(event.getAction()==MotionEvent.ACTION_MOVE)
				{
					if(thisMove == false)
					{
						thisMove = true;
						start_x = (int)event.getX();
						start_y = (int)event.getY();
						Log.i("myList","listView MOVE");
					}
				}
				return false;
			}
			
		});
		
		
		/*
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				
			}
			
		});	*/
		
		IntentFilter filter = new IntentFilter();  
		filter.addAction(UPDATE_PLAYING_SONG_NAME);
		filter.addAction(PLAY_STATE_CHANGE_ACTION);

		registerReceiver(new localMusicListReceiver(),filter);
		
	//	fLrcView = AminationActivity.getFloatLrcView();

	}
	
	
	public class localMusicListReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
		//	String broadcastMsg = arg1.getStringExtra("broadcastMsg");
			String myaction = intent.getAction().toString();
			if(myaction.equals(UPDATE_PLAYING_SONG_NAME))
			{
				//mylocal_song_name_text.setText(localmusicList.get(MusicManagerActivity.currentPosition).getName());
				mylocal_song_name_text.setText(MusicManagerActivity.getMusicList().get(MusicManagerActivity.currentPosition).getName());
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
			else if(myaction.equals(SNED_CURRENT_SONG_NAME_ACTION))
			{
				//String songName = intent.getStringExtra("currentSongName");
				//Bundle b = new Bundle();
				//b.putString("songName",intent.getStringExtra("currentSongName"));
				String currentSongName = intent.getStringExtra("currentSongName");

				mylocal_song_name_text.setText(currentSongName);
			}*/
		}
		
	}
	
	public class MusicListItemClickListener implements listListenerImpl
	{
		@Override
		public void onItemClick(int position) {
			// TODO Auto-generated method stub
		//	play(position);
		//	MusicManagerActivity.setAdapter(adapter,localmusicList);
		//	SetAdapterListener l = MusicManagerActivity.getAdapterListener();
		//	l.setAdapter(localmusicList);
			MusicManagerActivity.setMusicList(localmusicList,position);
			String url = adapter.getCurrentSelectUrl();
			Intent intent= new Intent();
			intent.setAction(PLAY_SEARCH_SONG_ACTION);
			intent.putExtra("select_search_song",url );
			sendBroadcast(intent);
			isPlaying = true;
			MusicManagerActivity.currentPosition = position;
			setPlayButton(true);
			
			mylocal_song_name_text.setText(localmusicList.get(MusicManagerActivity.currentPosition).getName());

			//finish();
		}

		
		@Override
		public void onItemLongClick(int position) {
			// TODO Auto-generated method stub
			saveSongPosition = position;
			showConfirmExitGameDialog();
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
	
	
	public void showConfirmExitGameDialog()
	{
		Builder alertDialog=new AlertDialog.Builder(MyLocalMusicListActivity.this);
		alertDialog.setTitle("确认收藏该歌曲？");
		//alertDialog.setMessage("是否退出?");
		alertDialog.setPositiveButton("是",new DialogInterface.OnClickListener(){
	
			public void onClick(DialogInterface dialog, int which) {
				
				SaveCollectSongThread thread = new SaveCollectSongThread();
				thread.saveSongName =localmusicList.get(saveSongPosition).getName();
				thread.context = MyLocalMusicListActivity.this;
				thread.start();
				Toast.makeText(MyLocalMusicListActivity.this,"歌曲收藏成功",Toast.LENGTH_SHORT).show();
			}
			
		});
		alertDialog.setNegativeButton("否",new DialogInterface.OnClickListener(){
	
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//finish();

			}
			  
		});
		alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener(){

			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				// TODO Auto-generated method stub
				switch(keyCode)
				{
				case KeyEvent.KEYCODE_BACK:
					finish();
					break;
				default:
					break;
				
				}
				return false;
			}
			
		}
		);
		alertDialog.show();
		
	}
	
/*	
	
	public class MusicListItemLongClickListener implements OnItemLongClickListener
	{

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			// TODO Auto-generated method stub
			
			Toast.makeText(MyLocalMusicListActivity.this,"监听到长按事件",Toast.LENGTH_SHORT).show();
			return false;
		}
		
	}
	
	*/


	/*此函数用于设置播放按钮*/
	public void setPlayButton(Boolean isplaying)
	{
		if(isplaying)
		{
			pauseOrStart.setImageResource(R.drawable.pause_button);
		}
		else
		{
			pauseOrStart.setImageResource(R.drawable.play_button);
		}
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
		
		}
		
		
	}
		
	
	@Override
	public boolean onKeyDown(int keyCode,KeyEvent event){
		
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Intent intent = new Intent();
			Boolean aa = isPlaying;
			intent.putExtra("isplayingornot",isPlaying);
			setResult(1,intent);
			finish();
			return true;
		}
		else
		{
			return super.onKeyUp(keyCode, event);
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

		this.finish();

	}
	private void touchMoveLeft()
	{
		Toast.makeText(this,"向左滑动",Toast.LENGTH_SHORT).show();
		Intent intent = new Intent();
		intent.putExtra("isplaying",isPlaying);
		intent.setClass(MyLocalMusicListActivity.this, MyCollectMusicListActivity.class);
		startActivity(intent);
		int version = Integer.valueOf(android.os.Build.VERSION.SDK); 
		if(version  >= 5) {
			overridePendingTransition(R.anim.zoom_right_to_left_in, R.anim.zoom_right_to_left_out); 
		}
		this.finish();
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
	
	//LrcView fLrcView;
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
