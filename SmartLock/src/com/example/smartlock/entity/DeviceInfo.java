package com.example.smartlock.entity;

import android.bluetooth.BluetoothDevice;


public class DeviceInfo{
	
	private BluetoothDevice blueToothDevice;
	private String name;
	private String address;
	private boolean isOn=false;
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public DeviceInfo() {
	}
	public DeviceInfo(String name,String address){
		this.name=name;
		this.address=address;
	}
	public DeviceInfo(BluetoothDevice device){
		this.blueToothDevice=device;
		this.name=device.getName();
		this.address=device.getAddress();
	}
	public BluetoothDevice getBlueToothDevice() {
		return blueToothDevice;
	}
	public void setBlueToothDevice(BluetoothDevice blueToothDevice) {
		this.blueToothDevice = blueToothDevice;
		this.name=blueToothDevice.getName();
		this.address=blueToothDevice.getAddress();
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isOn() {
		return isOn;
	}
	public void setOn(boolean isOn) {
		this.isOn = isOn;
	}
	@Override
	public boolean equals(Object o) {
        if(o == null)
        {
            return false;
        }
        if (o == this)
        {
           return true;
        }
        if (getClass() != o.getClass())
        {
            return false;
        }
        DeviceInfo e = (DeviceInfo) o;
        return (this.address == e.address);
}
	@Override
	 public int hashCode()
	 {
	    final int PRIME = 31;
	    int result = 1;
	    result = PRIME * result + address.hashCode();
	    return result;
	 }
}
