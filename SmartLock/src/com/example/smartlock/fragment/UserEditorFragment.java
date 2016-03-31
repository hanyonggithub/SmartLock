package com.example.smartlock.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.MissingFormatArgumentException;

import com.example.smartlock.R;
import com.example.smartlock.activity.MainActivity2;
import com.example.smartlock.base.BaseFragment;
import com.example.smartlock.utils.Constants;
import com.example.smartlock.utils.DataFormatUtils;

public class UserEditorFragment extends BaseFragment implements OnClickListener, OnTouchListener{
	RelativeLayout rlt_root;
	ImageView iv_delete;
	TextView tvw_username;
	TextView tvw_switcher;
	TextView tvw_edit;
	Boolean isEnabled=true;
	RelativeLayout rlt_user_info;
	private FrameLayout fl_left_btn;
	private FrameLayout fl_right_btn;
	private String name;
	private String psw;
	private String userIndex;

	@Override
	public View initView(LayoutInflater inflater) {
		rlt_root=(RelativeLayout) inflater.inflate(R.layout.user_editor_pager, null);
		
		return rlt_root;
	}
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		rlt_user_info=(RelativeLayout) view.findViewById(R.id.rlt_user_info);
		fl_left_btn = (FrameLayout) view
				.findViewById(R.id.fl_left_btn);
		fl_right_btn = (FrameLayout) view
				.findViewById(R.id.fl_right_btn);
		fl_left_btn.removeAllViews();
		fl_right_btn.removeAllViews();
		TextView tv_center_title = (TextView) view
				.findViewById(R.id.tv_center_title);
		TextView tvw_right_btn = new TextView(mActivity);
   		tvw_right_btn.setText("OK");
   		tvw_right_btn.setTextSize(20);
		tvw_right_btn.setTextColor(mRes.getColor(R.color.sm_46a6c7));
   		fl_right_btn.addView(tvw_right_btn);
   		tv_center_title.setText("User");
   		ImageView iv_left_btn=new ImageView(mActivity);
		iv_left_btn.setImageResource(R.drawable.arr_left);
		fl_left_btn.addView(iv_left_btn);
		
		iv_delete=(ImageView) view.findViewById(R.id.iv_delete);
		tvw_username=(TextView) view.findViewById(R.id.tvw_username);
		tvw_switcher=(TextView) view.findViewById(R.id.tvw_switcher);
		tvw_edit=(TextView) view.findViewById(R.id.tvw_edit);
		iv_delete.setOnClickListener(this);
		tvw_switcher.setOnClickListener(this);
		tvw_edit.setOnClickListener(this);
		view.setOnTouchListener(this);
		
		fl_left_btn.setOnClickListener(this);
		fl_right_btn.setOnClickListener(this);
		
		Bundle bundle=getArguments();
		name=bundle.getString(Constants.KEY_NAME);
		psw=bundle.getString(Constants.KEY_PSW);
		userIndex=bundle.getString(Constants.KEY_INDEX);
		isEnabled=bundle.getBoolean(Constants.KEY_ENABLED);
		
		tvw_username.setText(DataFormatUtils.hexStr2Str(name));
		if(isEnabled){
			tvw_switcher.setBackgroundResource(R.drawable.switch_enabled);
			isEnabled=true;
		}else{
			tvw_switcher.setBackgroundResource(R.drawable.switch_disabled);
			isEnabled=false;
		}
		
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_delete:
			rlt_user_info.removeAllViews();
			((MainActivity2)mActivity).deleteUser(userIndex);
			break;
		case R.id.tvw_switcher:
			if(isEnabled){
				tvw_switcher.setBackgroundResource(R.drawable.switch_disabled);
				isEnabled=false;
				((MainActivity2)mActivity).pauseUser(userIndex);
			}else{
				tvw_switcher.setBackgroundResource(R.drawable.switch_enabled);
				isEnabled=true;
				((MainActivity2)mActivity).enableUser(userIndex);
			}
			
			break;
		case R.id.tvw_edit:
			//
			NameAndPswFragment nameAndPswFrg=new NameAndPswFragment();
			Bundle bundle=new Bundle();
			bundle.putInt(Constants.KEY_TYPE, Constants.type_edit);
			bundle.putString(Constants.KEY_NAME, name);
			bundle.putString(Constants.KEY_PSW, psw);
			bundle.putString(Constants.KEY_INDEX, userIndex);
			nameAndPswFrg.setArguments(bundle);
			getFragmentManager().beginTransaction().replace(R.id.rlt_user_pager_root, nameAndPswFrg).addToBackStack("nameAndPsw").commit();
			break;
			
		case R.id.fl_left_btn:
			getFragmentManager().popBackStack();
			break;
		case R.id.fl_right_btn:
			getFragmentManager().popBackStack();
			break;
		default:
			break;
		}
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}

}
