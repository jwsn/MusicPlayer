package com.example.musicplayer;

import java.util.List;
import com.example.musicplayer.interfa.Audio;
import com.example.musicplayer.lrc.LrcView;
import com.example.musicplayer.myAdapter.MyListViewAdapter;
import com.example.musicplayer.myListener.listListenerImpl;
import com.example.musicplayer.myclass.songInfo;


import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SearchSongActivity  extends Activity{
	
	private ImageButton searchButton;
	private AutoCompleteTextView atoTextView;
	private Button netSearchButton;
	private List<songInfo> musicList;
	private Audio laudio = new LocalAudio(this);
	MyListViewAdapter adapter;
	private ListView listView;
	private Context context;
	private int lastSelectPosition;
	final String PLAY_SEARCH_SONG_ACTION = "com.android.example.select_search_song_play";
    final String PAUSE_PLAY_SONG_ACTION = "com.android.example.select_song_pause";
    final String RESUME_PLAY_SONG_ACTION = "com.android.example.select_song_resume";
    final String PLAY_STATE_CHANGE_ACTION = "play_state_change";
    private boolean isPlaying;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		context = this;
		lastSelectPosition = -1;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_song_activity);
		
		Intent intent =  getIntent();
		isPlaying = intent.getBooleanExtra("isplaying",false);
		
		listView = (ListView) this.findViewById(R.id.search_result_listview); 
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
		
		
		atoTextView = (AutoCompleteTextView)findViewById(R.id.search_edit);
		searchButton  = (ImageButton)findViewById(R.id.search_button);
		searchButton.setOnTouchListener(new View.OnTouchListener(){
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					searchButton.setImageResource(R.drawable.net_search_button_press);
					return true;
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{

					musicList = laudio.getLocalAudioListByName(atoTextView.getText().toString());
					adapter = new MyListViewAdapter(context,musicList,Color.BLACK,true);
					adapter.setOnClickListener(new myItemClickListener());
					listView.setAdapter(adapter);
		
					searchButton.setImageResource(R.drawable.net_search_button);
					    return true;
					}
				return false;
			}			
		});
		
		
		netSearchButton  = (Button)findViewById(R.id.network_search_button);
		netSearchButton.setOnTouchListener(new View.OnTouchListener(){
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					//netSearchButton.setImageResource(R.drawable.search_button);
					return true;
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{

					Intent intent = new Intent();
					intent.setClass(SearchSongActivity.this,netDownloadActivity.class);
					startActivity(intent);
		
				//	netSearchButton.setImageResource(R.drawable.search_button_press);
					    return true;
					}
				return false;
			}			
		});
		
		//fLrcView = AminationActivity.getFloatLrcView();
	
	//	SearchSongActivityPlayThread sst = new SearchSongActivityPlayThread();
	//	Thread t = new Thread(sst);
	//	t.start();
		
		/*增加list监听，实现点击item ，播放音乐的功能*/
//		
//	list.setOnItemClickListener(new OnItemClickListener(){
//			@Override
//		public void onItemClick(AdapterView<?> parent, View view, int position,
//				long id) {
			// TODO Auto-generated method stub
			//play(arg2);
		//	list.removeAllViews(); 
		//	list.setAdapter(adapter);
		//	musicList = laudio.getLocalAudioListByName(atoTextView.getText().toString());
			//	adapter = new MyListViewAdapter(context,musicList);
			//list.setAdapter(adapter);
///			for(int i=0;i<adapter.imageButtons.size(); i++)
//				{
//					ImageButton igb = adapter.imageButtons.get(i);
//					igb.setVisibility(View.INVISIBLE);
//				}
//				final ImageButton imageB1 = (ImageButton)view.findViewById(R.id.listview_play_btn);
//				imageB1.setOnClickListener(new OnClickListener(){
//
//					@Override
//					public void onClick(View v) {
//						// TODO Auto-generated method stub
//						imageB1.setImageResource(R.drawable.list_pause_indicator);
//					}
//					
//				});
//				/*
//				if(position == lastSelectPosition)
//				{
//					imageB1.setImageResource(R.drawable.list_playing_indicator);
//				}
//				else
//				*/
//				{
//					lastSelectPosition = position;
//					imageB1.setImageResource(R.drawable.list_playing_indicator);
//					imageB1.setVisibility(View.VISIBLE);
//				}
//				//imageB1.setVisibility(View.VISIBLE);
//				
	//			for(int i =0;i<musicList.size();i++){}
////				{
////					ImageButton imageB = (ImageButton)(parent.getItem(i)).findViewById(R.id.listview_play_btn);
////					imageB.setVisibility(0);
////				}
//				String url = musicList.get(position).getPath();
//				Intent intent= new Intent();
//				intent.setAction(PLAY_SEARCH_SONG_ACTION);
//				intent.putExtra("select_search_song",url );
//				sendBroadcast(intent);
//
//			}
//	});
		
	}
	
	public class myItemClickListener implements listListenerImpl
	{

		private String lastUrl = null;
		private boolean resume = false;
		@Override
		public void onItemClick(int position) {
			// TODO Auto-generated method stub
			if(adapter!=null)
			{
				
				String url = adapter.getCurrentSelectUrl();
			//	adapter.setClickState(false);
				Intent intent= new Intent();
				if(url.equals(lastUrl))
				{	if(resume == false)
					{
						resume = true;
						intent.setAction(PAUSE_PLAY_SONG_ACTION);
					}
					else 
					{
						resume = false;
						intent.setAction(RESUME_PLAY_SONG_ACTION);
					}
				}
				else
				{
					lastUrl = url;
					resume = false;
					intent.setAction(PLAY_SEARCH_SONG_ACTION);
					intent.putExtra("select_search_song",url );
				}
				sendBroadcast(intent);
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

	/*
	public class SearchSongActivityPlayThread implements Runnable
	{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String lastUrl = null;
			boolean resume = false;
			while(true)
			{	
			
				if(adapter!=null&&adapter.getClickState())
				{
					
					String url = adapter.getCurrentSelectUrl();
					//adapter.setClickState(false);
					Intent intent= new Intent();
					if(url.equals(lastUrl))
					{	if(resume == false)
						{
							resume = true;
							intent.setAction(PAUSE_PLAY_SONG_ACTION);
						}
						else 
						{
							resume = false;
							intent.setAction(RESUME_PLAY_SONG_ACTION);
						}
					}
					else
					{
						lastUrl = url;
						resume = false;
						intent.setAction(PLAY_SEARCH_SONG_ACTION);
						intent.putExtra("select_search_song",url );
					}
					sendBroadcast(intent);
				}
				
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		}
		
		
	} */
	
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
		Intent intent = new Intent();
		intent.putExtra("isplaying",isPlaying);
		intent.setClass(SearchSongActivity.this, PlayHistoryListActivity.class);
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
		/*
		Intent intent = new Intent();
		intent.putExtra("isplaying",isPlaying);
		intent.setClass(SearchSongActivity.this, MusicManagerActivity.class);
		startActivity(intent);
		int version = Integer.valueOf(android.os.Build.VERSION.SDK); 
		if(version  >= 5) {
			overridePendingTransition(R.anim.zoom_right_to_left_in, R.anim.zoom_right_to_left_out); 
		}*/
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
