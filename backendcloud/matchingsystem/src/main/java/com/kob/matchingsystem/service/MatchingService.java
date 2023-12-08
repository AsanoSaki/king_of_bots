package com.kob.matchingsystem.service;

public interface MatchingService {
    String addPlayer(Integer userId, Integer rating);  // 将玩家添加到匹配池中
    String removePlayer(Integer userId);  // 从匹配池中删除玩家
}
