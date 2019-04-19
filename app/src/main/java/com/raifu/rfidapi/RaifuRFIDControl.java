package com.raifu.rfidapi;

public interface RaifuRFIDControl {

    /***
     * 连接读写器
     * @param path 传入连接地址
     * @return 运行结果
     */
    ResultMsg openReader(String path);

    /***
     * 初始化读写器
     * @return 运行结果
     */
    ResultMsg initReader();

    /***
     * 自动设置天线
     * @param threshold 回损阈值
     * @param power 功率
     * @return 运行结果
     */
    ResultMsg setAntennaPortAuto(int threshold, int power);

    /***
     * 获取自动识别的天线
     * @param threshold 回损阈值
     * @param power 功率
     * @return 运行结果
     */
    int[][] getAntennaPortAuto(int threshold, int power);

    /***
     * 设置天线
     * @param antPower 天线号,功率
     * @return 运行结果
     */
    ResultMsg setAntennaPort(int[][] antPower);

    /***
     * 获取设置天线
     * @return 运行结果
     */
    int[][] getAntennaPort();

    /***
     * 根据天线号设置功率
     * @param power 功率
     * @param antId 天线号
     * @return 运行结果
     */
    ResultMsg setPowerByAnt(int power, int antId);


    /***
     * 盘点标签,盘点过程中及时抛出异常,非阻塞
     * @param runMaxCount 最大运行次数
     * @param timeOut 超时时间
     * @param callback 回调句柄
     * @return 运行结果
     */
    ResultMsg startRead(int runMaxCount, int timeOut, ReadCallback callback);

    /***
     * 停止盘点
     * @return 运行结果
     */
    ResultMsg stopRead();

    /***
     * 关闭读写器
     * @return 运行结果
     */
    ResultMsg closeReader();

    /***
     * 复位读写器  当读写器出现异常可通过复位尝试恢复
     * @return 运行结果
     */
    ResultMsg resetReader();

    /***
     * 固件升级接口,阻塞
     * @param path 传入文件路径
     * @return 运行结果
     */
    ResultMsg updateFirmware(String path);

    /***
     * 获取固件版本号的int值,返回int 错误返回-1
     * @return 运行结果
     */
    int getFirmwareVer();

    /***
     * 获取序列号
     * @return 序列号
     */
    String getSerialNumber();
}
