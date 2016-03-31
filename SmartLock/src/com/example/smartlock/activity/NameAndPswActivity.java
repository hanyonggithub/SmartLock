package com.example.smartlock.activity;

import com.example.smartlock.base.BasePager.OnTitleClickListener;
import com.example.smartlock.pager.NameAndPswPager;

import android.app.Activity;
import android.os.Bundle;

public class NameAndPswActivity extends Activity implements OnTitleClickListener {
	NameAndPswPager pager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pager=new NameAndPswPager(this);
		setContentView(pager.getRootView());
		pager.setOnTitleClickListener(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
//		finish();
	}

	@Override
	public void onLeftBtnClick() {
		finish();
	}

	@Override
	public void onRightBtnClick() {
		//跳转到开锁界面
		finish();
		
		
	}
	
	
}
