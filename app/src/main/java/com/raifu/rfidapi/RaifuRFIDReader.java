package com.raifu.rfidapi;

import android.util.Log;

import com.sample.UHFDevice;
import com.sample.uhf.EpcDataDifference;
import com.sample.uhf.EpcDataHistory;
import com.sample.uhf.CommandReceiveListener;
import com.sample.uhf.ReaderStatus;
import com.sample.uhf.Rfid_AntPower;
import com.sample.uhf.Rfid_Packet_18K6C_Inventory;
import com.sample.uhf.Rfid_Packet_CmdEnd;
import com.sample.uhf.Rfid_Packet_Common;
import com.sample.uhf.Rfid_Version;
import com.sample.utility.TwoTuple;
import com.utils.Log4;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import java.util.ArrayList;

public class RaifuRFIDReader implements RaifuRFIDControl {

    private String TAG = "RaifuRFIDReader";

    @Override
    public ResultMsg openReader(String path) {
        UHFDevice.setReaderName(path);
        TwoTuple<Integer, String> serial = UHFDevice.rfid_GetSoftSerialNumber();
        if (serial.Item1 != 0) {
            return ResultMsg.ERROR_RFID_CON;
        }
        return ResultMsg.ERROR_RFID_OK;
    }

    @Override
    public ResultMsg initReader() {
        int err_no = UHFDevice.rfid_InitializeReader();
        return errNoToResultMsg(err_no);
    }

    @Override
    public ResultMsg setAntennaPortAuto(int threshold, int power) {
        int err_no = UHFDevice.rfid_AntennaPortSetAuto(threshold, power);
        return errNoToResultMsg(err_no);
    }

    @Override
    public int[][] getAntennaPortAuto(int threshold, int power) {
        TwoTuple<Integer, Rfid_AntPower[]> ants = UHFDevice.rfid_AntennaPortGetAuto(threshold, power);
        if (ants.Item1 != 0) {
            return new int[][]{};
        }
        ArrayList<int[]> arrayList = new ArrayList();
        for (int i = 0; i < ants.Item2.length; i++) {
            if (null == ants.Item2[i]) {
                continue;
            }
            if (!ants.Item2[i].enable) {
                continue;
            }
            arrayList.add(new int[]{ants.Item2[i].port, ants.Item2[i].power});
        }
        int[][] ret = new int[arrayList.size()][2];
        for (int i = 0; i < arrayList.size(); i++) {
            ret[i][0] = arrayList.get(i)[0] + 1;
            ret[i][1] = arrayList.get(i)[1];
        }
        return ret;
    }

    @Override
    public ResultMsg setAntennaPort(int[][] antPower) {
        int AntMaxCount = 16;
        Rfid_AntPower[] initAntPort = new Rfid_AntPower[AntMaxCount];
        for (int i = 0; i < AntMaxCount; i++) {
            boolean enable = false;
            int power = 0;
            for (int j = 0; j < antPower.length; j++) {
                if ((antPower[j][0] - 1) == i) {
                    enable = true;
                    power = antPower[j][1];
                    break;
                }
            }
            if (null == initAntPort[i]) {
                initAntPort[i] = new Rfid_AntPower(i, enable, i, power);
            } else {
                initAntPort[i].enable = enable;
                initAntPort[i].power = power;
            }
        }
        Integer err_no = UHFDevice.rfid_AntennaPortSet(initAntPort);
        return errNoToResultMsg(err_no);
    }

    @Override
    public int[][] getAntennaPort() {
        TwoTuple<Integer, Rfid_AntPower[]> ants = UHFDevice.rfid_AntennaPortGet();
        if (ants.Item1 != 0) {
            return new int[][]{};
        }
        ArrayList<int[]> arrayList = new ArrayList();
        for (int i = 0; i < ants.Item2.length; i++) {
            if (null == ants.Item2[i]) {
                continue;
            }
            if (!ants.Item2[i].enable) {
                continue;
            }
            arrayList.add(new int[]{ants.Item2[i].port, ants.Item2[i].power});
        }
        int[][] ret = new int[arrayList.size()][2];
        for (int i = 0; i < arrayList.size(); i++) {
            ret[i][0] = arrayList.get(i)[0] + 1;
            ret[i][1] = arrayList.get(i)[1];
        }
        return ret;
    }

    @Override
    public ResultMsg setPowerByAnt(int power, int antId) {
        Integer err_no = UHFDevice.rfid_AntennaPortSet(antId, true, antId, power);
        return errNoToResultMsg(err_no);
    }

    @Override
    public ResultMsg startRead(final int runMaxCount, final int timeOut, ReadCallback callback) {
        if (isInventorying) {
            return ResultMsg.ERROR_RFID_RUNNING;
        }
        isInventorying = true;
        this.callback = callback;
        new Thread() {
            public void run() {
                Inventory(runMaxCount, timeOut);
            }
        }.start();
        return ResultMsg.ERROR_RFID_OK;
    }

