package com.example.musicplayer.interfa;

import java.util.List;

import com.example.musicplayer.myclass.songInfo;

import android.content.ContentValues;
public interface Audio {
	List<String> getMusicListByPId(String id);

	void addMediaToPlaylist(ContentValues values);

	String getMusicPathByName(String name);

	List<songInfo> getLocalAudioList();

	List<String> getMusicPathListByName(String name);

	List<songInfo> getLocalAudioListByName(String name);

	void removeAudioFromPlaylist(String audioId, String playlistId);

	List<songInfo> getAudioListByPlaylistId(String playlistId);

	List<songInfo> getLocalAudioPathList();
}
