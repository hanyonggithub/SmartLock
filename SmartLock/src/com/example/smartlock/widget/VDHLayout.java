package com.example.smartlock.widget;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartlock.R;
import com.example.smartlock.activity.MainActivity2;
import com.example.smartlock.utils.DataFormatUtils;

public class VDHLayout extends RelativeLayout {
	private ViewDragHelper mDragger;

	private TextView tvw_key;
	private TextView tvw_lock;
	private Point mAutoBackOriginPos = new Point();
	private Point lockPos=new Point();
	public Context context;
	boolean isOpen=false;
	boolean beginOpen=false;
	public VDHLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context=context;
		mDragger = ViewDragHelper.create(this, 1.0f,
				new ViewDragHelper.Callback() {
					@Override
					public boolean tryCaptureView(View child, int pointerId) {
						if(tvw_key==child){
							return true;
						}
						return false;
					}

					@Override
					public int clampViewPositionHorizontal(View child,
							int left, int dx) {
//						final int leftBound = getPaddingLeft();
//						final int rightBound = getWidth() - child.getWidth()
//								- leftBound;
//
//						final int newLeft = Math.min(Math.max(left, leftBound),
//								rightBound);

						return mAutoBackOriginPos.x;
					}

					@Override
					public int clampViewPositionVertical(View child, int top,
							int dy) {
						if(child==tvw_key){
							final int topBound=lockPos.y+tvw_lock.getHeight()/4;
							final int bottomBound=mAutoBackOriginPos.y;
							if(top<bottomBound-tvw_key.getHeight()/2){
//								if(!((MainActivity)context).){
//									isOpen=true;
//									startOpenAnim();
//								}
								if(!beginOpen){
									openLock();
									beginOpen=true;
								}
								
								
							}else{
								beginOpen=false;
							}
							return Math.min(Math.max(top, topBound), bottomBound);
						}
						return top;
					}
					
		            @Override
		            public void onViewReleased(View releasedChild, float xvel, float yvel)
		            {
		                if (releasedChild == tvw_key)
		                {
		                    mDragger.settleCapturedViewAt(mAutoBackOriginPos.x, mAutoBackOriginPos.y);
		                    tvw_key.setSelected(false);
		                    invalidate();
		                }
		            }

		            @Override
		            public void onEdgeDragStarted(int edgeFlags, int pointerId)
		            {
		            }
		            
		            @Override
		        	public int getViewHorizontalDragRange(View child)
		        	{
		        	     return getMeasuredWidth()-child.getMeasuredWidth();
		        	}

		        	@Override
		        	public int getViewVerticalDragRange(View child)
		        	{
		        	     return getMeasuredHeight()-child.getMeasuredHeight();
		        	}
				});
		
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		return mDragger.shouldInterceptTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mDragger.processTouchEvent(event);
		return true;
	}
	
	
	@Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        tvw_key = (TextView) findViewById(R.id.tvw_key);
        tvw_lock=(TextView) findViewById(R.id.tvw_lock);
        if(tvw_key==null){
        	tvw_key=new TextView(context);
        }
        if(tvw_lock==null){
        	tvw_lock=new TextView(context);
        }
        tvw_key.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					v.setSelected(true);
					break;
				case MotionEvent.ACTION_UP:
					v.setSelected(false);
					break;

				default:
					break;
				}
				return true;
			}
		});
    }
	
	
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        super.onLayout(changed, l, t, r, b);

        mAutoBackOriginPos.x = tvw_key.getLeft();
        mAutoBackOriginPos.y = tvw_key.getTop();
        lockPos.x=tvw_lock.getLeft();
        lockPos.y=tvw_lock.getTop();
        
    }
    
    
    @Override
    public void computeScroll()
    {
        if(mDragger.continueSettling(true))
        {
            invalidate();
        }
    }
    public void openLock(){
    	if(((MainActivity2)context).mCharacteristic==null||!((MainActivity2)context).mConnected){
    		
		}else if(((MainActivity2)context).mCharacteristic!=null&&((MainActivity2)context).mConnected&&((MainActivity2)context).hasLogin){
			if(!((MainActivity2)context).isOpen){
	    		((MainActivity2)context).openLock();
	    	}else{
//	    		Toast.makeText(context, "锁已开！", Toast.LENGTH_SHORT).show();
	    	}
		}else{
		
		}
    	
    }
    
    public void startOpenAnim(){
    	Animation rotateAnim=new RotateAnimation(0, 60, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.6f);
    	rotateAnim.setDuration(300);
    	rotateAnim.setFillAfter(true);
    	tvw_lock.startAnimation(rotateAnim);
    }
    public void startCloseAnim(){
    	Animation rotateAnim=new RotateAnimation(60, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.6f);
    	rotateAnim.setDuration(300);
    	rotateAnim.setFillAfter(true);
    	tvw_lock.startAnimation(rotateAnim);
    }
    
    
}
