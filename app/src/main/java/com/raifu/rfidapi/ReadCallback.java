package com.raifu.rfidapi;

import com.sample.uhf.EpcDataDifference;

public interface ReadCallback {
    //返回结果需要去重
    void inventorySuccess(EpcDataDifference diff, Object[] invInfo);

    //epc数量发生变化
    void epcCountChange(int epcCount);

    //运行中间出现错误由该接口抛出
    void inventoryFail(ResultMsg reason, int err_No);

    //盘点终止
    void inventoryComplete();
}
