package com.example.musicplayer.lrc;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Text;

import android.text.TextUtils;
import android.util.Log;

public class LrcContent{
	
	/**歌词内容**/
	private String lrcStr;
	/**开始时间 为00:10:00***/
	private String timeStr;
	/**该行歌词显示的总时间**/
	private int totalTime;
	/**开始时间 毫米数  00:10:00  为10000**/
	private int time;
	
	public LrcContent(){
		super();
	}
	
	public LrcContent(String timeStr, int time, String lrcStr)
	{
		super();
		this.timeStr = timeStr;
		this.time = time;
		this.lrcStr = lrcStr;
	}
	
	
	public int getTotalTime()
	{
		return totalTime;
	}
	
	public void setTotalTime(int totalTime)
	{
		this.totalTime = totalTime;
	}
	
	public String getTimeStr()
	{
		return timeStr;
	}
	
	public void setTimeStr(String timeStr)
	{
		this.timeStr = timeStr;
	}
	
	public int getTime()
	{
		return time;
	}
	
	public void setTime(int time)
	{
		this.time = time;
	}
	
	public String getLrcStr(){
		return lrcStr;
	}
	
	public void setLrcStr(String lrcStr){
		this.lrcStr = lrcStr;
	}
	
	/**
	 * 将歌词文件中的某一行 解析成一个List<LrcContent> 
	 * 因为一行中可能包含了多个LrcRow对象
	 * 比如  [03:33.02][00:36.37]当鸽子不再象征和平  ，就包含了2个对象
	 * @param lrcLine
	 * @return
	 */
	public static final List<LrcContent> createLrcContent(String lrcLine){
		if(!lrcLine.startsWith("[") || lrcLine.indexOf("]") != 9){
			return null;
		}
		//最后一个"]" 
		int lastIndexOfRightBracket = lrcLine.lastIndexOf("]");
		//歌词内容
		String content = lrcLine.substring(lastIndexOfRightBracket + 1, lrcLine.length());
		//截取出歌词时间，并将"[" 和"]" 替换为"-"   [offset:0]
		Log.d("lrcContent", "createLreContent()");
		// -03:33.02--00:36.37-
		String times = lrcLine.substring(0, lastIndexOfRightBracket+1).replace("[", "-").replace("]", "-");
		String[] timeArray = times.split("-");
		List<LrcContent> lrcContents = new ArrayList<LrcContent>();
		for(String tem : timeArray){
			if(TextUtils.isEmpty(tem.trim())){
				continue;
			}
			try{
				LrcContent lrcContent = new LrcContent(tem, formatTime(tem), content);
				lrcContents.add(lrcContent);
			}catch(Exception e){
				Log.w("LrcContent", e.getMessage());
			}
		}
		return lrcContents;
	}
	
	
	
	
	
	/**************************************TRC***********************************/
	public static final List<LrcContent> createTrcContent(String s){
		if(!s.startsWith("[") || s.indexOf("]") != 9){
			return null;
		}
		
		s = s.replace("[", "");
		s = s.replace("]", "@");
		
		String splitLrcData[] = s.split("@");

		s = s.replace("[", "");
		s = s.replace("]", "@");
		List<LrcContent> lrcContents = new ArrayList<LrcContent>();
		String splitTrcData[] = s.split("@");
		
		if(splitTrcData.length > 1){						
			int trcTime = time2Str(splitTrcData[0]);
			String[] words = splitTrcData[1].split(">");
			
			///////////////////////////////////////////
			String str = new String();
			for(int i=0;i<words.length;i++)
			{
				String []word = words[i].split("<");
				for(int j =0;j<word.length;j++)
				{
					boolean isNum = word[j].matches("[0-9]+");
					if(!isNum)
					{
						str+=word[j];
					}
				
				}
			}
			LrcContent lrcContent = new LrcContent(formatTimeFromInt(trcTime),trcTime , str);
			lrcContents.add(lrcContent);
            ////////////////////////////////////////
			
			/*
			int subTime =0;
			for(int i=0;i<words.length;i++)
			{
				String []word = words[i].split("<");
				for(int j =0;j<word.length;j++)
				{
					boolean isNum = word[j].matches("[0-9]+");
					if(isNum)
					{
						subTime+=Integer.parseInt(word[j]);
					}
					else
					{ 
						LrcContent lrcContent = new LrcContent(formatTimeFromInt(trcTime+subTime),trcTime+subTime , word[j]);
						lrcContents.add(lrcContent);
					}
				}
			}*/
				
			
			
		}
	
		/*
		 * 		
		 * //最后一个"]" 
		int lastIndexOfRightBracket = lrcLine.lastIndexOf("]");
		//歌词内容
		String content = lrcLine.substring(lastIndexOfRightBracket + 1, lrcLine.length());
		List<LrcContent> lrcContents = new ArrayList<LrcContent>();
		//截取出歌词时间，并将"[" 和"]" 替换为"-"   [offset:0]
		Log.d("lrcContent", "createLreContent()");
		// -03:33.02--00:36.37-
		String times = lrcLine.substring(0, lastIndexOfRightBracket+1).replace("[", "-").replace("]", "-");
		String[] timeArray = times.split("-");
		List<LrcContent> lrcContents = new ArrayList<LrcContent>();
		for(String tem : timeArray){
			if(TextUtils.isEmpty(tem.trim())){
				continue;
			}
			try{
				LrcContent lrcContent = new LrcContent(tem, formatTime(tem), content);
				lrcContents.add(lrcContent);
			}catch(Exception e){
				Log.w("LrcContent", e.getMessage());
			}
		}*/
		return lrcContents;
	}
	
	
	public static int time2Str(String timeStr){
		timeStr = timeStr.replace(":", ".");
		timeStr = timeStr.replace(".", "@");
		
		String timeData[] = timeStr.split("@");
		
		int minute = Integer.parseInt(timeData[0]);
		int second = Integer.parseInt(timeData[1]);
		int millisecond = Integer.parseInt(timeData[2]);
		
		int currentTime = (minute * 60 + second) * 1000 + millisecond * 10;
		
		return currentTime; 
	}
	
	
	public static String formatTimeFromInt(int progress){
		//总的秒数 
		int msecTotal = progress/1000;
		int min = msecTotal/60;
		int msec = msecTotal%60;
		String minStr = min < 10 ? "0"+min:""+min;
		String msecStr = msec < 10 ? "0"+msec:""+msec;
		return minStr+":"+msecStr;
	}
	
	/*****************TRC******************************/
	
	
	
	
	/****
	 * 把歌词时间转换为毫秒值  如 将00:10.00  转为10000
	 * @param tem
	 * @return
	 */
	private static int formatTime(String timeStr){
		timeStr = timeStr.replace('.', ':');
		String[] times = timeStr.split(":");
		
		return Integer.parseInt(times[0])*60*1000
				+ Integer.parseInt(times[1])*1000
				+ Integer.parseInt(times[2]);
	}
	
	public int compareTo(LrcContent anotherLrcContent){
		return (int) (this.time - anotherLrcContent.time);
	}
	
	public String toString(){
		return "LrcContent [timeStr=" + timeStr + ", time=" + time + ", lrcStr="+lrcStr + "]";
	}
	
}


