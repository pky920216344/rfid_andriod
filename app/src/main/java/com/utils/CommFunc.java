package com.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.MyApplication;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.text.SimpleDateFormat;

public class CommFunc {
    public static final String URL_Base = "http://www.jsrfiot.com/";
    //通知服务器序列号
    private static final String URL_SendSerial = URL_Base + "Server/Serial";

    /**
     * 发送序列号到服务器
     *
     * @param serial
     */
    public static void sendSerialNo(Context context, String serial) {
        if (TextUtils.isEmpty(serial)) return;
        if (!isNetworkConnected(context)) return;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serial", serial);
            jsonObject.put("context", context.getClass().getSimpleName());
            jsonObject.put("imei", MyApplication.IMEI);
            jsonObject.put("version", MyApplication.VersionName);
            jsonObject.put("time", CommFunc.GetTimeStr_ms());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String jsonStr = jsonObject.toString();
        Log.d("sendSerialNo:", jsonStr);
        RequestParams params = new RequestParams(URL_SendSerial);
        params.setConnectTimeout(5000);
        params.setAsJsonContent(true);
        params.setBodyContent(jsonStr);
        params.addHeader("Content-Type", "applicaiton/json");
        x.http().post(params, new Callback.CommonCallback<String>() {
            String logResult = "";

            @Override
            public void onSuccess(String result) {
                logResult = "onSuccess:" + result;
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                logResult = "onError:" + Log.getStackTraceString(ex);
            }

            @Override
            public void onCancelled(CancelledException cex) {
                logResult = "onCancelled";
            }

            @Override
            public void onFinished() {
                Log.i("Serial", "result=" + logResult);
            }
        });
    }

    /**
     * 判断网络连接是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context == null) return false;

        ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }

    private static String macAddress = "";

    /**
     * 使用IMEI作为MAC地址返回
     */
    public static String getIMEI() {
        return getIMEI(MyApplication.getAppContext());
    }

    /**
     * 使用IMEI作为MAC地址返回
     */
    public static String getIMEI(Context context) {
        if (TextUtils.isEmpty(macAddress)) {
            try {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                @SuppressLint("MissingPermission") String imei = telephonyManager.getDeviceId();
                if (imei.length() == 14) imei = imei + getImei15(imei);
                macAddress = imei;
            } catch (Exception e) {
                macAddress = "get_empty_imei_";
            }
        }
        return macAddress;
    }

    /**
     * 计算14位imei的第15位校验位
     *
     * @param imei
     * @return
     */
    private static String getImei15(String imei) {
        if (imei.length() == 14) {
            char[] imeiChar = imei.toCharArray();
            int resultInt = 0;
            for (int i = 0; i < imeiChar.length; i++) {
                int a = Integer.parseInt(String.valueOf(imeiChar[i]));
                i++;
                final int temp = Integer.parseInt(String.valueOf(imeiChar[i])) * 2;
                final int b = temp < 10 ? temp : temp - 9;
                resultInt += a + b;
            }
            resultInt %= 10;
            resultInt = resultInt == 0 ? 0 : 10 - resultInt;
            return resultInt + "";
        } else {
            return "";
        }
    }

    /**
     * 获取时间字符串
     *
     * @return
     */
    public static String GetTimeStr_ms() {
        //获取当前系统的时间
        java.util.Date date = new java.util.Date();
        //格式化日期
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        //转为字符串
        String dateStr = format.format(date);
        return dateStr;
    }
}
