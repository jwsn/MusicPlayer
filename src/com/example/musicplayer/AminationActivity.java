package com.example.musicplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.example.musicplayer.MusicManagerActivity.MusicListReceiver;
import com.example.musicplayer.interfa.Audio;
import com.example.musicplayer.lrc.LrcContent;
import com.example.musicplayer.lrc.LrcView;
import com.example.musicplayer.myclass.songInfo;
import com.igexin.sdk.PushManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.Toast;

import com.igexin.sdk.PushManager;



public class AminationActivity extends Activity {

	//private List<songInfo> musicList;
	//private Audio laudio = new LocalAudio(this);
	private  Audio laudio = new LocalAudio(this);
	private static List<songInfo> localmusicList;
	//private static List<songInfo> collectmusicList = new ArrayList<songInfo>();
	private static List<songInfo> collectmusicList;
	private String exitMusicName;
	private String exitPlayMode;
	private String shakeseting;
	private String lrcshow;

	private  Context Aminationcontext; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_amination);
		//musicList = laudio.getLocalAudioList();
		//PushManager.getInstance().initialize(this.getApplicationContext());
		Aminationcontext = this.getApplicationContext();
		PushManager.getInstance().initialize(this.getApplicationContext());
		

		
		final DBHelper data=new DBHelper(this);
	    SQLiteDatabase db=data.getReadableDatabase();
	    Cursor c = db.query("exitinfo", null, null, null, null, null, null);
	    while(c.moveToNext())
	    {
	    	exitMusicName = c.getString(0);
	    	exitPlayMode = c.getString(1);
	    } 
	    Cursor d = db.query("settings", null, null, null, null, null, null);
	    while(d.moveToNext())
	    {
	    	shakeseting = d.getString(0);
	    	lrcshow = d.getString(1);
	    } 
	   

		//musicList = laudio.getLocalAudioList();
		new Handler().postDelayed(new Runnable(){

			@Override
			public void run() {
				Bundle b = new Bundle();
				// TODO Auto-generated method stub
			
				Intent  intent  = new Intent();
				intent.putExtra("isplaying",false);
				b.putString("exitMusicName",exitMusicName);
				b.putString("exitPlayMode",exitPlayMode);
				 b.putString("shakeseting",shakeseting);
				 b.putString("lrcshow",lrcshow);
			//	b.putParcelableList("list",musicList);
				//intent.putExtra("list",musicList);
				intent.putExtras(b);
				intent.setClass(AminationActivity.this,MusicManagerActivity.class);
				startActivity(intent);
				AminationActivity.this.finish();
				
				
			}
			
		},1500);
		
		

 

	//	wmParams.gravity=Gravity.LEFT|Gravity.TOP;  
		//wmParams.x = 10;
	//	wmParams.y = 50;



		localmusicList = laudio.getLocalAudioList();
		/*
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
		db.close();*/

	}
	

	
	public static List<songInfo> getMyLocalMusicList()
	{
		return localmusicList;
	}
	

	
	public static List<songInfo> getMyCollectMusicList()
	{
		return collectmusicList;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.amination, menu);
		return true;
	}

}
