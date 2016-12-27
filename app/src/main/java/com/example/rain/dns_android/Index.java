package com.example.rain.dns_android;

/**
 * Created by rain on 2016/12/27.
 */

public class Index {
    private String name;
    private String ip;
    private boolean key = false;

    public Index(String n, String i, boolean b) {
        name = n;
        ip = i;
        key = b;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void lock() {
        key = true;
    }

    public void unlock() {
        key = false;
    }

    public boolean isKey() {
        return key;
    }

    public void setKey(boolean key) {
        this.key = key;
    }
}
