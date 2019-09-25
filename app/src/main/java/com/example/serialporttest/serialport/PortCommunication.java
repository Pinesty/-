package com.example.serialporttest.serialport;

import android.util.Log;

import com.example.serialporttest.serialport.SerialPortUtil;
import com.google.android.material.tabs.TabLayout;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.LockSupport;

import android_serialport_api.SerialPort;
/*
    问题1：232转485接口是半双工模式
           可以用单线程依次完成写读操作
           多线程需要互斥锁
    问题2：读线程占用端口时间有限（超时时限）
           超过时间以后不可以继续监听端口
    问题3：读到的数据不一定完整
           要多次读取数据
           要解析到开始符和结束符才算完成通信
 */

public class PortCommunication {
    private final String TAG = "PortCommunicate";
    private final static int repeatCount = 3;
    private final static int waitingTime = 3;
    private volatile boolean isRunning = true;
    private volatile boolean needResponse = false;
    private volatile boolean successFlag = false;
    private static WriteThread wt = null;
    private static ReadThread rt = null;

    public PortCommunication(){
        if(wt==null)
            wt = new WriteThread();
        if(rt==null)
            rt = new ReadThread();
    }
    public void startCommunicate(){
        wt.start();
        rt.start();
    }
    public class WriteThread extends Thread{
        private BlockingDeque<String> dataQueue;

        public WriteThread(){
            dataQueue = new LinkedBlockingDeque<String>();
        }
        public void pushData(String data){
            dataQueue.push(data);
        }
        public void cleanQueue(){
            dataQueue.clear();
        }
        @Override
        public void run(){
            int repeat = 0;
            String data = null;
            while(isRunning){
                try{data = dataQueue.take();}catch (Exception e){
                    Log.e(TAG,"写数据队列出错！");
                }
                if(data!=null){
                    repeat = repeatCount;
                    needResponse = true;
                    successFlag = false;
                    while(repeat>0&&!successFlag){
                        SerialPortUtil.sendData(data);
                        repeat--;
                        Log.i(TAG,repeat+" left times to retry.");
                        LockSupport.unpark(rt);
                        LockSupport.parkNanos(waitingTime*(long)Math.pow(10,9));
                    }
                    needResponse = false;
                    if(!successFlag)
                        Log.i(TAG,"Reach max retry count.");
                    else{
                        Log.i(TAG,"Communicate complete!.");
                    }
                }
            }
        }
    }
    public class ReadThread extends Thread{
        @Override
        public void run(){
            while(isRunning){
                try{
                    LockSupport.park();
                    Log.i(TAG,"Waiting for response...");
                    while(needResponse&&SerialPortUtil.getInputStream().available()<=0)
                        Thread.sleep(200);
                    if(needResponse){
                        String data = SerialPortUtil.getInputString();
                        if(data.length()>0){ //收到正确的回应后
                            successFlag = true;
                            LockSupport.unpark(wt);
                            Log.i(TAG,"Response got.");
                        }
                    }
                }catch (Exception e){
                    Log.e(TAG,"Error while reading from port: "+e.toString());
                }
            }
        }
    }
    public WriteThread getWt() {
        return wt;
    }
    public ReadThread getRt() {
        return rt;
    }
}
