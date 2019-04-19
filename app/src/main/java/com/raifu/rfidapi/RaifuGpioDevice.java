package com.raifu.rfidapi;

import android.util.Log;

import com.sample.GpioDevice;
import com.sample.gpio.IOChangeListener;

public class RaifuGpioDevice {
    private static int getDoorLockStatus() {
        int hoare = GpioDevice.io_Hoare();//霍尔
        int door = GpioDevice.io_Door();//门状态
        int lock = GpioDevice.io_Lock();//锁状态
        //if ((door == 0) && (hoare == 0) && (lock == 0)) {
        if (lock == 1 && door == 0) {
            Log.i("getDoorLockStatus[1]", "0 关锁,门关");
            return 0;//关门
        }
        //if ((door == 1) || (hoare == 1)) {
        if (lock == 0 && door == 1) {
            Log.i("getDoorLockStatus[1]", "1 开锁,门开");
            return 1;//开门
        }
        //if ((lock == 0) && ((door == 1) && (hoare == 1))) {
        if (lock == 1 && door == 1) {
            Log.i("getDoorLockStatus[1]", "2 关锁,门开");
            return 2;//意外落锁
        }
        if (lock == 0 && door == 0) {
            Log.i("getDoorLockStatus[1]", "3 开锁,门关");
            return 3;//开锁,门关
        }
        Log.i("getDoorLockStatus[1]", "4 其他");
        return 4;//其他
    }
    private static int getDoorLockStatus2() {
        int hoare = GpioDevice.io_Hoare2();//霍尔
        int door = GpioDevice.io_Door2();//门状态
        int lock = GpioDevice.io_Lock2();//锁状态
        //if ((door == 0) && (hoare == 0) && (lock == 0)) {
        if (lock == 1 && door == 0) {
            Log.i("getDoorLockStatus[2]", "0 关锁,门关");
            return 0;//关门
        }
        //if ((door == 1) || (hoare == 1)) {
        if (lock == 0 && door == 1) {
            Log.i("getDoorLockStatus[2]", "1 开锁,门开");
            return 1;//开门
        }
        //if ((lock == 0) && ((door == 1) && (hoare == 1))) {
        if (lock == 1 && door == 1) {
            Log.i("getDoorLockStatus[2]", "2 关锁,门开");
            return 2;//意外落锁
        }
        if (lock == 0 && door == 0) {
            Log.i("getDoorLockStatus[2]", "3 开锁,门关");
            return 3;//开锁,门关
        }
        Log.i("getDoorLockStatus[2]", "4 其他");
        return 4;//其他
    }
    private static int getDoorLockStatus2_H() {
        int hoare = GpioDevice.io_Hoare2();//霍尔
        int door = GpioDevice.io_Door2();//门状态
        int lock = GpioDevice.io_Lock2();//锁状态
        if (lock == 1 && door == 0 && hoare == 0) {
            Log.i("getDoorLockStatus[2]", "0 关锁,门关");
            return 0;//关门
        }
        if (lock == 0 && (door == 1 || (hoare == 1))) {
            Log.i("getDoorLockStatus[2]", "1 开锁,门开");
            return 1;//开门
        }
        if (lock == 1 && (door == 1 || (hoare == 1))) {
            Log.i("getDoorLockStatus[2]", "2 关锁,门开");
            return 2;//意外落锁
        }
        if (lock == 0 && door == 0 && hoare == 0) {
            Log.i("getDoorLockStatus[2]", "3 开锁,门关");
            return 3;//开锁,门关
        }
        Log.i("getDoorLockStatus[2]", "4 其他");
        return 4;//其他
    }
    private static int getDoorLockStatus_H() {
        int hoare = GpioDevice.io_Hoare();//霍尔
        int door = GpioDevice.io_Door();//门状态
        int lock = GpioDevice.io_Lock();//锁状态
        if (door == 0 && lock == 1 && hoare == 0) {
            Log.i("getDoorLockStatus[1]", "0 关锁,门关");
            return 0;//关门
        }
        if (lock == 0 && (door == 1 || (hoare == 1))) {
            Log.i("getDoorLockStatus[1]", "1 开锁,门开");
            return 1;//开门
        }
        if (lock == 1 && (door == 1 || (hoare == 1))) {
            Log.i("getDoorLockStatus[1]", "2 关锁,门开");
            return 2;//意外落锁
        }
        if (lock == 0 && door == 0 && hoare == 0) {
            Log.i("getDoorLockStatus[1]", "3 开锁,门关");
            return 3;//开锁,门关
        }
        Log.i("getDoorLockStatus[1]", "4 其他");
        return 4;//其他
    }
    private static WatchDoorThread watchDoorThread = new WatchDoorThread();

    private static class WatchDoorThread extends Thread {
        public void setDoorKind(int doorKind) {
            this.doorKind = doorKind;
        }
        private int doorKind = 1;
        private IOChangeListener ioChangeListener;

        public void setIoChangeListener(IOChangeListener ioChangeListener) {
            this.ioChangeListener = ioChangeListener;
        }

