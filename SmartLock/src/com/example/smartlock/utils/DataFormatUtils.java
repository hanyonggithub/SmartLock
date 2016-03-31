package com.example.smartlock.utils;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

import com.example.smartlock.R;

import android.content.Context;
import android.text.TextUtils;

public class DataFormatUtils {

public static Calendar getDate(String time_hex){
	
	int year=Integer.parseInt(time_hex.substring(0,2), 16)+2000;
	int month=Integer.parseInt(time_hex.substring(2,4), 16);
	int day=Integer.parseInt(time_hex.substring(4,6), 16);
	int hour=Integer.parseInt(time_hex.substring(6,8), 16);
	int min=Integer.parseInt(time_hex.substring(8,10), 16);
	Calendar calendar=Calendar.getInstance();
	calendar.set(Calendar.YEAR, year);
	calendar.set(Calendar.MONTH, month);
	calendar.set(Calendar.DAY_OF_MONTH, day);
	calendar.set(Calendar.HOUR_OF_DAY, hour);
	calendar.set(Calendar.MINUTE, min);
	calendar.set(Calendar.SECOND, 0);
	return calendar;
}

public static String anylyseDate(Context context,String hex_str){
	
	if(hex_str==null||hex_str.length()==0){
		return "";
	}
	if(hex_str.equals("0000000000")){
		return context.getResources().getString(R.string.action_time, "00","00","00","00");
	}else{
		int year=Integer.parseInt(hex_str.substring(0,2), 16)+2000;
		int month=Integer.parseInt(hex_str.substring(2,4), 16);
		int day=Integer.parseInt(hex_str.substring(4,6), 16);
		int hour=Integer.parseInt(hex_str.substring(6,8), 16);
		int min=Integer.parseInt(hex_str.substring(8,10), 16);
		String monthStr=String.valueOf(month).length()<2?"0".concat(String.valueOf(month)):String.valueOf(month);
		String dayStr=String.valueOf(day).length()<2?"0".concat(String.valueOf(day)):String.valueOf(day);
		String hourStr=String.valueOf(hour).length()<2?"0".concat(String.valueOf(hour)):String.valueOf(hour);
		String minStr=String.valueOf(min).length()<2?"0".concat(String.valueOf(min)):String.valueOf(min);
//		return year+"年"+month+"月"+day+"日"+hour+"时"+min+"分";
		return context.getResources().getString(R.string.action_time, monthStr,dayStr,hourStr,minStr);
	}
		
	
}


public static String hexStr2Str(String hexStr){

	if(TextUtils.isEmpty(hexStr)||hexStr.length()==1){
		return "";
	}
	hexStr=hexStr.toUpperCase();
	//TODO:特殊字符的处理
	if(hexStr.length()%2!=0){
		hexStr=hexStr.substring(0,hexStr.length()-1);
	}
	StringBuffer buffer=new StringBuffer("");
	for(int i=0;i<hexStr.length();i+=2){
		int end=i+2<=hexStr.length()?i+2:hexStr.length();
		if(hexStr.substring(i,end).equals("FF")||hexStr.substring(i,end).equals("F")){
		}else{
			buffer.append((char)Integer.parseInt(hexStr.substring(i,i+2),16));
		}
	}
	return buffer.toString();
}

public static String deleteOdd(String str){
	StringBuffer buffer=new StringBuffer();
	for(char c:str.toCharArray()){
		
		if (c >= 48 && c <= 57) {// 数字
			buffer.append(c);
		}
		if (c >= 65 && c <= 90) {// 大写字母
			buffer.append(c);
		}
		if (c >=97 && c <= 122) {// 小写字母
			buffer.append(c);
		}
	}
	return buffer.toString();
}

private static String hexString = "0123456789ABCDEF";
/*
 * 将16进制数字解码成字符串,适用于所有字符（包括中文）
 */
public static String decode(String bytes) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(
            bytes.length() / 2);
    // 将每2位16进制整数组装成一个字节
    for (int i = 0; i < bytes.length(); i += 2)
        baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString
                .indexOf(bytes.charAt(i + 1))));
    return new String(baos.toByteArray());
}

/**
 * desc:将byte数组转为16进制字符串
 * @param bArray
 * @return
 * modified:    
 */
public static String bytesToHexString(byte[] bArray) {
    if(bArray == null){
        return null;
    }
    if(bArray.length == 0){
        return "";
    }
    StringBuffer sb = new StringBuffer(bArray.length);
    String sTemp;
    for (int i = 0; i < bArray.length; i++) {
        sTemp = Integer.toHexString(0xFF & bArray[i]);
        if (sTemp.length() < 2)
            sb.append(0);
        sb.append(sTemp.toUpperCase());
    }
    return sb.toString();
}
	

