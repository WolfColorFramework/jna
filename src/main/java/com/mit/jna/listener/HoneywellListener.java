package com.mit.jna.listener;

import com.alibaba.fastjson.JSONObject;
import com.mit.jna.utils.broadcast.BroadcastDevice;
import com.mit.jna.utils.broadcast.DcsStatus;
import com.mit.jna.utils.broadcast.sdk.HoneywellLibrary;
import com.mit.jna.websocket.WebSocketServer;
import com.sun.jna.ptr.ByteByReference;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import com.esotericsoftware.yamlbeans.YamlReader;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebListener
@Slf4j
public class HoneywellListener implements ServletContextListener {

    // dcs设备状态
    public static ConcurrentHashMap<Integer, String> dcsStatus = new ConcurrentHashMap<>();

    // dcs设备信息回调方法
    static HoneywellLibrary.PDCSSTATUS pdcsstatus = (dcsID, deviceStatus, deviceFault, pAmpStatus, contactInputOpenFault, contactInputShortFault) -> {
//        log.error("============================================");
//        log.error("callback-dcsId:" + dcsID);
//        log.error("callback-deviceStatus:" + deviceStatus);
//        log.error("callback-deviceFault:" + Integer.toBinaryString(deviceFault));
//        // 解析设备状态码
//        log.error("callback-pAmpStatus:");
//        int[] intArray = pAmpStatus.getPointer().getIntArray(0, 8);
//        for (int i = 0; i < intArray.length; i++) {
//            log.error("通道" + i + ":" + Integer.toBinaryString(intArray[i]));
//        }

        String deviceFault2String = Integer.toBinaryString(deviceFault);
        DcsCallbackHandler handler = new DcsCallbackHandler();
        DcsStatus status = handler.handleDcsCallbackInfo(deviceFault2String);
        if (status != null) dcsStatus.put(dcsID, status.getDesc());
        else dcsStatus.put(dcsID, "未知错误");
    };

    // zone音频信息
    static HoneywellLibrary.PMONITORZONE pmonitorzone = (dcsID, zoneID, pAudioData, AudioSize) -> {

//        log.error("=================================");
//        log.error("callback-dcsId:" + dcsID);
//        log.error("callback-zoneID:" + zoneID);
//        log.error("callback-AudioSize:" + AudioSize);
//        log.error("callback-pAudioData:" + pAudioData.getValue());
//        byte[] byteArray = pAudioData.getPointer().getByteArray(0, AudioSize);

        String key = String.format("%d-%d", dcsID, zoneID);
        byte[] callbackBytes = pAudioData.getPointer().getByteArray(0, AudioSize);
        // log.info("monitorZoneAudio-size:" + monitorZoneAudio.size());

        if (AudioSize == 4096) {
            // 将key存放在4096的末尾，这样可以监听多个区域不同的音频
            byte[] byteKeys = key.getBytes();
            mergeBytes(callbackBytes, byteKeys);
            WebSocketServer.BroadcastMessage(callbackBytes);
        }

        // mergePCMFile(pAudioData, AudioSize);
    };

    // 车载数据回调
    static HoneywellLibrary.PTRAINMESSAGE pTrainMsg = (int npmsId, int trainID, int message) -> {
        String format = String.format("receive train msg! npmsId:%d,trainId:%d,message:%d", npmsId, trainID, message);
        log.error(format);
    };

    // 车载音频数据回调
    static HoneywellLibrary.PTRAINAUDIO pTrainAudio = (int NPMSID, int TaskID, ByteByReference pAudioData, int audioSize) -> {
        String format = String.format("receive audio! audioSize:%d", audioSize);
        log.error(format);
    };

