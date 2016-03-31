package com.example.smartlock.entity;

import java.io.Serializable;

public class UserInfo implements Serializable{
	private static final long serialVersionUID = 3768081162299657458L;
	public final static int ACT_LOCK=1;
	public final static int ACT_OPEN=2;
	public String userName;
	public String deviceAddr;
	public String deviceName;
	public String password;
	public int lastAction;
	public String lastActTime;
	public String userIndex;
	public boolean enabled=true;
	public String checkNum="";
	public UserInfo(){
		
	}
	
	public UserInfo(String userName, String deviceAddr, String deviceName, String password, int lastAction,
			String lastActTime, String userIndex) {
		super();
		this.userName = userName;
		this.deviceAddr = deviceAddr;
		this.deviceName = deviceName;
		this.password = password;
		this.lastAction = lastAction;
		this.lastActTime = lastActTime;
		this.userIndex = userIndex;
	}

	@Override
	public String toString() {
		return "UserInfo [userName=" + userName + ", deviceAddr=" + deviceAddr + ", deviceName=" + deviceName
				+ ", password=" + password + ", lastAction=" + lastAction + ", lastActTime=" + lastActTime+",userIndex="
				+ userIndex+"]";
	}
	
    @Override  
    public int hashCode() {  
        final int prime = 31;  
        int result = 1;  
        result = prime * result + ((userName == null) ? 0 :userName.hashCode());  
        result = prime * result + ((userIndex == null) ? 0 : userIndex.hashCode());  
        return result;  
    }  
  
    @Override  
    public boolean equals(Object obj) {  
        if (this == obj)  
            return true;  
        if (obj == null)  
            return false;  
        if (getClass() != obj.getClass())  
            return false;  
        UserInfo other = (UserInfo) obj;  
        if (userName != other.userName)  
            return false;  
        if (userName == null) {  
            if (other.userName != null)  
                return false;  
        } else if (!userName.equals(other.userName))  
            return false;  
        if (userIndex != other.userIndex)  
        	return false;  
        if (userIndex == null) {  
        	if (other.userIndex != null)  
        		return false;  
        } else if (!userIndex.equals(other.userIndex))  
        	return false;  
        return true;  
    }  
	
}
