package com.example.musicplayer.lrc;

import java.util.ArrayList;
import java.util.List;

import com.example.musicplayer.MusicManagerActivity;
import com.example.musicplayer.PlayMusicService;
import com.example.musicplayer.myListener.FloatViewTouchListener;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.Scroller;

/***
 * 
 * 须知：
 * 在ViewGroup里面 scrollTo，scrollBy方法移动的是子View
 * 在View里面scrollTo，scrollBy方法移动的是View里面绘制的内容
 * 要点：
 * 1:歌词的上下平移用什么实现？
 *    用Scroller实现，Scroller只是一个工具而已，
 *    真正实现滚动效果的还是View的scrollTo方法
 * 2：歌词的水平滚动怎么实现？
 *    通过属性动画ValueAnimator控制高亮歌词绘制的x轴起始坐标
 */

public class LrcView extends View implements ILrcView{
	/**所有的歌词***/
	private List<LrcContent> mLrcContents;
	/**无歌词数据的时候 显示的默认文字**/
	private static final String DEFAULT_TEXT = "小黄人找不到歌词哦";
	/**默认文字的字体大小**/
    private static final float SIZE_FOR_DEFAULT_TEXT = 35;
    
    /**画高亮歌词的画笔***/
	private Paint mPaintForHighLightLrc;
	/**高亮歌词的默认字体大小***/
	private static final float DEFAULT_SIZE_FOR_HIGHT_LIGHT_LRC = 40;
	/**高亮歌词当前的字体大小***/
	private float mCurSizeForHightLightLrc = DEFAULT_SIZE_FOR_HIGHT_LIGHT_LRC;
	/**高亮歌词的默认字体颜色**/
	//private static final int DEFAULT_COLOR_FOR_HIGHT_LIGHT_LRC = 0XFFEE4000;
	private static final int DEFAULT_COLOR_FOR_HIGHT_LIGHT_LRC = Color.BLUE;
	/**高亮歌词当前的字体颜色**/
	private int mCurColorForHightLightLrc = DEFAULT_COLOR_FOR_HIGHT_LIGHT_LRC;
	
	/**画其他歌词的画笔***/
	private Paint mPaintForOtherLrc;
	/**其他歌词的默认字体大小***/
	private static final float DEFAULT_SIZE_FOR_OTHER_LRC = 30;
	/**其他歌词当前的字体大小***/
	private float mCurSizeForOtherLrc = DEFAULT_SIZE_FOR_OTHER_LRC;
	/**高亮歌词的默认字体颜色**/
	private static final int DEFAULT_COLOR_FOR_OTHER_LRC = Color.WHITE;
	/**高亮歌词当前的字体颜色**/
	private int mCurColorForOtherLrc = DEFAULT_COLOR_FOR_OTHER_LRC;
	
	/**画时间线的画笔***/
	private Paint mPaintForTimeLine;
	/***时间线的颜色**/
	private static final int COLOR_FOR_TIME_LINE = 0xffD02090;
	/**时间文字大小**/
	private static final int SIZE_FOR_TIME = 18;
	/**是否画时间线**/
	private boolean mIsDrawTimeLine = false;

	/**歌词间默认的行距**/
	private static final float DEFAULT_PADDING = 20;
	/**歌词当前的行距**/
	private float mCurPadding = DEFAULT_PADDING;

	/**歌词的最大缩放比例**/
	public static final float MAX_SCALING_FACTOR = 5.0f;
	/**歌词的最小缩放比例**/
	public static final float MIN_SCALING_FACTOR = 0.5f;
	/**默认缩放比例**/
	private static final float DEFAULT_SCALING_FACTOR = 1.0f;
	/**歌词的当前缩放比例**/
	private float mCurScalingFactor = DEFAULT_SCALING_FACTOR;

	/**实现歌词竖直方向平滑滚动的辅助对象**/
	private Scroller mScroller;
	/***移动一句歌词的持续时间**/
	private static final int DURATION_FOR_LRC_SCROLL = 1500;
	/***停止触摸时 如果View需要滚动 时的持续时间**/
	private static final int DURATION_FOR_ACTION_UP = 400;
	
	/**控制文字缩放的因子**/
	private float mCurFraction = 0;
	private int mTouchSlop;
	private int mTotleDrawRow;
	/**控制歌词水平滚动的属性动画***/
	