        private void invokeIoChange(int flag, String info) {
            if (null != ioChangeListener) {
                ioChangeListener.IOChangeListener(flag, info);
            }
        }

        private void open1() {
            int doorStatus = getDoorLockStatus();
            int sendStatus = 0;
            if (0 != doorStatus) {
                //TODO 初始状态不对
                Log.d("info", "初始状态不对");
                invokeIoChange(-2, "初始状态异常");
                return;
            }

            //开锁
            GpioDevice.io_setDoor(true);
            //锁已打开
            invokeIoChange(2, "锁已打开");
            //监听开门信号
            int HoldMun = 40;
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int tempDoorStatus = getDoorLockStatus();
                if (tempDoorStatus == doorStatus) {
                    //门开或者锁坏了
                    if ((tempDoorStatus == 3) || (tempDoorStatus == 0)) {
                        HoldMun--;
                        if (HoldMun > 0) {
                            continue;
                        } else {
                            //超时关门
                            Log.d("io", "超时关门");
                            GpioDevice.io_setDoor(false);
                            break;
                        }
                    } else {
                        continue;
                    }
                }

                //往下是门状态发生变化时
                if (tempDoorStatus == 1) {
                    sendStatus = 1;
                    Log.d("io", "门开了");
                    //TODO 开门
                    invokeIoChange(1, "门已打开");
                    break;
                }

                //意外落锁
                if (tempDoorStatus == 2) {
                    GpioDevice.io_setDoor(true);
                }
                doorStatus = tempDoorStatus;
            }

            GpioDevice.io_setDoor(false);
            Log.d("io", "监听关门");

