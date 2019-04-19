package com.raifu.rfidapi;

public enum ResultMsg {
    ERROR_RFID_OK(0, "执行成功"),
    ERROR_RFID_CON(100, "读写器连接异常"),
    ERROR_RFID_READER(200, "读写器异常"),
    ERROR_RFID_COMMOND(300, "命令执行失败"),
    ERROR_RFID_ANT(400, "天线异常"),
    ERROR_RFID_UNSUPPORT(500, "不支持操作"),
    ERROR_RFID_RUNNING(600,"正在盘点中");
    int code;
    String msg;
    ResultMsg(int val, String var) {
        this.code = val;
        this.msg = var;
    }
}
