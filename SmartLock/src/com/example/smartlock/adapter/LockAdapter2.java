package com.example.smartlock.adapter;

import java.util.ArrayList;

import com.example.smartlock.R;
import com.roamer.slidelistview.SlideBaseAdapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class LockAdapter2 extends SlideBaseAdapter {
	public final static String TAG=LockAdapter2.class.getSimpleName();
	private ArrayList<BluetoothDevice> mData;
	private Context context;
	public LockAdapter2(Context context) {
		super(context);
		this.context=context;
	}

	public LockAdapter2(Context context, ArrayList<BluetoothDevice> data){
		super(context);
		this.context=context;
		mData=data;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		View menuView=null;
		final int loc=position;
		if (convertView == null) {
			convertView = createConvertView(position);
			holder = new Holder();
			holder.tvw_door=(TextView) convertView.findViewById(R.id.tvw_door);
			holder.delete = (Button) convertView.findViewById(R.id.delete);
			convertView.setTag(holder);
			
		} else {
			holder = (Holder) convertView.getTag();
		}
		//TODO是否只显示UNI_lock,如不，是否要针对不同情况处理
		holder.tvw_door.setText(mData.get(position).getName());
//		if(position==selectItem){
//			convertView.setBackgroundResource(R.drawable.lock_selected_bg);
//		}else{
//			convertView.setBackgroundDrawable(null);;
//		}
		Log.e(TAG, "getView 方法执行了！");
		return convertView;
		
		
		
	}

	@Override
	public int getFrontViewId(int position) {
		return R.layout.item_lock_list;
	}

	@Override
	public int getLeftBackViewId(int position) {
		return R.layout.item_right_btn;
	}

	@Override
	public int getRightBackViewId(int position) {
		return R.layout.item_right_btn;
	}
	
	class Holder {
		TextView tvw_door;
		Button delete;
	}


}
