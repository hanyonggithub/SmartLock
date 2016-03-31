package com.example.smartlock.base;

import com.example.smartlock.R;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;


public class BasePager implements OnClickListener {
	public Context mContext;
	public TextView tv_center_text;
	public FrameLayout fl_left_btn;
	public FrameLayout fl_right_btn;
	public FrameLayout fl_content;
	private View rootView;
	private OnTitleClickListener listener;

	public BasePager(Context context) {
		this.mContext = context;
		rootView = initView();
	}

	private View initView() {
		View view = View.inflate(mContext, R.layout.base_pager, null);
		fl_left_btn=(FrameLayout) view.findViewById(R.id.fl_left_btn);
		fl_right_btn=(FrameLayout) view.findViewById(R.id.fl_right_btn);
		tv_center_text=(TextView) view.findViewById(R.id.tv_center_title);
		fl_content=(FrameLayout) view.findViewById(R.id.fl_base_pager_content);
		fl_left_btn.setOnClickListener(this);
		fl_right_btn.setOnClickListener(this);
		return view;
	}
	
	/**
	 * 获得当前页面布局对象
	 * @return
	 */
	public View getRootView() {
		return rootView;
	}
	
	public void initData() {
		
	}
	
	

	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.fl_left_btn){
			listener.onLeftBtnClick();
		}else if(v.getId()==R.id.fl_right_btn){
			listener.onRightBtnClick();
		}
	}
	
	public void setOnTitleClickListener(OnTitleClickListener listener){
		this.listener=listener;
	}
	
	public interface OnTitleClickListener{
		public void onLeftBtnClick();
		public void onRightBtnClick();
	}
}
