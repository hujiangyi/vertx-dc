package com.tdj.common.utils;

import java.io.UnsupportedEncodingException;

/**
 * CRC16校验码工具，算法参考CRC16 ModBus
 *
 * @author zhoubin
 * @date 2022/6/24
 */
public class CRC16Util {

    public static final int POLYNOMIAL = 0XA001;

    public static final int CRC = 0XFFFF;

    private static final String HEX_CHARS = "0123456789ABCDEF";

    /**
     * 对16进制字符串计算CRC16校验码
     *
     * @param hexStr 16进制字符串数据
     * @return {@link String }
     * @author zhoubin
     * @date 2022/6/25
     */
    public static String getCrc(String hexStr) {
        hexStr = hexStr.toUpperCase();
        int currentCrc = CRC;
        byte[] bytes = hexStr2ByteArr(hexStr);
        for (byte aByte : bytes) {
            currentCrc ^= (aByte & 0XFF);
            for (int i = 0; i < 8; i++) {
                if ((currentCrc & 1) == 0) {
                    currentCrc >>>= 1;
                } else {
                    currentCrc >>>= 1;
                    currentCrc ^= POLYNOMIAL;
                }
            }
        }
        return Integer.toHexString(currentCrc & 0XFFFF).toUpperCase();
    }

    /**
     * 将字符串转换成16进制字符串(大写)
     *
     * @param target  目标字符串
     * @param charset 字符集
     * @return {@link String }
     * @author zhoubin
     * @date 2022/6/25
     */
    public static String toHexStr(String target, String charset){
        byte[] bytes = new byte[0];
        try {
            bytes = target.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder builder = new StringBuilder();
        for (byte aByte : bytes) {
            builder.append(HEX_CHARS.charAt((aByte & 0XFF) >>> 4))
                    .append(HEX_CHARS.charAt(aByte & 0XF));
        }
        return builder.toString().toUpperCase();
    }

    /**
     * 将16进制字符串转换成byte数组
     *
     * @param hexStr 16进制字符串
     * @return {@link byte[] }
     * @author zhoubin
     * @date 2022/6/25
     */
    private static byte[] hexStr2ByteArr(String hexStr) {
        byte[] bytes = new byte[hexStr.length() >> 1];
        char[] chars = hexStr.toCharArray();
        int b;
        for (int i = 0, len = bytes.length; i < len; i++) {
            b = HEX_CHARS.indexOf(chars[2 * i]) << 4 | HEX_CHARS.indexOf(chars[2 * i + 1]);
            bytes[i] = (byte) b;
        }
        return bytes;
    }
}