    @SneakyThrows
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // linux操作系统，sdk才有效
        String system = System.getProperty("os.name");
        log.error(system);
        if (system.equalsIgnoreCase("linux")) {

            log.error("初始化开始");

            try {
                initDevices();
                log.error("初始化完成");
            } catch (Exception e) {
                log.error("初始化失败", e);
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        HoneywellLibrary.INSTANCE.X618_Deinitialize();
    }

    private void initDevices() throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("broadcast.yml");
        InputStream inputStream = classPathResource.getInputStream();
        Reader reader = new InputStreamReader(inputStream, "UTF-8");
        YamlReader ymlReader = new YamlReader(reader);
        Object obj = ((Map) ymlReader.read()).get("broadcast-device");
        String s = JSONObject.toJSONString(obj);
        BroadcastDevice broadcastDevice = JSONObject.parseObject(s, BroadcastDevice.class);

        // 初始化+添加设备
        HoneywellLibrary.INSTANCE.X618_Initialize();
        // 设置PCM 采样率44.1KHZ, 16Bit, mono
        HoneywellLibrary.INSTANCE.X618_SetAudioStreamFormat(broadcastDevice.getAudioStreamFormat());
        // 设置nicIP
        HoneywellLibrary.INSTANCE.X618_SetNIC(broadcastDevice.getNicIP());
        // 设置组播ip
        HoneywellLibrary.INSTANCE.X618_SetFirstMulticastIP(broadcastDevice.getMulticastIP());
        // 设置sdk
        HoneywellLibrary.INSTANCE.X618_SetNodeID(broadcastDevice.getNodeID());

        // 注册回调
//        HoneywellLibrary.INSTANCE.X618_SetDeviceCallback(pdcsstatus,
//                null, null, null,
//                null, null, null);

        // 注册列车回调函数
        //HoneywellLibrary.INSTANCE.X618_SetTrainCallback(pTrainMsg, null);
        HoneywellLibrary.INSTANCE.X618_SetTrainCallback(pTrainMsg, pTrainAudio);

        // 添加设备
        broadcastDevice.addDevices(c -> HoneywellLibrary.INSTANCE.X618_AddDevice(Integer.parseInt(c.get(0)), Integer.parseInt(c.get(1)), c.get(2)));

        // 添加虚拟车
        broadcastDevice.addTrains(c -> HoneywellLibrary.INSTANCE.X618_AddDevice(Integer.parseInt(c.get(0)), Integer.parseInt(c.get(1)), c.get(2)));
    }

    private static class DcsCallbackHandler {
        // 处理dcs回调信息
        private DcsStatus handleDcsCallbackInfo(String deviceFault2String) {
            if (deviceFault2String.equals("0"))
                return DcsStatus.正常;

            int index = 0;
            for (int i = deviceFault2String.length() - 1; i >= 0; i--) {
                char c = deviceFault2String.charAt(i);
                if (c == '1') break;
                index++;
            }

            return DcsStatus.getDcsStatus(index);
        }
    }

    private static void mergePCMFile(ByteByReference pAudioData, int AudioSize) throws IOException {
        synchronized (HoneywellListener.class) {
            long l = System.currentTimeMillis();
            if (AudioSize == 4096) {
                String fileName = "pcm.pcm";
                File file = new File(fileName);
                byte[] bytes = pAudioData.getPointer().getByteArray(0, AudioSize);
                log.error("AudioSize:" + AudioSize);
                log.error("pAudioData-length:" + bytes.length);
                if (!file.exists()) {
                    OutputStream write = new FileOutputStream(file);
                    write.write(bytes);
                    write.flush();
                    write.close();
                } else {
                    RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
                    // 文件长度，字节数
                    long fileLength = randomFile.length();
                    log.error("fileLength:" + fileLength);
                    // 将写文件指针移到文件尾。
                    randomFile.seek(fileLength);
                    randomFile.write(bytes);
                    randomFile.close();
                }
            }
        }
    }

    private static byte[] mergeBytes(byte[] bytes1, byte[] bytes2) {
        byte[] mergeBytes = new byte[bytes1.length + bytes2.length];

        System.arraycopy(bytes1, 0, mergeBytes, 0, bytes1.length);
        System.arraycopy(bytes2, 0, mergeBytes, bytes1.length, bytes2.length);

        return mergeBytes;
    }
}
