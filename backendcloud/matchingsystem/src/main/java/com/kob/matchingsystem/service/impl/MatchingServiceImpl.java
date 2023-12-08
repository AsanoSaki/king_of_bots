package com.kob.matchingsystem.service.impl;

import com.kob.matchingsystem.service.MatchingService;
import com.kob.matchingsystem.service.impl.utils.MatchingPool;
import org.springframework.stereotype.Service;

@Service
public class MatchingServiceImpl implements MatchingService {
    public static final MatchingPool matchingPool = new MatchingPool();  // 全局只有一个匹配线程

    @Override
    public String addPlayer(Integer userId, Integer rating) {
        System.out.println("Add Player: " + userId + ", Rating: " + rating);
        matchingPool.addPlayer(userId, rating);
        return "success";
    }

    @Override
    public String removePlayer(Integer userId) {
        System.out.println("Remove Player: " + userId);
        matchingPool.removePlayer(userId);
        return "success";
    }
}
