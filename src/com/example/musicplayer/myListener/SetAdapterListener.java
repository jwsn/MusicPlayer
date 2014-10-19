package com.example.musicplayer.myListener;

import java.util.List;

import com.example.musicplayer.myAdapter.MyListViewAdapter;
import com.example.musicplayer.myclass.songInfo;

public interface SetAdapterListener {
	public  void setAdapter(List<songInfo> mmusicList);
}
