package com.example.musicplayer.lrc;

import java.util.List;

/**
 * ¸è´Ê½âÎöÆ÷
 *
 */
public interface ILrcHandler{
	List<LrcContent> getLrcContent(String str,int RCType);
}