package com.example.serialporttest.serialport;

import android.util.Log;

import static com.example.serialporttest.serialport.ProtocolConstant.*;

public class Modbus {
    /*
     *       注意：
     *       每一个字符转换成对应的ascii
     *       再计算LRC
     *       substring()取左闭右开区间
     * */
    public static String hex2String(int num,int count){
        String result = "";
        String strnum = "0123456789ABCDEF";
        char[] numchars = strnum.toCharArray();
        while((num/16)!=0){
            int pos = num % 16;
            result = numchars[pos] + result;
            num = num / 16;
        }
        int pos = num % 16;
        result = numchars[pos] + result;
        while(result.length()<count)
            result = '0'+result;
        return result;
    }
    private static int string2ByteSum(String str) {
        if(str==null||str.length()<=0)
            return 0;
        byte[] tmp = str.getBytes();
        int sum = 0;
        for(int i=0;i<tmp.length;i++){
            sum += tmp[i];
        }
        return sum;
    }
    private static int LrcCheck(String addr,String fn_code,String lockAddrH,String lockAddrL,String ringstat){
        int sum = string2ByteSum(addr)+
                string2ByteSum(fn_code)+
                string2ByteSum(lockAddrH)+
                string2ByteSum(lockAddrL)+
                string2ByteSum(ringstat);
        int mod = sum % 256;
        return 256-mod;
    }
    public static String generateProtocol(PortCommand cmd){
        String lrcstr = hex2String(
                LrcCheck(cmd.address,FUNCTION_CODE,cmd.lockAddrH,cmd.lockAddrL,RING_STATUS)
                ,2);
        return HEAD+cmd.address+FUNCTION_CODE+cmd.lockAddrH+cmd.lockAddrL+RING_STATUS+lrcstr+END;
    }
    public static PortCommand parsingProtocol(String data){
        /*
         *      功能： 解析协议，字符串转换成内存中的数据结构
         *      输入： data 协议字符串
         *      输出： PortCommand 命令数据结构
         */
        String addr = data.substring(1,3);
        String fncode = data.substring(3,5);
        String lockaddH = data.substring(5,7);
        String lockaddL = data.substring(7,9);
        String ringstat = data.substring(9,13);
        String lrc = data.substring(9,13);
        boolean flag = Integer.parseInt(lrc,16) == LrcCheck(addr,fncode,lockaddH,lockaddL,ringstat);
        if(!flag){
            Log.e("Modbus","LRC check bad data!");
            return null;
        }
        PortCommand cmd = new PortCommand(addr,lockaddH,lockaddL);
        return cmd;
    }
}