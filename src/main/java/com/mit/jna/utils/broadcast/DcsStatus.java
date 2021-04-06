package com.mit.jna.utils.broadcast;

import lombok.Getter;

/**
 * dcs状态
 */
@Getter
public enum DcsStatus {
    主电源故障(0, "主电源故障"),
    备用电源故障(1, "备用电源故障"),
    系统故障(3, "系统故障"),
    通信故障(4, "通信故障"),
    正常(-1, "正常");

    private Integer index;
    private String desc;

    DcsStatus(Integer index, String desc) {
        this.index = index;
        this.desc = desc;
    }

    public static DcsStatus getDcsStatus(Integer index) {
        for (DcsStatus c : DcsStatus.values()) {
            if (c.getIndex().equals(index)) {
                return c;
            }
        }
        return null;
    }
}
