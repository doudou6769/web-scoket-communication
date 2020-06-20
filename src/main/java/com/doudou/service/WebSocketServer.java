package com.doudou.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author DouDou
 * @Description
 * @date 2020/6/20
 */
@Slf4j
@Component
@ServerEndpoint("/websocket/{username}")
public class WebSocketServer {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 记录总的在线数
    private static AtomicInteger totalCount = new AtomicInteger(0);

    // 存放当前在线的所有会话对象
    private static ConcurrentHashMap<String, WebSocketServer> connectionList = new ConcurrentHashMap<>();

    // 当前连接的用户名
    private String username;

    // 存放当前连接的会话对象
    private Session session;

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username){
        this.username = username;
        this.session = session;
        connectionList.put(username,this);
        int currentCount = totalCount.incrementAndGet();
        log.info("有新用户加入：<" + username + "> 当前在线人数：" + currentCount);
        Map<String,Object> data = new HashMap<>();
        // 所有在线用户名
        data.put("users",connectionList.keys());
        data.put("msg","连接成功！");
        try {
            sendMessageToAll(objectMapper.writeValueAsString(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(){
        connectionList.remove(username);
        int currentCount = totalCount.decrementAndGet();
        log.info("用户<" + username + ">与服务器断开连接！当前在线人数："+currentCount);
        Map<String,Object> data = new HashMap<>();
        // 所有在线用户名
        data.put("users",connectionList.keys());
        data.put("msg","连接成功！");
        try {
            sendMessageToAll(objectMapper.writeValueAsString(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(String message){
        String[] sendInfo = message.split(",");
        try {
            sendMessageToUser(sendInfo[0],sendInfo[1]);
        } catch (IOException e) {
            e.printStackTrace();
            log.info("发送失败！ -> " + e.getMessage());
        }
    }

    @OnError
    public void onError(Session session,Throwable error){
        log.error("发生错误！ -> " + error.getMessage());
        error.printStackTrace();
    }

    /**
     * 向当前客户端发送消息
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    /**
     * 向指定客户端发送消息
     * @param username
     * @param message
     * @throws IOException
     */
    public void sendMessageToUser(String username,String message) throws IOException {
        WebSocketServer webSocketServer = connectionList.get(username);
        if(webSocketServer == null)
            throw new RuntimeException("用户已下线");
        webSocketServer.sendMessage(message);
    }

    /**
     * 群发消息到所有在线用户
     * @param message
     */
    public void sendMessageToAll(String message){
        connectionList.values().forEach(webSocketServer -> {
            try {
                webSocketServer.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
