package com.example.smartlock.fragment;

import java.util.ArrayList;
import java.util.List;

import android.R.color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartlock.R;
import com.example.smartlock.activity.MainActivity2;
import com.example.smartlock.base.BaseFragment;
import com.example.smartlock.entity.UserInfo;
import com.example.smartlock.utils.Constants;
import com.example.smartlock.utils.DataFormatUtils;

public class UserFragment extends BaseFragment implements OnItemClickListener, OnClickListener {
	TextView tvw_super_user_name;
	TextView tvw_last_action;
	ListView lvw_users;
	List<UserInfo> users = new ArrayList<UserInfo>();
	private int selectedItem=0;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		userAdapter.notifyDataSetChanged();
	}
	@Override
	public View initView(LayoutInflater inflater) {
		View view = inflater.inflate(R.layout.user_pager, null);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		fl_left_btn = (FrameLayout) view
				.findViewById(R.id.fl_left_btn);
		fl_right_btn = (FrameLayout) view
				.findViewById(R.id.fl_right_btn);
		tv_center_title = (TextView) view
				.findViewById(R.id.tv_center_title);
		fl_left_btn.removeAllViews();
		fl_right_btn.removeAllViews();
		ImageView iv_left_btn=new ImageView(mActivity);
		iv_left_btn.setImageResource(R.drawable.arr_left);
		fl_left_btn.addView(iv_left_btn);
		fl_right_btn.setBackgroundResource(R.drawable.add);
		tv_center_title.setText("User");
		tvw_super_user_name = (TextView) view
				.findViewById(R.id.tvw_super_user_name);
		tvw_last_action = (TextView) view.findViewById(R.id.tvw_last_action);
		setOwnerInfo();
		lvw_users = (ListView) view.findViewById(R.id.lvw_users);
		lvw_users.setAdapter(userAdapter);
		lvw_users.setOnItemClickListener(this);
		fl_left_btn.setOnClickListener(this);
		fl_right_btn.setOnClickListener(this);
	}
	public void setOwnerInfo() {
		if(tvw_super_user_name==null||tvw_last_action==null){
			return;
		}
		if(((MainActivity2)mActivity).mCharacteristic==null||!((MainActivity2)mActivity).mConnected){
				tvw_super_user_name.setText(mActivity.getResources().getString(R.string.no_device));
				tvw_last_action.setVisibility(View.INVISIBLE);
		}else if(((MainActivity2)mActivity).mCharacteristic!=null&&((MainActivity2)mActivity).mConnected&&!((MainActivity2)mActivity).hasLogin){
			tvw_super_user_name.setText(mActivity.getResources().getString(R.string.not_log));
			tvw_last_action.setVisibility(View.INVISIBLE);
		}else{
			tvw_super_user_name.setText(DataFormatUtils.hexStr2Str(((MainActivity2)mActivity).mName));
			tvw_last_action.setText(DataFormatUtils.anylyseDate(mActivity,((MainActivity2)mActivity).mLastActionTime)+"|"+(((MainActivity2)mActivity).isOpen?"opened":"opened"));
		}
	}

	@Override
	public void initData() {
		super.initData();
	}
	public void setTitle(String title){
		tv_center_title.setText(title);
		
	}
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if(!hidden){
			userAdapter.notifyDataSetChanged();
		}
	}
	public void setOwnerInfo(String name,String time,int action){
		if(tvw_super_user_name!=null&&tvw_last_action!=null){
			tvw_super_user_name.setVisibility(View.VISIBLE);
			tvw_super_user_name.setText(name);
			tvw_last_action.setVisibility(View.VISIBLE);
			tvw_last_action.setText(time+"|"+(action==UserInfo.ACT_OPEN?"opened":"opened"));
		}
	}
	BaseAdapter userAdapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(mActivity).inflate(
						R.layout.item_user, null);
				holder = new Holder();
				holder.tvw_username = (TextView) convertView
						.findViewById(R.id.tvw_username);
				holder.tvw_time = (TextView) convertView
						.findViewById(R.id.tvw_time);
				holder.tvw_action = (TextView) convertView
						.findViewById(R.id.tvw_action);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			holder.tvw_username.setText(DataFormatUtils.hexStr2Str(((MainActivity2)mActivity).userList.get(position).userName));
		
			holder.tvw_time.setText(DataFormatUtils.anylyseDate(mActivity,((MainActivity2)mActivity).userList.get(position).lastActTime));
			
