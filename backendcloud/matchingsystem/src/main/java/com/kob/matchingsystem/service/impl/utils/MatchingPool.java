package com.kob.matchingsystem.service.impl.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@Component  // 为了在类中能够注入Bean
public class MatchingPool extends Thread {
    private static List<Player> players = new ArrayList<>();  // 我们之后会自己加锁，因此不需要用线程安全的集合
    private ReentrantLock lock = new ReentrantLock();

    private static WebClient webClient;
    private static final String startGameUrl = "http://localhost:3000/pk/startgame/";

    @Autowired
    public void setWebClient(WebClient webClient) {
        MatchingPool.webClient = webClient;
    }

    public void addPlayer(Integer userId, Integer rating) {
        lock.lock();
        try {
            players.add(new Player(userId, rating, 0));
        } finally {
            lock.unlock();
        }
    }

    public void removePlayer(Integer userId) {
        lock.lock();
        try {
            players.removeIf(player -> player.getUserId().equals(userId));
        } finally {
            lock.unlock();
        }
    }

    private void increaseWaitingTime(Integer waitingTime) {  // 将当前所有等待匹配的玩家等待时间加waitingTime秒
        for (Player player: players) {
            player.setWaitingTime(player.getWaitingTime() + waitingTime);
        }
    }

    private boolean checkMatched(Player a, Player b) {  // 判断两名玩家是否能够匹配
        int ratingDelta = Math.abs(a.getRating() - b.getRating());  // 分差
        int minWatingTime = Math.min(a.getWaitingTime(), b.getWaitingTime());  // 等待时间较短的玩家符合匹配要求那么等待时间长的也一定符合要求
        return ratingDelta <= minWatingTime * 10;  // 每多匹配一秒则匹配的分值范围加10
    }

    private void sendResult(Player a, Player b) {  // 返回匹配结果给Web后端
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("a_id", String.valueOf(a.getUserId()));
        data.add("b_id", String.valueOf(b.getUserId()));
        webClient.post()
                .uri(startGameUrl)
                .body(BodyInserters.fromFormData(data))
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        resp -> {
                            if ("success".equals(resp)) {
                                System.out.println("Match Success: Player " + a.getUserId() + " and Player " + b.getUserId());
                            }
                        },
                        error -> {
                            System.out.println("Matching WebClient Error: " + error.getMessage());
                        });
    }

    private void matchPlayers() {  // 尝试匹配所有玩家
        Set<Player> used = new HashSet<>();  // 标记玩家是否已经被匹配
        for (int i = 0; i < players.size(); i++) {
            if (used.contains(players.get(i))) continue;
            for (int j = i + 1; j < players.size(); j++) {
                if (used.contains(players.get(j))) continue;
                Player a = players.get(i), b = players.get(j);
                if (checkMatched(a, b)) {
                    used.add(a);
                    used.add(b);
                    sendResult(a, b);
                    break;
                }
            }
        }
        players.removeIf(used::contains);  // 从players中移除used中的玩家
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
                System.out.println(players);  // 输出当前匹配池中的玩家
                lock.lock();
                try {
                    increaseWaitingTime(1);
                    matchPlayers();
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
