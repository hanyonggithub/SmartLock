package com.example.smartlock.pager;

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.sax.StartElementListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.smartlock.R;
import com.example.smartlock.activity.NameAndPswActivity;
import com.example.smartlock.base.BasePager;
import com.example.smartlock.entity.DeviceInfo;

public class LockListPager extends BasePager implements OnItemClickListener {
	
	List<DeviceInfo> devices=new ArrayList<DeviceInfo>();

	public LockListPager(Context context) {
		super(context);
		fl_left_btn.setBackgroundResource(R.drawable.back);
		fl_right_btn.setBackgroundResource(R.drawable.refresh);
		tv_center_text.setText(R.string.title_list);
		
		ListView listView=new ListView(mContext);
		LockListAdapter lockAdapter=new LockListAdapter();
		listView.setAdapter(lockAdapter);
		
		//ģ������
		for(int i=0;i<4;i++){
//			devices.add(new DeviceInfo("fontDoor"));
		}
		
		fl_content.addView(listView);
		lockAdapter.notifyDataSetChanged();
		listView.setOnItemClickListener(this);
	}
	@Override
	public void initData() {
		super.initData();
		
	}
	
	class LockListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return devices.size();
		}

		@Override
		public Object getItem(int position) {
			return devices.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder=null;
			if(convertView==null){
				convertView=LayoutInflater.from(mContext).inflate(R.layout.item_lock_list, null);
				holder=new Holder();
//				holder.iv_lock=(ImageView) convertView.findViewById(R.id.iv_lock);
				holder.tvw_door=(TextView) convertView.findViewById(R.id.tvw_door);
//				holder.iv_next=(ImageView)convertView.findViewById(R.id.iv_next);
				convertView.setTag(holder);
			}else{
				holder=(Holder) convertView.getTag();
			}
			holder.tvw_door.setText(devices.get(position).getName());
			return convertView;
		}
		
	}
	
	static class Holder{ 
		ImageView iv_lock;
		TextView tvw_door;
		ImageView iv_next;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Intent intent=new Intent(mContext, NameAndPswActivity.class);
		intent.putExtra("deviceID", 11111111);
		mContext.startActivity(intent);
		
	}
	
}
