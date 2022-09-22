package com.pflm.common.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;

public class NUtils {
    /**
     * 获取本地mac地址
     * 注意：物理地址是48位，别和ipv6搞错了
     * @param count
     * @return 本地mac地址
     */
    public static String getRandomKey(String count) {
        try {
            byte[] machineInfoStr = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < machineInfoStr.length; i++) {
                if (i != 0) {
                    sb.append("-");
                }
                int temp = machineInfoStr[i] & 0xff;
                String str = Integer.toHexString(temp);
                if (str.length() == Integer.parseInt(count)) {
                    sb.append("0" + str);
                } else {
                    sb.append(str);
                }
            }
            return sb.toString();
        } catch (Exception exception) {
            return "exceptions";
        }
    }

}
