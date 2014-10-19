package com.example.musicplayer;

import java.util.ArrayList;
import java.util.List;

import android.R.string;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.example.musicplayer.interfa.Audio;
import com.example.musicplayer.lrc.LrcHandler;
import com.example.musicplayer.myclass.songInfo;

public class LocalAudio extends ContextWrapper implements Audio{

	private LrcHandler mLrcHandler;
	private String root = "/storage/emulated/0"; 
	private String root1 = "/storage/sdcard0"; 
	public LocalAudio(Context base) {
		super(base);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<String> getMusicListByPId(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addMediaToPlaylist(ContentValues values) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getMusicPathByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<songInfo> getLocalAudioPathList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getMusicPathListByName(String name) {
		// TODO Auto-generated method stub		
		return null;
	}

	@Override
	public List<songInfo> getLocalAudioListByName(String name) {
		// TODO Auto-generated method stub
		List<songInfo> musicList = new ArrayList<songInfo>();
		ContentResolver resolver = getContentResolver();
		String[] projection = { MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.DISPLAY_NAME,
				MediaStore.Audio.Media.DATA };
		String selection = MediaStore.Audio.Media.DATA + " like ?";
		String[] selectionArgs = { "%" + name + "%" };
		Cursor cursor = resolver.query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
				selection, selectionArgs,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		if (cursor.moveToFirst()) {
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToPosition(i);
				songInfo audio = new songInfo();
				audio.setId(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
				audio.setName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)));
				audio.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
				musicList.add(audio);
			}
		}
		cursor.close();
		return musicList;
	}

	@Override
	public void removeAudioFromPlaylist(String audioId, String playlistId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<songInfo> getAudioListByPlaylistId(String playlistId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<songInfo> getLocalAudioList() {
		List<songInfo> musicList = new ArrayList<songInfo>();
		ContentResolver resolver = getContentResolver();
		Cursor cursor = resolver.query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		if (cursor.moveToFirst()) {
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToPosition(i);
				songInfo audio = new songInfo();
				audio.setId(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
				audio.setName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)));
				audio.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
				musicList.add(audio);
			}
		}
		cursor.close();
		
		Log.d("LocalAudio.java->getLocalAudioList()", "searchLrcFilr()");
		
		mLrcHandler = LrcHandler.getInstance();
		//mLrcHandler.getLrcContent(mediaPlayerSongPath);
		mLrcHandler.searchLrcFile(root);
		if(mLrcHandler.getLrcSize() == 0)
		{
			mLrcHandler.searchLrcFile(root1);
		}
		return musicList;
	}

}