//			holder.tvw_action
//					.setText(((MainActivity2)mActivity).userList.get(position).lastAction == UserInfo.ACT_OPEN ? "opened"
//							: "opened");
			if(((MainActivity2)mActivity).userList.get(position).password.startsWith("00")){
				((MainActivity2)mActivity).userList.get(position).enabled=true;
			}
			holder.tvw_action
			.setText(((MainActivity2)mActivity).userList.get(position).enabled == true? "opened"
					: "locked");
			if(position==selectedItem){
				convertView.setBackgroundColor(mActivity.getResources().getColor(R.color.sm_b1b1b1));
			}else{
				convertView.setBackgroundColor(color.white);
			}
			return convertView;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public Object getItem(int position) {
			return ((MainActivity2)mActivity).userList.get(position);
		}

		@Override
		public int getCount() {
			return ((MainActivity2)mActivity).userList.size();
		}
	};
	private FrameLayout fl_left_btn;
	private FrameLayout fl_right_btn;
	private TextView tv_center_title;
	public BaseAdapter getUserAdapter(){
		return userAdapter;
	}
	static class Holder {
		TextView tvw_username;
		TextView tvw_time;
		TextView tvw_action;
		ImageView iv_next;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		selectedItem=position;
		userAdapter.notifyDataSetChanged();
		UserEditorFragment userEditorFrg=new UserEditorFragment();
		Bundle bundle=new Bundle();
		bundle.putInt(Constants.KEY_TYPE, Constants.type_create);
		bundle.putString(Constants.KEY_NAME,((MainActivity2)mActivity).userList.get(position).userName);
		bundle.putString(Constants.KEY_PSW,((MainActivity2)mActivity).userList.get(position).password);
		bundle.putString(Constants.KEY_INDEX,((MainActivity2)mActivity).userList.get(position).userIndex);
		bundle.putBoolean(Constants.KEY_ENABLED,((MainActivity2)mActivity).userList.get(position).enabled);
		userEditorFrg.setArguments(bundle);
		getFragmentManager().beginTransaction().replace(R.id.rlt_user_pager_root, userEditorFrg).addToBackStack("nameAndPsw_edit").commit();
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fl_left_btn:
//			getFragmentManager().popBackStack();
			((MainActivity2)mActivity).switchTab(1);
			break;
		case R.id.fl_right_btn:
			if(((MainActivity2)mActivity).mCharacteristic==null||!((MainActivity2)mActivity).mConnected){
				Toast.makeText(mActivity, mActivity.getResources().getString(R.string.no_device), Toast.LENGTH_SHORT).show();
				return;
			}else if(!((MainActivity2)mActivity).hasLogin){
				Toast.makeText(mActivity, mActivity.getResources().getString(R.string.not_log), Toast.LENGTH_SHORT).show();
				return;
			}
			if(!((MainActivity2)mActivity).mIndex.equals("00")){
				Toast.makeText(mActivity,mActivity.getResources().getString(R.string.no_privilage),Toast.LENGTH_SHORT).show();
				return;
			}
			if(((MainActivity2)mActivity).userList.size()>=3){
				Toast.makeText(mActivity, mActivity.getResources().getString(R.string.too_many_user),Toast.LENGTH_SHORT).show();
				return;
			}
			NameAndPswFragment nameAndPswFrg=new NameAndPswFragment();
			Bundle bundle=new Bundle();
			bundle.putInt(Constants.KEY_TYPE, Constants.type_create);
			nameAndPswFrg.setArguments(bundle);
			getFragmentManager().beginTransaction().replace(R.id.rlt_user_pager_root, nameAndPswFrg).addToBackStack("nameAndPsw_create").commit();
			break;

		default:
			break;
		}
		
	}

}
