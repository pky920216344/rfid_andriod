package com.protocol;

public class ServiceProtocalDef {

    public static final int RFID_ID_MIN_LEN = 24;

    //JSON字段定义
    public static final String MAC = "mac";
    public static final String IMEI = "imei";
    public static final String UNICODE = "unicode";
    public static final String DIS = "dispatcher";
    public static final String SUC = "success";
    public static final String ERR = "error";
    public static final String DATA = "data";

    //JSON命令描述
    /*
    *  REQ:  SERVICE -> APK
    *  RESP: APK -> SERVICE
    *  IND: APK -> SERVICE
    *  CFM SERVICE->APK
    * */
    //开锁
    public static final String OPTION_CMD_OPEN_DOOR_REQ = "openDoor";
    public static final String OPTION_CMD_OPEN_DOOR_RESP = "@openDoor";

    //门锁报警上报
    public static final String OPTION_CMD_DOOR_ALAARM_IND = "doorError";
    public static final String OPTION_CMD_DOOR_ALAARM_CFM = "@doorError";

    //天线设置参数
    public static final String OPTION_CMD_ANT_SET_REQ = "setAntenna";
    public static final String OPTION_CMD_ANT_SET_RESP = "@setAntenna";

    //获取天线参数
    public static final String OPTION_CMD_ANT_GET_REQ = "getAntenna";
    public static final String OPTION_CMD_ANT_GET_RESP = "@getAntenna";

    //关门盘点数量
    public static final String OPTION_CMD_DEVICE_AUTO_INVENTORY_IND = "autoInventory";
    public static final String OPTION_CMD_DEVICE_AUTO_INVENTORY_CFM = "@autoInventory";

    //服务器人工盘点
    public static final String OPTION_CMD_INVENTORY_GET_REQ = "artificialInventory";
    public static final String OPTION_CMD_INVENTORY_GET_RESP = "@artificialInventory";

    //心跳包
    public static final String OPTION_CMD_HEARTBEAT_REQ         = "heartbeat";
    public static final String OPTION_CMD_HEARTBEAT_RESP        = "@heartbeat";
}
