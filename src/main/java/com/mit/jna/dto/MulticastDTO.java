package com.mit.jna.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MulticastDTO {

    // npmsId
    private String npmsId;
    // 广播-区域ids
    private Map<String, List<Integer>> multicastZones;
    // 列车
    private List<Integer> trainIds;
    // 文件路径集合
    private List<String> filePaths;
    // 重复次数
    private Integer repeat;
}
