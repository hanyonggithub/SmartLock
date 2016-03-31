package com.example.smartlock.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import com.example.smartlock.R;
import com.example.smartlock.base.BaseFragment;
import com.example.smartlock.ble.BluetoothLeService;
import com.example.smartlock.callback.TitleBarClickListener;
import com.example.smartlock.entity.DeviceInfo;
import com.example.smartlock.entity.UserInfo;
import com.example.smartlock.fragment.LockFragment;
import com.example.smartlock.fragment.MainFragment;
import com.example.smartlock.fragment.NameAndPswFragment;
import com.example.smartlock.fragment.UserFragment;
import com.example.smartlock.utils.Constants;
import com.example.smartlock.utils.DataFormatUtils;
import com.example.smartlock.utils.SharePreferenceUtil;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity3 extends FragmentActivity implements OnClickListener {
	private final static String TAG = MainActivity3.class.getSimpleName();
	private FragmentPagerAdapter mAdapter;
	private List<BaseFragment> mFragments = new ArrayList<BaseFragment>();
	public FragmentManager fmg;
	private ImageView iv_lockList;
	private ImageView iv_key;
	private ImageView iv_user;
	private int btnCount = 3;
	private TitleBarClickListener titleBarClickListener;
	private int currentIndex;

	private BluetoothAdapter mBluetoothAdapter;
	private ArrayList<BluetoothDevice> mLeDevices;
	public ArrayList<DeviceInfo> mDeviceInfos;
	private boolean mScanning;
	private Handler mHandler;
	public DeviceInfo mDevice;
	private static final int REQUEST_ENABLE_BT = 1;
	private static final long SCAN_PERIOD = 5000;

	private static final String SER_UUID = "0000fff0-0000-1000-8000-00805f9b34fb";
	private static final String CHAR_UUID = "0000fff6-0000-1000-8000-00805f9b34fb";

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

	private TextView mConnectionState;
	private TextView mDataField;

	private ExpandableListView mGattServicesList;
	private BluetoothLeService mBluetoothLeService;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	private BluetoothGattService mGattService;
	public BluetoothGattCharacteristic mCharacteristic;

	public boolean mConnected = false;
	private BluetoothGattCharacteristic mNotifyCharacteristic;

	private final String LIST_NAME = "NAME";
	private final String LIST_UUID = "UUID";

	public String mDeviceName;// 设备名称
	public String mDeviceAddress;// 设备地址
	public String mName;// 用户名称
	public String mPsw;// 密码
	public String mIndex = "00";// 用户序号
	public String mLastAction;// 上次操作
	public String mLastActionTime;// 上次操作时间
	public int mDevBatLevel;// 电池电量
	public boolean isAuto = true;// 是否自动开锁
	public boolean hasLogin = false;
	public boolean isOpen = false;// 锁是否已经打开

	public int position = 0;

	public List<UserInfo> userList = new ArrayList<UserInfo>();
	// byte[] WriteBytes = null;
	byte[] WriteBytes = new byte[20];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main2);
		initView();
		initData();
		initBluetooth();
		Log.e(TAG, "oncreate finish");
	}

	private void initView() {
		iv_lockList = (ImageView) findViewById(R.id.iv_locklist);
		iv_key = (ImageView) findViewById(R.id.iv_key);
		iv_user = (ImageView) findViewById(R.id.iv_user);
		iv_lockList.setOnClickListener(this);
		iv_key.setOnClickListener(this);
		iv_user.setOnClickListener(this);

		lockFragment = new LockFragment();
		mainFragment = new MainFragment();
		userFragment = new UserFragment();
		mFragments.add(lockFragment);
		mFragments.add(mainFragment);
		mFragments.add(userFragment);
		fmg = getSupportFragmentManager();
		Log.e(TAG, "has init view");
	}

	private void initData() {
		mName = SharePreferenceUtil.getString(this, Constants.KEY_NAME);
		mPsw = SharePreferenceUtil.getString(this, Constants.KEY_PSW);
		mDeviceAddress = SharePreferenceUtil.getString(this, Constants.KEY_DEVICE_ADDR);
		mDeviceName = SharePreferenceUtil.getString(this, Constants.KEY_DEVICE_NAME);
		isAuto = SharePreferenceUtil.getBoolean(this, Constants.KEY_AUTO);
		Log.e(TAG, "缓存值：name=" + mName + "psw=" + mPsw + ",mDeviceAddr=" + mDeviceAddress + "isAuto=" + isAuto);
		mLeDevices = new ArrayList<BluetoothDevice>();
		mDeviceInfos = new ArrayList<DeviceInfo>();
		if (lockFragment != null && lockFragment.getDeviceAdapter() != null) {
			// lockFragment.getDeviceAdapter().refresh(mLeDevices);
			lockFragment.getDeviceAdapter().refresh(mDeviceInfos);
		}

	}

	public void initBluetooth() {
		mHandler = new Handler();
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}
		bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		Log.e(TAG, "get the bluetoothAdapter");
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		if (mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.enable();
			Log.e(TAG, "enable the bluetoothAdapter");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.e(TAG, "onresume");
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		switchTab(position);
	

		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		/*
		 * if (mBluetoothLeService != null &&
		 * !TextUtils.isEmpty(mDeviceAddress)) { final boolean result =
		 * mBluetoothLeService.connect(mDeviceAddress); Log.e(TAG,
		 * "Connect request result=" + result); } Intent gattServiceIntent = new
		 * Intent(this, BluetoothLeService.class);
		 * bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
		 */
		scanLeDevice(true);

	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.e(TAG, "onpause 方法执行了");
//		scanLeDevice(false);
		try {
			unregisterReceiver(mGattUpdateReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}

	@Override
	protected void onDestroy() {
		Log.e(TAG, "ondestroy 方法执行了----");
		if (mServiceConnection != null) {
			Log.e(TAG, "unbindservice");
			try {
				unbindService(mServiceConnection);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		Log.e(TAG, "unbindservice--end");
		mBluetoothLeService = null;
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_locklist:
			position = 0;
			break;
		case R.id.iv_key:
			position = 1;
			break;
		case R.id.iv_user:
			position = 2;
			break;
		}
		switchTab(position);
	}

	public void switchTab(int index) {
		position = index;

		// if(!mFragments.get(index).isAdded()){
		// fmg.beginTransaction().add(R.id.fl_content,
		// mFragments.get(index)).commit();
		// }
		// if(mFragments.get(index).isHidden()){
		// fmg.beginTransaction().show(mFragments.get(index)).commit();
		// }
		try {
			getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, mFragments.get(index)).commit();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, e.toString());
		}
		// getSupportFragmentManager().beginTransaction().replace(R.id.fl_content,
		// new testFragment()).commit();
		switch (index) {
		case 0:
			resetTabBtn(0);
			break;
		case 1:
			resetTabBtn(1);
			break;
		case 2:
			resetTabBtn(2);
			break;

		default:

			break;
		}

	}

	protected void resetTabBtn(int currentIndex) {
		iv_lockList.setImageResource(R.drawable.tab_lock);
		iv_key.setImageResource(R.drawable.tab_key);
		iv_user.setImageResource(R.drawable.tab_user);
		switch (currentIndex) {
		case 0:
			iv_lockList.setImageResource(R.drawable.tab_lock_selected);
			;
			break;
		case 1:
			iv_key.setImageResource(R.drawable.tab_key_selected);
			break;
		case 2:
			iv_user.setImageResource(R.drawable.tab_user_selected);
			break;

		default:
			iv_key.setImageResource(R.drawable.tab_key_selected);
			break;
		}
		Log.e(TAG, "reset Tab btn+"+currentIndex);
	}

	private long mExitTime;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			getSupportFragmentManager().popBackStack();
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();
			} else {
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

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
			List<DeviceInfo> tem = new ArrayList<DeviceInfo>();
			/*
			 * for (DeviceInfo device : mDeviceInfos) { if (device.isOn()) {
			 * tem.add(device); } }
			 */
			mDeviceInfos.clear();
			// mDeviceInfos.addAll(tem);
			if (mConnected) {
				mDeviceInfos.add(mDevice);
			}
			if (((LockFragment) mFragments.get(0)).getDeviceAdapter() != null) {
				((LockFragment) mFragments.get(0)).getDeviceAdapter().refresh(mDeviceInfos);
				((LockFragment) mFragments.get(0)).getDeviceAdapter().notifyDataSetChanged();
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
			((LockFragment) mFragments.get(0)).startRefreshAnim();
		} else {
			Log.e(TAG, "停止扫描设备-----");
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			((LockFragment) mFragments.get(0)).stopRefreshAnim();
			;
		}

	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		DeviceInfo deviceInfo;

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
//					Log.e(TAG, "扫描到设备--deviceName=" + device.getName() + ",devAddr=" + device.getAddress()
//							+ ",bondStatus=" + device.getBondState());
					deviceInfo = new DeviceInfo(device);
					boolean hasAdd = false;
					for (DeviceInfo dev : mDeviceInfos) {// 判断设备是否已经被添加到设备列表中
						if (dev.getAddress().equals(device.getAddress())) {
							hasAdd = true;
						}
					}
					if (!hasAdd) { // 如果设备没有被添加，则添加
						mDeviceInfos.add(deviceInfo);
						// 刷新列表决
						if (((LockFragment) mFragments.get(0)).getDeviceAdapter() != null) {
							((LockFragment) mFragments.get(0)).getDeviceAdapter().refresh(mDeviceInfos);
							((LockFragment) mFragments.get(0)).getDeviceAdapter().notifyDataSetChanged();
						}
					}
				
					;
					// 扫描到当前设备则进行连接，自动登录
					
					if (device.getAddress().equals(mDeviceAddress)) {
						Log.e(TAG, "扫描到记录设备 adrress，bindservice");
						// 如果扫描到当前设备、当前设备为 从缓存中取出的上次成功登陆的设备
						bindService(deviceInfo);
					}
				}
			});
		}
	};

	public void bindService(String deviceName, String deviceAddress) {
		mDeviceName = deviceName;
		mDeviceAddress = deviceAddress;
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
		Log.e(TAG, "begin bindservice----");
		if (mBluetoothLeService != null) {
			if (TextUtils.isEmpty(mDeviceAddress)) {
				Toast.makeText(MainActivity3.this, "设备未连接", Toast.LENGTH_SHORT).show();
				return;
			}
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			if(!result){
				bindService(mDevice);
			}
			Log.e(TAG, "Connect request result=" + result);
		}
		// ((MainFragment)mFragments.get(1)).setTitle(mDeviceName);
	}

	public void bindService(DeviceInfo device) {
		Log.e(TAG, "begin bindservice----");
		mDevice = device;
		mDeviceName = device.getName();
		mDeviceAddress = device.getAddress();
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
		if (mBluetoothLeService != null) {//已连接，直接跳转
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			Log.e(TAG, "Connect request result=" + result);
		}
	}

	public void unBindService() {
		if (mConnected == true) {
			unbindService(mServiceConnection);
			if(mBluetoothLeService!=null){
				mBluetoothLeService.disconnect();
			}
		}
	}

	public void unRegisterRec() {
		if (mConnected == true) {
			unregisterReceiver(mGattUpdateReceiver);
		}
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}

	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			Log.e(TAG, "service connected");
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			if (!TextUtils.isEmpty(mDeviceAddress)) {
				Log.e(TAG, "service 已连接，connect 设备 adrres=" + mDeviceAddress);
				mBluetoothLeService.connect(mDeviceAddress);
			}
			Log.e(TAG, "service is bund!");
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			Log.e(TAG, "service disconnected");
			mBluetoothLeService = null;
		}
	};

	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device. This can be a
	// result of read
	// or notification operations.
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				// Toast.makeText(MainActivity2.this, "connected",
				// Toast.LENGTH_LONG).show();
				Log.e(TAG, "mGattUpdateReceiver ---设备已连接--begin");
				mConnected = true;
				if (!TextUtils.isEmpty(mName) && !TextUtils.isEmpty(mPsw) && !TextUtils.isEmpty(mDeviceAddress)) {
				}else{
					inputNameAndPsw();
				}
	
				Log.e(TAG, "mGattUpdateReceiver connected---mDeviceInfos-isNull:" + (mDeviceInfos == null));
				if (mDeviceInfos != null && mDeviceInfos.size() > 0) {
					int index = mDeviceInfos.indexOf(mDevice);
					Log.e(TAG, "mGattupdateReceiver connected:当前设备index=" + index);
					if (index != -1) {
						Log.e(TAG, "设置当前设备状态为已连接");
						mDeviceInfos.get(mDeviceInfos.indexOf(mDevice)).setOn(true);// 设备
						if (((LockFragment) mFragments.get(0)).getDeviceAdapter() != null) {
							((LockFragment) mFragments.get(0)).getDeviceAdapter().refresh(mDeviceInfos);
							((LockFragment) mFragments.get(0)).getDeviceAdapter().notifyDataSetChanged();
						}
					}
					
				}

				// if (!TextUtils.isEmpty(mName) && !TextUtils.isEmpty(mPsw) &&
				// !TextUtils.isEmpty(mDeviceAddress)) {
				// // 自动登录 ，跳转到开锁界面，使用上次登录成功的 名称 密码 设备，，考虑保存多个设备名，来匹配当前搜索到的设备
				// login(mName, mPsw, mDeviceAddress, mDeviceName);
				// }
				Log.e(TAG, "mGattUpdateReceiver ---设备已连接--end");
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				Log.e(TAG, "mGattUpdateReceiver ---设备连接断开");
				Toast.makeText(MainActivity3.this, "设备连接已断开！", Toast.LENGTH_LONG).show();
				mConnected = false;
				// 如果在主界面，执行关锁动画，如果没有在该界面 则 在，当回到该界面的时候针对mconnected 做
				Log.e(TAG, "mGattUpdateReceiver disconnected---mDeviceInfos-isNull:" + (mDeviceInfos == null));
				if (mDeviceInfos != null && mDeviceInfos.size() > 0) {
					if(mDevice!=null){
						int index = mDeviceInfos.indexOf(mDevice);
						Log.e(TAG, "mGattupdateReceiver disconnected:当前设备index=" + index);
						if (index != -1) {
							Log.e(TAG, "设置当前设备状态为已断开");
							mDeviceInfos.get(mDeviceInfos.indexOf(mDevice)).setOn(false);// 设备
							if (((LockFragment) mFragments.get(0)).getDeviceAdapter() != null) {
								((LockFragment) mFragments.get(0)).getDeviceAdapter().refresh(mDeviceInfos);
								((LockFragment) mFragments.get(0)).getDeviceAdapter().notifyDataSetChanged();
							}
						}
					}
				}
				isOpen = false;
				hasLogin = false;
				mDevBatLevel = 0;
				if (position == 1) {
					((MainFragment) mFragments.get(1)).startCloseAnim();
					((MainFragment) mFragments.get(1)).setBatLevel(0);
					
					// ((UserFragment)mFragments.get(2)).setOwnerInfo();
					// if(userList!=null){
					// userList.clear();
					// if(((UserFragment)mFragments.get(2)).getUserAdapter()!=null){
					// ((UserFragment)mFragments.get(2)).getUserAdapter().notifyDataSetInvalidated();
					// }
					// }
				}
				userList.clear();
				Log.e(TAG,"d断开连接，清楚用户信息");
				((UserFragment) mFragments.get(2)).setOwnerInfo();
				if (((UserFragment) mFragments.get(2)).getUserAdapter() != null) {
					((UserFragment) mFragments.get(2)).getUserAdapter().notifyDataSetChanged();
				}

				// invalidateOptionsMenu();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				// Show all the supported services and characteristics on the
				// user interface.
				Log.e(TAG, "action discovered");
				List<BluetoothGattService> gattServices = mBluetoothLeService.getSupportedGattServices();

				for (BluetoothGattService service : gattServices) {
					Log.e(TAG, "service uuid=" + service.getUuid());
					if (service.getUuid().toString().equals(SER_UUID)) {
						mGattService = service;
						mCharacteristic = mGattService.getCharacteristic(UUID.fromString(CHAR_UUID));
						Log.e(TAG, "获取目标设备 charater");
					}
				}
				if (mCharacteristic != null) {

					if (!TextUtils.isEmpty(mName) && !TextUtils.isEmpty(mPsw) && !TextUtils.isEmpty(mDeviceAddress)) {
						Log.e(TAG, "获取charecter后自动登录！");
						login(mName, mPsw, mDeviceAddress, mDeviceName);
					}
				}
				// if (mCharacteristic != null && !TextUtils.isEmpty(mName) &&
				// !TextUtils.isEmpty(mPsw)
				// && !TextUtils.isEmpty(mDeviceAddress)) {
				// // 自动登录 ，跳转到开锁界面，使用上次登录成功的 名称 密码 设备，，考虑保存多个设备名，来匹配当前搜索到的设备
				// Log.e(TAG, "获取charecter后自动登录！");
				// login(mName, mPsw, mDeviceAddress, mDeviceName);
				// }
				// List<BluetoothGattCharacteristic> gattCharacteristics =
				// gattService.getCharacteristics();

			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				// 解析数据 指令码 校验值
				processData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
			}
		}
	};

	public BluetoothAdapter getBleAdapter() {
		return mBluetoothAdapter;
	}

	public static String bin2hex(String bin) {
		char[] digital = "0123456789ABCDEF".toCharArray();
		StringBuffer sb = new StringBuffer("");
		byte[] bs = bin.getBytes();
		int bit;
		for (int i = 0; i < bs.length; i++) {
			bit = (bs[i] & 0x0f0) >> 4;
			sb.append(digital[bit]);
			bit = bs[i] & 0x0f;
			sb.append(digital[bit]);
		}
		return sb.toString();
	}

	public static byte[] hex2byte(byte[] b) {
		if ((b.length % 2) != 0) {
			throw new IllegalArgumentException("长度不是偶数");
		}
		byte[] b2 = new byte[b.length / 2];
		for (int n = 0; n < b.length; n += 2) {
			String item = new String(b, n, 2);
			// 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个进制字节
			b2[n / 2] = (byte) Integer.parseInt(item, 16);
		}
		b = null;
		return b2;
	}

	public static String getTimeStamp() {
		Calendar calendar = Calendar.getInstance(Locale.getDefault());
		String year_hex = "0".concat(Integer.toHexString(calendar.get(Calendar.YEAR) % 100));
		String month_hex = "0".concat(Integer.toHexString(calendar.get(Calendar.MONTH) + 1));
		String day_hex = "0".concat(Integer.toHexString(calendar.get(Calendar.DAY_OF_MONTH)));
		String hour_hex = "0".concat(Integer.toHexString(calendar.get(Calendar.HOUR_OF_DAY)));
		String minute_hex = "0".concat(Integer.toHexString(calendar.get(Calendar.MINUTE)));

		year_hex = year_hex.substring(year_hex.length() - 2, year_hex.length());
		month_hex = month_hex.substring(month_hex.length() - 2, month_hex.length());
		day_hex = day_hex.substring(day_hex.length() - 2, day_hex.length());
		hour_hex = hour_hex.substring(hour_hex.length() - 2, hour_hex.length());
		minute_hex = minute_hex.substring(minute_hex.length() - 2, minute_hex.length());
		return year_hex.concat(month_hex).concat(day_hex).concat(hour_hex).concat(minute_hex);
	}

	public static String getCheckNum() {
		Random random = new Random();
		int checkNum = random.nextInt(89) + 10;
		return String.valueOf(checkNum);
	}

	public void inputNameAndPsw() {
		NameAndPswFragment nameAndPswFrg = new NameAndPswFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constants.KEY_DEVICE_ADDR, mDevice.getAddress());
		bundle.putString(Constants.KEY_DEVICE_NAME, mDevice.getName());
		bundle.putInt(Constants.KEY_TYPE, Constants.type_login);
		nameAndPswFrg.setArguments(bundle);
		getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, nameAndPswFrg)
				.addToBackStack("NameAndpsw_login").commit();
	}

	public boolean write(String str) {
		Log.e(TAG, "write:" + str);
		if (mCharacteristic != null) {
			final int charaProp = mCharacteristic.getProperties();
			if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
				byte[] value = new byte[20];
				value[0] = (byte) 0x00;
				if (str != null && str.length() > 0) {
					WriteBytes = hex2byte(str.getBytes());
				}
				mCharacteristic.setValue(value[0], BluetoothGattCharacteristic.FORMAT_UINT8, 0);
				mCharacteristic.setValue(WriteBytes);
				mBluetoothLeService.writeCharacteristic(mCharacteristic);

				if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
					mNotifyCharacteristic = mCharacteristic;
					mBluetoothLeService.setCharacteristicNotification(mCharacteristic, true);
				}

				return true;
			}
		} else {
			Log.e(TAG, "当前设备未连接！");
			Toast.makeText(MainActivity3.this, "当前设备未连接", Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	public void login(String name, String psw, String deviceAddr, String deviceName) {
		Log.e(TAG, "开始登录");
		this.mName = name;
		this.mPsw = psw;
		this.mDeviceAddress = deviceAddr;
		this.mDeviceName = deviceName;
		// 连接
		// bindService(mDeviceName, mDeviceAddress);
		// 写指令
		String login_token = "A3".concat(name).concat(psw).concat(MainActivity3.getTimeStamp()).concat("FF")
				.concat(MainActivity3.getCheckNum());
		write(login_token);
	}

	public void login() {
		if(!TextUtils.isEmpty(mName)&&!TextUtils.isEmpty(mPsw)){
			String login_token = "A3".concat(mName).concat(mPsw).concat(MainActivity3.getTimeStamp()).concat("FF")
					.concat(MainActivity3.getCheckNum());
			write(login_token);
		}else{
			inputNameAndPsw();
		}
	
	}

	String new_userIndex = "";

	public void create(String name, String psw, String deviceAddr, String deviceName) {
		String[] userNums = { "01", "02", "03" };
		new_userIndex = "";
		for (String num : userNums) {
			new_userIndex = num;
			for (UserInfo userinfo : userList) {
				if (num.equals(userinfo.userIndex)) {
					new_userIndex = "";
					break;
				} else {
					new_userIndex = num;
				}

			}
			if (!TextUtils.isEmpty(new_userIndex)) {
				break;
			}
		}
		String create_token = "A2".concat(name).concat(psw).concat(MainActivity3.getTimeStamp()).concat(new_userIndex)
				.concat(MainActivity3.getCheckNum());
		write(create_token);
	}

	String edit_userIndex = "";
	String edit_userName = "";
	String edit_userPsw = "";

	public void editUser(String name, String psw, String deviceAddr, String deviceName, String userIndex) {
		String create_token = "A4".concat(name).concat(psw).concat(MainActivity3.getTimeStamp()).concat(userIndex)
				.concat(MainActivity3.getCheckNum());
		edit_userIndex = userIndex;
		edit_userName = name;
		edit_userPsw = psw;
		write(create_token);
	}

	public void openLock() {
		// bindService(mDeviceName, mDeviceAddress);
		String open_token = "A0".concat(mName).concat(mPsw).concat(getTimeStamp()).concat(mIndex).concat(getCheckNum());
		write(open_token);
	}

	public void queryPower() {
		// bindService(mDeviceName, mDeviceAddress);
		String query_power_token = "A1".concat(mName).concat(mPsw).concat(getTimeStamp()).concat(mIndex)
				.concat(getCheckNum());
		write(query_power_token);
	}

	String delete_userIndex = "";

	public void deleteUser(String userIndex) {
		// bindService(mDeviceName, mDeviceAddress);
		String delete_user_token = "A7".concat(mName).concat(mPsw).concat(getTimeStamp()).concat(mIndex)
				.concat(getCheckNum());
		delete_userIndex = userIndex;
		write(delete_user_token);
	}

	String pause_userIndex = "";

	public void pauseUser(String userIndex) {
		// bindService(mDeviceName, mDeviceAddress);
		String pause_user_token = "A5".concat(mName).concat(mPsw).concat(getTimeStamp()).concat(mIndex)
				.concat(getCheckNum());
		pause_userIndex = userIndex;
		write(pause_user_token);
	}

	String enable_userIndex = "";

	public void enableUser(String userIndex) {
		// bindService(mDeviceName, mDeviceAddress);
		String enable_user_token = "A6".concat(mName).concat(mPsw).concat(getTimeStamp()).concat(mIndex)
				.concat(getCheckNum());
		enable_userIndex = userIndex;
		write(enable_user_token);
	}

	private String newDeviceName;

	private LockFragment lockFragment;
	private MainFragment mainFragment;
	private UserFragment userFragment;
	private BluetoothManager bluetoothManager;

	public void changeDevName(String newName) {
		newDeviceName = newName;
		// bindService(mDeviceName, mDeviceAddress);
		String enable_user_token = "A8".concat(newName).concat(getTimeStamp()).concat(mIndex).concat(getCheckNum());
		write(enable_user_token);
	}

	public void processData(String str) {
		if (str == null) {
			Toast.makeText(MainActivity3.this, "异常数据", Toast.LENGTH_SHORT).show();
			return;
		}
		Log.e(TAG, "read:" + str.substring(21, str.length()));
		String[] strs = str.substring(21, str.length()).split("");
		String token_type = strs[1].concat(strs[2]);
		String token_sum = strs[58].concat(strs[59]);
		String token_byte = strs[55].concat(strs[56]);
		String token_time = "";
		for (int i = 40; i < 55; i++) {
			token_time = token_time.concat(strs[i]);
		}
		token_time = token_time.replace(" ", "");
		String userName = "";
		for (int j = 3; j < 21; j++) {
			if (!TextUtils.isEmpty(strs[j])) {
				userName = userName.concat(strs[j]);
			}
		}
		userName = userName.replace(" ", "");
		String userpsw = "";
		for (int k = 22; k < 40; k++) {
			if (!TextUtils.isEmpty(strs[k])) {
				userpsw = userpsw.concat(strs[k]);
			}

		}
		userpsw = userpsw.replace(" ", "");
		Log.e(TAG, "type:" + token_type + ",sum:" + token_sum + ",byte:" + token_byte + ",time:" + token_time + ",name:"
				+ userName + ",psw:" + userpsw);

		if (token_type.equals(Constants.A0)) {
			if (token_byte.equals(Constants.OK)) {
				// 如果在主界面执行开锁动画
				isOpen = true;
				mLastActionTime = token_time;
				SharePreferenceUtil.putString(MainActivity3.this, Constants.KEY_LAST_ACT_TIME, token_time);
				if (position == 1) {
					((MainFragment) mFragments.get(1)).startOpenAnim();
				}

				queryPower();
				// 如果不在开锁界面则记录下状态等进入主界面直接 展现开锁状态
			} else if (token_byte.equals(Constants.NO)) {
				// 开锁失败
				Toast.makeText(this, "开锁失败", Toast.LENGTH_SHORT).show();
			}

		} else if (token_type.equals(Constants.A1)) {
			// 电量查询，每次到主界面要请求一次最新电量，考虑是否定时请求以避免长时间停留主界面
			int bat_level = Integer.parseInt(token_byte, 16);
			if (bat_level < 0 || bat_level > 100) {
				Toast.makeText(this, "电量获取失败", Toast.LENGTH_SHORT).show();
				return;
			}
			mDevBatLevel = bat_level;
			Log.e(TAG, "电量为：" + mDevBatLevel);
			SharePreferenceUtil.putInt(this, Constants.KEY_BAT_LEVEL, mDevBatLevel);
			if (((MainFragment) mFragments.get(1)) != null) {
				((MainFragment) mFragments.get(1)).setBatLevel(mDevBatLevel);
			}
			// 设置电量

		} else if (token_type.equals(Constants.A2)) {
			// 创建用户 保存刚才创建的用户名密码，如果创建成功则添加到userList中 默认没有任何操作
			if (token_byte.equals(Constants.OK)) {
				userList.add(new UserInfo(userName, mDeviceAddress, mDeviceName, userpsw, UserInfo.ACT_LOCK, token_time,
						new_userIndex));
				if (position == 2) {
					((UserFragment) mFragments.get(2)).getUserAdapter().notifyDataSetChanged();
				}
				Log.e(TAG, "userList.size=" + userList.size());

			} else if (token_byte.equals(Constants.NO)) {
				// 创建失败，不添加
				Toast.makeText(this, "创建用户失败", Toast.LENGTH_SHORT).show();
			}

		} else if (token_type.equals(Constants.A3)) {
			if(userList!=null&&userList.size()>0){
				if(!userList.get(0).checkNum.equals(token_sum)){
					userList.clear();
					Log.e(TAG, "新的登录，清除以前用户信息");
				}
			}
			// 判斷是否成功
			if (token_byte.equals("00") || token_byte.equals("01") || token_byte.equals("02")
					|| token_byte.equals("03")) {
				hasLogin = true;
				if (token_byte.equals("00")) {
					if (userName.replace(" ", "").equals(mName)) {
						if (token_time.equals("0000000000")) {
							return;
						}

						mLastActionTime = token_time;
						mIndex = token_byte;
						SharePreferenceUtil.putString(MainActivity3.this, Constants.KEY_NAME, mName);
						SharePreferenceUtil.putString(MainActivity3.this, Constants.KEY_PSW, mPsw);
						SharePreferenceUtil.putString(MainActivity3.this, Constants.KEY_DEVICE_ADDR, mDeviceAddress);
						SharePreferenceUtil.putString(MainActivity3.this, Constants.KEY_DEVICE_NAME, mDeviceName);
						SharePreferenceUtil.putInt(MainActivity3.this, Constants.KEY_LAST_ACT, UserInfo.ACT_OPEN);
						SharePreferenceUtil.putString(MainActivity3.this, Constants.KEY_INDEX, token_byte);
						mIndex = token_byte;
							SharePreferenceUtil.putString(MainActivity3.this, Constants.KEY_LAST_ACT_TIME, token_time);
						Log.e(TAG, "写入缓存：name=" + mName + "psw=" + mPsw + "index=" + token_byte + ",mDeviceAddr="
								+ mDeviceAddress);
						if (position == 0) {
							switchTab(1);
						} else if (position == 1) {
							try {
								((MainFragment) mFragments.get(1)).setTitle(DataFormatUtils.deleteOdd(mDeviceName));
								;
							} catch (Exception e) {
								e.printStackTrace();
							}
							
						}
						queryPower();
						isAuto = SharePreferenceUtil.getBoolean(this, Constants.KEY_AUTO);
						Log.e(TAG, "isAuto="+isAuto);
						if (isAuto) {
							openLock();
						}
					}
				} else {

					if (!userName.equals("000000000000")) {
						UserInfo user = new UserInfo(userName, mDeviceAddress, mDeviceName, userpsw, UserInfo.ACT_OPEN,
								token_time, token_byte);
						user.checkNum=token_sum;
						if (userpsw.startsWith("00")) {
							user.enabled = false;
						} else {
							user.enabled = true;
						}
						if (!userList.contains(user)) {
							userList.add(user);
						}
					}
				}

				// 如果在用户列表界面要刷新相关用户数据
				if (position == 2) {
					((UserFragment) mFragments.get(2)).setOwnerInfo(DataFormatUtils.hexStr2Str(mName),
							DataFormatUtils.hexStr2Str(token_time), UserInfo.ACT_OPEN);
					((UserFragment) mFragments.get(2)).getUserAdapter().notifyDataSetChanged();
				}
				// TODO:相同username去重问题 什么时候清空用户列表
				// 跳转页面 ，是之前跳转还是之后跳转
				// Toast.makeText(this, "登录成功,userlist.size=" + userList.size(),
				// Toast.LENGTH_SHORT).show();
				Log.e(TAG, "userList.size=" + userList.size());
				
			} else {
				Log.e(TAG, "登陆失败！");
				Toast.makeText(MainActivity3.this, "登录失败，请重新登录！", Toast.LENGTH_SHORT).show();
				inputNameAndPsw();
			}

		} else if (token_type.equals(Constants.A4)) {
			if (token_byte.equals(Constants.OK)) {
				Toast.makeText(this, "修改用户成功", Toast.LENGTH_SHORT).show();
				for (UserInfo user : userList) {
					if (user.userIndex.equals(edit_userIndex)) {
						user.userName = edit_userName;
						user.password = edit_userPsw;
					}
				}
			} else if (token_byte.equals(Constants.NO)) {
				Toast.makeText(this, "修改用户失败", Toast.LENGTH_SHORT).show();
				edit_userIndex = "";
			}

			if (position == 2) {
				((UserFragment) mFragments.get(2)).getUserAdapter().notifyDataSetChanged();
			}

		} else if (token_type.equals(Constants.A5)) {
			if (token_byte.equals(Constants.OK)) {
				Toast.makeText(this, "已暂停", Toast.LENGTH_SHORT).show();
				for (UserInfo user : userList) {
					if (user.userIndex.equals(pause_userIndex)) {
						user.enabled = false;
					}
				}
			} else if (token_byte.equals(Constants.NO)) {
				Toast.makeText(this, "设置失败", Toast.LENGTH_SHORT).show();
				pause_userIndex = "";
			}
			if (position == 2) {
				((UserFragment) mFragments.get(2)).getUserAdapter().notifyDataSetChanged();
			}

		} else if (token_type.equals(Constants.A6)) {
			if (token_byte.equals(Constants.OK)) {
				Toast.makeText(this, "已恢复", Toast.LENGTH_SHORT).show();
				for (UserInfo user : userList) {
					if (user.userIndex.equals(enable_userIndex)) {
						user.enabled = true;
					}
				}
			} else if (token_byte.equals(Constants.NO)) {
				Toast.makeText(this, "设置失败", Toast.LENGTH_SHORT).show();
				enable_userIndex = "";
			}

			if (position == 2) {
				((UserFragment) mFragments.get(2)).getUserAdapter().notifyDataSetChanged();
			}
		} else if (token_type.equals(Constants.A7)) {
			if (token_byte.equals(Constants.OK)) {
				Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
				UserInfo temp = null;
				for (UserInfo user : userList) {
					if (user.userIndex.equals(delete_userIndex)) {
						temp = user;
					}
				}
				userList.remove(temp);
				// 记录刚才的操作，本地删除
			} else if (token_byte.equals(Constants.NO)) {
				Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
			}
			delete_userIndex = "";
			if (position == 2) {
				((UserFragment) mFragments.get(2)).getUserAdapter().notifyDataSetChanged();
			}
		} else if (token_type.equals(Constants.A8)) {
			if (token_byte.equals(Constants.OK)) {
				Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
				// 记录刚才的修改 设置mDeviceName;
				if (!TextUtils.isEmpty(newDeviceName)) {
					mDeviceName = DataFormatUtils.deleteOdd(DataFormatUtils.hexStr2Str(newDeviceName));
					SharePreferenceUtil.putString(this, Constants.KEY_DEVICE_NAME, mDeviceName);
					newDeviceName = "";
				}
				((LockFragment) mFragments.get(0)).getDeviceAdapter().notifyDataSetChanged();
			} else if (token_byte.equals(Constants.NO)) {
				Toast.makeText(this, this.getString(R.string.fail_to_new_name), Toast.LENGTH_SHORT).show();
			}

		}

	}

}
