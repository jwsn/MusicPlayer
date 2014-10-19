package com.example.musicplayer.myAdapter;

import java.util.ArrayList;
import java.util.List;

import com.example.musicplayer.R;
import com.example.musicplayer.myListener.listListenerImpl;
import com.example.musicplayer.myclass.songInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MyListViewAdapter extends BaseAdapter{
	private List<songInfo> musicList;
	private Context context;
	final String PLAY_SEARCH_SONG_ACTION = "com.android.example.select_search_song_play";
	public ArrayList<ImageButton> imageButtons;
	private boolean IsPlaying;
	private int lastSelectPosition;
	private String currentSelectUrl;
	public listListenerImpl Listener;
	private int ItemTextColor;
	private boolean needDisplayImageButton;
	
	public MyListViewAdapter(Context context,List<songInfo> musicList,boolean displayImageButton)
	{
		this(context,musicList,Color.WHITE,displayImageButton);
	}
	
	public MyListViewAdapter(Context context,List<songInfo> musicList ,int color,boolean displayImageButton)
	{
		this.context = context;
		this.musicList = musicList;
		imageButtons = new ArrayList<ImageButton>();
		IsPlaying = false;
		lastSelectPosition = -1;
		ItemTextColor = color;
		currentSelectUrl = null;
		needDisplayImageButton = displayImageButton;
	}
	
	
	public String getCurrentSelectUrl()
	{
		return currentSelectUrl;
	}
	
	public void setLastSelectPosition(int arg0)
	{
		lastSelectPosition = arg0;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return musicList.size();
	}
	
	public void setOnClickListener(listListenerImpl l)
	{
		Listener = l;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		int kk = arg0;
		return musicList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		if(convertView == null)
		{
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.my_list_view_adapter,null);
		}

		TextView t_1 = (TextView)convertView.findViewById(R.id.listview_line1);		
		t_1.setText(musicList.get(position).getName());
		
		t_1.setTextColor(ItemTextColor);
		t_1.setTextSize(20);
		ImageButton igb = (ImageButton)convertView.findViewById(R.id.listview_play_btn);
		imageButtons.add(igb);
		this.setOnClickListener(position,convertView);

		this.setOnTouchListener(position,convertView);
		this.setOnLongClickListener(position,convertView,parent);
		//convertView.requestFocusFromTouch();
		return convertView;

	}

	
	private int startTouchX;
	private int startTouchY;
	private void setOnTouchListener(final int position,final View convertView)
	{
		convertView.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					startTouchX = (int)event.getX();
					startTouchY = (int)event.getY();
					Log.i("myList","adapter Down");
					return false;
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					
					Log.i("myList","adapter UP");
					
					int moveX = (int)event.getX() - startTouchX ;
					int moveY = (int)event.getY() - startTouchY;
					if(Math.abs(moveX)<Math.abs(moveY))
					{
						if(Math.abs(moveY)<10)
						{
							//onItemClick(position,convertView);
							return false;
						}
					/*	
						if(moveY>0)
							Listener.onTouchDown();
						else
							Listener.onTouchUp();*/
						
						return false;
						
					}
					else
					{
						if(Math.abs(moveX)<10)
						{
							//onItemClick(position,convertView);
							return false;
						}
						
						if(moveX>0)
							Listener.onTouchRight();
						else
							Listener.onTouchLeft();
						return true;
					}
				}

				return false;
			}
			
		});
	}
	
	
	private void onItemClick(final int position,final View convertView)
	{

		currentSelectUrl = ((songInfo) getItem(position)).getPath();
		int j= position;
		ImageButton imageB = (ImageButton)convertView.findViewById(R.id.listview_play_btn);
		int dddd = lastSelectPosition;
		if(lastSelectPosition == position)
		{
			if(true == IsPlaying)
			{
				IsPlaying =false;
				imageB.setImageResource(R.drawable.list_playing_indicator);
			}
			else
			{	IsPlaying = true;
				imageB.setImageResource(R.drawable.list_pause_indicator);
			}
		}
		else
		{
			lastSelectPosition = position;
			int i = lastSelectPosition;
			IsPlaying = true;
			imageB.setImageResource(R.drawable.list_pause_indicator);
			
		}
		
		if(Listener!=null)
			Listener.onItemClick(position);
		
		if(needDisplayImageButton == false)
			return;
		
		for(int i=0;i<imageButtons.size(); i++)
		{
			ImageButton igb = imageButtons.get(i);
			igb.setVisibility(View.INVISIBLE);
		}
		imageB.setVisibility(1);
	
	}
	
	private void setOnClickListener(final int position, final View convertView) {
		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i("myList","*********myadapter***************");
				onItemClick(position,convertView);
			}
			
			

		});
	}
	
	private void setOnLongClickListener(final int position, final View convertView,final ViewGroup parent)
	{
		convertView.setOnLongClickListener(new OnLongClickListener(){

			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				if(Listener!=null)
					Listener.onItemLongClick(position);
				
				return true;
			}
			
		});
	}

}
