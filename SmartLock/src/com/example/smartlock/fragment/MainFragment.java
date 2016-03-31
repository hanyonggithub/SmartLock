package com.example.smartlock.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.logging.Level;

import com.example.smartlock.R;
import com.example.smartlock.activity.MainActivity2;
import com.example.smartlock.base.BaseFragment;
import com.example.smartlock.utils.Constants;
import com.example.smartlock.utils.DataFormatUtils;
import com.example.smartlock.utils.SharePreferenceUtil;
import com.example.smartlock.widget.BatteryView;

public class MainFragment extends BaseFragment implements OnClickListener {
	private final static String TAG=MainFragment.class.getSimpleName();
	TextView tvw_lock;
	TextView tvw_key;
	private RelativeLayout llt_main;
	private FrameLayout fl_left_btn;
	private boolean autoOpen = true;
	private TextView tv_center_title;
	private TextView tvw_battery_level_value;
	private TextView swither_status;
	private BatteryView bat;

	@Override
	public View initView(LayoutInflater inflater) {
		llt_main = (RelativeLayout) inflater.inflate(R.layout.mian_pager, null);
		return llt_main;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Log.e(TAG, "mainfragment onViewcreated---begin");
		fl_left_btn = (FrameLayout) view.findViewById(R.id.fl_left_btn);
		FrameLayout fl_right_btn = (FrameLayout) view.findViewById(R.id.fl_right_btn);
		tv_center_title = (TextView) view.findViewById(R.id.tv_center_title);
		
		Drawable drawble_battery_frame = mRes.getDrawable(R.drawable.battery_frame);
		int width = drawble_battery_frame.getIntrinsicWidth();
		int height = drawble_battery_frame.getIntrinsicHeight();
		
		bat = new BatteryView(mActivity);
		bat.setLayoutParams(new LayoutParams(width, height*2/3));
		fl_right_btn.addView(bat);
		bat.setLevel(((MainActivity2) mActivity).mDevBatLevel);
		tvw_battery_level_value=(TextView) view.findViewById(R.id.tvw_battery_level_value);
		tvw_battery_level_value.setText(((MainActivity2) mActivity).mDevBatLevel+"%");
		if(((MainActivity2)mActivity).mCharacteristic==null){
			tv_center_title.setText("NO-Device");
		}else{
			tv_center_title.setText(DataFormatUtils.deleteOdd(((MainActivity2)mActivity).mDeviceName));
		}
	
		swither_status=(TextView) view.findViewById(R.id.swither_status);
		boolean atuo=SharePreferenceUtil.getBoolean(mActivity,Constants.KEY_AUTO);
		swither_status.setText(atuo?mActivity.getResources().getString(R.string.auto_unlock):mActivity.getResources().getString(R.string.manual_unlock));
		fl_left_btn.setBackgroundResource(atuo?R.drawable.switcher_open:R.drawable.switcher_close);
//		swither_status.setText(((MainActivity2) mActivity).isAuto?"Auto Unlock":"Manual Unlock");
//		fl_left_btn.setBackgroundResource(((MainActivity2) mActivity).isAuto?R.drawable.switcher_open:R.drawable.switcher_close);
		tvw_key = (TextView) view.findViewById(R.id.tvw_key);
		tvw_lock = (TextView) view.findViewById(R.id.tvw_lock);

		// TODO:考虑更合适的位置适配方法
		tvw_lock.setTop(tvw_lock.getTop() - tvw_lock.getHeight());
		tvw_lock.setX(tvw_lock.getX() - tvw_lock.getHeight());
		fl_left_btn.setOnClickListener(this);
		if(((MainActivity2) mActivity).isOpen){
			keepOpen();
		}else{
			tvw_lock.clearAnimation();
		}

		// 设置默认的锁的状态
		Log.e(TAG, "mainfragment onViewcreated---end");
	}

	@Override
	public void initData() {
		super.initData();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fl_left_btn:
			if (autoOpen) {
				fl_left_btn.setBackgroundResource(R.drawable.switcher_close);
				autoOpen = false;
				swither_status.setText("Manual Unlock");
				Log.e(TAG, "autoOpen="+autoOpen);
				SharePreferenceUtil.putBoolean(mActivity, Constants.KEY_AUTO, autoOpen);
			} else {
				fl_left_btn.setBackgroundResource(R.drawable.switcher_open);
				autoOpen = true;
				swither_status.setText("Auto Unlock");
				Log.e(TAG, "autoOpen="+autoOpen);
				SharePreferenceUtil.putBoolean(mActivity, Constants.KEY_AUTO, autoOpen);
				//如果设为自定，则立即执行一次开锁
				if(!((MainActivity2)mActivity).isOpen){
					((MainActivity2)mActivity).openLock();
				}
				// 本地存储
			}
			((MainActivity2)mActivity).isAuto=autoOpen;
			break;

		default:
			break;
		}
	}

	public void setBatLevel(int level) {
//		Toast.makeText(mActivity, "设置电量为：" + level + "%", Toast.LENGTH_SHORT).show();
		if(bat!=null&&tvw_battery_level_value!=null){
			bat.setLevel(level);
			tvw_battery_level_value.setText(level+"%");
		}
	}
	public void setTitle(String title){
		if(tv_center_title!=null){
			tv_center_title.setText(title);
		}
		
	}

	public void startOpenAnim() {
		Animation rotateAnim = new RotateAnimation(0, 60, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rotateAnim.setDuration(300);
		rotateAnim.setFillAfter(true);
		tvw_lock.startAnimation(rotateAnim);
	}

	public void startCloseAnim() {
		Animation rotateAnim = new RotateAnimation(60, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rotateAnim.setDuration(300);
		rotateAnim.setFillAfter(true);
		tvw_lock.startAnimation(rotateAnim);
	}
	public void keepOpen(){
		Animation rotateAnim = new RotateAnimation(0, 60, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rotateAnim.setDuration(0);
		rotateAnim.setFillAfter(true);
		tvw_lock.startAnimation(rotateAnim);
	}

}
