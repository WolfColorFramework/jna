package com.mit.jna.utils.broadcast;

import com.mit.jna.dto.MulticastDTO;

/**
 * 播放器
 */
public interface Player {

    /**
     * 打开语音播报
     *
     * @param multicastDTO 广播参数类
     * @return
     */
    Boolean openVoice(MulticastDTO multicastDTO);

    /**
     * 打开语音播报（针对列车）
     *
     * @param multicastDTO 广播参数类
     * @return
     */
    Boolean openTrainVoice(MulticastDTO multicastDTO);

    /**
     * 关闭语音播报
     *
     * @param multicastDTO 广播参数类
     * @return
     */
    Boolean closeVoice(MulticastDTO multicastDTO);

    /**
     * 音频播报
     *
     * @param multicastDTO 广播查询类
     */
    void playAudio(MulticastDTO multicastDTO);
}
