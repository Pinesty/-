package com.example.serialporttest.serialport;

import static com.example.serialporttest.serialport.ProtocolConstant.*;

public class PortCommand {
    public String address;
    public String functionCode;
    public String lockAddrH;
    public String lockAddrL;
    public String ringStat;

    public PortCommand(String address,String functionCode,String lockAddrH,String lockAddrL,String ringStat){
        this.address = address;
        this.functionCode = functionCode;
        this.lockAddrH = lockAddrH;
        this.lockAddrL = lockAddrL;
        this.ringStat = ringStat;
    }
    public PortCommand(String address,String lockAddrH,String lockAddrL){
        this.address = address;
        this.functionCode = FUNCTION_CODE;
        this.lockAddrH = lockAddrH;
        this.lockAddrL = lockAddrL;
        this.ringStat = RING_STATUS;
    }
}
