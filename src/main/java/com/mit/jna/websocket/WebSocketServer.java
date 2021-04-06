package com.mit.jna.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@ServerEndpoint(value = "/ws/honeywell")
public class WebSocketServer {

    private static final AtomicInteger OnlineCount = new AtomicInteger(0);

    // 存放每个客户端对应的Session对象
    private static CopyOnWriteArraySet<Session> sessionSet = new CopyOnWriteArraySet<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        sessionSet.add(session);
        int cnt = OnlineCount.incrementAndGet(); // 在线数加1
        log.info("有连接加入，当前连接数为：{}", cnt);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        sessionSet.remove(session);
    }

    /**
     * 出现错误
     */
    @OnError
    public void onError(Session session, Throwable error) throws IOException {
        log.error("发生错误：{}，Session ID： {}", error.getMessage(), session.getId());
        session.close();
        sessionSet.remove(session);
    }

    /**
     * 群发消息
     *
     * @param audioBuf 音频流
     */
    public static void BroadcastMessage(byte[] audioBuf) throws IOException {
        for (Session session : sessionSet) {
            if (session.isOpen()) {
                sendMessage(session, audioBuf);
            }
        }
    }

    /**
     * 传送音频，实践表明，每次浏览器刷新，session会发生变化。
     * @param session
     * @param audioBuf 音频流
     * @throws IOException
     */
    private static void sendMessage(Session session, byte[] audioBuf) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(audioBuf);
        session.getBasicRemote().sendBinary(buffer);
    }
}
