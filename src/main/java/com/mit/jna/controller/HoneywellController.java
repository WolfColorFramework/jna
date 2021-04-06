package com.mit.jna.controller;

import com.alibaba.fastjson.JSONObject;
import com.mit.jna.dto.MulticastDTO;
import com.mit.jna.listener.HoneywellListener;
import com.mit.jna.utils.broadcast.BroadcastFile;
import com.mit.jna.utils.broadcast.PlayOperate;
import com.mit.jna.utils.broadcast.Player;
import com.mit.jna.utils.broadcast.BroadcastDevice;
import com.mit.jna.utils.broadcast.sdk.HoneywellLibrary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.Executor;

@RestController
@RequestMapping("/honeywell")
@Slf4j
public class HoneywellController {

    @Autowired
    Player player;
    @Autowired
    PlayOperate playOperate;
    @Autowired
    BroadcastDevice broadcastDevice;
    @Autowired
    BroadcastFile broadcastFiles;
    @Autowired
    Executor threadExecutor;

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String test() {
        return "test";
    }

    @RequestMapping(value = "/openVoice", method = RequestMethod.POST)
    public String openVoice(@RequestBody MulticastDTO multicastDTO) {
        Boolean result = player.openVoice(multicastDTO);
        return result ? "success" : "error";
    }

    @RequestMapping(value = "/openTrainVoice", method = RequestMethod.POST)
    public String openTrainVoice(@RequestBody MulticastDTO multicastDTO) {
        Boolean result = player.openTrainVoice(multicastDTO);
        return result ? "success" : "error";
    }

    @RequestMapping(value = "/closeVoice", method = RequestMethod.POST)
    public String closeVoice(@RequestBody MulticastDTO multicastDTO) {
        Boolean result = player.closeVoice(multicastDTO);
        return result ? "success" : "error";
    }

    @RequestMapping(value = "/playAudio", method = RequestMethod.POST)
    public String playAudio(@RequestBody MulticastDTO multicastDTO) {
        threadExecutor.execute(() -> {
            player.playAudio(multicastDTO);
        });

        threadExecutor.execute(() -> {
            while (true) {
                HoneywellLibrary.INSTANCE.X618_SetTrainStatus(900, 1);
            }
        });
        return "success";
    }

    @RequestMapping(value = "/broadcastFiles", method = RequestMethod.GET)
    public BroadcastFile broadcastFiles() {
        return broadcastFiles;
    }

    @RequestMapping(value = "/setVolume", method = RequestMethod.POST)
    public String setVolume(@RequestBody String requestBody) {
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String dcsId = requestJson.getString("dcsId");
        String zoneIdBin = requestJson.getString("zoneIdBin");
        Integer volume = requestJson.getInteger("volume");
        Boolean result = playOperate.setVolume(dcsId, zoneIdBin, volume);
        return result ? "success" : "false";
    }

    @RequestMapping(value = "/getVolume/{dcsId}", method = RequestMethod.GET)
    public Integer getVolume(@PathVariable("dcsId") Integer dcsId) {
        return playOperate.getVolume(dcsId);
    }

    @RequestMapping(value = "/openMonitorZone/{dcsId}/{zoneId}", method = RequestMethod.GET)
    public String openMonitorZone(@PathVariable("dcsId") Integer dcsId, @PathVariable("zoneId") Integer zoneId) {
        Boolean flag = playOperate.openMonitorZone(dcsId, zoneId);
        return flag ? "success" : "false";
    }

    @RequestMapping(value = "/closeMonitorZone/{dcsId}/{zoneId}", method = RequestMethod.GET)
    public String closeMonitorZone(@PathVariable("dcsId") Integer dcsId, @PathVariable("zoneId") Integer zoneId) {
        Boolean flag = playOperate.closeMonitorZone(dcsId, zoneId);
        return flag ? "success" : "false";
    }

    @RequestMapping(value = "/dcsStatus/{dcsId}", method = RequestMethod.GET)
    public String dcsStatus(@PathVariable("dcsId") Integer dcsId) {
        return HoneywellListener.dcsStatus.get(dcsId);
    }

    @RequestMapping(value = "/openNPMSMonitor/{npmsId}/{dcsId}/{zoneId}", method = RequestMethod.GET)
    public String openNPMSMonitorZone(@PathVariable("npmsId") Integer npmsId, @PathVariable("dcsId") Integer dcsId, @PathVariable("zoneId") Integer zoneId) {
        Boolean flag = playOperate.openNPMSMonitorZone(npmsId, dcsId, zoneId);
        return flag ? "success" : "false";
    }

    @RequestMapping(value = "/closeNPMSMonitor/{npmsId}", method = RequestMethod.GET)
    public String closeNPMSMonitorZone(@PathVariable("npmsId") Integer npmsId) {
        Boolean flag = playOperate.closeNPMSMonitorZone(npmsId);
        return flag ? "success" : "false";
    }
}