	public boolean isFloatWindow = false;
	WindowManager wm;
	WindowManager.LayoutParams wmParams;
	private ValueAnimator mAnimator;
	private FloatViewTouchListener floatViewTouchListener;
	
	public void setFloatViewTouchListener(FloatViewTouchListener l)
	{
		floatViewTouchListener = l;
	}
	public LrcView(Context context) {
		super(context);
		init();
	}
	public LrcView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	
	public void setLrcTextColor(int Hightcolor,int otherColor)
	{
		mCurColorForHightLightLrc = Hightcolor;
		mPaintForHighLightLrc.setColor(mCurColorForHightLightLrc);
		
		mCurColorForOtherLrc = otherColor;
		mPaintForOtherLrc.setColor(mCurColorForOtherLrc);
	}

	/**
	 * 初始化画笔等
	 */
	@Override
	public void init() {
		mScroller = new Scroller(getContext());
		mPaintForHighLightLrc = new Paint();
		mPaintForHighLightLrc.setColor(mCurColorForHightLightLrc);
		mPaintForHighLightLrc.setTextSize(mCurSizeForHightLightLrc);
		//mPaintForHighLightLrc.setTypeface(Typeface.DEFAULT );

		mPaintForOtherLrc = new Paint();
		mPaintForOtherLrc.setColor(mCurColorForOtherLrc);
		mPaintForOtherLrc.setTextSize(mCurSizeForOtherLrc);
		//mPaintForOtherLrc.setTypeface(Typeface.DEFAULT);

		mPaintForTimeLine = new Paint();
		mPaintForTimeLine.setColor(COLOR_FOR_TIME_LINE);
		mPaintForTimeLine.setTextSize(SIZE_FOR_TIME);

		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(mLrcContents == null || mLrcContents.size() == 0){
			//画默认的显示文字
			mPaintForOtherLrc.setTextSize(DEFAULT_SIZE_FOR_HIGHT_LIGHT_LRC);
			String currentSongName = MusicManagerActivity.getMusicList().get(MusicManagerActivity.currentPosition).getName();
			float textWidth = mPaintForOtherLrc.measureText(currentSongName);
			float textX = (getWidth()-textWidth)/2;
			canvas.drawText(currentSongName, textX, getHeight()/2-DEFAULT_SIZE_FOR_HIGHT_LIGHT_LRC, mPaintForOtherLrc);
			
		//	mPaintForOtherLrc.setTextSize(getTextSize());
			textWidth = mPaintForOtherLrc.measureText(DEFAULT_TEXT);
			textX = (getWidth()-textWidth)/2;
			canvas.drawText(DEFAULT_TEXT, textX, getHeight()/2, mPaintForOtherLrc);

			return;
		}
		
	//	int h =  getHeight();
		if(isFloatWindow == false)
		mTotleDrawRow = (int) (getHeight()/(mCurSizeForOtherLrc+mCurPadding)+3);
		else
		{
			mTotleDrawRow = 3;
		}
		if(mTotleDrawRow == 0){
			//初始化将要绘制的歌词行数
			mTotleDrawRow = (int) (getHeight()/(mCurSizeForOtherLrc+mCurPadding))+4;
		}
		//因为不需要将所有歌词画出来
		int minRaw = mCurRow - (mTotleDrawRow-1)/2;
		int maxRaw = mCurRow + (mTotleDrawRow-1)/2;
		minRaw = Math.max(minRaw, 0);  //处理上边界
		maxRaw = Math.min(maxRaw, mLrcContents.size()-1);  //处理下边界
		//实现渐变的最大歌词行数
		int count = Math.max(maxRaw-mCurRow, mCurRow-minRaw);
		//两行歌词间字体颜色变化的透明度
		int alpha = (0xFF-0x11)/count;
		//画出来的第一行歌词的y坐标
		float rowY=0 ;
		//if(isFloatWindow == false)
			rowY= getHeight()/2 + minRaw*(mCurSizeForOtherLrc + mCurPadding);
		//else
			//rowY = mCurSizeForOtherLrc;
		for (int i = minRaw; i <= maxRaw; i++) {
			
			if(i == mCurRow){//画高亮歌词
				//因为有缩放效果，所有需要动态设置歌词的字体大小

				float textSize = mCurSizeForOtherLrc + (mCurSizeForHightLightLrc - mCurSizeForOtherLrc)*mCurFraction;
				mPaintForHighLightLrc.setTextSize(textSize);

				String text = mLrcContents.get(i).getLrcStr();//获取到高亮歌词
				float textWidth = mPaintForHighLightLrc.measureText(text);//用画笔测量歌词的宽度
				if(textWidth > getWidth()){
					//如果歌词宽度大于view的宽，则需要动态设置歌词的起始x坐标，以实现水平滚动
					canvas.drawText(text, mCurTextXForHighLightLrc, rowY, mPaintForHighLightLrc);
				}else{
					//如果歌词宽度小于view的宽，则让歌词居中显示
					float textX =  (getWidth()-textWidth)/2;
					canvas.drawText(text, textX, rowY, mPaintForHighLightLrc);
				}
				
				//Log.d("MYLRCView:","onDraw i == mCurRow");
			}else{
				if(i == mLastRow){//画高亮歌词的上一句
					//因为有缩放效果，所有需要动态设置歌词的字体大小
					float textSize = mCurSizeForHightLightLrc - (mCurSizeForHightLightLrc - mCurSizeForOtherLrc)*mCurFraction;
					//if(isFloatWindow == false)
						mPaintForOtherLrc.setTextSize(textSize);
					//else
					//	mPaintForOtherLrc.setTextSize( mCurSizeForOtherLrc + (mCurSizeForHightLightLrc - mCurSizeForOtherLrc)*mCurFraction);
				}else{//画其他的歌词
					mPaintForOtherLrc.setTextSize(mCurSizeForOtherLrc);
				}
				String text = mLrcContents.get(i).getLrcStr();
				float textWidth = mPaintForOtherLrc.measureText(text);
				float textX = (getWidth()-textWidth)/2;
				//如果计算出的textX为负数，将textX置为0(实现：如果歌词宽大于view宽，则居左显示，否则居中显示)
				textX = Math.max(textX, 0);
				//实现颜色渐变  从0xFFFFFFFF 逐渐变为 0x11FFFFFF(颜色还是白色，只是透明度变化)
				int curAlpha = 255-(Math.abs(i-mCurRow)-1)*alpha; //求出当前歌词颜色的透明度
				mPaintForOtherLrc.setColor(0x1000000*curAlpha+mCurColorForOtherLrc);
				canvas.drawText(text, textX, rowY, mPaintForOtherLrc);
				Log.d("MYLRCView:","onDraw i != mCurrow");
			}
			//计算出下一行歌词绘制的y坐标
			rowY += mCurSizeForOtherLrc + mCurPadding;
		}

		//画时间线和时间
		if(mIsDrawTimeLine){
			float y = getHeight()/2 + getScrollY();
			canvas.drawText(mLrcContents.get(mCurRow).getTimeStr(), 0, y-5, mPaintForTimeLine);
			canvas.drawLine(0, y, getWidth(), y, mPaintForTimeLine);
		}

	}
	
