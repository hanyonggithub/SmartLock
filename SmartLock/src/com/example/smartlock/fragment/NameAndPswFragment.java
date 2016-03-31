package com.example.smartlock.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartlock.R;
import com.example.smartlock.activity.MainActivity2;
import com.example.smartlock.base.BaseFragment;
import com.example.smartlock.utils.Constants;
import com.example.smartlock.utils.DataFormatUtils;

public class NameAndPswFragment extends BaseFragment implements OnClickListener, OnTouchListener, TextWatcher {

	private EditText et_name;
	private EditText et_psw;
	private ImageView iv_psw_visiblility;
	private boolean isVisable = false;
	private FrameLayout fl_left_btn;
	private FrameLayout fl_right_btn;
	private String name;
	private String psw;
	private String userIndex;
	private String addr;
	private String mDeviceName;
	private int type = -1;
	private String new_psw_first;

	@Override
	public View initView(LayoutInflater inflater) {
		View view = inflater.inflate(R.layout.name_and_psw, null);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		fl_left_btn = (FrameLayout) view.findViewById(R.id.fl_left_btn);
		fl_right_btn = (FrameLayout) view.findViewById(R.id.fl_right_btn);
		fl_left_btn.removeAllViews();
		fl_right_btn.removeAllViews();
		TextView tv_center_title = (TextView) view.findViewById(R.id.tv_center_title);
		ImageView iv_left = new ImageView(mActivity);
		iv_left.setImageResource(R.drawable.arr_left);
		fl_left_btn.addView(iv_left);
		
		TextView tvw_right_btn = new TextView(mActivity);
		tvw_right_btn.setText("OK");
		tvw_right_btn.setTextSize(20);
		tvw_right_btn.setTextColor(mRes.getColor(R.color.sm_46a6c7));
		fl_right_btn.addView(tvw_right_btn);

		et_name = (EditText) view.findViewById(R.id.et_name);
		et_name.addTextChangedListener(new TextWatcher(){

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
				
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if (s == null || s.length() <= 0) {
					return;
				}
				String temp = s.toString();
				String tem = temp.substring(temp.length() - 1, temp.length());
				char[] temC = tem.toCharArray();
				int mid = temC[0];
				if (mid >= 65 && mid <= 90) {// 大写字母
					return;
				}
				if (mid >= 97 && mid <= 122) {// 小写字母
					return;
				}
				if (String.valueOf((char) mid).equals("_")) {
					return;
				}
				Toast.makeText(mActivity, mActivity.getResources().getString(R.string.name_not_invalid), Toast.LENGTH_SHORT).show();
				s.delete(temp.length() - 1, temp.length());

				if (temp.length() > 6) {
					Toast.makeText(mActivity, "用户名不能超过六位", Toast.LENGTH_SHORT).show();
					s.delete(5, temp.length());
				}

				
			}
			
		});
		et_psw = (EditText) view.findViewById(R.id.et_psw);
		et_psw.addTextChangedListener(this);
		iv_psw_visiblility = (ImageView) view.findViewById(R.id.iv_psw_visiblility);
		iv_psw_visiblility.setOnClickListener(this);
		view.setOnTouchListener(this);
		fl_left_btn.setOnClickListener(this);
		fl_right_btn.setOnClickListener(this);

		Bundle bundle = getArguments();
		if (bundle != null) {
			addr = bundle.getString(Constants.KEY_DEVICE_ADDR);
			mDeviceName = bundle.getString(Constants.KEY_DEVICE_NAME);
			type = bundle.getInt(Constants.KEY_TYPE);
			name = bundle.getString(Constants.KEY_NAME);
			psw = bundle.getString(Constants.KEY_PSW);
			userIndex = bundle.getString(Constants.KEY_INDEX);
		}
		if (!TextUtils.isEmpty(name)) {
			et_name.setText(DataFormatUtils.hexStr2CleanStr(name));
		}
//		if (!TextUtils.isEmpty(psw)) {
//			et_psw.setText(DataFormatUtils.hexStr2CleanStr(psw));
//		}
		
