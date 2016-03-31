package com.example.smartlock.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import com.example.smartlock.R;
import com.example.smartlock.base.BaseFragment;
import com.example.smartlock.ble.BluetoothLeService;
import com.example.smartlock.callback.TitleBarClickListener;
import com.example.smartlock.entity.UserInfo;
import com.example.smartlock.fragment.LockFragment;
import com.example.smartlock.fragment.MainFragment;
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
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
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

public class MainActivity extends FragmentActivity implements OnClickListener {
	private final static String TAG = MainActivity.class.getSimpleName();
	private ViewPager mViewPager;
	private FragmentPagerAdapter mAdapter;
	private List<BaseFragment> mFragments = new ArrayList<BaseFragment>();
	private ImageView iv_lockList;
	private ImageView iv_key;
	private ImageView iv_user;
	private int btnCount = 3;
	private TitleBarClickListener titleBarClickListener;
	private int currentIndex;

	private BluetoothAdapter mBluetoothAdapter;
	private static final int REQUEST_ENABLE_BT = 1;

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
	private BluetoothGattCharacteristic mCharacteristic;

	private boolean mConnected = false;
	private BluetoothGattCharacteristic mNotifyCharacteristic;

	private final String LIST_NAME = "NAME";
	private final String LIST_UUID = "UUID";

	public String mDeviceName;
	public String mDeviceAddress;
	public String mName;
	public String mPsw;
	public String index;
	public int mDevBatLevel;
	public boolean isOpen = false;
	public boolean isAuto = true;

	public List<UserInfo> userList = new ArrayList<UserInfo>();
	// byte[] WriteBytes = null;
	byte[] WriteBytes = new byte[20];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		mName = SharePreferenceUtil.getString(this, Constants.KEY_NAME);
		mPsw = SharePreferenceUtil.getString(this, Constants.KEY_PSW);
		index = SharePreferenceUtil.getString(this, Constants.KEY_INDEX);
		mDevBatLevel = SharePreferenceUtil.getInt(this, Constants.KEY_BAT_LEVEL);
		mDeviceAddress=SharePreferenceUtil.getString(this, Constants.KEY_DEVICE_ADDR);
		mDeviceName=SharePreferenceUtil.getString(this, Constants.KEY_DEVICE_NAME);
		isAuto = SharePreferenceUtil.getBoolean(this, Constants.KEY_AUTO);
		
		Log.e(TAG, "缓存值：name="+mName+"psw="+mPsw+"index="+index+",mDeviceAddr="+mDeviceAddress+"isAuto="+isAuto);
	
		initView();
		initBluetooth();
		mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

			@Override
			public int getCount() {
				return mFragments.size();
			}

			@Override
			public Fragment getItem(int arg0) {
				return mFragments.get(arg0);
			}
		};
		mViewPager.setOffscreenPageLimit(1);
		mViewPager.setAdapter(mAdapter);

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {

				switch (position) {
				case 0:
					break;
				case 1:
					break;
				case 2:
					break;
				}

				currentIndex = position;
				if (position == 0) {
					currentIndex = btnCount;
				} else if (position == btnCount + 1) {
					currentIndex = 1;
				}
				if (position != currentIndex) {
					mViewPager.setCurrentItem(currentIndex, false);
					return;
				}
				Log.e(TAG, "onpagechange--currentindex:"+currentIndex);
				resetTabBtn(currentIndex);

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		
		if (TextUtils.isEmpty(mName) || TextUtils.isEmpty(mPsw) || TextUtils.isEmpty(index)) {
			mViewPager.setCurrentItem(1);
		} else {
//			mViewPager.setCurrentItem(2);
			mViewPager.setCurrentItem(1);
		}

	        Log.e(TAG, "oncreate finish");
	}

	private void initView() {
		mViewPager = (ViewPager) findViewById(R.id.vp_content);
		iv_lockList = (ImageView) findViewById(R.id.iv_locklist);
		iv_key = (ImageView) findViewById(R.id.iv_key);
		iv_user = (ImageView) findViewById(R.id.iv_user);
		iv_lockList.setOnClickListener(this);
		iv_key.setOnClickListener(this);
		iv_user.setOnClickListener(this);

		LockFragment lockFragment = new LockFragment();
		MainFragment mainFragment = new MainFragment();
		UserFragment userFragment = new UserFragment();
		mFragments.add(new UserFragment());
		mFragments.add(lockFragment);
		mFragments.add(mainFragment);
		mFragments.add(userFragment);
		mFragments.add(new LockFragment());
		Log.e(TAG, "has init view");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.e(TAG, "onresume");
		 registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null&&mDeviceAddress!=null) {
	            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
	            Log.e(TAG, "Connect request result=" + result);
	        }
		 Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
	     bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