    @Override
    public ResultMsg stopRead() {
        isExit = true;
        int times = 100;
        while (isInventorying) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            times--;
            if (times < 0) {
                break;
            }
        }
        return ResultMsg.ERROR_RFID_OK;
    }

    private EpcDataHistory tempData = new EpcDataHistory();
    private volatile boolean isInventorying = false;
    private volatile boolean isExit = false;
    private ReadCallback callback = null;

    // expectCount=0 表示无限个数
    private int Inventory(int expectCount, boolean isA) {
        final int[] err_no = {0};
        UHFDevice.rfid_18K6CTagInventory(expectCount, isA, new CommandReceiveListener() {
            @Override
            public void CommandReceiveListener(Rfid_Packet_Common pkt) {
                if (pkt.com_pkt_type == 0) {
//                    System.out.println("数据包头部：表示开始工作");
                }
                if (pkt.com_pkt_type == 5) {
                    Rfid_Packet_18K6C_Inventory inventory = (Rfid_Packet_18K6C_Inventory) pkt;
                    boolean isAdd = tempData.update(inventory.getEPCStr());
                    if (isAdd) {
                        final int epcCount = tempData.size();
                        if (null != callback) {
                            callback.epcCountChange(epcCount);
                        }
                    }
//                    String str = inventory.getEPCStr();  //返回标签内容，字符串形式
//                    byte[] data = inventory.inv_data; //返回标签内容，数组形式，包含PC+EPC+CRC
//                    int chidx = inventory.chidx;//返回标签被识别的频点
//                    int phyant = inventory.phyant;//返回标签被识别的天线
                }
                if (pkt.com_pkt_type == 1) {
                    Rfid_Packet_CmdEnd cmdEnd = (Rfid_Packet_CmdEnd) pkt;
                    err_no[0] = cmdEnd.err_no;
//                    int errno = cmdEnd.err_no; //数据包尾部包含错误信息,0表示无错误
//                    System.out.println("数据包尾部：表示结束工作");
                }
            }
        });

        if (err_no[0] != 0) {
            TwoTuple<Integer, int[]> macErr = UHFDevice.rfid_MacGetError();
            Log4.error("inventory_Err MacErr:" + macErr.Item2[0] + " LastErr:" + macErr.Item2[1] + " Code1:" + macErr.Item2[2] + " Code2:" + macErr.Item2[3] + " Code3:" + macErr.Item2[4]);
            if (err_no[0] == 0x0309 && macErr.Item2[4] != -1) {
                int ant = macErr.Item2[4] & 0x0F;
                int Freq = (macErr.Item2[4] & 0x07E0) >>> 5;
                Log4.error("inventory_Err 0x0309:Ant:" + (ant+1) + " Freq:" + Freq);
            }
            UHFDevice.rfid_MacClearError();
        }

        return err_no[0];
    }

    //盘点
    private void Inventory(int runMaxCount, int timeOut) {
        isInventorying = true;
        isExit = false;
        boolean isA = true;
        int runCount = 1;
        int err_no = 0;
        long useTime = 0;
        long lastAddTime = 0;
        tempData.clear();
        while (!isExit) {
            int beginNum = tempData.size();
            err_no = Inventory(0, isA);
            useTime = tempData.getUseTime();
            lastAddTime = tempData.getLastAddTime();

            if (err_no != 0) break;
            if ((runMaxCount > 0) && (runCount >= runMaxCount)) break;
            if ((timeOut > 0) && (useTime > timeOut)) break;

            int endNum = tempData.size();
            if (beginNum == endNum) {
                isA = !isA;
                runCount++;
            }
        }
        EpcDataDifference diff = UHFDevice.history.update(tempData);
        if (null != this.callback) {
            if (err_no == 0) {
                this.callback.inventorySuccess(diff, new Object[]{tempData.size(), runCount, useTime, lastAddTime});
            } else {
                this.callback.inventoryFail(ResultMsg.ERROR_RFID_ANT, err_no);
            }
        }
        UHFDevice.rfid_SelectTargetToA();
        //停止盘点
        if (null != this.callback) this.callback.inventoryComplete();
        isInventorying = false;
    }

    @Override
    public ResultMsg closeReader() {
        UHFDevice.rfid_CloseReader();
        return ResultMsg.ERROR_RFID_OK;
    }

    @Override
    public ResultMsg resetReader() {
        int err_no = UHFDevice.rfid_ReSetToSoft();
        return errNoToResultMsg(err_no);
    }

    public ResultMsg updateFirmware(String path) {
        //①校验升级文件
        File file = null;
        try {
            file = new File(path);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultMsg.ERROR_RFID_UNSUPPORT;
        }
        if (file.exists() == false ||
                file.isFile() == false ||
                file.canRead() == false) {
            Log.e(TAG, "文件不存在或不可读" + file.exists() + "  " + file.canRead());
            return ResultMsg.ERROR_RFID_UNSUPPORT;
        }

        //②读取升级文件的字节数据内容
        byte[] byteArray = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bags = new ByteArrayOutputStream();

            int count;
            byte[] buffer = new byte[1024];
            while ((count = fis.read(buffer)) > 0) {
                bags.write(buffer, 0, count);
            }
            byteArray = bags.toByteArray();
            fis.close();
            bags.flush();
            bags.close();
        } catch (Exception e) {
            e.printStackTrace();
            return ResultMsg.ERROR_RFID_UNSUPPORT;
        }

        //执行升级
        Integer err_no = UHFDevice.rfid_Upgrade(byteArray);
        return errNoToResultMsg(err_no);
    }

    @Override
    public int getFirmwareVer() {
        TwoTuple<Integer, Rfid_Version> version = UHFDevice.rfid_MacGetVersion();
        if (version.Item1 != ReaderStatus.Success) {
            return -1;
        }
        return version.Item2.toInteger();
    }

    @Override
    public String getSerialNumber() {
        TwoTuple<Integer, String> serial = UHFDevice.rfid_GetSoftSerialNumber();
        if (serial.Item1 != ReaderStatus.Success) {
            return null;
        }
        return serial.Item2;
    }


    private ResultMsg errNoToResultMsg(Integer err_no) {
        if (err_no == 0) {
            return ResultMsg.ERROR_RFID_OK;
        }
        if ((err_no >= 100) && (err_no < 200)) {
            return ResultMsg.ERROR_RFID_CON;
        }
        if ((err_no >= 200) && (err_no < 300)) {
            return ResultMsg.ERROR_RFID_COMMOND;
        }
        return ResultMsg.ERROR_RFID_READER;
    }
}