		switch (type) {
		case Constants.type_create:
			tv_center_title.setText(mActivity.getResources().getString(R.string.create_user));
			break;
		case Constants.type_edit:
			tv_center_title.setText(mActivity.getResources().getString(R.string.edit_user));
			break;
		default:
			tv_center_title.setText(mActivity.getResources().getString(R.string.login));
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_psw_visiblility:
			if (isVisable) {
				iv_psw_visiblility.setImageResource(R.drawable.unvisible);
				et_psw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				isVisable = false;
			} else {
				iv_psw_visiblility.setImageResource(R.drawable.visible);
				et_psw.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				isVisable = true;
			}
			break;
		case R.id.fl_left_btn:
			getFragmentManager().popBackStack();
			break;
		case R.id.fl_right_btn:
			
			InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(mActivity.INPUT_METHOD_SERVICE);  
			//得到InputMethodManager的实例 
			if (imm.isActive(et_psw)||imm.isActive(et_name)) { 
				
				getView().requestFocus();//强制获取焦点，不然getActivity().getCurrentFocus().getWindowToken()会报错
				imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				imm.restartInput(et_psw);
				/* if(mActivity.getWindow().getAttributes().softInputMode==WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

				 {
				 //隐藏软键盘
					 mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
				 }*/
			//如果开启 
//			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS); 
			//关闭软键盘，开启方法相同，这个方法是切换开启与关闭状态的 
			} 
			name = et_name.getText().toString();
			psw = et_psw.getText().toString();
			if (name.length() <= 0 || psw.length() <= 0) {
				Toast.makeText(mActivity, mActivity.getResources().getString(R.string.name_psw_not_null), Toast.LENGTH_SHORT).show();
				return;
			}
			// if (psw.length() < 6) {
			// // 如果是第一次登陆注册，提示密码应为六位
			// Toast.makeText(mActivity, "密码不足六位", Toast.LENGTH_SHORT).show();
			// return;
			// }
			if (name.length() >= 6) {
				name = name.substring(0, 6);
			}
			String name_hex = DataFormatUtils.str2hexStr(name, 12);
			if (psw.length() >= 6) {
				psw = psw.substring(0, 6);
			}
			String psw_hex = DataFormatUtils.str2hexStr(psw, 12);
			if (psw_hex.length() > 12) {
				psw = psw_hex.substring(0, 12);
			}
			if (type == Constants.type_login) {
				((MainActivity2) mActivity).login(name_hex, psw_hex, addr, mDeviceName);
				getFragmentManager().popBackStack();
				((MainActivity2) mActivity).switchTab(1);
			} else if (type == Constants.type_create) {
				((MainActivity2) mActivity).create(name_hex, psw_hex, addr, mDeviceName);
				getFragmentManager().popBackStack();
			} else if (type == Constants.type_edit) {
				if(!TextUtils.isEmpty(new_psw_first)){
					if(new_psw_first.equals(psw_hex)){
						((MainActivity2) mActivity).editUser(name_hex, psw_hex, addr, mDeviceName, userIndex);
						getFragmentManager().popBackStack();
						new_psw_first="";
					}else{
						Toast.makeText(getActivity(),mActivity.getResources().getString(R.string.psw_confirm_fail), Toast.LENGTH_SHORT).show();
						et_psw.setHint(mActivity.getResources().getString(R.string.confirm_psw));
						et_psw.setHintTextColor(Color.RED);
					}
					
				}else{
					new_psw_first=psw_hex;
					et_psw.setText("");
					et_psw.setHint(mActivity.getResources().getString(R.string.confirm_psw));
					et_psw.setHintTextColor(Color.RED);
				}
			}
			break;

		default:
			break;
		}

	}
	@Override
	public void onPause() {
		super.onPause();
		InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(mActivity.INPUT_METHOD_SERVICE);  
		//得到InputMethodManager的实例 
		if (imm.isActive(et_psw)||imm.isActive(et_name)) { 
			
			getView().requestFocus();//强制获取焦点，不然getActivity().getCurrentFocus().getWindowToken()会报错
			imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			imm.restartInput(et_psw);
		} 
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// if (count > 6) {
		// Toast.makeText(mActivity, "密码码必须为六位", Toast.LENGTH_SHORT).show();
		// s = s.subSequence(0, 6);
		// }
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (s == null || s.length() <= 0) {
			return;
		}
		String temp = s.toString();
		String tem = temp.substring(temp.length() - 1, temp.length());
		char[] temC = tem.toCharArray();
		int mid = temC[0];
		if (mid >= 48 && mid <= 57) {// 数字
			return;
		}
		if (mid >= 65 && mid <= 90) {// 大写字母
			return;
		}
		if (mid >= 97 && mid <= 122) {// 小写字母
			return;
		}
		if (String.valueOf((char) mid).equals("_")) {
			return;
		}
		Toast.makeText(mActivity, mActivity.getResources().getString(R.string.name_psw_not_invalid), Toast.LENGTH_SHORT).show();
		s.delete(temp.length() - 1, temp.length());

		if (temp.length() > 6) {
			Toast.makeText(mActivity, "密码不能超过六位", Toast.LENGTH_SHORT).show();
			s.delete(5, temp.length());
		}

	}

}
