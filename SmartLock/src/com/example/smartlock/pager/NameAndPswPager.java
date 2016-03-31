package com.example.smartlock.pager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.smartlock.R;
import com.example.smartlock.base.BasePager;

public class NameAndPswPager extends BasePager {
	EditText et_name;
	EditText et_psw;
	ImageView iv_psw_visiblility;
	public NameAndPswPager(Context context) {
		super(context);
		
		fl_left_btn.setBackgroundResource(R.drawable.back);
		fl_right_btn.removeAllViews();
		TextView tv_Ok=new TextView(mContext);
		tv_Ok.setText(R.string.btn_ok);
		fl_right_btn.addView(tv_Ok);
		tv_center_text.setText(R.string.title_lock);
		
		View view=LayoutInflater.from(mContext).inflate(R.layout.name_and_psw, null);
		fl_content.addView(view);
		et_name=(EditText) view.findViewById(R.id.et_name);
		et_psw=(EditText) view.findViewById(R.id.et_psw);
		iv_psw_visiblility=(ImageView) view.findViewById(R.id.iv_psw_visiblility);
	}
	
}
