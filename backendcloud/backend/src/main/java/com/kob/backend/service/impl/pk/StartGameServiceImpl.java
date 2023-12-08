package com.kob.backend.service.impl.pk;

import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.service.pk.StartGameService;
import org.springframework.stereotype.Service;

@Service
public class StartGameServiceImpl implements StartGameService {
    @Override
    public String startGame(Integer aId, Integer bId) {
        System.out.println("Start Game: Player " + aId + " and Player " + bId);
        WebSocketServer webSocketServer = WebSocketServer.users.get(aId);
        webSocketServer.startGame(aId, bId);
        return "success";
    }
}
