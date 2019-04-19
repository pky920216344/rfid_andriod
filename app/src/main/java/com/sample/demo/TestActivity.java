package com.sample.demo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import com.MyApplication;
import com.blankj.utilcode.util.ToastUtils;
import com.protocol.ServiceMessage;
import com.protocol.ServiceProtocalDef;
import com.protocol.ServiceProtocalHandler;
import com.raifu.rfidapi.RaifuGpioDevice;
import com.raifu.rfidapi.RaifuRFIDControl;
import com.raifu.rfidapi.RaifuRFIDReader;
import com.raifu.rfidapi.ReadCallback;
import com.raifu.rfidapi.ResultMsg;
import com.sample.GpioDevice;
import com.sample.Rs485Device;
import com.sample.gpio.IOChangeListener;
import com.sample.gpio.IOChangeObserver;
import com.sample.uhf.EpcDataDifference;
import com.sample.utility.PrefUtils;
import com.utils.CommFunc;
import com.utils.Log4;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;

import broadcast.BootBroadcastReceiver;
import me.shihao.library.XRadioGroup;
import com.netUtils.BaseClientConfig;
import com.netUtils.BaseClient;

@ContentView(R.layout.activity_test)
public class TestActivity extends Activity {

    private static final String TAGS = "TestActivity";
    //1:单门无霍尔
    //2:双门无霍尔
    //3:单门带霍尔
    //4:双门带霍尔
    private static int doorKind = 1;
    private static int power = 290;

    private static RaifuRFIDControl reader = new RaifuRFIDReader();

    static {
        if (GpioDevice.getDeviceKind() == 0) {
            GpioDevice.io_setReader(true);
            //reader.openReader("/dev/ttyS4");
            reader.openReader("/dev/ttyS1");
            Rs485Device.setRs485Name("/dev/ttyS3");
            Log.e("DeviceKind", "DeviceKind:0");
        } else if (GpioDevice.getDeviceKind() == 1) {
            GpioDevice.io_setReader(true);
            reader.openReader("/dev/ttyHSL1");
            Rs485Device.setRs485Name("/dev/ttyHSL2");
            Log.e("DeviceKind", "DeviceKind:1");
        } else {
            Log.e("DeviceKind", "DeviceKind:" + GpioDevice.getDeviceKind());
        }
    }

    //模块对应的二维码
    @ViewInject(R.id.iv_barcode)
    private ImageView ivBarcode;
    //模块对应的二维码
    @ViewInject(R.id.iv_barcode2)
    private ImageView ivBarcode2;
    //IO状态
    @ViewInject(R.id.tv_io_status)
    private TextView tvIoStatus;
    //模块序列号
    @ViewInject(R.id.tv_serial_number)
    private TextView tvSerialNumber;
    //单门无霍尔
    @ViewInject(R.id.rb_single_door_no)
    private RadioButton rbSingleDoorNo;
    //双门无霍尔
    @ViewInject(R.id.rb_double_door_no)
    private RadioButton rbDoubleDoorNo;
    //单门有霍尔
    @ViewInject(R.id.rb_single_door_yes)
    private RadioButton rbSingleDoorYes;
    //双门有霍尔
    @ViewInject(R.id.rb_double_door_yes)
    private RadioButton rbDoubleDoorYes;
    //工作轮数
    @ViewInject(R.id.et_run_count)
    private EditText etRunCount;
    //盘点超时时间
    @ViewInject(R.id.et_timeout)
    private EditText etTimeout;
    //开机启动
    @ViewInject(R.id.switch_broadcast)
    private Switch switchBroadcast;
    //盘点模式
    @ViewInject(R.id.switch_inventory_run_mode)
    private Switch switchInventoryRunMode;
    //IO切换
    @ViewInject(R.id.switch_io)
    private Switch switchIO;
    //盘点状态显示
    @ViewInject(R.id.tv_inventory_status)
    private TextView tvInventoryStatus;
    //盘点按钮
    @ViewInject(R.id.btn_inventory)
    private TextView btnInventory;
    //盘点标签数量
    @ViewInject(R.id.tv_epc_count)
    private TextView tvEpcCount;
    //每次盘点减少的标签
    @ViewInject(R.id.tv_epcs)
    private TextView tvEpcs;

