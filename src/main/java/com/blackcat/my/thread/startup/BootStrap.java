package com.blackcat.my.thread.startup;

import com.blackcat.my.thread.connector.HttpConnector;


public class BootStrap {

    /**
     * 谷歌浏览器会发送多次请求
     * @param args
     */
    public static void main(String[] args){
        HttpConnector connector=new HttpConnector();
        connector.start();
    }
}
