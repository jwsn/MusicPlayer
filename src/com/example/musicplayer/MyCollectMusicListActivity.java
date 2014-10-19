package com.example.musicplayer;

import java.util.ArrayList;
import java.util.List;

import com.example.musicplayer.MyLocalMusicListActivity.localMusicListReceiver;
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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class MyCollectMusicListActivity extends Activity {

	private Audio laudio = new LocalAudio(this);
	private List<songInfo> localmusicList;
	private List<songInfo> collectmusicList;
	private ListView listView;
	MyListViewAdapter adapter;
	
	private TextView myColect_song_name_text;
	private TextView myColect_name_text;

	private ImageButton pauseOrStart;
	private ImageButton next;
	private ImageButton previous;
	private ImageButton collectbutton;
	private ImageButton back_button;
	private ImageButton next_page_button;
	private ImageButton palyingButton;
	
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
	
	public int deleteSongPosition;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_collect_music_list_activity);
		myColect_song_name_text = (TextView)findViewById(R.id.myColect_song_name_text);
		//myColect_song_name_text.setText(MusicManagerActivity.getCurrentSongName());//
		myColect_song_name_text.setText(MusicManagerActivity.getMusicList().get(MusicManagerActivity.currentPosition).getName());
		
		Log.i("MYTAG","collect getcurrentPosition = "+MusicManagerActivity.currentPosition);
		
		myColect_name_text = (TextView)findViewById(R.id.mycolect_name_text);
		myColect_name_text.setText("我的收藏歌曲");
	//	localmusicList = laudio.getLocalAudioList();
		localmusicList = AminationActivity.getMyLocalMusicList();
		collectmusicList= new ArrayList<songInfo>();;

	
		final DBHelper helpter=new DBHelper(this,"CollectMusiclist");

		SQLiteDatabase db = helpter.getWritableDatabase();

		
		Cursor c = db.query("mycollect", null, null, null, null, null, null);


		if (c.moveToFirst()) {
			for (int i = c.getCount()-1; i >=0 ; i--) {
				c.moveToPosition(i);
				songInfo audio = new songInfo();
				Boolean inlocallist = false;
				for(int j= 0;j<localmusicList.size();j++){
					if(c.getString(0).equals(localmusicList.get(j).getName())){
						audio.setId(localmusicList.get(j).getId());
						audio.setName(c.getString(0));
						audio.setPath(localmusicList.get(j).getPath());
						collectmusicList.add(audio);
						inlocallist = true;
						break;
					}
				}
				if(!inlocallist){
					db.delete("mycollect", "collectmusicname=?", new String[]{c.getString(0)});
				}
				
			}
		}
		db.close();
		
		/*实现暂停和开始播放功能*/
		pauseOrStart = (ImageButton)findViewById(R.id.mycollect_pause_or_start);
		pauseOrStart.setOnClickListener(new OnClickListener(){
			private boolean firstTime =true;
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
		back_button = (ImageButton)findViewById(R.id.mycollect_backbutton);
		back_button.getBackground().setAlpha(150);
		back_button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				Boolean aa = isPlaying;
				intent.putExtra("isplayingornot",isPlaying);
				setResult(3,intent);
                finish();
			}
				
		});
		
		/*实现移动到播放历史界面的功能*/
		next_page_button = (ImageButton)findViewById(R.id.mycolect_next_page_button);
		//back_button.getBackground().setAlpha(150);
		next_page_button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				touchMoveLeft();
			}
				
		});
		
		/*实现播放下一首歌曲的功能*/
		next = (ImageButton)findViewById(R.id.mycollect_next);
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
		previous = (ImageButton)findViewById(R.id.mycollect_previous);
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
		
		palyingButton = (ImageButton)findViewById(R.id.mycollect_playingbutton);
		palyingButton.setOnTouchListener(new View.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					palyingButton.setImageResource(R.drawable.playing_button_press);
					return true;
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					palyingButton.setImageResource(R.drawable.playing_button);
					Intent intent = new Intent();
					intent.putExtra("isplaying",isPlaying);
					//intent.putExtra("currentSongName",MusicManagerActivity.getCurrentSongName());
					intent.putExtra("currentSongName",MusicManagerActivity.getMusicList().get(MusicManagerActivity.currentPosition).getName());
					intent.setClass(MyCollectMusicListActivity.this, PlayingMusicActivity.class);
					startActivityForResult(intent,3);

					return true;
				}
				return false;
			
			}
			
		});

		View v = findViewById(R.id.mycollect_ButtonLayout);//找到你要设透明背景的layout 的id
		v.getBackground().setAlpha(150);
		
		Intent intent =  getIntent();
		isPlaying = intent.getBooleanExtra("isplaying",false);
		setPlayButton(isPlaying);

		/*获取到歌曲列表，并且放到adapter里面，注册监听器，用于监听是否点击了list的item*/
		listView= (ListView) this.findViewById(R.id.mycollectmusiclistview);  
		adapter = new MyListViewAdapter(this,collectmusicList,Color.BLUE,false);
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
					Log.i("myList","event_down x =, y ="+event.getX()+", / "+event.getY());
					return true;
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
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
							touchMoveUp();
						*/
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
						Log.i("myList","event_move x =, y ="+event.getX()+", / "+event.getY());
					}
				}
				return false;
			}
			
		});
		
		
		IntentFilter filter = new IntentFilter();  
		filter.addAction(UPDATE_PLAYING_SONG_NAME);
		filter.addAction(SNED_CURRENT_SONG_NAME_ACTION);
		filter.addAction(PLAY_STATE_CHANGE_ACTION);
		registerReceiver(new collectMusicListReceiver(),filter);
	//	fLrcView = AminationActivity.getFloatLrcView();
	}
	
	
	public class collectMusicListReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
		//	String broadcastMsg = arg1.getStringExtra("broadcastMsg");
			String myaction = intent.getAction().toString();
			if(myaction.equals(UPDATE_PLAYING_SONG_NAME))
			{
				myColect_song_name_text.setText(MusicManagerActivity.getMusicList().get(MusicManagerActivity.currentPosition).getName());

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
				Bundle b = new Bundle();
				//b.putString("songName",intent.getStringExtra("currentSongName"));
				String currentSongName = intent.getStringExtra("currentSongName");

				myColect_song_name_text.setText(currentSongName);
			}*/
		}
		
	}

	public class MusicListItemClickListener implements listListenerImpl
	{
		@Override
		public void onItemClick(int position) {
			// TODO Auto-generated method stub
		//	play(position);
		//	MusicManagerActivity.setAdapter(adapter,collectmusicList);
		//	SetAdapterListener l = MusicManagerActivity.getAdapterListener();
		//	l.setAdapter(collectmusicList);
			MusicManagerActivity.setMusicList(collectmusicList,position);
			String url = adapter.getCurrentSelectUrl();
			Intent intent= new Intent();
			intent.setAction(PLAY_SEARCH_SONG_ACTION);
			intent.putExtra("select_search_song",url );
			sendBroadcast(intent);
			isPlaying = true;
			myColect_song_name_text.setText(collectmusicList.get(MusicManagerActivity.currentPosition).getName());
			setPlayButton(isPlaying);
		}

		@Override
		public void onItemLongClick(int position) {
			// TODO Auto-generated method stub
			deleteSongPosition = position;
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
		Builder alertDialog=new AlertDialog.Builder(MyCollectMusicListActivity.this);
		alertDialog.setTitle("确认取消收藏该歌曲？");
		//alertDialog.setMessage("是否退出?");
		alertDialog.setPositiveButton("是",new DialogInterface.OnClickListener(){
	
			public void onClick(DialogInterface dialog, int which) {
				
				DeleteCollectSongThread thread = new DeleteCollectSongThread();
				thread.deleteSongName =collectmusicList.get(deleteSongPosition).getName();
				collectmusicList.remove(deleteSongPosition);				
				thread.context = MyCollectMusicListActivity.this;
				thread.start();
				Toast.makeText(MyCollectMusicListActivity.this,"该歌曲取消收藏",Toast.LENGTH_SHORT).show();
				/*
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				adapter.notifyDataSetChanged();
				/*
				
				try {
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Intent intent = new Intent(MyCollectMusicListActivity.this, MyCollectMusicListActivity.class);
				startActivity(intent);                                    
				//close this activity
				finish();
				*/

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
			intent.putExtra("isplayingornot",isPlaying);
			setResult(3,intent);
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
		
		Log.i("myList","*********Activity onTouch***************");
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
		Intent intent = new Intent();
		intent.putExtra("isplaying",isPlaying);
		intent.setClass(MyCollectMusicListActivity.this, MyLocalMusicListActivity.class);
		startActivity(intent);
		int version = Integer.valueOf(android.os.Build.VERSION.SDK); 
		if(version  >= 5) {
			overridePendingTransition(R.anim.zoom_left_to_right_in, R.anim.zoom_left_to_right_out); 
		}
		this.finish();
	}
	private void touchMoveLeft()
	{
		Toast.makeText(this,"向左滑动",Toast.LENGTH_SHORT).show();
		Intent intent = new Intent();
		intent.putExtra("isplaying",isPlaying);
		intent.setClass(MyCollectMusicListActivity.this, PlayHistoryListActivity.class);
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