	public int getTextSize()
	{
		
		return (int) (mCurSizeForOtherLrc + (mCurSizeForHightLightLrc - mCurSizeForOtherLrc)*mCurFraction + mCurPadding);
	}
	
	public void setWmAndParams(WindowManager wm,WindowManager.LayoutParams params)
	{
		this.wm =wm;
		this.wmParams= params;
	}
	private void updatePosition(float x, float y){   
        // View的当前位置             params.x = (int)( x - startX);   
		if(Math.abs(y-firstY)<Math.abs(x-firstX))
			return;
		wmParams.y = (int) y;  
        wm.updateViewLayout(this, wmParams);   
}  
	
	/**是否可拖动歌词**/
	private boolean canDrag = false;
	/**事件的第一次的y坐标**/
	private float firstY;
	private float firstX;
	/**事件的上一次的y坐标**/
	private float lastY;
	private float lastX;
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if(isFloatWindow==false&&(mLrcContents == null || mLrcContents.size() == 0)){
			return false;
		}
		
		if(isFloatWindow == true)
		{
			
			if(floatViewTouchListener!=null)
			{
		
			   switch(event.getAction()) {   
			    	case MotionEvent.ACTION_DOWN:   
			    		floatViewTouchListener.onTouchDown(this,event);
	            		//firstX = event.getRawX();   
	            		//firstY = event.getRawY();   
	                break;   
	            case MotionEvent.ACTION_MOVE:  
	                	//updatePosition(event.getRawX(),event.getRawY()); 
	            	floatViewTouchListener.onTouchMove(this,event);
	                break;   
	            case MotionEvent.ACTION_UP:   
	                   // updatePosition(event.getX(),event.getY());   
	                   // firstX = firstY = 0;   
	            	floatViewTouchListener.onTouchUp(this,event);
	                 break;  
				}
			}
				//floatViewTouchListener.onTouch(this,event);
/*
		    switch(event.getAction()) {   
            case MotionEvent.ACTION_DOWN:   
            		firstX = event.getRawX();   
            		firstY = event.getRawY();   
                break;   
            case MotionEvent.ACTION_MOVE:  
                	updatePosition(event.getRawX(),event.getRawY());   
                break;   
            case MotionEvent.ACTION_UP:   
                   // updatePosition(event.getX(),event.getY());   
                    firstX = firstY = 0;   
                 break;   
		    }   */
        
		    return true;   
		} 
		else
		