//			if(mName!=null&&mPsw!=null){
//				//自动登录  ，跳转到开锁界面，使用上次登录成功的 名称 密码 设备，，考虑保存多个设备名，来匹配当前搜索到的设备
//				login(mName, mPsw, mDeviceAddress, mDeviceName);
//			}
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(mGattUpdateReceiver);
		unbindService(mServiceConnection);
		mBluetoothLeService = null;
		super.onDestroy();
	}

	protected void resetTabBtn(int currentIndex) {
	/*	iv_lockList.setImageResource(R.drawable.tab_lock);
		iv_key.setImageResource(R.drawable.tab_key);
		iv_user.setImageResource(R.drawable.tab_user);
		switch (currentIndex) {
		case 1:
			iv_lockList.setImageResource(R.drawable.tab_lock_selected);
			;
			break;
		case 2:
			iv_key.setImageResource(R.drawable.tab_key_selected);
			break;
		case 3:
			iv_user.setImageResource(R.drawable.tab_user_selected);
			break;

		default:
			iv_key.setImageResource(R.drawable.tab_key_selected);
			break;
		}*/
		Log.e(TAG, "reset Tab btn");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_locklist:
			mViewPager.setCurrentItem(1);
			resetTabBtn(1);
			Log.e(TAG, "onclick  lock");
			break;
		case R.id.iv_key:
			mViewPager.setCurrentItem(2);
			resetTabBtn(2);
			break;
		case R.id.iv_user:
			mViewPager.setCurrentItem(3);
			resetTabBtn(3);
			break;
		}
	}

	public void switchTab(int index) {
		switch (index) {
		case 1:
			mViewPager.setCurrentItem(1);
			resetTabBtn(1);
			break;
		case 2:
			mViewPager.setCurrentItem(2);
			resetTabBtn(2);
			break;
		case 3:
			mViewPager.setCurrentItem(3);
			resetTabBtn(3);
			break;

		default:

			break;
		}

	}



	private long mExitTime;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

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

	List<BluetoothDevice> deviceLists = new ArrayList<BluetoothDevice>();

	public void initBluetooth() {

		// Use this check to determine whether BLE is supported on the device.
		// Then you can
		// selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}

		// Initializes a Bluetooth adapter. For API level 18 and above, get a
		// reference to
		// BluetoothAdapter through BluetoothManager.
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		Log.e(TAG, "get the bluetoothAdapter");

		// Checks if Bluetooth is supported on the device.
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

	}

	public void bindService(String deviceName, String deviceAddress) {
		mDeviceName = deviceName;
		mDeviceAddress = deviceAddress;
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
		if (mBluetoothLeService != null) {
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			Log.e(TAG, "Connect request result=" + result);
		}
		((MainFragment)mFragments.get(2)).setTitle(DataFormatUtils.deleteOdd(mDeviceName));
	}
	public void unBindService(){
		if(mConnected==true){
			unbindService(mServiceConnection);
		}
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
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
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			if(!TextUtils.isEmpty(mDeviceAddress)){
				Log.e(TAG, "service 已连接，connect 设备");
				mBluetoothLeService.connect(mDeviceAddress);
			}
		
			Log.e(TAG, "service is bund!");
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
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
				Toast.makeText(MainActivity.this, "connected", Toast.LENGTH_LONG).show();
				Log.e(TAG, "Action connected");
				mConnected = true;
				
				invalidateOptionsMenu();
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				Toast.makeText(MainActivity.this, "连接断开了d", Toast.LENGTH_LONG).show();
				mConnected = false;
				// 如果在主界面，执行关锁动画，如果没有在该界面 则 在，当回到该界面的时候针对mconnected 做
				isOpen=false;
				((MainFragment)mFragments.get(2)).startCloseAnim();
				invalidateOptionsMenu();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				// Show all the supported services and characteristics on the
				// user interface.
				List<BluetoothGattService> gattServices = mBluetoothLeService.getSupportedGattServices();
				for (BluetoothGattService service : gattServices) {
					if (service.getUuid().toString().equals(SER_UUID)) {
						mGattService = service;
						mCharacteristic = mGattService.getCharacteristic(UUID.fromString(CHAR_UUID));
						Log.e(TAG, "获取目标设备 charater");
					}
				}
				if(mName!=null&&mPsw!=null){
					//自动登录  ，跳转到开锁界面，使用上次登录成功的 名称 密码 设备，，考虑保存多个设备名，来匹配当前搜索到的设备
					login(mName, mPsw, mDeviceAddress, mDeviceName);
				}
				// List<BluetoothGattCharacteristic> gattCharacteristics =
				// gattService.getCharacteristics();

			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				Toast.makeText(MainActivity.this, "返回:" + intent.getStringExtra(BluetoothLeService.EXTRA_DATA),
						Toast.LENGTH_LONG).show();
				;
				// 解析数据 指令码 校验值
				processData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));

				// displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
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
		String year_hex = "0".concat(Integer.toHexString(calendar.get(Calendar.YEAR)%100));
		String month_hex = "0".concat(Integer.toHexString(calendar.get(Calendar.MONTH) + 1));
		String day_hex = "0".concat(Integer.toHexString(calendar.get(Calendar.DAY_OF_MONTH)));
		String hour_hex = "0".concat(Integer.toHexString(calendar.get(Calendar.HOUR_OF_DAY)));
		String minute_hex = "0".concat(Integer.toHexString(calendar.get(Calendar.MINUTE)));
		
		year_hex=year_hex.substring(year_hex.length()-2, year_hex.length());
		month_hex=month_hex.substring(month_hex.length()-2, month_hex.length());
		day_hex=day_hex.substring(day_hex.length()-2, day_hex.length());
		hour_hex=hour_hex.substring(hour_hex.length()-2, hour_hex.length());
		minute_hex=minute_hex.substring(minute_hex.length()-2, minute_hex.length());
		return year_hex.concat(month_hex).concat(day_hex).concat(hour_hex).concat(minute_hex);
	}

	public static String getCheckNum() {
		Random random = new Random();
		int checkNum = random.nextInt(89) + 10;
		return String.valueOf(checkNum);
	}

	public boolean write(String str) {
		Log.e(TAG, "write:"+str);
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
				Toast.makeText(this, "write:"+str, Toast.LENGTH_SHORT).show();
				mBluetoothLeService.writeCharacteristic(mCharacteristic);

				if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
					mNotifyCharacteristic = mCharacteristic;
					mBluetoothLeService.setCharacteristicNotification(mCharacteristic, true);
				}

				return true;
			}
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
//		bindService(mDeviceName, mDeviceAddress);
		// 写指令
		String login_token = "A3".concat(name).concat(psw).concat(MainActivity.getTimeStamp()).concat("FF")
				.concat(MainActivity.getCheckNum());
		write(login_token);
	}
	public void login(){
		String login_token = "A3".concat(mName).concat(mPsw).concat(MainActivity.getTimeStamp()).concat("FF")
				.concat(MainActivity.getCheckNum());
		write(login_token);
	}
	
	public void openLock(){
		bindService(mDeviceName, mDeviceAddress);
		Toast.makeText(this, "name:"+mName, Toast.LENGTH_SHORT).show();
		Toast.makeText(this, "pswd:"+mPsw, Toast.LENGTH_SHORT).show();
		Toast.makeText(this, "time:"+getTimeStamp(), Toast.LENGTH_SHORT).show();
		Toast.makeText(this, "inde:"+index, Toast.LENGTH_SHORT).show();
		Toast.makeText(this, "chec:"+getCheckNum(), Toast.LENGTH_SHORT).show();
		
		Log.e(TAG, "name="+mName+",psw"+mPsw+",time="+getTimeStamp()+",index"+index);
		String open_token="A0".concat(mName).concat(mPsw).concat(getTimeStamp()).concat(index).concat(getCheckNum());
		write(open_token);
	}

	public void processData(String str) {
		if (str == null) {
			Toast.makeText(MainActivity.this, "异常数据", Toast.LENGTH_SHORT).show();
			return;
		}
		String[] strs=str.substring(21, str.length()).split(" ");
		// 获取指令码
		String token_type = strs[0];
		String token_sum = strs[19];
		String token_byte = strs[18];
		String token_time = "";
		for(int i=13;i<18;i++){
			token_time=token_time.concat(strs[i]);
		}
		String userName ="";
	for(int j=1;j<7;j++){
		userName=userName.concat(strs[j]);
	}
		String userpsw = "";
		for(int k=7;k<13;k++){
			userpsw=userpsw.concat(strs[k]);
		}
		Log.e(TAG, "type:"+token_byte+",sum:"+token_sum+",byte:"+token_byte+",time:"+token_time+",name:"+userName+",psw:"+userpsw);

		if (token_type.equals(Constants.A0)) {
			if (token_byte.equals(Constants.OK)) {
				// 如果在主界面执行开锁动画
				isOpen=true;
				((MainFragment)mFragments.get(2)).startOpenAnim();
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
			SharePreferenceUtil.putInt(this,Constants.KEY_BAT_LEVEL,mDevBatLevel);
			((MainFragment) mFragments.get(3)).setBatLevel(mDevBatLevel);
			// 设置电量

		} else if (token_type.equals(Constants.A2)) {
			// 创建用户 保存刚才创建的用户名密码，如果创建成功则添加到userList中 默认没有任何操作
			if (token_byte.equals(Constants.OK)) {

			} else if (token_byte.equals(Constants.NO)) {
				// 创建失败，不添加
				Toast.makeText(this, "创建用户失败", Toast.LENGTH_SHORT).show();
			}

		} else if (token_type.equals(Constants.A3)) {
			// 判斷是否成功
			if (token_byte.equals("00") || token_byte.equals("01") || token_byte.equals("02")
					|| token_byte.equals("03")) {
				UserInfo user=new UserInfo(userName, mDeviceAddress, mDeviceName, mPsw, UserInfo.ACT_OPEN, token_time,
						token_byte);
				userList.add(user);
			
//				SharePreferenceUtil.put(MainActivity.this, Constants.KEY_USER+token_type,user);
				if(userName.equals(mName)){
					SharePreferenceUtil.putString(MainActivity.this, Constants.KEY_NAME, mName);
					SharePreferenceUtil.putString(MainActivity.this, Constants.KEY_PSW, mPsw);
					SharePreferenceUtil.putString(MainActivity.this, Constants.KEY_DEVICE_ADDR, mDeviceAddress);
					SharePreferenceUtil.putString(MainActivity.this, Constants.KEY_DEVICE_NAME, mDeviceName);
					SharePreferenceUtil.putString(MainActivity.this, Constants.KEY_INDEX, token_byte);
					Log.e(TAG, "写入缓存：name="+mName+"psw="+mPsw+"index="+token_byte+",mDeviceAddr="+mDeviceAddress);
				}
				
				//如果在用户列表界面要刷新相关用户数据
				
				try {
//					((UserFragment)mFragments.get(3)).getUserAdapter().notifyDataSetChanged();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
//				((UserFragment)mFragments.get(3)).setTitle(name);
				// TODO:相同username去重问题 什么时候清空用户列表 
				// 跳转页面 ，是之前跳转还是之后跳转
//				Toast.makeText(this, "登录成功,userlist.size=" + userList.size(), Toast.LENGTH_SHORT).show();
				Log.e(TAG, "userList.size="+userList.size());
			} else {
				Toast.makeText(MainActivity.this, "登录失败，请重新登录！", Toast.LENGTH_SHORT).show();
			}

		} else if (token_type.equals(Constants.A4)) {
			if (token_byte.equals(Constants.OK)) {
				Toast.makeText(this, "修改用户成功", Toast.LENGTH_SHORT).show();
			} else if (token_byte.equals(Constants.NO)) {
				Toast.makeText(this, "修改用户失败", Toast.LENGTH_SHORT).show();
			}

		} else if (token_type.equals(Constants.A5)) {
			if (token_byte.equals(Constants.OK)) {
				Toast.makeText(this, "已暂停", Toast.LENGTH_SHORT).show();
			} else if (token_byte.equals(Constants.NO)) {
				Toast.makeText(this, "设置失败", Toast.LENGTH_SHORT).show();
			}

		} else if (token_type.equals(Constants.A6)) {
			if (token_byte.equals(Constants.OK)) {
				Toast.makeText(this, "已恢复", Toast.LENGTH_SHORT).show();
			} else if (token_byte.equals(Constants.NO)) {
				Toast.makeText(this, "设置失败", Toast.LENGTH_SHORT).show();
			}
		} else if (token_type.equals(Constants.A7)) {
			if (token_byte.equals(Constants.OK)) {
				Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
				// 记录刚才的操作，本地删除

			} else if (token_byte.equals(Constants.NO)) {
				Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
			}
		} else if (token_type.equals(Constants.A8)) {
			if (token_byte.equals(Constants.OK)) {
				Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
				// 记录刚才的修改 设置mDeviceName;

			} else if (token_byte.equals(Constants.NO)) {
				Toast.makeText(this, "修改失败", Toast.LENGTH_SHORT).show();
			}

		}

	}

}
