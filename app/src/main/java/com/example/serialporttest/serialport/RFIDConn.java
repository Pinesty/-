package com.example.serialporttest.serialport;
import android.util.Log;

import com.module.interaction.ModuleConnector;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.bean.RXInventoryTag;

public class RFIDConn {
    private static final String TAG = "RFIDConn";
    private static final String PORT = "dev/tty04";
    private static final int BT = 115200;
    private static RFIDReaderHelper helper;
    private static ModuleConnector connector;
    private static MyObserver observer;

    public RFIDConn(){
        connector = new ReaderConnector();
        observer = new MyObserver();
    }
    public void start(){
        connector.connectCom(PORT,BT);
        try {
            helper=RFIDReaderHelper.getDefaultHelper();
        } catch (Exception e) {
            Log.e(TAG,e.toString());
        }

        if(helper==null) {
            Log.e(TAG,"Connect failed!");
            return;
        }
        helper.registerObserver(observer);
    }
    public void realTimeInventory(){
        helper.realTimeInventory((byte)0xFF,(byte)0x01);
    }
    public static class MyObserver extends RXObserver {
        @Override
        protected void onInventoryTag(RXInventoryTag tag) {
            super.onInventoryTag(tag);
            Log.i(TAG,"EPC read:"+tag.strEPC);
        }

        @Override
        protected void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd) {
            super.onInventoryTagEnd(tagEnd);
        }
    }
}
