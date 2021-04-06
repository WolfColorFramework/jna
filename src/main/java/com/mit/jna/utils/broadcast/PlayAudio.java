package com.mit.jna.utils.broadcast;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Data
@Slf4j
public class PlayAudio {

    private String filePath;

    // 文件播放总时长(秒)
    private Double fileDuration;

    // 文件开始播放时间
    private Long startPlayTime;

    public PlayAudio(String filePath) {
        this.filePath = filePath;
        this.startPlayTime = System.currentTimeMillis();

        // 文件播放时长（秒）44100HZ 样本位数16bit 单声道
        File file = new File(filePath);
        fileDuration = Math.ceil(file.length() / 44100 / (16 / 8));
    }

    // 剩余音频播放时间
    public Double remainingTime() {
        long now = System.currentTimeMillis();
        // 已播放时间（秒）
        double alreadyPlayTime = (now - startPlayTime) / 1000.0;
        // 剩余播放时间（秒）
        return fileDuration - alreadyPlayTime;
    }

    // 播放剩余音频
    public void playRemainingAudio() {
        // select audio format parameters
        AudioFormat af = new AudioFormat(44100, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
        SourceDataLine line = null;
        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
        } catch (LineUnavailableException e) {
            log.error(e.getMessage(), e);
        }

        // prepare audio output
        try {
            line.open(af, 4096);
            line.start();

            // 播放音频
            File file = new File(filePath);
            FileInputStream reader = new FileInputStream(file);
            long now = System.currentTimeMillis();
            // 已播放流
            double alreadyPlayTime = (now - startPlayTime) / 1000;
            double alreadyBuffer = alreadyPlayTime * 44100 * (16 / 8);

            double readBuffer = 0;
            byte[] buffer = new byte[4096];
            log.error("Thread:" + Thread.currentThread().getName());
            while (reader.read(buffer) > 0) {
                if (readBuffer >= alreadyBuffer) {
                    // 多线程死锁，导致write卡住
                    line.write(buffer, 0, buffer.length);
                }
                readBuffer += 4096;
            }
        } catch (LineUnavailableException | IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            // 关闭
            line.drain();
            line.stop();
            line.close();
        }
    }
}
