package com.example.smartlock.fragment;

import java.util.ArrayList;
import java.util.List;

import com.example.smartlock.R;
import com.example.smartlock.activity.MainActivity2;
import com.example.smartlock.base.BaseFragment;
import com.example.smartlock.entity.DeviceInfo;
import com.example.smartlock.entity.UserInfo;
import com.example.smartlock.pager.LockPager;
import com.example.smartlock.utils.Constants;
import com.example.smartlock.utils.DataFormatUtils;
import com.example.smartlock.utils.SharePreferenceUtil;
import com.roamer.slidelistview.SlideBaseAdapter;
import com.roamer.slidelistview.SlideListView;
import com.roamer.slidelistview.SlideListView.SlideMode;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import u.aly.de;

public class LockFragment extends BaseFragment implements OnClickListener, OnItemLongClickListener {
	private static final String TAG = LockFragment.class.getSimpleName();

	LockPager pager;
	SlideListView lvw_lock;
	List<DeviceInfo> lockList = new ArrayList<DeviceInfo>();
	NameAndPswFragment nameAndPswFrg;
	private int selectItem = 0;
	// public ArrayList<DeviceInfo> mDeviceInfos;

	private LeDeviceListAdapter mLeDeviceListAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning;
	private Handler mHandler;

	private static final int REQUEST_ENABLE_BT = 1;
	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 10000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new Handler();
		mLeDeviceListAdapter = new LeDeviceListAdapter(mActivity);

		mHandler = new Handler();

