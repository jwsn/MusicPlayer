package com.example.musicplayer;

import java.util.ArrayList;
import java.util.List;
import com.example.musicplayer.interfa.Audio;
import com.example.musicplayer.lrc.LrcView;
import com.example.musicplayer.myAdapter.MyListViewAdapter;
import com.example.musicplayer.myListener.listListenerImpl;
import com.example.musicplayer.myclass.songInfo;


import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class PlayHistoryListActivity extends Activity {

	private Audio laudio = new LocalAudio(this);
	private List<songInfo> musichistoryList;
	private List<songInfo> musicList;

	private ListView listView;
	final String TAG = "PlayHistory";
	
	private ImageButton back_button;
	private ImageButton next_page_button;
	
	boolean isPlaying = true;
	private myHandler handler;
	MyListViewAdapter adapter;
	final String PLAY_SEARCH_SONG_ACTION = "com.android.example.select_search_song_play";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play_history_list_activity);
		
		Intent intent =  getIntent();
		isPlaying = intent.getBooleanExtra("isplaying",false);
		
		
		musicList = laudio.getLocalAudioList();
		musichistoryList = new ArrayList<songInfo>();
		
		final DBHelper helpter=new DBHelper(this,"PlayHistory");

		SQLiteDatabase db = helpter.getWritableDatabase();

		
		Cursor c = db.query("history", null, null, null, null, null, null);
		if (c.moveToFirst()) {
			for (int i = c.getCount()-1; i >=0 ; i--) {
				c.moveToPosition(i);
				songInfo audio = new songInfo();
				Boolean inlocallist = false;
				for(int j= 0;j<musicList.size();j++){
					if(c.getString(1).equals(musicList.get(j).getName())){
						audio.setId(c.getLong(0));
						audio.setName(c.getString(1));
						audio.setPath(c.getString(2));
						musichistoryList.add(audio);
						inlocallist = true;
						break;
					}
				}
				if(!inlocallist){
					db.delete("history", "musicname=?", new String[]{c.getString(1)});
				}
				
			}
		}
		
		/*if (c.moveToFirst()) {
			for (int i = c.getCount()-1; i >=0 ; i--) {
				c.moveToPosition(i);
				songInfo audio = new songInfo();
				audio.setId(c.getLong(0));
				audio.setName(c.getString(1));
				audio.setPath(c.getString(2));
				musichistoryList.add(audio);
			}
		}*/
		db.close();
        
		/*实现返回播放列表界面的功能*/
		back_button = (ImageButton)findViewById(R.id.myhistory_backbutton);
		back_button.getBackground().setAlpha(150);
		back_button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
                finish();
			}
				
		});
		
		/*实现移动到搜索界面的功能*/
		next_page_button = (ImageButton)findViewById(R.id.myhistory_next_page_button);
		//back_button.getBackground().setAlpha(150);
		next_page_button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				touchMoveLeft();
			}

		});
		
		/*获取到歌曲列表，并且放到adapter里面，注册监听器，用于监听是否点击了list的item*/
		listView= (ListView) this.findViewById(R.id.historylistview);  
		adapter = new MyListViewAdapter(this,musichistoryList,Color.BLUE,false);
		adapter.setOnClickListener(new HistoryListItemClickListener());

		listView.setAdapter(adapter);

		handler = new myHandler();
		

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
		
		//fLrcView = AminationActivity.getFloatLrcView();
		
	}
	
	public class HistoryListItemClickListener implements listListenerImpl
	{
		@Override
		public void onItemClick(int position) {
			// TODO Auto-generated method stub
			if(isInMusiListOrNot(position)){
				//MusicManagerActivity.setAdapter(adapter,musichistoryList);


				Intent intent1 = new Intent();
				isPlaying = true;
				intent1.putExtra("isplaying",isPlaying);
				intent1.putExtra("currentSongName",((songInfo) listView.getItemAtPosition(position)).getName());
				intent1.setClass(PlayHistoryListActivity.this, PlayingMusicActivity.class);
				startActivity(intent1);
				
				String url = adapter.getCurrentSelectUrl();
				Intent intent= new Intent();
				intent.setAction(PLAY_SEARCH_SONG_ACTION);
				intent.putExtra("select_search_song",url );
				sendBroadcast(intent);
				finish();
			}
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
			
		}

		@Override
		public void onTouchLeft() {
			// TODO Auto-generated method stub
			
		}
	}
	
	/*判断歌曲是否已经被移除播放列表*/
	public Boolean isInMusiListOrNot(int position)
	{
		Boolean inlist = false;
		for(int j = 0;j<musicList.size();j++){
			if(musicList.get(j).getName().equals(musichistoryList.get(position).getName()))
			{
				inlist = true;
				break;
			}
        }
        if(!inlist){ 
    		Message msg = new Message();
    		msg.obj = "歌曲已不在播放列表中 !";
    		handler.sendMessage(msg);
        }
		return inlist;
	}

	public class myHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			String str = (String)msg.obj;
			Toast.makeText(PlayHistoryListActivity.this,str,Toast.LENGTH_SHORT).show();
			//playNext();
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
		intent.setClass(PlayHistoryListActivity.this,  MyCollectMusicListActivity.class);
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
		intent.setClass(PlayHistoryListActivity.this, SearchSongActivity.class);
		startActivity(intent);
		int version = Integer.valueOf(android.os.Build.VERSION.SDK); 
		if(version  >= 5) {
			overridePendingTransition(R.anim.zoom_right_to_left_in, R.anim.zoom_right_to_left_out); 
		}
		this.finish();
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
