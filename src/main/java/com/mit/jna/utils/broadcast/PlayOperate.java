package com.mit.jna.utils.broadcast;

/**
 * 控制器
 */
public interface PlayOperate {

    /**
     * 设置通道音量
     * @param dcsId dcsId
     * @param zoneIdBin 区域字符串
     * @param volume 音量值（-1：不设置，正常值：0~19）
     * @return
     */
    Boolean setVolume(String dcsId, String zoneIdBin, Integer volume);

    /**
     * 获取dcsId音量
     * @param dcsId
     * @return
     */
    Integer getVolume(Integer dcsId);

    /**
     * 打开监听分区
     * @param dcsId dcsId
     * @param zoneId 分区Id
     * @return
     */
    Boolean openMonitorZone(Integer dcsId, Integer zoneId);

    /**
     * 关闭监听分区
     * @param dcsId dcsId
     * @param zoneId 分区Id
     * @return
     */
    Boolean closeMonitorZone(Integer dcsId, Integer zoneId);

    /**
     * 打开npms监听
     *
     * @param npmsId
     * @param dcsId
     * @param zoneId
     */
    Boolean openNPMSMonitorZone(Integer npmsId, Integer dcsId, Integer zoneId);

    /**
     * 关闭npms监听
     *
     * @param npmsId
     */
    Boolean closeNPMSMonitorZone(Integer npmsId);
}