		final BluetoothManager bluetoothManager = (BluetoothManager) mActivity
				.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// mDeviceInfos = new ArrayList<DeviceInfo>();
	}

	@Override
	public View initView(LayoutInflater inflater) {
		View view = inflater.inflate(R.layout.lock_pager, null);
		lvw_lock = (SlideListView) view.findViewById(R.id.lvw_lock);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		fl_left_btn = (FrameLayout) view.findViewById(R.id.fl_left_btn);
		fl_left_btn.removeAllViews();
		fl_right_btn = (FrameLayout) view.findViewById(R.id.fl_right_btn);
		fl_right_btn.removeAllViews();
		TextView tv_center_title = (TextView) view.findViewById(R.id.tv_center_title);
		ImageView iv_left = new ImageView(mActivity);
		iv_left.setImageResource(R.drawable.arr_left);
		ImageView iv_right = new ImageView(mActivity);
		iv_right.setImageResource(R.drawable.refresh);
		fl_left_btn.addView(iv_left);
		fl_right_btn.addView(iv_right);
		tv_center_title.setText("List");
		fl_left_btn.setOnClickListener(this);
		fl_right_btn.setOnClickListener(this);
	}

	@Override
	public void initData() {
		super.initData();

	}

	public void onResume() {
		super.onResume();
		if (((MainActivity2) mActivity).isFirstScan) {
			scanLeDevice(true);
			((MainActivity2) mActivity).isFirstScan = false;
		}

		mLeDeviceListAdapter.refresh(((MainActivity2) mActivity).mDeviceInfos);
		lvw_lock.setAdapter(mLeDeviceListAdapter);
		lvw_lock.setOnItemLongClickListener(this);
		Log.e(TAG, "lock frg onResume");
	};

	public void scanLeDevice(final boolean enable) {
		if (enable) {
			// 针对扫描不到当前已连接设备

			/*
			 * if (mConnected) { BluetoothDevice device =
			 * mBluetoothAdapter.getRemoteDevice(mDeviceAddress); Log.e(TAG,
			 * "已有连接设备，diceName="+device.getName()+",deviceAddr="+device.
			 * getAddress()+",status="+device.getBondState());
			 * mLeDevices.add(device); }
			 */
			// List<DeviceInfo> tem = new ArrayList<DeviceInfo>();
			/*
			 * for (DeviceInfo device : mDeviceInfos) { if (device.isOn()) {
			 * tem.add(device); } }
			 */
			((MainActivity2) mActivity).mDeviceInfos.clear();
			// mDeviceInfos.addAll(tem);
			if (((MainActivity2) mActivity).mConnected) {
				((MainActivity2) mActivity).mDeviceInfos.add(((MainActivity2) mActivity).mDevice);
			}
			if (mLeDeviceListAdapter != null) {
				mLeDeviceListAdapter.refresh(((MainActivity2) mActivity).mDeviceInfos);
				mLeDeviceListAdapter.notifyDataSetChanged();
			}
			Log.e(TAG, "开始扫描设备-----");
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
				}
			}, SCAN_PERIOD);

			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
			startRefreshAnim();
		} else {
			Log.e(TAG, "停止扫描设备-----");
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			stopRefreshAnim();
			;
		}

	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		DeviceInfo deviceInfo;

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Log.e(TAG, "扫描到设备--deviceName=" + device.getName() + ",devAddr=" + device.getAddress()
							+ ",bondStatus=" + device.getBondState());
					
					synchronized (this) {
						deviceInfo = new DeviceInfo(device);
						boolean hasAdd = false;
						for (DeviceInfo dev : ((MainActivity2) mActivity).mDeviceInfos) {// 判断设备是否已经被添加到设备列表中
							if (dev.getAddress().equals(device.getAddress())) {
								hasAdd = true;
							}
						}
						if (!hasAdd) { // 如果设备没有被添加，则添加
							((MainActivity2) mActivity).mDeviceInfos.add(deviceInfo);
							// 刷新列表决
							if (mLeDeviceListAdapter != null) {
								mLeDeviceListAdapter.refresh(((MainActivity2) mActivity).mDeviceInfos);
								mLeDeviceListAdapter.notifyDataSetChanged();
							}
						}
						;
					}
					
					// 扫描到当前设备则进行连接，自动登录

					if (device.getAddress().equals((SharePreferenceUtil.getString(mActivity, Constants.KEY_DEVICE_ADDR)))) {
						Log.e(TAG, "扫描到记录设备 adrress，bindservice");
						// 是否要停止扫描
						// 如果扫描到当前设备、当前设备为 从缓存中取出的上次成功登陆的设备
						((MainActivity2) mActivity).bindService(deviceInfo);
					}
				}
			});
		}
	};

	@Override
	public void onPause() {
		super.onPause();
		try {
			 scanLeDevice(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.e(TAG, "lockfragment onPause");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fl_left_btn:
			scanLeDevice(false);
			((MainActivity2) mActivity).switchTab(1);
			break;
		case R.id.fl_right_btn:
			// 刷新设备连接
			scanLeDevice(true);
			break;

		default:
			break;
		}
	}

	public void startRefreshAnim() {
		RotateAnimation anim = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		anim.setFillAfter(false);
		anim.setDuration(500);
		anim.setRepeatMode(Animation.INFINITE);
		anim.setRepeatCount(4);
		if (fl_left_btn != null) {
			fl_right_btn.startAnimation(anim);
		}
		Log.e(TAG, "开始刷新设备动画！");
	}

	public void stopRefreshAnim() {
		fl_right_btn.clearAnimation();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Log.e(TAG, "OnItemlongClick方法执行了---");
		return false;
	}

	public LeDeviceListAdapter getDeviceAdapter() {
		return mLeDeviceListAdapter;
	}

	public class LeDeviceListAdapter extends SlideBaseAdapter {
		// private List<BluetoothDevice> mLeDevices;
		private List<DeviceInfo> mDeviceInfos;
		private Context context;

		public LeDeviceListAdapter(Context context) {
			super(context);
			this.context = context;
			mDeviceInfos = new ArrayList<DeviceInfo>();
		}

		public LeDeviceListAdapter(Context context, ArrayList<DeviceInfo> devices) {
			super(context);
			this.context = context;
			this.mDeviceInfos = devices;
		}

		public void addDevice(DeviceInfo device) {
			if (!mDeviceInfos.contains(device)) {
				mDeviceInfos.add(device);
			}
		}

		public DeviceInfo getDevice(int position) {
			return mDeviceInfos.get(position);
		}

		public void clear() {
			mDeviceInfos.clear();
			notifyDataSetChanged();
		}

		public void refresh(List<DeviceInfo> list) {
			mDeviceInfos = list;
			notifyDataSetChanged();
		}

		@Override
		public SlideMode getSlideModeInPosition(int position) {
			return super.getSlideModeInPosition(position);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// Log.e(TAG, "getView begin！");
			Holder holder = null;
			final int loc = position;
			if (convertView == null) {
				convertView = createConvertView(position);
				holder = new Holder();
				holder.tvw_door = (TextView) convertView.findViewById(R.id.tvw_door);
				holder.delete = (TextView) convertView.findViewById(R.id.delete);
				holder.rlt_front_view = (RelativeLayout) convertView.findViewById(R.id.rlt_front_view);
				holder.tvw_status = (TextView) convertView.findViewById(R.id.tvw_status);
				holder.tvw_click_to_conn=(TextView)convertView.findViewById(R.id.tvw_click_to_conn);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			holder.rlt_front_view.setTag(position);
			if(!mDeviceInfos.get(position).isOn()){
				holder.tvw_click_to_conn.setVisibility(View.VISIBLE);
			}else{
				holder.tvw_click_to_conn.setVisibility(View.GONE);
			}
			holder.tvw_status.setText(mDeviceInfos.get(position).isOn() ?(((MainActivity2) mActivity).hasLogin?mActivity.getResources().getString(R.string.has_login):mActivity.getResources().getString(R.string.has_connected)) : mActivity.getResources().getString(R.string.not_conncted));
			if (mDeviceInfos.get(position).getAddress().equals(((MainActivity2) mActivity).mDeviceAddress)) {
				holder.tvw_door.setText(DataFormatUtils.deleteOdd(((MainActivity2) mActivity).mDeviceName));
			} else {
				holder.tvw_door.setText(DataFormatUtils.deleteOdd(mDeviceInfos.get(position).getName()));
			}
			holder.delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					
					Log.e(TAG, "清除所有缓存----");
					if ((SharePreferenceUtil.getString(mActivity, Constants.KEY_DEVICE_ADDR)).equals(mDeviceInfos.get(position).getAddress())) {
						SharePreferenceUtil.putString(mActivity, Constants.KEY_NAME, "");
						SharePreferenceUtil.putString(mActivity, Constants.KEY_PSW, "");
						SharePreferenceUtil.putString(mActivity, Constants.KEY_DEVICE_ADDR, "");
						SharePreferenceUtil.putString(mActivity, Constants.KEY_DEVICE_NAME, "");
						SharePreferenceUtil.putInt(mActivity, Constants.KEY_LAST_ACT, UserInfo.ACT_LOCK);
						SharePreferenceUtil.putString(mActivity, Constants.KEY_INDEX, "04");

						((MainActivity2) mActivity).mName = "";
						((MainActivity2) mActivity).mPsw = "";
						((MainActivity2) mActivity).mDeviceAddress = "";
						((MainActivity2) mActivity).mDeviceName = "";
						((MainActivity2) mActivity).mIndex = "04";
						Log.e(TAG, "disconnect ---");
						if (((MainActivity2) mActivity).mConnected) {
							((MainActivity2) mActivity).disconnect();
//							((MainActivity2) mActivity).unBindService();
							Log.e(TAG, "删除设备，disconnect then unbindervice");
						}
						((MainActivity2)mActivity).hasLogin=false;
					}
					SharePreferenceUtil.clear(mActivity);// 删除所有缓存数据
					mDeviceInfos.remove(position);
					mLeDeviceListAdapter.notifyDataSetChanged();
				}
			});
			holder.rlt_front_view.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					Log.e(TAG, "长按设备，修改设备名称");
					AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
					View view_dialog = LayoutInflater.from(mActivity).inflate(R.layout.change_lock_name_dialog, null);
					builder.setView(view_dialog);
					final EditText et_lock_name = (EditText) view_dialog.findViewById(R.id.et_lock_name);
					// builder.setTitle("修改锁名") //标题
					// .setIcon(R.drawable.ic_launcher) //icon
					builder.setCancelable(false) // 不响应back按钮
							// 设置按钮
							.setPositiveButton(mActivity.getString(R.string.confirm),
									new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String new_lock_name = et_lock_name.getText().toString();
							if (new_lock_name.length() > 10) {
								new_lock_name = new_lock_name.substring(0, 10);
							}
							// String new_lock_name_hex =
							// MainActivity2.bin2hex(new_lock_name);
							String new_lock_name_hex = DataFormatUtils.str2hexStr(new_lock_name, 20);
							((MainActivity2) mActivity).changeDevName(new_lock_name_hex);

						}
					}).setNegativeButton(mActivity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					});
					// 创建Dialog对象
					AlertDialog dlg = builder.create();
					dlg.show();
					return false;
				}
			});
			holder.rlt_front_view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Log.e(TAG, "点击了设备，开始连接设备");
					if (mScanning) {
						scanLeDevice(false);
						mScanning = false;
					}
					final DeviceInfo device = mLeDeviceListAdapter.getDevice(position);
					if (device == null)
						return;
					if ((SharePreferenceUtil.getString(mActivity, Constants.KEY_DEVICE_ADDR)).equals(device.getAddress())
							&& ((MainActivity2) mActivity).mConnected&& ((MainActivity2) mActivity).hasLogin&&((MainActivity2) mActivity).mIndex.equals("00")) {
						Log.e(TAG, "ontimclick 已连接已登录 修改用户名密码");
						nameAndPswFrg = new NameAndPswFragment();
						Bundle bundle = new Bundle();
						bundle.putString(Constants.KEY_DEVICE_ADDR, device.getAddress());
						bundle.putString(Constants.KEY_DEVICE_NAME, device.getName());
						bundle.putInt(Constants.KEY_TYPE, Constants.type_edit);
						bundle.putString(Constants.KEY_NAME, ((MainActivity2)mActivity).mName);
						bundle.putString(Constants.KEY_PSW, ((MainActivity2)mActivity).mPsw);
						bundle.putString(Constants.KEY_INDEX, ((MainActivity2)mActivity).mIndex);
						nameAndPswFrg.setArguments(bundle);
						getFragmentManager().beginTransaction().replace(R.id.rlt_lock_pager_root, nameAndPswFrg)
								.addToBackStack("NameAndpsw_login").commit();

					} else if(((MainActivity2) mActivity).mDeviceAddress.equals(device.getAddress())&&((MainActivity2) mActivity).mConnected&&!((MainActivity2) mActivity).hasLogin&&((MainActivity2) mActivity).mCharacteristic!=null){
						Log.e(TAG, "onitemclick 设备已连接，未登录");
						((MainActivity2) mActivity).inputNameAndPsw();
						
					}else {
						Log.e(TAG, "ontimclick bindservice");
						((MainActivity2) mActivity).bindService(device);
						// }

						selectItem = position;
						mLeDeviceListAdapter.notifyDataSetInvalidated();

						/*
						 * nameAndPswFrg = new NameAndPswFragment(); Bundle
						 * bundle = new Bundle();
						 * bundle.putString(Constants.KEY_DEVICE_ADDR,
						 * device.getAddress());
						 * bundle.putString(Constants.KEY_DEVICE_NAME,
						 * device.getName()); bundle.putInt(Constants.KEY_TYPE,
						 * Constants.type_login);
						 * nameAndPswFrg.setArguments(bundle);
						 * getFragmentManager().beginTransaction().replace(R.id.
						 * rlt_lock_pager_root, nameAndPswFrg)
						 * .addToBackStack("NameAndpsw_login").commit();
						 */
					}
				}
			});

			if (position == selectItem) {
				// convertView.setBackgroundResource(R.drawable.lock_selected_bg);
				convertView.setBackgroundColor(Color.parseColor("#cecece"));
			} else {
				convertView.setBackgroundDrawable(null);
			}
			Log.e(TAG, "getView end！");
			return convertView;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public Object getItem(int position) {
			return mDeviceInfos.get(position);
		}

		@Override
		public int getCount() {
			return mDeviceInfos.size();
		}

		@Override
		public int getFrontViewId(int position) {
			return R.layout.item_lock_list;
		}

		@Override
		public int getLeftBackViewId(int position) {
			return 0;
		}

		@Override
		public int getRightBackViewId(int position) {
			return R.layout.item_right_btn;
		}

	};

	private FrameLayout fl_right_btn;
	private FrameLayout fl_left_btn;

	static class Holder {
		TextView tvw_door;
		TextView delete;
		RelativeLayout rlt_front_view;
		TextView tvw_status;
		TextView tvw_click_to_conn;
	}

}
