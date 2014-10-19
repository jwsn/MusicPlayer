package com.example.musicplayer.myListener;

import android.view.MotionEvent;
import android.view.View;

public interface FloatViewTouchListener {
	public boolean onTouchDown(View v, MotionEvent event);
	public boolean onTouchMove(View v, MotionEvent event);
	public boolean onTouchUp(View v, MotionEvent event);
}
