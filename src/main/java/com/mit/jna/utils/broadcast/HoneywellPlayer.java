package com.mit.jna.utils.broadcast;

import com.mit.jna.dto.MulticastDTO;
import com.mit.jna.utils.broadcast.sdk.HoneywellLibrary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Component
@Slf4j
public class HoneywellPlayer implements Player {
    @Autowired
    BroadcastDevice broadcastDevice;
    @Autowired
    Executor threadExecutor;

    @Override
    public Boolean openVoice(MulticastDTO multicastDTO) {
        String npmsId = multicastDTO.getNpmsId();
        String allZone = allZone(multicastDTO);
        int i = HoneywellLibrary.INSTANCE.X618_StartNPMSTask(Integer.parseInt(npmsId), 3, 100, allZone);
        return i == 0;
    }

    @Override
    public Boolean openTrainVoice(MulticastDTO multicastDTO) {

        // 保持连接
        threadExecutor.execute(() -> {
            while (true) {
                for (Integer trainId : multicastDTO.getTrainIds()) {
                    HoneywellLibrary.INSTANCE.X618_SetTrainStatus(trainId, 1);
                }
            }
        });

        String npmsId = multicastDTO.getNpmsId();
        String zoneString = broadcastDevice.getTrain2Zones(multicastDTO.getTrainIds());
        int i = HoneywellLibrary.INSTANCE.X618_StartNPMSTask(Integer.parseInt(npmsId), 3, 100, zoneString);
        return i == 0 ? true : false;
    }

    @Override
    public Boolean closeVoice(MulticastDTO multicastDTO) {
        String npmsId = multicastDTO.getNpmsId();
        int i = HoneywellLibrary.INSTANCE.X618_StopNPMSTask(Integer.parseInt(npmsId), 3);
        return i == 0;
    }

    @Override
    public void playAudio(MulticastDTO multicastDTO) {

        String filePaths = multicastDTO.getFilePaths().stream().collect(Collectors.joining("|"));

        // 播放音频
        String zoneString = allZone(multicastDTO);
        int taskId = new Random().nextInt(5000) + 1;
        HoneywellLibrary.INSTANCE.X618_StartAudioFileTask(
                taskId,                     // 任务ID
                100,                // 音量100%
                70,                 // 优先级70
                0,                  // 不中断恢复
                zoneString,                 // DCS的ID, ZONE的分区1~8
                0,         // 不打开输出干接点
                filePaths,                  // 文件路径
                multicastDTO.getRepeat()    // 重复次数
        );
    }

    private String allZone(MulticastDTO multicastDTO) {
        List<String> zone2Strings = new ArrayList<>();
        for (String multicastId : multicastDTO.getMulticastZones().keySet()) {
            List<Integer> zones = multicastDTO.getMulticastZones().get(multicastId);
            String zone2String = broadcastDevice.getZone2String(multicastId, zones);
            zone2Strings.add(zone2String);
        }
        return zone2Strings.stream().collect(Collectors.joining(";"));
    }
}
