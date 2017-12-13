package com.moko.beaconsupport.utils;

/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.utils.Utils
 */
public class Utils {
    /**
     * A - 发射端和接收端相隔1米时的信号强度
     */
    private static final double n_Value = 2.0;/** n - 环境衰减因子*/

    /**
     * @Date 2017/12/11 0011
     * @Author wenzheng.liu
     * @Description 根据Rssi获得返回的距离, 返回数据单位为m
     */
    public static double getDistance(int rssi, int acc) {
        int iRssi = Math.abs(rssi);
        double power = (iRssi - acc) / (10 * n_Value);
        return Math.pow(10, power);
    }

    public static String byte2HexString(byte b) {
        return String.format("%02X", b);
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * @Date 2017/5/16
     * @Author wenzheng.liu
     * @Description 16进制转2进制
     */
    public static String hexString2binaryString(String hexString) {
        if (hexString == null || hexString.length() % 2 != 0)
            return null;
        String bString = "", tmp;
        for (int i = 0; i < hexString.length(); i++) {
            tmp = "0000"
                    + Integer.toBinaryString(Integer.parseInt(
                    hexString.substring(i, i + 1), 16));
            bString += tmp.substring(tmp.length() - 4);
        }
        return bString;
    }

    /**
     * @Date 2017/6/9
     * @Author wenzheng.liu
     * @Description 2进制转16进制
     */
    public static String binaryString2hexString(String bString) {
        if (bString == null || bString.equals("") || bString.length() % 8 != 0)
            return null;
        StringBuffer tmp = new StringBuffer();
        int iTmp = 0;
        for (int i = 0; i < bString.length(); i += 4) {
            iTmp = 0;
            for (int j = 0; j < 4; j++) {
                iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << (4 - j - 1);
            }
            tmp.append(Integer.toHexString(iTmp));
        }
        return tmp.toString();
    }
}
