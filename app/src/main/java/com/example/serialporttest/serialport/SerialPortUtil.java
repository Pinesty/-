package com.example.serialporttest.serialport;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.os.Handler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android_serialport_api.SerialPort;

public class SerialPortUtil{

    //常量
    public final static String TAG = "SerialPortUtils";
    public final static String path = "/dev/ttyS1";
    public final static int baudrate = 9600;
    public final static int corePoolsize = 2;
    public final static int maxPoolsize = 12;
    public final static int keepAliveTime = 10;
    //状态
    public static boolean isListening = false;
    //实例
    public static SerialPort spInstance = null;
    private static InputStream inStream = null;
    private static OutputStream outStream = null;
    private static receiveThread reThread = null;
    private static Handler handler = new Handler();
    protected static TextView outView = null;
    protected static String changetxt = null;

    public static void setTextView(TextView v){outView = v;}
    public static void changeText(String str){
        changetxt = str;
        if(outView!=null&&str!=null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    outView.setText(outView.getText()+changetxt);
                }
            });
        }
    }
    public static synchronized SerialPort startSerialPort() throws Exception{
        if(spInstance==null){
            spInstance = new SerialPort(new File(path),baudrate,0);
            inStream = spInstance.getInputStream();
            outStream = spInstance.getOutputStream();
        }
        return spInstance;
    }
    public static void closeSerialPort(){
        try{
            isListening = false;
            inStream.close();
            outStream.close();
            spInstance.close();
            spInstance = null;
            Log.i(TAG,"Serial Port closed.");
        }catch (Exception e){
            Log.e(TAG,"Error while closing port: "+e.toString());
        }
    }
    public static int sendData(String data) {
        Log.i(TAG,"Sending data...");
        try{
            if(spInstance == null){
                Log.e(TAG,"No connection.");
                return -1;
            }
            if(data.length()>0){
                outStream.write(data.getBytes());
                outStream.flush();
                Log.i(TAG,"Sending data complete.");
            }
        }
        catch (Exception e){
            Log.e(TAG,"Error while sending data: "+e.toString());
            return -2;
        }
        return 0;
    }
    public static void startReceiveData(){
        if(reThread==null)
            reThread = new receiveThread();
        isListening = true;
        reThread.start();
    }
    public static String getInputString() throws Exception{
        byte[] buffer = new byte[256];
        String result = "";
        int size = 0;
        size = inStream.read(buffer,0,256);
        if(size>0){
            result = new String(buffer,"utf-8").trim();
            Log.i(TAG,"Data received: "+result);
//            changeText(result);
        }
        return result;
    }
    public static class receiveThread extends Thread{
        private String str;

        public receiveThread(){}
        @Override
        public void run() {
            while(isListening){
                if(inStream==null)
                    return;
                try{
                    byte[] buffer = new byte[256];
                    Log.i(TAG,"Waiting for reading...");
                    int size = inStream.read(buffer);//若没有数据则会阻塞
                    if(size>0){
                        str = new String(buffer,"utf-8");
                        changeText(str);
                        Log.i(TAG,"Data received.");
                    }
                }catch (Exception e){
                    Log.e(TAG,"Error while reading from port: "+e.toString());
                }
            }
        }
    }
    public static InputStream getInputStream(){ return inStream;}
    public static OutputStream getOutputStream(){ return outStream;}
}
