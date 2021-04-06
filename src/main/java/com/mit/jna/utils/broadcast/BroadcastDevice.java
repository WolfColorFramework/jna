package com.mit.jna.utils.broadcast;

import com.alibaba.fastjson.annotation.JSONField;
import com.mit.jna.configuration.YamlPropertySourceFactory;
import com.mit.jna.dto.MulticastDTO;
import com.mit.jna.utils.transfer.BinUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Data
@Component
@PropertySource(value = {"classpath:broadcast.yml"}, factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "broadcast-device")
@Slf4j
public class BroadcastDevice {

    private String nicIP;
    private String multicastIP;
    private Integer nodeID;
    private Integer audioStreamFormat;
    private List<Multicast> multicasts;
    private List<Device> trains;

    @JSONField(serialize = false)
    private Map<String, Multicast> mapMulticast;

    @PostConstruct
    private void transfer() {
        // map-multicast
        mapMulticast = new HashMap<>();
        for (Multicast multicast : multicasts) {
            if (multicast.getEnable())
                mapMulticast.put(multicast.getId(), multicast);
        }
    }

//    public String getNPMSId(Integer multicastId) {
//        Multicast multicast = mapMulticast.get(multicastId);
//        Optional<Device> npms = multicast.getDevices().stream().filter(c -> c.getType().equals("2")).findFirst();
//        Device device = npms.orElseGet(() -> new Device().setId(""));
//        return device.getId();
//    }

    public String getZone2String(String multicastId, List<Integer> zones) {
        Multicast multicast = mapMulticast.get(multicastId);
        Optional<Device> dcs = multicast.getDevices().stream().filter(c -> c.getType().equals("1")).findFirst();
        if (dcs.isPresent()) {
            return String.format("%4s%2s", Integer.toHexString(Integer.parseInt(dcs.get().getId())), BinUtils.bin2Hex(BinUtils.list2Bin(zones))).replace(' ', '0');
        }
        return "";
    }

    public String getTrain2Zones(List<Integer> deviceIds) {
        List<String> zoneArr = new ArrayList<>();
        for (int i = 0; i < deviceIds.size(); i++) {
            String hex = Integer.toHexString(deviceIds.get(i));
            for (int j = hex.length(); j < 4; j++) {
                hex = "0" + hex;
            }
            zoneArr.add(i, hex + "01");
        }
        return zoneArr.stream().collect(Collectors.joining(";"));
    }

    public void addDevices(Consumer<List<String>> addDevice) {
        this.getMulticasts().stream().forEach(c -> {
            c.getDevices().stream().forEach(d -> {
                List<String> deviceArgs = Arrays.asList(d.getId(), d.getType(), d.getIp());
                addDevice.accept(deviceArgs);
            });
        });
    }

    public void addTrains(Consumer<List<String>> addDevice) {
        this.trains.forEach(d -> {
            List<String> deviceArgs = Arrays.asList(d.getId(), d.getType(), d.getIp());
            addDevice.accept(deviceArgs);
        });
    }
}

// 广播
@Data
class Multicast {
    private String id;
    private Boolean enable;
    private List<Device> devices;
    private List<Zone> zones;
}

@Data
class Zone {
    private String id;
    private Boolean enabled;
}

@Data
@Accessors(chain = true)
class Device {
    private String id;
    private String type;
    private String ip;
    private String desc;
}