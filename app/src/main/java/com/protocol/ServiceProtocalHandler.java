package com.protocol;

import android.util.Log;

import com.MyApplication;
import com.google.gson.JsonObject;
import com.netUtils.BaseClient;
import com.utils.Log4;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//协议解析
public class ServiceProtocalHandler {

    private static final String TAGS = "ServiceProtocalHandler";
    private BaseClient _BaseClient = null;

    //协议解析入口
    public void onMessage(String msg) {
        Log.e(TAGS, msg);
        if (msg.contains("{") && msg.contains("}")) {
            String jsonStr = msg.substring(msg.indexOf("{"), msg.lastIndexOf("}") + 1);
            parseProtocal(jsonStr);
        }
    }

    /**
     * 具体的协议解析
     *
     * @param jsonStr
     */
    private void parseProtocal(String jsonStr) {
        JSONObject jsonObject = null;
        ServiceMessage mServiceMessage = new ServiceMessage();
        try {
            jsonObject = new JSONObject(jsonStr);

            mServiceMessage.set_mac(jsonObject.getString(ServiceProtocalDef.MAC));
            mServiceMessage.set_imei(jsonObject.getString(ServiceProtocalDef.IMEI));
            mServiceMessage.set_unicode(jsonObject.getString(ServiceProtocalDef.UNICODE));
            mServiceMessage.set_dispatcher(jsonObject.getString(ServiceProtocalDef.DIS));

            switch (mServiceMessage.get_dispatcher()) {
                //开门指令
                case ServiceProtocalDef.OPTION_CMD_OPEN_DOOR_REQ:
                    Log.d(TAGS, "开门指令");
                    break;

                //天线设置参数
                case ServiceProtocalDef.OPTION_CMD_ANT_SET_REQ:
                    Log.d(TAGS, "天线设置参数");
                    List<Map<String, Integer>> list = new ArrayList<Map<String, Integer>>();

                    JSONArray antArrys = jsonObject.getJSONArray(ServiceProtocalDef.DATA);
                    for (int idx = 0; idx < antArrys.length(); idx++) {
                        Map<String, Integer> map = new HashMap<>();
                        JSONObject AntObject = antArrys.getJSONObject(idx);
                        int AntNo = AntObject.getInt("no");
                        int AntPower = AntObject.getInt("power");
                        if (AntPower > 0 && AntNo > 0) {
                            map.put("no", AntNo);
                            map.put("power", AntPower);
                            list.add(map);
                        }
                    }

                    mServiceMessage.setData(list);

                    //Log.d(TAGS,"LIST:"+list.toString());
                    break;
                //获取天线参数
                case ServiceProtocalDef.OPTION_CMD_ANT_GET_REQ:
                    Log.d(TAGS, "获取天线参数");
                    break;

                //服务器人工盘点
                case ServiceProtocalDef.OPTION_CMD_INVENTORY_GET_REQ:
                    Log.d(TAGS, "服务器人工盘点");
                    break;
                //门锁报警反馈
                case ServiceProtocalDef.OPTION_CMD_DOOR_ALAARM_CFM:
                    Log.d(TAGS, "门锁报警反馈");
                    break;
                //关门盘点数量反馈
                case ServiceProtocalDef.OPTION_CMD_DEVICE_AUTO_INVENTORY_CFM:
                    Log.d(TAGS, "关门盘点数量反馈");
                    break;
                case ServiceProtocalDef.OPTION_CMD_HEARTBEAT_RESP:
                    Log.d(TAGS, "心跳反馈");
                    return;
                default:
                    Log.e(TAGS, "Unsupport cmds" + mServiceMessage.get_dispatcher());
                    return;
            }
            //发送消息给主线程
            EventBus.getDefault().post(mServiceMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setBaseClient(BaseClient mBaseClient) {
        this._BaseClient = mBaseClient;
    }

    /*****
     *  下面就是设备的一些反馈和上报命令组包
     */

    /**
     * 门锁状态反馈
     *
     * @param mServiceMessage
     */
    public void doorCmdResp(ServiceMessage mServiceMessage) {
        JSONObject objJson = new JSONObject();
        try {
            objJson.put(ServiceProtocalDef.MAC, mServiceMessage.get_mac());
            objJson.put(ServiceProtocalDef.IMEI, mServiceMessage.get_imei());
            objJson.put(ServiceProtocalDef.UNICODE, mServiceMessage.get_unicode());
            objJson.put(ServiceProtocalDef.DIS, mServiceMessage.get_dispatcher());
            objJson.put(ServiceProtocalDef.SUC, mServiceMessage.get_success());
            objJson.put(ServiceProtocalDef.ERR, mServiceMessage.get_error());

            Log4.info(objJson.toString());

            sendMessage(objJson.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 门锁错误上报
     *
     * @param mServiceMessage
     */
    public void doorErrCmdInd(ServiceMessage mServiceMessage) {
        JSONObject objJson = new JSONObject();
        try {
            objJson.put(ServiceProtocalDef.MAC, mServiceMessage.get_mac());
            objJson.put(ServiceProtocalDef.IMEI, mServiceMessage.get_imei());
            objJson.put(ServiceProtocalDef.UNICODE, mServiceMessage.get_unicode());
            objJson.put(ServiceProtocalDef.DIS, mServiceMessage.get_dispatcher());

            Log4.info(objJson.toString());
            sendMessage(objJson.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 天线设置反馈
     *
     * @param mServiceMessage
     */
    public void antSetCmdResp(ServiceMessage mServiceMessage) {
        JSONObject objJson = new JSONObject();
        try {
            objJson.put(ServiceProtocalDef.MAC, mServiceMessage.get_mac());
            objJson.put(ServiceProtocalDef.IMEI, mServiceMessage.get_imei());
            objJson.put(ServiceProtocalDef.UNICODE, mServiceMessage.get_unicode());
            objJson.put(ServiceProtocalDef.DIS, mServiceMessage.get_dispatcher());
            objJson.put(ServiceProtocalDef.SUC, mServiceMessage.get_success());
            objJson.put(ServiceProtocalDef.ERR, mServiceMessage.get_error());

            Log4.info(objJson.toString());
            sendMessage(objJson.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void antGetCmdResp(ServiceMessage mServiceMessage) {
        JSONObject objJson = new JSONObject();
        try {
            objJson.put(ServiceProtocalDef.MAC, mServiceMessage.get_mac());
            objJson.put(ServiceProtocalDef.IMEI, mServiceMessage.get_imei());
            objJson.put(ServiceProtocalDef.UNICODE, mServiceMessage.get_unicode());
            objJson.put(ServiceProtocalDef.DIS, mServiceMessage.get_dispatcher());
            objJson.put(ServiceProtocalDef.SUC, mServiceMessage.get_success());
            objJson.put(ServiceProtocalDef.ERR, mServiceMessage.get_error());
            List<Map<String, Integer>> list = (List<Map<String, Integer>>) mServiceMessage.getData();

            if (list.size() == 0)
                objJson.put(ServiceProtocalDef.DATA, "");
            else
                objJson.put(ServiceProtocalDef.DATA, new JSONArray(list.toString()));

            Log4.info("antGetCmdResp" + objJson.toString());
            sendMessage(objJson.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关门盘点状态上报
     *
     * @param mServiceMessage
     */
    public void autoInventoryCmdInd(ServiceMessage mServiceMessage) {
        JSONObject objJson = new JSONObject();
        try {
            objJson.put(ServiceProtocalDef.MAC, mServiceMessage.get_mac());
            objJson.put(ServiceProtocalDef.IMEI, mServiceMessage.get_imei());
            objJson.put(ServiceProtocalDef.UNICODE, mServiceMessage.get_unicode());
            objJson.put(ServiceProtocalDef.DIS, mServiceMessage.get_dispatcher());
            String data = (String) mServiceMessage.getData();
            if (data.isEmpty())
                objJson.put(ServiceProtocalDef.DATA, "");
            else
                objJson.put(ServiceProtocalDef.DATA, new JSONObject(data));

            Log4.info(objJson.toString());
            sendMessage(objJson.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 服务器人工盘点
     *
     * @param mServiceMessage
     */
    public void artificialInventoryCmdResp(ServiceMessage mServiceMessage) {
        JSONObject objJson = new JSONObject();
        try {
            objJson.put(ServiceProtocalDef.MAC, mServiceMessage.get_mac());
            objJson.put(ServiceProtocalDef.IMEI, mServiceMessage.get_imei());
            objJson.put(ServiceProtocalDef.UNICODE, mServiceMessage.get_unicode());
            objJson.put(ServiceProtocalDef.DIS, mServiceMessage.get_dispatcher());
            objJson.put(ServiceProtocalDef.SUC, mServiceMessage.get_success());

            String data = (String) mServiceMessage.getData();
            if (data.isEmpty()) {
                objJson.put(ServiceProtocalDef.ERR, false);
                objJson.put(ServiceProtocalDef.DATA, "");
            } else {
                objJson.put(ServiceProtocalDef.ERR, mServiceMessage.get_error());
                objJson.put(ServiceProtocalDef.DATA, new JSONObject(data));
            }

            Log4.info(objJson.toString());
            sendMessage(objJson.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String heartbeat() {
        JSONObject objJson = new JSONObject();
        try {
            objJson.put(ServiceProtocalDef.MAC, MyApplication.DeviceMacMD5);
            objJson.put(ServiceProtocalDef.IMEI, MyApplication.IMEISMD5);
            objJson.put(ServiceProtocalDef.UNICODE, UUID.randomUUID().toString().replace("-", ""));
            objJson.put(ServiceProtocalDef.DIS, ServiceProtocalDef.OPTION_CMD_HEARTBEAT_REQ);

            Log4.info(objJson.toString());
            return objJson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }

    private void sendMessage(String msg) {
        if (_BaseClient != null) {
            _BaseClient.sendMsgToServer(msg);
        }
    }

}
