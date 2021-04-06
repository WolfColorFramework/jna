package com.mit.jna.utils.broadcast.sdk;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;

import java.io.IOException;

public interface HoneywellLibrary extends Library {

    HoneywellLibrary INSTANCE = Native.load("libx618sdk.so", HoneywellLibrary.class);

    // 初始化
    int X618_Initialize();

    // 释放sdk资源
    int X618_Deinitialize();

    // 设置PCM 采样率44.1KHZ, 16Bit, mono
    int X618_SetAudioStreamFormat(int audioFormat);

    // 设置发送音频的网卡IP
    int X618_SetNIC(String nicIP);

    // 设置SDK组播起始地址
    int X618_SetFirstMulticastIP(String firstMulticastIP);

    // 设置SDK节点号9500
    int X618_SetNodeID(int nodeID);

    // 添加一台设备，如X-DCS/X-NPMS/列车/内通
    int X618_AddDevice(int deviceID, int deviceType, String deviceIP);

    // 获取dcs通道（共8个）的音量
    int X618_GetVolume(int deviceID);

    // 设置dcs通道（共8个）的音量
    int X618_SetVolume(int deviceID, int[] volumes);

    // 监听分区
    int X618_MonitorZone(int dcsID, int zoneID, int flag);

    // 打开X-NPMS麦克风或者线路输入
    int X618_StartNPMSTask(int npmsID, int taskID, int priority, String zones);

    // 关闭X-NPMS麦克风或者线路输入
    int X618_StopNPMSTask(int npmsID, int taskID);

    // 播放文件
    int X618_StartAudioFileTask(int taskID, int volume, int priority, int resume,
                                String zones, int dryContactOutput, String audioFiles, int repeat);

    // 开启NPMS监听
    int X618_StartNPMSMonitor(int npmsID, int dcsID, int zoneID);

    // 关闭NPMS监听
    int X618_StopNPMSMonitor(int npmsID);

    // 设置列车的状态,用于X-NPMS或X-Smart显示列车状态.
    int X618_SetTrainStatus(int trainID, int status);

    //----------------------------------回调函数---------------------------------
    // 设置设备回调函数, 用于获取设备状态和故障，获取设备音量，获取任务状态，
    int X618_SetDeviceCallback(PDCSSTATUS pDCSStatusProc, PZONESTATUS pZoneStatusProc,
                               PDCSVOLUME pDCSVolumeProc, PTASKSTATUS pTaskStatusProc,
                               PMONITORZONE pMonitorZoneProc, PNPMSSTATUS pNPMSStatusProc,
                               PNPMSPTTAUDIO pNPMSPTTAudioProc);

    // SDK将返回X-DCS的设备状态和故障，功放通道的状态和故障
    interface PDCSSTATUS extends Callback {
        void callback(int dcsID, int deviceStatus, int deviceFault,
                      IntByReference pAmpStatus, int contactInputOpenFault,
                      int contactInputShortFault);
    }

    // SDK将返回ZONE状态和故障, 分区音源ID
    interface PZONESTATUS extends Callback {
        void callback(int dcsID, IntByReference pZoneStatus, LongByReference pZoneAudioID);
    }

    // SDK将返回X-DCS的功放通道音量值
    interface PDCSVOLUME extends Callback {
        void callback(int dcsID, IntByReference pVolume);
    }

    // 如果任务状态改变了，该回调函数返回任务的状态
    interface PTASKSTATUS extends Callback {
        void callback(int taskID, int taskStatus);
    }

    // 当分区被监听时，如果分区正在播放，该回调函数返回分区的实时音频数据
    // 该回调函数可用于分区录音和监听
    interface PMONITORZONE extends Callback {
        void callback(int dcsID, int zoneID, ByteByReference pAudioData, int audioSize) throws IOException;
    }

    // SDK将不断地返回X-NPMS的设备状态和故障
    interface PNPMSSTATUS extends Callback {
        void callback(int npmsID, int deviceStatus,
                      int intercomStatus, int monitorDeviceID,
                      int monitorCanID, int monitorZoneID);
    }

    // 当X-NPMS麦克风喊话时, 该回调函数不断返回X-NPMS麦克风的音频数据
    // 该回调函数可用于X-NPMS话筒录音和监听
    interface PNPMSPTTAUDIO extends Callback {
        void callback(int npmsID, int taskID, String pAudioData, int audioSize);
    }

    // 设置通知列车广播的回调函数，用于获取通知列车打开广播/关闭广播消息，并获取向列车播放的音频数据
    // 用于npms与train通信
    int X618_SetTrainCallback(
            PTRAINMESSAGE pTrainMessageProc,
            PTRAINAUDIO pTrainAudioProc
    );

    // 当X-NPMS向列车播放开始/停止时，SDK将返回播放开始/停止信息
    interface PTRAINMESSAGE extends Callback {
        void callback(int npmsID, int trainID, int message);
    }

    // 当X-NPMS向列车播放时, 该回调函数将不断返回X-NPMS的音频数据
    interface PTRAINAUDIO extends Callback {
        void callback(int npmsID, int taskID, ByteByReference pAudioData, int audioSize);
    }
}
