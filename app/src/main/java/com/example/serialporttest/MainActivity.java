package com.example.serialporttest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.serialporttest.serialport.Modbus;
import com.example.serialporttest.serialport.PortCommand;
import com.example.serialporttest.serialport.RFIDConn;
import com.example.serialporttest.serialport.SerialPortUtil;
import com.example.serialporttest.serialport.PortCommunication;

import static com.example.serialporttest.serialport.ProtocolConstant.*;

public class MainActivity extends AppCompatActivity {

    private EditText msg;
    private Button btn;
    private Button lockbtn1;
    private Button lockbtn2;
    private Button lockbtn3;
    private Button lockbtn1_10;
    private TextView tvtext;
    private PortCommunication pcomm;
    private RFIDConn rfidConn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        msg = findViewById(R.id.editText);
        btn = findViewById(R.id.button);
        tvtext = findViewById(R.id.textView);
        lockbtn1 = findViewById(R.id.lockbtn1);
        lockbtn2 = findViewById(R.id.lockbtn2);
        lockbtn3 = findViewById(R.id.lockbtn3);
        lockbtn1_10 = findViewById(R.id.btn1_10);
        pcomm = new PortCommunication();
        rfidConn = new RFIDConn();

        try{
            Log.i(SerialPortUtil.TAG,"Starting serial port...");
            SerialPortUtil.setTextView(tvtext);
            SerialPortUtil.startSerialPort();
//            SerialPortUtil.startReceiveData();
            pcomm.startCommunicate();
            rfidConn.start();
        }
        catch (Exception e){
            Log.e(SerialPortUtil.TAG,"fail to initialize serial port:"+e.toString());
            Toast.makeText(this,"串口连接失败",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        if(SerialPortUtil.spInstance!=null)
            Log.i(SerialPortUtil.TAG,"Serial port successfully started!");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = msg.getText().toString();
                if(text.length()>0) {
                    int flag = SerialPortUtil.sendData(text+'\n');
                    if(flag==-1)
                        Toast.makeText(v.getContext(),"串口未连接",Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(v.getContext(),"请输入文本",Toast.LENGTH_SHORT).show();
            }
        });
        lockbtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = Modbus.generateProtocol(new PortCommand(ADDR01,"00","01"));
//                SerialPortUtil.sendData(msg);
                pcomm.getWt().pushData(msg);
            }
        });
        lockbtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = Modbus.generateProtocol(new PortCommand(ADDR01,"00","02"));
//                SerialPortUtil.sendData(msg);
                pcomm.getWt().pushData(msg);
            }
        });
        lockbtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = Modbus.generateProtocol(new PortCommand(ADDR01,"00","03"));
//                SerialPortUtil.sendData(msg);
                pcomm.getWt().pushData(msg);
            }
        });
        lockbtn1_10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rfidConn.realTimeInventory();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SerialPortUtil.closeSerialPort();
    }
}