/**
 * desc:将16进制的数据转为数组
 * <p>创建人：聂旭阳 , 2014-5-25 上午11:08:33</p>
 * @param data
 * @return
 * modified:    
 */
public static byte[] StringToBytes(String data){
    String hexString=data.toUpperCase().trim();
    if (hexString.length()%2!=0) {
        return null;
    }
    byte[] retData=new byte[hexString.length()/2];
    for(int i=0;i<hexString.length();i++) {
    	int int_ch; //两位16进制数转化后的10进制数="" 
    	char hex_char1=hexString.charAt(i);// 两位16进制数中的第一位(高位*16)=""
    	int int_ch1; 
    	if(hex_char1>= '0' && hex_char1 <='9')
           int_ch1 = (hex_char1-48)*16;   //// 0 的Ascll - 48
        else if(hex_char1 >= 'A' && hex_char1 <='F')
            int_ch1 = (hex_char1-55)*16; //// A 的Ascll - 65
        else
            return null;
        i++;
        char hex_char2 = hexString.charAt(i); ///两位16进制数中的第二位(低位)
        int int_ch2;
        if(hex_char2 >= '0' && hex_char2 <='9')
            int_ch2 = (hex_char2-48); //// 0 的Ascll - 48
        else if(hex_char2 >= 'A' && hex_char2 <='F')
            int_ch2 = hex_char2-55; //// A 的Ascll - 65
        else
            return null;
        int_ch = int_ch1+int_ch2;
        retData[i/2]=(byte) int_ch;//将转化后的数放入Byte里
    }
    return retData;
}

//文字转化为16进制字符y用于用户名密码设备名称
public static String str2hexStr(String str){
	char[] digital = "0123456789ABCDEF".toCharArray();
	StringBuffer sb = new StringBuffer("");
	byte[] bs = str.getBytes();
	int bit;
	for (int i = 0; i < bs.length; i++) {
		if(bs[i]>=48&bs[i]<=59){
			
			sb.append("0").append(bs[i]-48);
		}else{
			bit = (bs[i] & 0x0f0) >> 4;
			sb.append(digital[bit]);
			bit = bs[i] & 0x0f;
			sb.append(digital[bit]);
		}
	}
	return sb.toString();
}
//文字转化为16进制字符y用于用户名密码设备名称
public static String str2hexStr(String str,int length){
	char[] digital = "0123456789ABCDEF".toCharArray();
	StringBuffer sb = new StringBuffer("");
	byte[] bs = str.getBytes();
	int bit;
	for (int i = 0; i < bs.length; i++) {
		if(bs[i]>=48&bs[i]<=59){
			
			sb.append("0").append(bs[i]-48);
		}else{
			bit = (bs[i] & 0x0f0) >> 4;
		sb.append(digital[bit]);
		bit = bs[i] & 0x0f;
		sb.append(digital[bit]);
		}
	}
	while(sb.length()<length){
		sb.append("F");
	}
	return sb.toString();
}

public static String hexStr2str(String hexstr){
	StringBuffer sb= new StringBuffer("");
	if(hexstr==null){
		return "";
	}
	byte[] b=hexstr.getBytes();
	if ((b.length % 2) != 0) {
		throw new IllegalArgumentException("长度不是偶数");
	}
	byte[] b2 = new byte[b.length / 2];
	for (int n = 0; n < b.length; n += 2) {
		String item = new String(b, n, 2);
		// 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个进制字节
		if(Integer.parseInt(item, 16)>=0&&Integer.parseInt(item, 16)<=9){
			sb.append(String.valueOf(Integer.parseInt(item, 16)));
		}else{
			sb.append((char)Integer.parseInt(item,16));
		}
	}
	b = null;
	return sb.toString();
}
public static String hexStr2CleanStr(String hexstr){
	StringBuffer sb= new StringBuffer("");
	if(hexstr==null){
		return "";
	}
	byte[] b=hexstr.getBytes();
	if ((b.length % 2) != 0) {
		throw new IllegalArgumentException("长度不是偶数");
	}
	byte[] b2 = new byte[b.length / 2];
	for (int n = 0; n < b.length; n += 2) {
		String item = new String(b, n, 2);
		// 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个进制字节
		if(!item.equals("FF")){
			if(Integer.parseInt(item, 16)>=0&&Integer.parseInt(item, 16)<=9){
				sb.append(String.valueOf(Integer.parseInt(item, 16)));
			}else{
				sb.append((char)Integer.parseInt(item,16));
			}
		}
		
	}
	b = null;
	return sb.toString();
}

}
