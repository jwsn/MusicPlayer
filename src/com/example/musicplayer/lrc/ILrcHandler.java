package com.example.musicplayer.lrc;

import java.util.List;

/**
 * ��ʽ�����
 *
 */
public interface ILrcHandler{
	List<LrcContent> getLrcContent(String str,int RCType);
}