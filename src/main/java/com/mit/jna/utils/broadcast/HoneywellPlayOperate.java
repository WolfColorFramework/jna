package com.mit.jna.utils.broadcast;

import com.mit.jna.utils.broadcast.sdk.HoneywellLibrary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HoneywellPlayOperate implements PlayOperate {

    @Override
    public Boolean setVolume(String dcsId, String zoneIdBin, Integer volume) {
        int[] zoneVolumes = new int[8];
        int j = 0;
        for (int i = zoneIdBin.length() - 1; i >= 0; i--) {
            char c = zoneIdBin.charAt(i);
            if (c == '0') {
                zoneVolumes[j] = -1;
            } else {
                zoneVolumes[j] = volume;
            }
            j++;
        }

        for (int i = 0; i < zoneVolumes.length; i++) {
            log.error(i + ":" + zoneVolumes[i]);
        }

        int i = HoneywellLibrary.INSTANCE.X618_SetVolume(Integer.parseInt(dcsId), zoneVolumes);
        return i == 0;
    }

    @Override
    public Integer getVolume(Integer dcsId) {
        return HoneywellLibrary.INSTANCE.X618_GetVolume(dcsId);
    }

    @Override
    public Boolean openMonitorZone(Integer dcsId, Integer zoneId) {
        int i = HoneywellLibrary.INSTANCE.X618_MonitorZone(dcsId, zoneId, 1);
        return i == 0;
    }

    @Override
    public Boolean closeMonitorZone(Integer dcsId, Integer zoneId) {
        int i = HoneywellLibrary.INSTANCE.X618_MonitorZone(dcsId, zoneId, 0);
        return i == 0;
    }

    @Override
    public Boolean openNPMSMonitorZone(Integer npmsId, Integer dcsId, Integer zoneId) {
        int i = HoneywellLibrary.INSTANCE.X618_StartNPMSMonitor(npmsId, dcsId, zoneId);
        return i == 0;
    }

    @Override
    public Boolean closeNPMSMonitorZone(Integer npmsId) {
        int i = HoneywellLibrary.INSTANCE.X618_StopNPMSMonitor(npmsId);
        return i == 0;
    }
}
