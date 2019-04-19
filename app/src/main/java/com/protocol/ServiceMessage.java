package com.protocol;

import java.util.List;
import java.util.Map;

public class ServiceMessage  {
    private String _mac = null;
    private String _imei = null;
    private String _unicode = null;
    private String _dispatcher = null;
    private boolean _success = false;
    private int _error = 0;
    private Object data = null;

    public ServiceMessage()
    {

    }

    public ServiceMessage(String mac,String imei,String unicode,String dispatcher,boolean succ,int error,Object obj)
    {
        this._mac = mac;
        this._imei = imei;
        this._unicode = unicode;
        this._dispatcher = dispatcher;
        this._success = succ;
        this._error = error;
        this.data = obj;
    }

    public String get_mac() {
        return _mac;
    }

    public void set_mac(String _mac) {
        this._mac = _mac;
    }

    public String get_imei() {
        return _imei;
    }

    public void set_imei(String _imei) {
        this._imei = _imei;
    }

    public String get_unicode() {
        return _unicode;
    }

    public void set_unicode(String _unicode) {
        this._unicode = _unicode;
    }

    public String get_dispatcher() {
        return _dispatcher;
    }

    public void set_dispatcher(String _dispatcher) {
        this._dispatcher = _dispatcher;
    }

    public boolean get_success() {
        return _success;
    }

    public void set_success(boolean _success) {
        this._success = _success;
    }

    public int get_error() {
        return _error;
    }

    public void set_error(int _error) {
        this._error = _error;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
