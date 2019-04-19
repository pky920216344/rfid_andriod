package com;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Display;

import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.PhoneUtils;
import com.blankj.utilcode.util.Utils;
import com.sample.utility.PrefUtils;
import com.utils.CommFunc;
import com.utils.CrashHandler;
import com.utils.Log4;

import org.xutils.x;

import Util.APKVersionCodeUtils;
import broadcast.BootBroadcastReceiver;

public class MyApplication extends Application {

    private static Context context;
    public Display externDisplay;
    private static MyApplication myApplication;

    public static MyApplication getInstance() {
        return myApplication;
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

    public static String VersionName = "";
    public static String PackageName = "";
    public static String BroadcastClassName = "";
    public static String IMEI = "";
    public static String SerialNo = "";
    public static String IMEIS = "";
    public static String DeviceMac = "";
    public static String IMEISMD5 = "";
    public static String DeviceMacMD5 = "";

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        MyApplication.context = getApplicationContext();
        MyApplication.VersionName = APKVersionCodeUtils.getVerName(context);
        MyApplication.PackageName = APKVersionCodeUtils.getPackageName(context);
        MyApplication.BroadcastClassName = PrefUtils.getString(context, BootBroadcastReceiver.BroadcastClassName, "");
        MyApplication.IMEI = CommFunc.getIMEI();

        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] presentationDisplays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        Log.d("MyApplication", "" + presentationDisplays.length);
        if (presentationDisplays.length > 0) {
            externDisplay = presentationDisplays[0];//第一个合适的
        }

        //utilcode初始化
        Utils.init(getApplicationContext());
        //xUtils框架初始化
        x.Ext.init(this);
        x.Ext.setDebug(false);

        //Log4j框架初始化
        Log4.configLog();
        //记录启动log信息
        Log4.info("app启动");
        Log4.info("------ version:" + MyApplication.VersionName);
        Log4.info("------ imei:" + MyApplication.IMEI);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            IMEIS = DeviceUtils.getMacAddress().toUpperCase();
        }else{
            IMEIS = PhoneUtils.getIMEI().toUpperCase();
        }

        IMEISMD5 =  EncryptUtils.encryptMD5ToString(IMEIS).toLowerCase();

        Log4.info("IMEI:"+IMEIS);
        Log4.info("IMEIMD5:"+ IMEISMD5);

        DeviceMac = DeviceUtils.getMacAddress().toUpperCase();
        DeviceMacMD5 = EncryptUtils.encryptMD5ToString(DeviceMac).toLowerCase();

        Log4.info("DeviceMac:"+DeviceMac);
        Log4.info("DeviceMac MD5:"+ DeviceMacMD5);
        //全局未捕捉异常处理
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init();
    }


}
