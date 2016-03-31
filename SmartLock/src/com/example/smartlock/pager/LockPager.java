package com.example.smartlock.pager;

import android.content.Context;
import android.widget.Switch;

import com.example.smartlock.R;
import com.example.smartlock.base.BasePager;

public class LockPager extends BasePager {

	public LockPager(Context context) {
		super(context);
	}
	@Override
	public void initData() {
		super.initData();
		fl_left_btn.removeAllViews();
		Switch switcher =new Switch(mContext);
		fl_left_btn.addView(switcher);
		fl_right_btn.setBackgroundResource(R.drawable.refresh);
		tv_center_text.setText(R.string.title_list);
	}
	
	
}
