package com.example.smartlock.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class BatteryView extends View{
	private Context mContext;
	private Paint paint;
	private float frameWidth;
	private int level;
	public BatteryView(Context context) {
		super(context);
		mContext=context;
		init();
	}
	public BatteryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext=context;
		init();
	}
	private void init(){
		paint=new Paint();
		paint.setColor(Color.parseColor("#48A7C7"));
		paint.setAntiAlias(true);
	
		paint.setStyle(Style.STROKE);
	
	}
	public void setLevel(int level){
		this.level=level;
		postInvalidate();
	}
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int width=getWidth();
		int height=getHeight();
		frameWidth=width/15;
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(frameWidth);
		canvas.drawLine(frameWidth/2, height/4, frameWidth/2, height*3/4, paint);
		
		RectF rectF=new RectF(frameWidth*3/2,frameWidth/2,width-frameWidth/2,height-frameWidth/2);
		
		canvas.drawRoundRect(rectF, frameWidth, frameWidth, paint);
		float levelLen=(width-frameWidth*3)*level/100f;
		paint.setStyle(Style.FILL);
		canvas.drawRect(width-frameWidth-levelLen+2, frameWidth+2, width-frameWidth-2, height-frameWidth-2, paint);
		
	}


}