    private int runMaxCount = 2;
    private int timeOut = 7000;

    private ServiceProtocalHandler _ServiceProtocalHandlerCallback = null;
    private BaseClient _BaseClient = null;
    private ServiceMessage _ServiceMessage = null;
    private Timer _mTimer = new Timer();

    private IOChangeObserver ioChange = new IOChangeObserver() {
        @Override
        public void IOChangeObserver(final int count, final int[] value) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String str = "[" + count + "] ";
                    for (int i = 0; i < value.length; i++) {
                        str += value[i] + " ";
                    }
                    Log.d("IOChange", str);
                    tvIoStatus.setText(str);
                }
            });
        }
    };

    /*
        //定时任务，定时获取门锁状态
        private TimerTask task = new TimerTask() {
            public void run() {
                Log.d(TAGS,"TimerTask");
                //获取设备信息

            }
        };
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        setFullScreen(true);

        //LED翻转
        tagLEDThread.start();

        String serial = CommFunc.getIMEI();
        Log.e("Serial", serial);
        tvSerialNumber.setText(serial);
        //模块序列号
        String rfidNo = reader.getSerialNumber();
        //发送序列号到服务器
        CommFunc.sendSerialNo(this, rfidNo);

        //实例化协议处理类
        _ServiceProtocalHandlerCallback = new ServiceProtocalHandler();
        //连接到服务器
        _BaseClient = new BaseClient(BaseClientConfig.HOST, BaseClientConfig.PORT);
        _BaseClient.setCallbackHandler(_ServiceProtocalHandlerCallback);
        _BaseClient.connect();//启动连接

        //模块可用的二维码
        Bitmap bitmap = ZXingUtils.createQRImage("https://www.jsrfiot.com/View/mpscan.aspx?code=" + serial, serial, 120, 120);
        //Bitmap bitmap = ZXingUtils.createQRImage(serial, serial, 120, 120);
        ivBarcode.setImageBitmap(bitmap);

        Bitmap bitmap2 = ZXingUtils.createQRImage("https://www.jsrfiot.com/View/mpscan.aspx?code=" + serial, serial, 500, 500);
        //Bitmap bitmap2 = ZXingUtils.createQRImage( serial, serial, 500, 500);
        ivBarcode2.setImageBitmap(bitmap2);

        reader.initReader();
        reader.setAntennaPortAuto(60, power);
        initAnt();
        Inventory(2, 7000);
        //GpioDevice.io_setLED(true);

        initDoorKind();
        switchBroadcast.setChecked(BootBroadcastReceiver.getBootBroadcast(this));
        setFullScreen(true);

        //60s查询门锁状态
        // _mTimer.schedule(task, 0, 60*1000);

        //注册eventbus
        EventBus.getDefault().register(this);
    }

    //EventBus事件处理
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void ProcessEvent(final ServiceMessage event) {
        switch (event.get_dispatcher()) {
            //开门指令
            case ServiceProtocalDef.OPTION_CMD_OPEN_DOOR_REQ:
                Log.d(TAGS, "开门指令");

                RaifuGpioDevice.doorOpen(1, new IOChangeListener() {
                    @Override
                    public void IOChangeListener(int i, final String s) {
                        //消息状态返回
                        ServiceMessage mServiceMessage = new ServiceMessage(MyApplication.DeviceMacMD5, MyApplication.IMEISMD5, event.get_unicode(),
                                ServiceProtocalDef.OPTION_CMD_OPEN_DOOR_RESP, true, i, null);
                        _ServiceProtocalHandlerCallback.doorCmdResp(mServiceMessage);

                        //关门盘点
                        if (i == -1 || i == 0) {
                            mServiceMessage.set_dispatcher(ServiceProtocalDef.OPTION_CMD_DEVICE_AUTO_INVENTORY_IND);
                            EventBus.getDefault().post(mServiceMessage);
                        }
                    }
                });
                break;

            //天线设置参数
            case ServiceProtocalDef.OPTION_CMD_ANT_SET_REQ:
                Log.d(TAGS, "1天线设置参数");
                try {
                    //获取数组
                    List<Map<String, Integer>> list = (List<Map<String, Integer>>) event.getData();

                    Log.d(TAGS, "1LIST:" + list.toString());

                    int[][] antPower = new int[list.size()][2];

                    for (int idx = 0; idx < list.size(); idx++) {
                        Map<String, Integer> map = list.get(idx);
                        antPower[idx][0] = map.get("no");
                        antPower[idx][1] = map.get("power");
                        Log.d(TAGS, "No:" + antPower[idx][0] + " Power:" + antPower[idx][1]);
                    }

                    //设置天线参数
                    reader.setAntennaPort(antPower);
                    //显示天线参数
                    initAnt();
                    //消息状态返回
                    ServiceMessage mServiceMessage = new ServiceMessage(MyApplication.DeviceMacMD5, MyApplication.IMEISMD5, event.get_unicode(),
                            ServiceProtocalDef.OPTION_CMD_ANT_SET_RESP, true, 0, null);
                    _ServiceProtocalHandlerCallback.antSetCmdResp(mServiceMessage);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;

            //获取天线参数
            case ServiceProtocalDef.OPTION_CMD_ANT_GET_REQ:
                Log.d(TAGS, "获取天线参数");
                int index = 1;
                List<Map<String, Integer>> list = new ArrayList<Map<String, Integer>>();
                int[][] ants = reader.getAntennaPort();
                //消息状态返回

                for (int idx = 0; idx < 8; idx++) {
                    int power = getPowerByAntPower(idx, ants);
                    if (power > 0) {
                        Map<String, Integer> map = new HashMap<>();
                        map.put("no", idx + 1);
                        map.put("power", power);
                        list.add(map);
                    }
                }

                Log.d(TAGS, "OPTION_CMD_ANT_GET_REQ list:" + list.toString());

                ServiceMessage mServiceMessage = new ServiceMessage(MyApplication.DeviceMacMD5, MyApplication.IMEISMD5, event.get_unicode(),
                        ServiceProtocalDef.OPTION_CMD_ANT_GET_RESP, true, 0, list);
                _ServiceProtocalHandlerCallback.antGetCmdResp(mServiceMessage);

                break;
            //关门盘点
            case ServiceProtocalDef.OPTION_CMD_DEVICE_AUTO_INVENTORY_IND:
                Log.d(TAGS, "关门盘点上报");
                //服务器人工盘点
            case ServiceProtocalDef.OPTION_CMD_INVENTORY_GET_REQ:
                Log.d(TAGS, "服务器人工盘点");
                _ServiceMessage = event;
                this.Inventory(5, 7000);
                break;
            //门锁报警反馈
            case ServiceProtocalDef.OPTION_CMD_DOOR_ALAARM_CFM:
                Log.d(TAGS, "门锁报警反馈");
                break;
            //关门盘点数量反馈
            case ServiceProtocalDef.OPTION_CMD_DEVICE_AUTO_INVENTORY_CFM:
                Log.d(TAGS, "关门盘点数量反馈");
                break;
            default:
                Log.e(TAGS, "Unsupport cmds" + event.get_dispatcher());
                break;
        }
    }

    private String execCommand(String command) {
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec(command);
            InputStream inputstream = proc.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
            String line = "";
            StringBuilder sb = new StringBuilder(line);
            while ((line = bufferedreader.readLine()) != null) {
                sb.append(line);
            }

            try {
                if (proc.waitFor() == 0) {
                    return sb.toString();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("lifeCycle" + Thread.currentThread().getId(), "________onDestroy");
        //mqtt停止
        Log.e("ondestroy", "ok");

        //取消注册EventBus
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        //关闭连接
        _BaseClient.disconnect();
    }

    //盘点
    @Event(value = R.id.btn_inventory, type = View.OnClickListener.class)
    private void btnInventory_onClick(View v) {
        setFullScreen(true);
        String runMaxCountStr = etRunCount.getText().toString();
        String timeOutStr = etTimeout.getText().toString();
        runMaxCount = runMaxCountStr == "" ? 0 : Integer.parseInt(runMaxCountStr);
        timeOut = timeOutStr == "" ? 0 : Integer.parseInt(timeOutStr);
        InventoryWrap(runMaxCount, timeOut);
    }

    //连续盘点的停止
    @Event(value = R.id.btn_inventory_stop, type = View.OnClickListener.class)
    private void btnInventoryStop_onClick(View v) {
        isLoopInventory = false;
    }

    //开门指令
    @Event(value = R.id.btn_open_door, type = View.OnClickListener.class)
    private void btnOpenDoor_onClick(View v) {
        RaifuGpioDevice.doorOpen(doorKind, new IOChangeListener() {
            @Override
            public void IOChangeListener(final int i, final String s) {
                runOnUiThread(new Runnable() {
                    final String info = s;
                    final int state = i;

                    @Override
                    public void run() {
                        ToastUtils.showShort("[" + state + "]门状态发生改变" + info);
                    }
                });
            }
        });
    }

    //设定天线
    @Event(value = R.id.btn_ant_set, type = View.OnClickListener.class)
    private void btnAntSet_onClick(View v) {
        setAnt();
        initAnt();
        setFullScreen(true);
    }

    //自动读取天线设定
    @Event(value = R.id.btn_ant_auto, type = View.OnClickListener.class)
    private void btnAntAuto_onClick(View v) {
        reader.setAntennaPortAuto(60, power);
        initAnt();
        setFullScreen(true);
    }

    //盘点模式切换
    @Event(value = R.id.switch_inventory_run_mode, type = CompoundButton.OnCheckedChangeListener.class)
    private void switchInventoryRunMode_onCheckedChange(CompoundButton buttonView, boolean isChecked) {
        switchInventoryRunMode.setText(isChecked ? "连续" : "单次");
    }

    //开机启动切换
    @Event(value = R.id.switch_broadcast, type = CompoundButton.OnCheckedChangeListener.class)
    private void switchBroadcast_onCheckedChange(CompoundButton buttonView, boolean isChecked) {
        BootBroadcastReceiver.setBootBroadcast(this, isChecked);
    }

    //IO切换
    @Event(value = R.id.switch_io, type = CompoundButton.OnCheckedChangeListener.class)
    private void switchIO_onCheckedChange(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            switchIO.setText("开启");
            GpioDevice.addIOWatch(ioChange);
        } else {
            switchIO.setText("关闭");
            GpioDevice.removeIOWatch(ioChange);
            tvIoStatus.setText("");
        }
    }

    //开灯
    @Event(value = R.id.btn_led_on, type = View.OnClickListener.class)
    private void btnLedOn_onClick(View v) {
        setFullScreen(true);
        GpioDevice.io_setLED(true);
    }

    //关灯
    @Event(value = R.id.btn_led_off, type = View.OnClickListener.class)
    private void btnLedOff_onClick(View v) {
        setFullScreen(true);
        GpioDevice.io_setLED(false);
    }

    //开门
    @Event(value = R.id.btn_door_on, type = View.OnClickListener.class)
    private void btnDoorOn_onClick(View v) {
        setFullScreen(true);
        GpioDevice.io_setDoor(true);
    }

    //关门
    @Event(value = R.id.btn_door_off, type = View.OnClickListener.class)
    private void btnDoorOff_onClick(View v) {
        setFullScreen(true);
        GpioDevice.io_setDoor(false);
    }

    //开门2
    @Event(value = R.id.btn_door2_on, type = View.OnClickListener.class)
    private void btnDoor2On_onClick(View v) {
        setFullScreen(true);
        GpioDevice.io_setDoor2(true);
    }

    //关门2
    @Event(value = R.id.btn_door2_off, type = View.OnClickListener.class)
    private void btnDoor2Off_onClick(View v) {
        setFullScreen(true);
        GpioDevice.io_setDoor2(false);
    }

    //门类型选择
    @Event(value = R.id.rg_door_group, type = XRadioGroup.OnCheckedChangeListener.class)
    private void rgDoorGroup_onClick(XRadioGroup group, int checkedId) {
        if (checkedId == R.id.rb_single_door_no) {
            doorKind = 1;
        } else if (checkedId == R.id.rb_double_door_no) {
            doorKind = 2;
        } else if (checkedId == R.id.rb_single_door_yes) {
            doorKind = 3;
        } else if (checkedId == R.id.rb_double_door_yes) {
            doorKind = 4;
        }
        PrefUtils.putInt(this, "doorKind", doorKind);
    }

    //设定全屏显示
    private void setFullScreen(boolean fullScreen) {
        if (fullScreen) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
        }

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        Log.e("分辨率", "Density is " + displayMetrics.density + " densityDpi is " + displayMetrics.densityDpi + " height: " + displayMetrics.heightPixels +
                " width: " + displayMetrics.widthPixels);
    }

    //TagLED
    private static class TagLEDThread extends Thread {
        private int tStep = 10;
        private int timeSpace = 50;
        private boolean isStart = false;

        public void setContinue() {
            synchronized (this) {
                tStep = 10;
                isStart = true;
                this.notifyAll();
            }
        }

        @Override
        public void run() {
            while (true) {
                //0.如果没开始
                if (!isStart) {
                    synchronized (this) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //1.开始
                GpioDevice.io_setLED(true);
                tStep--;
                try {
                    Thread.sleep(timeSpace);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (this) {
                    if (tStep <= 0) {
                        GpioDevice.io_setLED(false);
                        isStart = false;
                    }
                }
            }
        }
    }

    private TagLEDThread tagLEDThread = new TagLEDThread();

    private volatile int oneInventoryCount = 1;
    private volatile boolean isLoopInventory = true;

    //盘点
    private void InventoryWrap(final int runMaxCount, final int timeOut) {
        new Thread() {
            public void run() {
                //禁用画面部分功能
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnInventory.setEnabled(false);
                        switchInventoryRunMode.setEnabled(false);
                        findViewById(R.id.btn_ant_auto).setEnabled(false);
                        findViewById(R.id.btn_ant_set).setEnabled(false);
                        findViewById(R.id.btn_inventory_stop).setEnabled(true);
                    }
                });

                boolean loopRun = switchInventoryRunMode.isChecked();
                if (loopRun) {      //连续盘点
                    isLoopInventory = true;
                } else {            //单次盘点
                    oneInventoryCount = 1;
                }

                //盘点(单次or连续)
                while ((loopRun && isLoopInventory) ||
                        (!loopRun && oneInventoryCount-- > 0)) {
                    //执行盘点
                    Inventory(runMaxCount, timeOut);
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //停止盘点后
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnInventory.setEnabled(true);
                        switchInventoryRunMode.setEnabled(true);
                        findViewById(R.id.btn_ant_auto).setEnabled(true);
                        findViewById(R.id.btn_ant_set).setEnabled(true);
                        findViewById(R.id.btn_inventory_stop).setEnabled(false);
                    }
                });
            }
        }.start();
    }

    //盘点
    private void Inventory(final int runMaxCount, final int timeOut) {

        if ((isInventorying) || (isOpening)) {
            return;
        }

        isInventorying = true;
        //GpioDevice.io_setLED(false);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvEpcCount.setText("0");
            }
        });

        //盘点开始
        reader.startRead(runMaxCount, timeOut, new ReadCallback() {

            //盘点结果log
            String inventoryLog = "";
            String difference = "";

            //正常盘点结束
            @Override
            public void inventorySuccess(EpcDataDifference diff, Object[] invInfo) {
                //GpioDevice.io_setLED(true);
                final int finalEPCCount = (int) invInfo[0];
                final int finalRunCount = (int) invInfo[1];
                final long finalUseTime = (long) invInfo[2];
                final long finalLastAddTime = (long) invInfo[3];
                final int finalErr_no = 0;

                //删除的
                for (int i = 0; i < diff.removed.size() && (i < 5); i++) {
                    difference += diff.removed.get(i);
                    if ((i < diff.removed.size() - 1) && (i < 4)) {
                        difference += "\r\n";
                    }
                }

                if (difference.length() > 0) {
                    difference = "与上一次相比减少的EPC:" + "\r\n" + difference;
                }

                inventoryLog = "Time:" + finalLastAddTime + "/" + finalUseTime + "  " + "RunCount:" + finalRunCount + "  ErrNo:" + finalErr_no;
                saveResultToLog(diff);

                //上报数据
                listRfidReport(listToJsonString(diff));
            }

            //盘点到新的标签
            @Override
            public void epcCountChange(final int epcCount) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvEpcCount.setText(epcCount + "");
                        tagLEDThread.setContinue();
                    }
                });
            }

            //盘点过程发生错误
            @Override
            public void inventoryFail(ResultMsg reason, final int err_No) {
                inventoryLog = "Time:" + 0 + "  " + "RunCount:" + reason.toString() + "  ErrNo:" + err_No;
                //发生错误时，也需要回复消息到服务器
                Log4.debug("inventoryFail:" + inventoryLog);
            }

            //盘点结束
            @Override
            public void inventoryComplete() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvEpcs.setText(difference);
                        tvInventoryStatus.setText(inventoryLog);
                        Log.e("Inventory", inventoryLog);
                    }
                });
                isInventorying = false;
            }

            //保存盘点结果到log文件
            private void saveResultToLog(EpcDataDifference diff) {
                Log4.error("本次盘点结果：" + diff.all.size());
                for (String str : diff.all) {

                }
            }

            private void listRfidReport(String devlist) {
                //返回数据到服务器
                //读取数据成功
                if (_ServiceMessage != null) {
                    ServiceMessage mServiceMessage = null;
                    switch (_ServiceMessage.get_dispatcher()) {
                        //关门盘点
                        case ServiceProtocalDef.OPTION_CMD_DEVICE_AUTO_INVENTORY_IND:
                            Log.d(TAGS, "关门盘点上报");
                            String uuidStr = UUID.randomUUID().toString().replace("-", "");

                            mServiceMessage = new ServiceMessage(MyApplication.DeviceMacMD5, MyApplication.IMEISMD5, uuidStr,
                                    ServiceProtocalDef.OPTION_CMD_DEVICE_AUTO_INVENTORY_IND, true, 0, devlist);
                            _ServiceProtocalHandlerCallback.autoInventoryCmdInd(mServiceMessage);

                            break;

                        //服务器人工盘点
                        case ServiceProtocalDef.OPTION_CMD_INVENTORY_GET_REQ:
                            Log.d(TAGS, "服务器人工盘点结果反馈");
                            mServiceMessage = new ServiceMessage(MyApplication.DeviceMacMD5, MyApplication.IMEISMD5, _ServiceMessage.get_unicode(),
                                    ServiceProtocalDef.OPTION_CMD_INVENTORY_GET_RESP, true, 0, devlist);
                            _ServiceProtocalHandlerCallback.artificialInventoryCmdResp(mServiceMessage);
                            break;
                    }
                    _ServiceMessage = null;
                }
            }

            //数据去重
            private boolean isContain(JSONArray arrays, String str) throws JSONException {
                boolean ret = false;

                if (arrays.length() <= 0)
                    return true;

                for (int idx = 0; idx < arrays.length(); idx++) {
                    String dstStr = arrays.getString(idx);
                    if (dstStr.equals(str)) {
                        return false;
                    }
                }

                return true;
            }

            //数据转JSON string
            private String listToJsonString(EpcDataDifference diff) {
                JSONObject json = null;
                //数据转为JSON格式
                try {
                    json = new JSONObject();
                    JSONArray allArrays = new JSONArray();
                    for (int idx = 0; idx < diff.all.size(); idx++) {
                        String id = diff.all.get(idx);
                        Log4.debug("ALL：" + id);

                        //过滤异常标签
                        if ((id.length() == ServiceProtocalDef.RFID_ID_MIN_LEN) && isContain(allArrays, id)) {
                            allArrays.put(id);
                        }
                    }

                    if (allArrays.length() > 0)
                        json.put("all", allArrays);

                    JSONArray addArrays = new JSONArray();
                    for (int idx = 0; idx < diff.added.size(); idx++) {
                        String id = diff.added.get(idx);
                        Log4.debug("added：" + id);
                        //过滤异常标签
                        if ((id.length() == ServiceProtocalDef.RFID_ID_MIN_LEN) && isContain(addArrays, id)) {
                            addArrays.put(id);
                        }
                        //addArrays.put(diff.added.get(idx));
                    }
                    if (addArrays.length() > 0)
                        json.put("added", addArrays);

                    JSONArray removeArrays = new JSONArray();
                    for (int idx = 0; idx < diff.removed.size(); idx++) {
                        // removeArrays.put(diff.removed.get(idx));
                        String id = diff.removed.get(idx);
                        Log4.debug("removed：" + id);
                        //过滤异常标签
                        if ((id.length() == ServiceProtocalDef.RFID_ID_MIN_LEN) && isContain(removeArrays, id)) {
                            removeArrays.put(id);
                        }
                    }

                    if (removeArrays.length() > 0)
                        json.put("removed", removeArrays);

                    return json.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return "";
            }
        });
    }

    private boolean isInventorying = false;
    private boolean isOpening = false;
    private boolean isWatching = false;
    private boolean isWatchingStop = false;

    private void stopWatchSelf() {
        isWatchingStop = true;
    }

    //刷新天线对应界面
    private void initAnt() {
        int[][] ants = reader.getAntennaPort();
        ((EditText) findViewById(R.id.et_ant_0)).setText(getPowerByAntPower(0, ants) + "");
        ((EditText) findViewById(R.id.et_ant_1)).setText(getPowerByAntPower(1, ants) + "");
        ((EditText) findViewById(R.id.et_ant_2)).setText(getPowerByAntPower(2, ants) + "");
        ((EditText) findViewById(R.id.et_ant_3)).setText(getPowerByAntPower(3, ants) + "");
        ((EditText) findViewById(R.id.et_ant_4)).setText(getPowerByAntPower(4, ants) + "");
        ((EditText) findViewById(R.id.et_ant_5)).setText(getPowerByAntPower(5, ants) + "");
        ((EditText) findViewById(R.id.et_ant_6)).setText(getPowerByAntPower(6, ants) + "");
        ((EditText) findViewById(R.id.et_ant_7)).setText(getPowerByAntPower(7, ants) + "");
    }

    private int getPowerByAntPower(int index, int[][] antPower) {
        for (int i = 0; i < antPower.length; i++) {
            if (index == antPower[i][0] - 1) {
                return antPower[i][1];
            }
        }
        return 0;
    }

    //设定天线
    private void setAnt() {
        String[] ant = new String[]
                {
                        ((EditText) findViewById(R.id.et_ant_0)).getText().toString(),
                        ((EditText) findViewById(R.id.et_ant_1)).getText().toString(),
                        ((EditText) findViewById(R.id.et_ant_2)).getText().toString(),
                        ((EditText) findViewById(R.id.et_ant_3)).getText().toString(),
                        ((EditText) findViewById(R.id.et_ant_4)).getText().toString(),
                        ((EditText) findViewById(R.id.et_ant_5)).getText().toString(),
                        ((EditText) findViewById(R.id.et_ant_6)).getText().toString(),
                        ((EditText) findViewById(R.id.et_ant_7)).getText().toString()
                };
        int count = 0;
        for (int i = 0; i < 8; i++) {
            int power = Integer.parseInt(ant[i]);
            if (power > 0) {
                count++;
            }
        }
        int[][] antPower = new int[count][2];
        count = 0;
        for (int i = 0; i < 8; i++) {
            int power = Integer.parseInt(ant[i]);
            if (power > 0) {
                antPower[count][0] = i + 1;
                antPower[count][1] = power;
                count++;
            }
        }
        reader.setAntennaPort(antPower);
    }

    //初始化门类型
    private void initDoorKind() {
        doorKind = PrefUtils.getInt(this, "doorKind", 1);
        switch (doorKind) {
            case 1:
                rbSingleDoorNo.setChecked(true);
                break;
            case 2:
                rbDoubleDoorNo.setChecked(true);
                break;
            case 3:
                rbSingleDoorYes.setChecked(true);
                break;
            case 4:
                rbDoubleDoorYes.setChecked(true);
                break;
            default:
                break;
        }
    }
}
