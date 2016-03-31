package com.example.smartlock.fragment;

import com.example.smartlock.base.BaseFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class testFragment extends BaseFragment {

	@Override
	public View initView(LayoutInflater inflater) {
		TextView tvw=new TextView(mActivity);
		tvw.setText("test fragment");
		return tvw;
	}

}
