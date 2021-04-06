package com.mit.jna.utils.transfer;

import java.util.List;

public class BinUtils {

    // zones（数组）转换为二进制
    public static String list2Bin(List<Integer> list) {
        StringBuilder result = new StringBuilder();
        list.sort(Integer::compareTo);

        for (int i = 1; i <= 8; i++) {
            result.append(list.contains(i) ? "1" : "0");
        }
        return result.reverse().toString();
    }

    // zone（二进制）转换为16进制
    public static String bin2Hex(String bin) {
        Integer temp = Integer.valueOf(bin, 2);
        return Integer.toHexString(temp);
    }

}
