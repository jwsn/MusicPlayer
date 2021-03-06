package com.example.musicplayer;

import java.util.List;

import com.example.musicplayer.myclass.songInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Message;

public class SaveCollectSongThread  extends Thread{
	
	public String saveSongName;
	public Context context;
	@Override
	public void run() {
		// TODO Auto-generated method stub
		saveCollectSong(saveSongName);
		//updateCollectList();
	}
	
	/*此函数用于保存播放历史*/
	public void saveCollectSong(String name)
	{		
  		ContentValues values=new ContentValues();
		values.put("collectmusicname", name);
		
		DBHelper plane=new DBHelper(context,"CollectMusiclist");
		SQLiteDatabase db=plane.getWritableDatabase();
		
		Cursor c = db.query("mycollect", null, null, null, null, null, null);
		if (c.moveToFirst()) {
			for (int i = 0; i < c.getCount(); i++) {
				c.moveToPosition(i);
				
				if(name.equals(c.getString(0)))
				{
					return;
				}
				
			}
		}
		/*
		Message msg = new Message();
		msg.obj = "歌曲收藏成功 !";
		handler.sendMessage(msg);
		*/
		db.insert("mycollect", null, values);
		db.close();
		
		
	}
	
	
	private void updateCollectList()
	{
		AminationActivity.getMyCollectMusicList().clear();
		synchronized(AminationActivity.getMyCollectMusicList())
		{
			final DBHelper helpter=new DBHelper(context,"CollectMusiclist");
			SQLiteDatabase dbr = helpter.getWritableDatabase();
	
			
			Cursor cr = dbr.query("mycollect", null, null, null, null, null, null);
	
			 List<songInfo>  localmusicList = AminationActivity.getMyLocalMusicList();
			if (cr.moveToFirst()) {
				for (int i = cr.getCount()-1; i >=0 ; i--) {
					cr.moveToPosition(i);
					songInfo audio = new songInfo();
					Boolean inlocallist = false;
					for(int j= 0;j<localmusicList.size();j++){
						if(cr.getString(0).equals(localmusicList.get(j).getName())){
							audio.setId(localmusicList.get(j).getId());
							audio.setName(cr.getString(0));
							audio.setPath(localmusicList.get(j).getPath());
							AminationActivity.getMyCollectMusicList().add(audio);
							inlocallist = true;
							break;
						}
					}
					if(!inlocallist){
						dbr.delete("mycollect", "collectmusicname=?", new String[]{cr.getString(0)});
					}
					
				}
			}
			dbr.close();
		}
	}


}