            //监听关门信号
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int tempDoorStatus = getDoorLockStatus();
                if (tempDoorStatus == 0) {
                    if (sendStatus == 0) {
                        //超时关门成功 TODO
                        invokeIoChange(-1, "超时关门");
                    }
                    if (sendStatus == 1) {
                        //手动关门成功 TODO
                        invokeIoChange(0, "门已关");
                    }
                    break;
                }
            }

        }

        private void open2() {
            int doorStatus = getDoorLockStatus();
            int doorStatus2 = getDoorLockStatus2();
            int sendStatus = 0;
            if ((0 != doorStatus) || (0 != doorStatus2)) {
                //TODO 初始状态不对
                Log.e("info", "初始状态不对");
                invokeIoChange(-2, "初始状态异常");
                return;
            }
            //开锁
            GpioDevice.io_setDoor(true);
            GpioDevice.io_setDoor2(true);
            //锁已打开
            invokeIoChange(2, "锁已打开");
            //监听开门信号
            int HoldMun = 40;
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int tempDoorStatus = getDoorLockStatus();
                int tempDoorStatus2 = getDoorLockStatus2();
                if ((tempDoorStatus == doorStatus) && (tempDoorStatus2 == doorStatus2)) {
                    //门没开或者锁坏了
                    if (((tempDoorStatus == 3) || (tempDoorStatus == 0)) && ((tempDoorStatus2 == 3) || (tempDoorStatus2 == 0))) {
                        HoldMun--;
                        if (HoldMun > 0) {
                            continue;
                        } else {
                            //超时关门
                            Log.e("io", "超时关门");
                            GpioDevice.io_setDoor(false);
                            GpioDevice.io_setDoor2(false);
                            break;
                        }
                    } else {
                        continue;
                    }
                }
                //往下是门状态发生变化时
                if (tempDoorStatus == 1) {
                    sendStatus = 1;
                    Log.e("io", "门开了");
                    //TODO 开门
                    invokeIoChange(1, "门已打开");
                    break;
                }
                if (tempDoorStatus2 == 1) {
                    sendStatus = 1;
                    Log.e("io", "门开了");
                    //TODO 开门
                    invokeIoChange(1, "门已打开");
                    break;
                }
                //意外落锁
                if (tempDoorStatus == 2) {
                    GpioDevice.io_setDoor(true);
                }
                if (tempDoorStatus2 == 2) {
                    GpioDevice.io_setDoor2(true);
                }
                doorStatus = tempDoorStatus;
                doorStatus2 = tempDoorStatus2;
                continue;
            }

            GpioDevice.io_setDoor(false);
            GpioDevice.io_setDoor2(false);
            Log.e("io", "监听关门");

            //监听关门信号
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int tempDoorStatus = getDoorLockStatus();
                int tempDoorStatus2 = getDoorLockStatus2();
                if ((tempDoorStatus == 0) && (tempDoorStatus2 == 0)) {
                    if (sendStatus == 0) {
                        //超时关门成功 TODO
                        invokeIoChange(-1, "超时关门");
                    }
                    if (sendStatus == 1) {
                        //手动关门成功 TODO
                        invokeIoChange(0, "门已关");
                    }
                    break;
                }
            }
        }

        private void open3() {
            int doorStatus = getDoorLockStatus_H();
            int sendStatus = 0;
            if (0 != doorStatus) {
                //TODO 初始状态不对
                Log.e("info", "初始状态不对");
                invokeIoChange(-2, "初始状态异常");
                return;
            }
            //开锁
            GpioDevice.io_setDoor(true);
            //锁已打开
            invokeIoChange(2, "锁已打开");
            //监听开门信号
            int HoldMun = 40;
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int tempDoorStatus = getDoorLockStatus_H();
                if (tempDoorStatus == doorStatus) {
                    //门开或者锁坏了
                    if ((tempDoorStatus == 3) || (tempDoorStatus == 0)) {
                        HoldMun--;
                        if (HoldMun > 0) {
                            continue;
                        } else {
                            //超时关门
                            Log.e("io", "超时关门");
                            GpioDevice.io_setDoor(false);
                            break;
                        }
                    } else {
                        continue;
                    }
                }
                //往下是门状态发生变化时
                if (tempDoorStatus == 1) {
                    sendStatus = 1;
                    Log.e("io", "门开了");
                    invokeIoChange(1, "门已打开");
                    break;
                }
                //意外落锁
                if (tempDoorStatus == 2) {
                    GpioDevice.io_setDoor(true);
                }
                doorStatus = tempDoorStatus;
            }

            GpioDevice.io_setDoor(false);
            Log.e("io", "监听关门");

            //监听关门信号
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int tempDoorStatus = getDoorLockStatus_H();
                if (tempDoorStatus == 0) {
                    if (sendStatus == 0) {
                        //超时关门成功 TODO
                        invokeIoChange(-1, "超时关门");
                    }
                    if (sendStatus == 1) {
                        //手动关门成功 TODO
                        invokeIoChange(0, "门已关");
                    }
                    break;
                }
            }

        }

        private void open4() {
            int doorStatus = getDoorLockStatus_H();
            int doorStatus2 = getDoorLockStatus2_H();
            int sendStatus = 0;
            if ((0 != doorStatus) || (0 != doorStatus2)) {
                //TODO 初始状态不对
                Log.e("info", "初始状态不对");
                invokeIoChange(-2, "初始状态异常");
                return;
            }
            //开锁
            GpioDevice.io_setDoor(true);
            GpioDevice.io_setDoor2(true);
            //锁已打开
            invokeIoChange(2, "锁已打开");
            //监听开门信号
            int HoldMun = 40;
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int tempDoorStatus = getDoorLockStatus_H();
                int tempDoorStatus2 = getDoorLockStatus2_H();
                if ((tempDoorStatus == doorStatus) && (tempDoorStatus2 == doorStatus2)) {
                    //门没开或者锁坏了
                    if (((tempDoorStatus == 3) || (tempDoorStatus == 0)) && ((tempDoorStatus2 == 3) || (tempDoorStatus2 == 0))) {
                        HoldMun--;
                        if (HoldMun > 0) {
                            continue;
                        } else {
                            //超时关门
                            Log.e("io", "超时关门");
                            GpioDevice.io_setDoor(false);
                            GpioDevice.io_setDoor2(false);
                            break;
                        }
                    } else {
                        continue;
                    }
                }
                //往下是门状态发生变化时
                if (tempDoorStatus == 1) {
                    sendStatus = 1;
                    Log.e("io", "门开了");
                    //TODO 开门
                    invokeIoChange(1, "门已打开");
                    break;
                }
                if (tempDoorStatus2 == 1) {
                    sendStatus = 1;
                    Log.e("io", "门开了");
                    //TODO 开门
                    invokeIoChange(1, "门已打开");
                    break;
                }
                //意外落锁
                if (tempDoorStatus == 2) {
                    GpioDevice.io_setDoor(true);
                }
                if (tempDoorStatus2 == 2) {
                    GpioDevice.io_setDoor2(true);
                }
                doorStatus = tempDoorStatus;
                doorStatus2 = tempDoorStatus2;
            }

            GpioDevice.io_setDoor(false);
            GpioDevice.io_setDoor2(false);
            Log.e("io", "监听关门");

            //监听关门信号
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int tempDoorStatus = getDoorLockStatus_H();
                int tempDoorStatus2 = getDoorLockStatus2_H();

                if ((tempDoorStatus == 0) && (tempDoorStatus2 == 0)) {
                    if (sendStatus == 0) {
                        //超时关门成功 TODO
                        invokeIoChange(-1, "超时关门");
                    }
                    if (sendStatus == 1) {
                        //手动关门成功 TODO
                        invokeIoChange(0, "门已关");
                    }
                    break;
                }
            }
        }

        @Override
        public void run() {
            //1个门不带霍尔
            if (doorKind == 1) {
                open1();
            }
            //2个门不带霍尔
            if (doorKind == 2) {
                open2();
            }
            //1个门带霍尔
            if (doorKind == 3) {
                open3();
            }
            //2个门带霍尔
            if (doorKind == 4) {
                open4();
            }
        }
    }

    public static void doorOpen(final int doorKind, final IOChangeListener ioChangeListener) {
        if (watchDoorThread.isAlive()) {
            return;
        }
        watchDoorThread = new WatchDoorThread();
        watchDoorThread.setIoChangeListener(ioChangeListener);
        watchDoorThread.setDoorKind(doorKind);
        watchDoorThread.start();
    }
}
