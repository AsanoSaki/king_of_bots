package com.kob.backend.consumer;

import com.alibaba.fastjson2.JSONObject;
import com.kob.backend.consumer.utils.Game;
import com.kob.backend.consumer.utils.JwtAuthentication;
import com.kob.backend.mapper.RecordMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾
public class WebSocketServer {
    // ConcurrentHashMap是一个线程安全的哈希表，用于将用户ID映射到WS实例
    public static final ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();
    private User user;
    private Session session = null;
    private Game game = null;

    private static UserMapper userMapper;
    public static RecordMapper recordMapper;  // 要在Game中调用
    private static RestTemplate restTemplate;  // 用于发送HTTP请求
    private static WebClient webClient;

    // 向匹配系统发送请求的URL
    private static final String matchingAddPlayerUrl = "http://localhost:3001/matching/add/";
    private static final String matchingRemovePlayerUrl = "http://localhost:3001/matching/remove/";

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }

    @Autowired
    public void setRecordMapper(RecordMapper recordMapper) {
        WebSocketServer.recordMapper = recordMapper;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        WebSocketServer.restTemplate = restTemplate;
    }

    @Autowired
    public void setWebClient(WebClient webClient) {
        WebSocketServer.webClient = webClient;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        this.session = session;

        Integer userId = JwtAuthentication.getUserId(token);
        user = userMapper.selectById(userId);

        if (user != null) {
            users.put(userId, this);
            System.out.println("Player " + user.getId() + " Connected!");
        } else {
            this.session.close();
        }
    }

    @OnClose
    public void onClose() {
        if (user != null) {
            users.remove(user.getId());
            System.out.println("Player " + user.getId() + " Disconnected!");
        }
        stopMatching();  // 断开连接时取消匹配
    }

    @OnMessage
    public void onMessage(String message, Session session) {  // 一般会把onMessage()当作路由
        JSONObject data = JSONObject.parseObject(message);
        String event = data.getString("event");  // 取出event的内容

        if ("start_match".equals(event)) {  // 开始匹配
            this.startMatching();
        } else if ("stop_match".equals(event)) {  // 取消匹配
            this.stopMatching();
        } else if ("move".equals(event)) {  // 移动
            move(data.getInteger("direction"));
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    public void sendMessage(String message) {  // 从后端向当前链接发送消息
        synchronized (session) {  // 由于是异步通信，需要加一个锁
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void startGame(Integer aId, Integer bId) {
        User a = userMapper.selectById(aId), b = userMapper.selectById(bId);

        game = new Game(13, 14, 20, a.getId(), b.getId());
        game.createMap();
        users.get(a.getId()).game = game;
        users.get(b.getId()).game = game;

        game.start();  // 开一个新的线程

        JSONObject respGame = new JSONObject();
        respGame.put("a_id", game.getPlayerA().getId());
        respGame.put("a_sx", game.getPlayerA().getSx());
        respGame.put("a_sy", game.getPlayerA().getSy());
        respGame.put("b_id", game.getPlayerB().getId());
        respGame.put("b_sx", game.getPlayerB().getSx());
        respGame.put("b_sy", game.getPlayerB().getSy());
        respGame.put("map", game.getG());

        JSONObject respA = new JSONObject(), respB = new JSONObject();  // 发送给A/B的信息
        respA.put("event", "match_success");
        respA.put("opponent_username", b.getUsername());
        respA.put("opponent_photo", b.getPhoto());
        respA.put("game", respGame);
        users.get(a.getId()).sendMessage(respA.toJSONString());  // A不一定是当前链接，因此要在users中获取

        respB.put("event", "match_success");
        respB.put("opponent_username", a.getUsername());
        respB.put("opponent_photo", a.getPhoto());
        respB.put("game", respGame);
        users.get(b.getId()).sendMessage(respB.toJSONString());
    }

    private void startMatching() {  // 需要向MatchingSystem发送请求
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", String.valueOf(user.getId()));
        data.add("rating", String.valueOf(user.getRating()));
//        String resp = restTemplate.postForObject(matchingAddPlayerUrl, data, String.class);  // 参数为请求地址、数据、返回值的Class
//        if ("success".equals(resp)) {
//            System.out.println("Player " + user.getId() + " start matching!");
//        }
        webClient.post()  // POST请求
                .uri(matchingAddPlayerUrl)  // 请求路径
                .body(BodyInserters.fromFormData(data))  // 请求体，MultiValueMap对象默认发起的是Form提交，使用BodyInserters.fromFormData()将其添加到请求体中
                .retrieve()  // 获取响应体
                .bodyToMono(String.class)  // 响应数据类型转换，返回Mono<String>类型的数据
                .subscribe(  // 回调函数会在请求完成时被调用，用于处理响应的结果
                        resp -> {  // 处理请求成功的响应
                            if ("success".equals(resp)) {
                                System.out.println("Player " + user.getId() + " start matching!");
                            }
                        },
                        error -> {  // 处理请求错误的响应
                            System.out.println("Start Matching WebClient Error: " + error.getMessage());
                        });
    }

    private void stopMatching() {  // 需要向MatchingSystem发送请求
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", String.valueOf(user.getId()));
//        String resp = restTemplate.postForObject(matchingRemovePlayerUrl, data, String.class);
//        if ("success".equals(resp)) {
//            System.out.println("Player " + user.getId() + " stop matching!");
//        }
        webClient.post()
                .uri(matchingRemovePlayerUrl)
                .body(BodyInserters.fromFormData(data))
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        resp -> {
                            if ("success".equals(resp)) {
                                System.out.println("Player " + user.getId() + " stop matching!");
                            }
                        },
                        error -> {
                            System.out.println("Stop Matching WebClient Error: " + error.getMessage());
                        });
    }

    private void move(Integer direction) {
        if (game.getPlayerA().getId().equals(user.getId())) {
            game.setNextStepA(direction);
        } else if (game.getPlayerB().getId().equals(user.getId())) {
            game.setNextStepB(direction);
        }
    }
}
