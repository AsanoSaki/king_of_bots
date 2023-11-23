package com.kob.backend.consumer;

import com.alibaba.fastjson2.JSONObject;
import com.kob.backend.consumer.utils.Game;
import com.kob.backend.consumer.utils.JwtAuthentication;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾
public class WebSocketServer {
    // ConcurrentHashMap是一个线程安全的哈希表，用于将用户ID映射到WS实例
    private static final ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();
    // CopyOnWriteArraySet也是线程安全的
    private static final CopyOnWriteArraySet<User> matchPool = new CopyOnWriteArraySet<>();  // 匹配池
    private User user;
    private Session session = null;

    private static UserMapper userMapper;

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        this.session = session;
        System.out.println("Connected!");

        Integer userId = JwtAuthentication.getUserId(token);
        this.user = userMapper.selectById(userId);

        if (user != null) {
            users.put(userId, this);
        } else {
            this.session.close();
        }
    }

    @OnClose
    public void onClose() {
        System.out.println("Disconnected!");
        if (this.user != null) {
            users.remove(this.user.getId());
            matchPool.remove(this.user);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {  // 一般会把onMessage()当作路由
        System.out.println("Receive message!");
        JSONObject data = JSONObject.parseObject(message);
        String event = data.getString("event");  // 取出event的内容

        if ("start_match".equals(event)) {
            this.startMatching();
        } else if ("stop_match".equals(event)) {
            this.stopMatching();
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    public void sendMessage(String message) {  // 从后端向当前链接发送消息
        synchronized (this.session) {  // 由于是异步通信，需要加一个锁
            try {
                this.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startMatching() {
        System.out.println("Start matching!");
        matchPool.add(this.user);

        while (matchPool.size() >= 2) {  // 临时调试用的，未来要替换成微服务
            Iterator<User> it = matchPool.iterator();
            User a = it.next(), b = it.next();
            matchPool.remove(a);
            matchPool.remove(b);

            Game game = new Game(13, 14, 20);
            game.createMap();

            JSONObject respA = new JSONObject();  // 发送给A的信息
            respA.put("event", "match_success");
            respA.put("opponent_username", b.getUsername());
            respA.put("opponent_photo", b.getPhoto());
            respA.put("game_map", game.getG());
            users.get(a.getId()).sendMessage(respA.toJSONString());  // A不一定是当前链接，因此要在users中获取

            JSONObject respB = new JSONObject();  // 发送给B的信息
            respB.put("event", "match_success");
            respB.put("opponent_username", a.getUsername());
            respB.put("opponent_photo", a.getPhoto());
            respB.put("game_map", game.getG());
            users.get(b.getId()).sendMessage(respB.toJSONString());
        }
    }

    private void stopMatching() {
        System.out.println("Stop matching!");
        matchPool.remove(this.user);
    }
}
