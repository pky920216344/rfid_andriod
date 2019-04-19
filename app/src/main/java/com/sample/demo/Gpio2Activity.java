package com.sample.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.sample.GpioDevice;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.x;


@ContentView(R.layout.activity_gpio2)
public class Gpio2Activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
    }

    //设置Gpio1
    @Event(value = R.id.sw_gpio1, type = CompoundButton.OnCheckedChangeListener.class)
    private void aSwitch1_onCheckedChange(CompoundButton buttonView, boolean isChecked) {
        GpioDevice.setGpioValue(1, isChecked ? 1 : 0);
    }

    //设置Gpio2
    @Event(value = R.id.sw_gpio2, type = CompoundButton.OnCheckedChangeListener.class)
    private void aSwitch2_onCheckedChange(CompoundButton buttonView, boolean isChecked) {
        GpioDevice.setGpioValue(2, isChecked ? 1 : 0);
    }

    //设置Gpio3
    @Event(value = R.id.sw_gpio3, type = CompoundButton.OnCheckedChangeListener.class)
    private void aSwitch3_onCheckedChange(CompoundButton buttonView, boolean isChecked) {
        GpioDevice.setGpioValue(3, isChecked ? 1 : 0);
    }

    //设置Gpio4
    @Event(value = R.id.sw_gpio4, type = CompoundButton.OnCheckedChangeListener.class)
    private void aSwitch4_onCheckedChange(CompoundButton buttonView, boolean isChecked) {
        GpioDevice.setGpioValue(4, isChecked ? 1 : 0);
    }

    //设置Gpio5
    @Event(value = R.id.sw_gpio5, type = CompoundButton.OnCheckedChangeListener.class)
    private void aSwitch5_onCheckedChange(CompoundButton buttonView, boolean isChecked) {
        GpioDevice.setGpioValue(5, isChecked ? 1 : 0);
    }

    //设置Gpio11
    @Event(value = R.id.sw_gpio11, type = CompoundButton.OnCheckedChangeListener.class)
    private void aSwitch11_onCheckedChange(CompoundButton buttonView, boolean isChecked) {
        GpioDevice.setGpioValue(11, isChecked ? 1 : 0);
    }

    //open2
    @Event(value = R.id.sw_open2, type = CompoundButton.OnCheckedChangeListener.class)
    private void aSwitchOpen2_onCheckedChange(CompoundButton buttonView, boolean isChecked) {
        GpioDevice.io_setDoor2(isChecked);
    }

    //open1
    @Event(value = R.id.sw_open1, type = CompoundButton.OnCheckedChangeListener.class)
    private void aSwitchOpen1_onCheckedChange(CompoundButton buttonView, boolean isChecked) {
        GpioDevice.io_setDoor(isChecked);
    }

    //门1检测
    @Event(value = R.id.b_door1, type = View.OnClickListener.class)
    private void b_door1_onClick(View v) {
        int value = GpioDevice.io_Door();
        Toast.makeText(Gpio2Activity.this, "b_door1:" + value, Toast.LENGTH_SHORT).show();
    }

    //门2检测
    @Event(value = R.id.b_door2, type = View.OnClickListener.class)
    private void b_door2_onClick(View v) {
        int value = GpioDevice.io_Door2();
        Toast.makeText(Gpio2Activity.this, "b_door2:" + value, Toast.LENGTH_SHORT).show();
    }

    //锁1检测
    @Event(value = R.id.b_lock1, type = View.OnClickListener.class)
    private void b_lock1_onClick(View v) {
        int value = GpioDevice.io_Lock();
        Toast.makeText(Gpio2Activity.this, "b_lock1:" + value, Toast.LENGTH_SHORT).show();
    }

    //锁2检测
    @Event(value = R.id.b_lock2, type = View.OnClickListener.class)
    private void b_lock2_onClick(View v) {
        int value = GpioDevice.io_Lock2();
        Toast.makeText(Gpio2Activity.this, "b_lock2:" + value, Toast.LENGTH_SHORT).show();
    }

    //霍尔1检测
    @Event(value = R.id.b_hoare1, type = View.OnClickListener.class)
    private void b_hoare1_onClick(View v) {
        int value = GpioDevice.io_Hoare();
        Toast.makeText(Gpio2Activity.this, "b_hoare1:" + value, Toast.LENGTH_SHORT).show();
    }

    //霍尔2检测
    @Event(value = R.id.b_hoare2, type = View.OnClickListener.class)
    private void b_hoare2_onClick(View v) {
        int value = GpioDevice.io_Hoare2();
        Toast.makeText(Gpio2Activity.this, "b_hoare2:" + value, Toast.LENGTH_SHORT).show();
    }

}