		{
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				firstY = event.getRawY();
				lastX = event.getRawX();
				break;
			case MotionEvent.ACTION_MOVE:
				if(!canDrag){
					if(Math.abs(event.getRawY() - firstY) > mTouchSlop && Math.abs(event.getRawY()-firstY) > Math.abs(event.getRawX()-lastX)){
						canDrag = true;
						mIsDrawTimeLine = true;
						mScroller.forceFinished(true);
						stopScrollLrc();
						mCurFraction = 1;
					}
					lastY = event.getRawY();
				}
	
				if(canDrag){
					float offset = event.getRawY() - lastY;//偏移量
					if( getScrollY() - offset < 0){ 
						if(offset > 0){
							offset = offset/3;
						}
					}else if(getScrollY() - offset >mLrcContents.size()*(mCurSizeForOtherLrc+mCurPadding)-mCurPadding){
						if(offset < 0 ){
							offset = offset/3;
						}
					}
					scrollBy(getScrollX(), -(int) offset);
					lastY = event.getRawY();
					int currentRow = (int) (getScrollY()/(mCurSizeForOtherLrc+mCurPadding));
					currentRow = Math.min(currentRow, mLrcContents.size()-1);
					currentRow = Math.max(currentRow, 0);
					seekTo(mLrcContents.get(currentRow).getTime(), false,false);
					return true;
				}
				lastY = event.getRawY();
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				if(!canDrag){
					if(onLrcClickListener != null){
						onLrcClickListener.onClick();
					}
				}else{
					if(onSeekToListener!= null && mCurRow != -1){
						onSeekToListener.onSeekTo(mLrcContents.get(mCurRow).getTime());
					}
					if(getScrollY() < 0){
						smoothScrollTo(0,DURATION_FOR_ACTION_UP);
					}else if(getScrollY() >mLrcContents.size()*(mCurSizeForOtherLrc+mCurPadding)-mCurPadding){
						smoothScrollTo((int) (mLrcContents.size()*(mCurSizeForOtherLrc+mCurPadding)-mCurPadding),DURATION_FOR_ACTION_UP);
					}
	
					canDrag = false;
					mIsDrawTimeLine = false;
					invalidate();
				}
				break;
			}
		}
		return true;
	}
	
	/**
	 * 为LrcView设置歌词List集合数据
	 */
	@Override
	public void setLrcContents(List<LrcContent> lrcRows) {
		reset();
		this.mLrcContents = lrcRows;
		invalidate();
	}

	/**当前高亮歌词的行号**/
	private int mCurRow =-1;
	/**上一次的高亮歌词的行号**/
	private int mLastRow = -1;
	
	@Override
	public void seekTo(int progress,boolean fromSeekBar,boolean fromSeekBarByUser) {
		if(mLrcContents == null || mLrcContents.size() == 0){
			return;
		} 
		//如果是由seekbar的进度改变触发 并且这时候处于拖动状态，则返回
		if(fromSeekBar && canDrag){
			return;
		}
		for (int i = mLrcContents.size()-1; i >= 0; i--) {

			if(progress >= mLrcContents.get(i).getTime()){
				if(mCurRow != i){
					mLastRow = mCurRow;
					mCurRow = i;
					log("mCurRow=i="+mCurRow);
					if(fromSeekBarByUser){
						if(!mScroller.isFinished()){
							mScroller.forceFinished(true);
						}
						scrollTo(getScrollX(), (int) (mCurRow * (mCurSizeForOtherLrc+mCurPadding)));
					}else{
						smoothScrollTo( (int) (mCurRow * (mCurSizeForOtherLrc+mCurPadding)), DURATION_FOR_LRC_SCROLL);
					}
					//如果高亮歌词的宽度大于View的宽，就需要开启属性动画，让它水平滚动
					float textWidth = mPaintForHighLightLrc.measureText(mLrcContents.get(mCurRow).getLrcStr());
					log("textWidth="+textWidth+"getWidth()="+getWidth());
					if(textWidth > getWidth()){
						if(fromSeekBarByUser){
							mScroller.forceFinished(true);
						}
						log("开始水平滚动歌词:"+mLrcContents.get(mCurRow).getLrcStr());
						startScrollLrc(getWidth()-textWidth, (long) (mLrcContents.get(mCurRow).getTotalTime()*0.6));
					}
					
					Log.d("MYLRCView:","seekTo mCurrow ="+mCurRow);
					invalidate();
					
				}
				break;
			}
		}

	}

	/**
	 * 开始水平滚动歌词
	 * @param endX 歌词第一个字的最终的x坐标
	 * @param duration 滚动的持续时间
	 */
	private void startScrollLrc(float endX,long duration){
		if(mAnimator == null){
			mAnimator = ValueAnimator.ofFloat(0,endX);
			mAnimator.addUpdateListener(updateListener);
		}else{
			mCurTextXForHighLightLrc = 0;
			mAnimator.cancel();
			mAnimator.setFloatValues(0,endX);
		}
		mAnimator.setDuration(duration);
		mAnimator.setStartDelay((long) (duration*0.3)); //延迟执行属性动画
		mAnimator.start();
	}

	/**
	 * 停止歌词的滚动
	 */
	private void stopScrollLrc(){
		if(mAnimator != null){
			mAnimator.cancel();
		}
		mCurTextXForHighLightLrc = 0;
	}
	/**高亮歌词当前的其实x轴绘制坐标**/
	private float mCurTextXForHighLightLrc;
	/***
	 * 监听属性动画的数值值的改变
	 */
	AnimatorUpdateListener updateListener = new AnimatorUpdateListener() {

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			//TODO
			mCurTextXForHighLightLrc = (Float) animation.getAnimatedValue();
			log("mCurTextXForHighLightLrc="+mCurTextXForHighLightLrc);
			invalidate();
		}
	};
	/**
	 * 设置歌词的缩放比例
	 */
	@Override
	public void setLrcScalingFactor(float scalingFactor) {
		mCurScalingFactor = scalingFactor;
		mCurSizeForHightLightLrc = DEFAULT_SIZE_FOR_HIGHT_LIGHT_LRC * mCurScalingFactor;
		mCurSizeForOtherLrc = DEFAULT_SIZE_FOR_OTHER_LRC * mCurScalingFactor;
		mCurPadding = DEFAULT_PADDING * mCurScalingFactor;
		//log("mRowTotal="+mTotleDrawRow);
		//Log.i("fontscale:","mCurScalingFactor = " +mCurScalingFactor+",mCurSizeForHightLightLrc = "+mCurSizeForHightLightLrc+",mTotleDrawRow = "+mTotleDrawRow+");
		scrollTo(getScrollX(), (int) (mCurRow*(mCurSizeForOtherLrc+mCurPadding)));
		invalidate();
		mScroller.forceFinished(true);
	}
	/**
	 * 重置
	 */
	@Override
	public void reset() {
		if(!mScroller.isFinished()){
			mScroller.forceFinished(true);
		}
		mLrcContents = null;
		scrollTo(getScrollX(), 0);
		invalidate();
	}


	/**
	 * 平滑的移动到某处
	 * @param dstY
	 */
	private void smoothScrollTo(int dstY,int duration){
		int oldScrollY = getScrollY();
		int offset = dstY - oldScrollY;
		mScroller.startScroll(getScrollX(), oldScrollY, getScrollX(), offset, duration);
		invalidate();
	}

	@Override
	public void computeScroll() {
		if (!mScroller.isFinished()) {
			if (mScroller.computeScrollOffset()) {
				int oldY = getScrollY();
				int y = mScroller.getCurrY();
				if (oldY != y && !canDrag) {
					scrollTo(getScrollX(), y);
				}
				mCurFraction = mScroller.timePassed()*3f/DURATION_FOR_LRC_SCROLL;
				mCurFraction = Math.min(mCurFraction, 1F);
				invalidate();
			}
		} 
	}
	
	/**
	 * 返回当前的歌词缩放比例
	 * @return
	 */
	public float getmCurScalingFactor() {
		return mCurScalingFactor;
	}
	
	private OnSeekToListener onSeekToListener;
	public void setOnSeekToListener(OnSeekToListener onSeekToListener) {
		this.onSeekToListener = onSeekToListener;
	}

	public interface OnSeekToListener{
		void onSeekTo(int progress);
	}

	private OnLrcClickListener onLrcClickListener;
	public void setOnLrcClickListener(OnLrcClickListener onLrcClickListener) {
		this.onLrcClickListener = onLrcClickListener;
	}

	public interface OnLrcClickListener{
		void onClick();
	}
	public void log(Object o){
		Log.d("LrcView", o+"");
	}
	
	
	
	public void setIndex(int index){
		this.mCurRow = index;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	private float width;
	private float height;
	private Paint currentPaint;
	private Paint notCurrentPaint;
	private float textHeight = 35;
	private float textSize = 28;
	private int index = 0;
	
	private List<LrcContent> mLrcList = new ArrayList<LrcContent>();
	
	public void setLrcList(List<LrcContent> mLrcList){
		this.mLrcList = mLrcList;
	}
	
	public LrcView(Context context){
		super(context);
		init();
	}
	
	public LrcView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		init();
	}
	
	public LrcView(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}
	
	private void init(){
		setFocusable(true);
		
		currentPaint = new Paint();
		currentPaint.setAntiAlias(true);
		currentPaint.setTextAlign(Paint.Align.CENTER);
		
		notCurrentPaint = new Paint();
		notCurrentPaint.setAntiAlias(true);
		notCurrentPaint.setTextAlign(Paint.Align.CENTER);
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		if(canvas == null){
			return;
		}
		
		currentPaint.setColor(Color.BLACK);
		currentPaint.setTypeface(Typeface.SERIF);
		currentPaint.setTextSize(textSize + 10);
		
		notCurrentPaint.setColor(Color.BLUE);
		notCurrentPaint.setTextSize(textSize);
		notCurrentPaint.setTypeface(Typeface.DEFAULT);
		
		try{
			
			canvas.drawText(mLrcList.get(index).getLrcStr(), width/2, height/2, currentPaint);
			
			float tempY = height / 2;
			
			for(int i = index - 1; i >= 0; i--){
				tempY = tempY - textHeight;
				canvas.drawText(mLrcList.get(i).getLrcStr(), width/2, tempY, notCurrentPaint);
			}
			
			tempY = height / 2;
			
			for(int i = index + 1; i < mLrcList.size() - 1; i++){
				tempY = tempY + textHeight;
				canvas.drawText(mLrcList.get(i).getLrcStr(), width/2, tempY, notCurrentPaint);
			}
		}catch(Exception e){
			//setText("....沒有歌詞，趕緊下載吧少年！");
			//setTextSize(textSize + 20);
			//setTextColor(Color.BLACK);
			canvas.drawText("小黃人找不到歌詞哦", width/2, height/2, currentPaint);
		}
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh){
		super.onSizeChanged(w, h, oldw, oldh);
		this.width = w;
		this.height = h;
	}
	
	public void setIndex(int index){
		this.index = index;
	}

	private int startTouchY;
	private int startTouchX;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction()==MotionEvent.ACTION_DOWN)
		{
			startTouchX = (int)event.getX();
			startTouchY = (int)event.getY();
			PlayMusicService.pauseMusic();
			return true;
		}
		else if(event.getAction()==MotionEvent.ACTION_UP)
		{
			/*
			int moveX = (int)event.getX() - startTouchX ;
			int moveY = (int)event.getY() - startTouchY;
			if(Math.abs(moveX)<Math.abs(moveY))
			{
				if(Math.abs(moveY)<textSize)
					return super.onTouchEvent(event);
				
				int i = (int) (moveY/textSize);
				this.index +=i;
				invalidate();
			
				
				return true;
				
			}
			//PlayMusicService.resumeMusic();
			
			
			int moveY = (int)event.getY() - startTouchY;
			
			int i = (int) (moveY/textSize/2);
			this.index -=i;
			
			invalidate();
			PlayMusicService.resumeMusic();
			PlayMusicService.setlrcIndex(index);
		}
		else if(event.getAction()==MotionEvent.ACTION_MOVE)
		{

			return true;
		}
		return super.onTouchEvent(event);
	}*/
	
	
}