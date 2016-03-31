package com.example.smartlock.base;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.smartlock.activity.MainActivity;
import com.example.smartlock.callback.TitleBarClickListener;

/**
 * @author andong
 * ����fragment�Ļ���
 */
public abstract class BaseFragment extends Fragment{
	
	public Activity mActivity; // ��fragment�󶨵��ĸ�Activity��, �����Ķ�������Ǹ�Activity.
	public Resources mRes;
	public boolean hasTitle=true;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = getActivity();
		mRes=mActivity.getResources();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = initView(inflater);
		if(view!=null&&view.getParent()!=null){
			((ViewGroup) view.getParent()).removeView(view);
		}
		return view;
	}
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initData();
	}
	
	public abstract View initView(LayoutInflater inflater);
	
	public void initData(){
		
		
	};
	
}
