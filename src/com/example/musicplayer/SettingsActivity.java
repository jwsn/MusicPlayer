package com.example.musicplayer;

import java.util.Random;

import com.example.musicplayer.MusicManagerActivity.MusicListReceiver;
import com.example.musicplayer.lrc.LrcView;
import com.example.musicplayer.myclass.songInfo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;


public class SettingsActivity extends Activity {
	
	private ImageButton offbutton;
	private ImageButton backbutton;
	
	private CheckBox lrcshowcheckbox;
	
	final String SHAKE_SETING_ON = "on";
	final String LRC_SHOW_ON = "on";
	private String shake_settings;
	private String lrcshow;
	
	final String SHOW_LRC_ON_IDLE_ACTION = "show_lrc_on_idle";
	final String CLOSE_LRC_ON_IDLE_ACTION =  "close_lrc_on_idle_action";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);
		
		/*实现摇一摇切歌的功能*/
		offbutton = (ImageButton)findViewById(R.id.settings_off_button);
		offbutton.setOnTouchListener(new View.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					//playingButton.setImageResource(R.drawable.playing_button_press);
					return true;
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					if(shake_settings.equals(SHAKE_SETING_ON)){
					    offbutton.setImageResource(R.drawable.off_button);
					    shake_settings = "off";
					} else {
						offbutton.setImageResource(R.drawable.on_button);
						shake_settings = "on";
					}
                    
					return true;
				}
				return false;
			
			}
		});
		
		/*实现桌面显示歌词的功能*/
		lrcshowcheckbox = (CheckBox)this.findViewById(R.id.lrccheckBox);
		lrcshowcheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					lrcshow = "on";
				}else{
					lrcshow = "off";
				}
				Intent intent= new Intent();
				intent.setAction(SHOW_LRC_ON_IDLE_ACTION);
				intent.putExtra("show_lrc_or_not",lrcshow);
				sendBroadcast(intent);
			}	
		});

		/*返回主界面的功能*/
		backbutton = (ImageButton)findViewById(R.id.settings_back_button);
		backbutton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra("shakingsetings",shake_settings);
				intent.putExtra("lrcshow",lrcshow);
				setResult(4,intent);
				saveSettings();

	    		
                finish();
			}
				
		});
		
		Intent intent =  getIntent();
		shake_settings = intent.getStringExtra("shakingsetings");
		lrcshow = intent.getStringExtra("lrcshow");
		if(shake_settings.equals(SHAKE_SETING_ON))
			offbutton.setImageResource(R.drawable.on_button);
		if(lrcshow.equals(LRC_SHOW_ON))
			lrcshowcheckbox.setChecked(true);
		//fLrcView = AminationActivity.getFloatLrcView();
		
		
		IntentFilter filter = new IntentFilter();  
		filter.addAction(CLOSE_LRC_ON_IDLE_ACTION);
		registerReceiver(new SettingsReceiver(),filter);
	
	}
	
	public class SettingsReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
		//	String broadcastMsg = arg1.getStringExtra("broadcastMsg");
			String myaction = arg1.getAction().toString();
			if(myaction.equals(CLOSE_LRC_ON_IDLE_ACTION))
			{
				String lrc_show = arg1.getStringExtra("lrcshow");
				if(lrc_show.equals("off")){
					lrcshowcheckbox.setChecked(false);
					lrcshow = "off";
				}
			}

		}

	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		
		super.onResume();
	}


	public void saveSettings(){
		ContentValues values=new ContentValues();
		
		values.put("shakeseting", shake_settings);
		values.put("lrcshow", lrcshow);
		DBHelper data=new DBHelper(SettingsActivity.this);
		SQLiteDatabase db=data.getWritableDatabase();
		db.insert("settings", null, values);
		
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode,KeyEvent event){
		
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Intent intent = new Intent();
			intent.putExtra("shakingsetings",shake_settings);
			intent.putExtra("lrcshow",lrcshow);
			setResult(4,intent);
			saveSettings();
			finish();
			
			return true;
		}
		else
		{
			return super.onKeyUp(keyCode, event);
		}
	
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
