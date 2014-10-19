package com.example.musicplayer.lrc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.text.TextUtils;
import android.util.Log;

/**
 * 默认的歌词解析器
 *
 */

public class LrcHandler implements ILrcHandler{
	
	private static final LrcHandler instance = new LrcHandler();
	final int LRCTYPE = 0;
	final int TRCTYPE = 1;
	private final int SEARCH_LRC_METHOD = 1;
	private List<String> lrcPathList = new ArrayList<String>();
	private int global_lrc_flag = 0;
	private String global_lrc_path = null;
	private int count = 0;
	private LinkedList<File> FileList = new LinkedList<File>(); //链式存储
	public static final LrcHandler getInstance(){
		return instance;
	}
	
	private LrcHandler(){
	
	}
	
	/***
	 * 将歌词文件里面的字符串 解析成一个List<LrcContent>
	 */
	@Override
	public List<LrcContent> getLrcContent(String str,int RCType){
		
		if(TextUtils.isEmpty(str)){
			return null;
		}
		
		BufferedReader br = new BufferedReader(new StringReader(str));
		
		List<LrcContent> lrcContents = new ArrayList<LrcContent>();
		
		String lrcLine;
		
		try{
			while((lrcLine = br.readLine()) != null){
				//List<LrcContent> rows = LrcContent.createLrcContent(lrcLine);
				List<LrcContent> rows =null;
				if(RCType ==LRCTYPE)
					rows = LrcContent.createLrcContent(lrcLine);
				else if(RCType ==TRCTYPE)
					rows = LrcContent.createTrcContent(lrcLine);
				
				if(rows != null && rows.size() > 0){
					lrcContents.addAll(rows);
				}
			}
			//Collections.sort(lrcContents);
			
			for(int i = 0; i < lrcContents.size() - 1; i++)
			{
				lrcContents.get(i).setTotalTime(lrcContents.get(i+1).getTime() - lrcContents.get(i).getTime());
			}
			
			lrcContents.get(lrcContents.size() - 1).setTotalTime(5000);
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}finally{
			if(br != null){
				try{
					br.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}
		return lrcContents;
	}
	
	public int getLrcSize()
	{
		return lrcPathList.size();
	}
	
	public List<LrcContent> readLRC(String path){
		List<LrcContent> LrcContents = null;
		int  RCType =LRCTYPE;
		String songName = path.substring(path.lastIndexOf("/") + 1, path.length());
		String root = "/";
		if(path.contains(".mp3") && (".mp3").equals(path.substring(path.lastIndexOf("."), path.length())))
		{
			File f;
			f= new File(path.replace(".mp3", ".lrc"));
			if(!f.exists())
			{
				f= new File(path.replace(".mp3", ".trc"));
				if(!f.exists())
				{
					global_lrc_flag = 0;
					global_lrc_path = null;
					if(lrcPathList.size() > 0){
						for(int i = 0; i < lrcPathList.size(); i++ ){
							String songNameTemp = lrcPathList.get(i).substring(lrcPathList.get(i).lastIndexOf("/") + 1, lrcPathList.get(i).length());
							String songNameLrc = songName.replace(".mp3", ".lrc");
							String songNameTrc = songName.replace(".mp3",  ".trc");
							
  							if(songNameLrc.equals(songNameTemp))
							{
								RCType = LRCTYPE;
								global_lrc_flag = 1;
								global_lrc_path = lrcPathList.get(i) ;
								break;
							}else if(songNameTrc.equals(songNameTemp)){
								RCType = TRCTYPE;
								global_lrc_flag = 1;
								global_lrc_path = lrcPathList.get(i) ;
								break;
							}
						}
					}
					if(global_lrc_flag == 1 && global_lrc_path != null){
						f= new File(global_lrc_path);
						if(!f.exists()){
							return null;
						}
					}
					else{
						return null;
					}
				}
				RCType = TRCTYPE;
			}
			
			String line;
			try{
				FileInputStream fis = new FileInputStream(f);
				InputStreamReader isr = new InputStreamReader(fis, "utf-8");
				BufferedReader br = new BufferedReader(isr);
				StringBuffer sb=new StringBuffer();
				try{
					while((line = br.readLine()) != null){
						sb.append(line+"\n");
					}
					Log.d("readLRC", sb.toString());
					LrcContents = getLrcContent(sb.toString(),RCType);
				}catch(IOException e){
					e.printStackTrace();
				}

			}catch(FileNotFoundException e){
				e.printStackTrace();
				//strBuilder.append("沒有歌詞");
			}catch(IOException e){
				e.printStackTrace();
				//strBuilder.append("沒有歌詞");
			}
		}
		return LrcContents;
	}
	

	public void searchLrcFile(String root){
		int num=0;

		File[] files = new File(root).listFiles();

		FileList.clear();
		
		if(files != null)
		{
			for(int i = 0; i < files.length; i++){
				if (files[i].canRead() && files[i].isDirectory()) { 
					FileList.add(files[i]);
					//searchLrcFile(file.getAbsolutePath());  
				} 
				else{
					if(files[i].getName().contains(".lrc") || files[i].getName().contains(".trc")){
						//global_lrc_path = file.getAbsolutePath();
						lrcPathList.add(files[i].getAbsolutePath());
						System.out.println("This file is Lrc File,fileName="+files[i].getName()+",filePath="+files[i].getAbsolutePath()); 
					}
				}
			}
		}
		
		File tempfile;
		
		while(!FileList.isEmpty()){
			count++;
			if(count >= 200)
			{
				break;
			}
			tempfile = FileList.removeFirst();
			if(tempfile.isDirectory() && tempfile.canRead()){
				files = tempfile.listFiles();
				if(files != null){
					for(int i = 0; i < files.length; i++){
						if(files[i].isDirectory() && files[i].canRead()){
							FileList.add(files[i]);
							System.out.println("This file is for times" + num++); 
						}else{
							if(files[i].getName().contains(".lrc") || files[i].getName().contains(".trc")){
								//global_lrc_path = file.getAbsolutePath();
								lrcPathList.add(files[i].getAbsolutePath());
								System.out.println("This file is Lrc File,fileName="+files[i].getName()+",filePath="+files[i].getAbsolutePath()); 
							}
						}
					}
				}
			}
			
		}
		
	}
	
	/*
	public void searchLrcFile(String root){

		File[] files = new File(root).listFiles();
		count++;
		if(count >= 60000)
			return;
		
		if(files != null)
		{
			for(File file : files){
				if (file.canRead() && file.isDirectory()) { 
					String name = file.getAbsolutePath();
					searchLrcFile(name);  
				}  
				else{
					if(file.getName().contains(".lrc") || file.getName().contains(".trc")){
						global_lrc_path = file.getAbsolutePath();
						lrcPathList.add(global_lrc_path);
						System.out.println("This file is Lrc File,fileName="+file.getName()+",filePath="+file.getAbsolutePath()); 
						//return ;
					}
				}
			}
		}
	}


	private List<LrcContent> lrcList;
	private LrcContent mLrcContent;
	
	public LrcHandler(){
		mLrcContent = new LrcContent();
		lrcList = new ArrayList<LrcContent>();
	}
	
	public String readLRC(String path){
		
		StringBuilder strBuilder = new StringBuilder();
		if(path.contains(".mp3"))
		{
		
			File f = new File(path.replace(".mp3", ".lrc"));
			
			try{
				FileInputStream fis = new FileInputStream(f);
				InputStreamReader isr = new InputStreamReader(fis, "utf-8");
				BufferedReader br = new BufferedReader(isr);
				
				String s = "";
				
				while((s = br.readLine()) != null){
					s = s.replace("[", "");
					s = s.replace("]", "@");
					
					String splitLrcData[] = s.split("@");
					
					if(splitLrcData.length > 1){
						mLrcContent.setLrcStr(splitLrcData[1]);
						
						int lrcTime = time2Str(splitLrcData[0]);
						
						mLrcContent.setLrcTime(lrcTime);
						
						lrcList.add(mLrcContent);
						
						mLrcContent = new LrcContent();
						
					}
				}
			}catch(FileNotFoundException e){
				e.printStackTrace();
				strBuilder.append("沒有歌詞");
			}catch(IOException e){
				e.printStackTrace();
				strBuilder.append("沒有歌詞");
			}
		}
		else
		{
			strBuilder.append("沒有歌詞");
		}
		return strBuilder.toString();
		
	}
	
	public int time2Str(String timeStr){
		timeStr = timeStr.replace(":", ".");
		timeStr = timeStr.replace(".", "@");
		
		String timeData[] = timeStr.split("@");
		
		int minute = Integer.parseInt(timeData[0]);
		int second = Integer.parseInt(timeData[1]);
		int millisecond = Integer.parseInt(timeData[2]);
		
		int currentTime = (minute * 60 + second) * 1000 + millisecond * 10;
		
		return currentTime; 
	}
	
	public List<LrcContent> getLrcList(){
		return lrcList;
	}
*/
}