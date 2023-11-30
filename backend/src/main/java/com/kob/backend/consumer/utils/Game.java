package com.kob.backend.consumer.utils;

import com.alibaba.fastjson2.JSONObject;
import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.pojo.Record;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Game extends Thread {
    private final Integer rows;
    private final Integer cols;
    private final Integer inner_walls_count;
    private final boolean[][] g;
    private static final int[] dx = { -1, 0, 1, 0 }, dy = { 0, 1, 0, -1 };
    private final Player playerA, playerB;
    private Integer nextStepA = null;  // 下一步操作，0、1、2、3分别表示四个方向，null表示还没有获取到
    private Integer nextStepB = null;
    private ReentrantLock lock = new ReentrantLock();  // 需要给nextStep变量上锁防止读写冲突
    private String status = "playing";  // 整局游戏的状态，结束后为finished
    private String loser = "";  // 输的一方是谁，all表示平局

    public Game(Integer rows, Integer cols, Integer inner_walls_count, Integer idA, Integer idB) {
        this.rows = rows;
        this.cols = cols;
        this.inner_walls_count = inner_walls_count;
        this.g = new boolean[rows][cols];
        playerA = new Player(idA, rows - 2, 1, new ArrayList<>());  // 默认A在左下角B在右上角
        playerB = new Player(idB, 1, cols - 2, new ArrayList<>());
    }

    public Player getPlayerA() {
        return playerA;
    }

    public Player getPlayerB() {
        return playerB;
    }

    public void setNextStepA(Integer nextStepA) {  // 未来会在另一个线程中调用
        lock.lock();  // 操作nextStep变量前先上锁
        try {
            this.nextStepA = nextStepA;
        } finally {
            lock.unlock();  // 操作完后无论是否有异常都解锁
        }
    }

    public void setNextStepB(Integer nextStepB) {
        lock.lock();
        try {
            this.nextStepB = nextStepB;
        } finally {
            lock.unlock();
        }
    }

    public boolean[][] getG() {
        return g;
    }

    private boolean check_connectivity(int sx, int sy, int tx, int ty) {
        if (sx == tx && sy == ty) return true;
        g[sx][sy] = true;
        for (int i = 0; i < 4; i++) {
            int nx = sx + dx[i], ny = sy + dy[i];
            if (!g[nx][ny] && check_connectivity(nx, ny, tx, ty)) {
                g[sx][sy] = false;  // 注意在这里我们用的g就是原始数组，因此修改后要记得还原
                return true;
            }
        }
        g[sx][sy] = false;  // 记得还原
        return false;
    }

    private boolean drawMap() {
        // 初始化障碍物标记数组
        for (int i = 0; i < this.rows; i++) {
            Arrays.fill(g[i], false);
        }

        // 给地图四周加上障碍物
        for (int r = 0; r < this.rows; r++) {
            g[r][0] = g[r][this.cols - 1] = true;
        }
        for (int c = 0; c < this.cols; c++) {
            g[0][c] = g[this.rows - 1][c] = true;
        }

        // 添加地图内部的随机障碍物，需要有对称性因此枚举一半即可，另一半对称生成
        Random random = new Random();
        for (int i = 0; i < this.inner_walls_count / 2; i++) {
            for (int j = 0; j < 10000; j++) {
                int r = random.nextInt(this.rows);  // 返回0~this.rows-1的随机整数
                int c = random.nextInt(this.cols);
                if (g[r][c] || g[this.rows - 1 - r][this.cols - 1 - c]) continue;
                if (r == this.rows - 2 && c == 1 || r == 1 && c == this.cols - 2) continue;
                g[r][c] = g[this.rows - 1 - r][this.cols - 1 - c] = true;
                break;
            }
        }

        return check_connectivity(this.rows - 2, 1, 1, this.cols - 2);
    }

    public void createMap() {
        for (int i = 0; i < 10000; i++) {
            if (drawMap()) {
                break;
            }
        }
    }

    private boolean nextStep() {  // 等待两名玩家的下一步操作，在该方法中也会操作nextStep变量
        try {
            Thread.sleep(500);  // 前端的蛇每秒走2格，因此走一格需要500ms，每次后端执行下一步时需要先sleep，否则快速的多次输入将会覆盖掉之前输入的信息
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 50; i++) {
            try {
                Thread.sleep(100);  // 每回合循环50次，每次睡眠100ms，即一回合等待用户输入的时间为5s
                lock.lock();
                try {
                    if (nextStepA != null && nextStepB != null) {  // 两名玩家的下一步操作都读到了
                        playerA.getSteps().add(nextStepA);
                        playerB.getSteps().add(nextStepB);
                        return true;
                    }
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean check_valid(List<Cell> cellsA, List<Cell> cellsB) {  // 判断A是否合法
        int n = cellsA.size();
        Cell headCellA = cellsA.get(n - 1);  // A的头，也就是最后一个Cell
        if (g[headCellA.x][headCellA.y]) {
            return false;
        }
        for (int i = 0; i < n - 1; i++) {  // 判断除了头以外的其他身体部分
            if (cellsA.get(i).x == headCellA.x && cellsA.get(i).y == headCellA.y) {
                return false;
            }
            if (cellsB.get(i).x == headCellA.x && cellsB.get(i).y == headCellA.y) {
                return false;
            }
        }
        return true;
    }

    private void judge() {  // 判断两名玩家下一步操作是否合法
        List<Cell> cellsA = playerA.getCells();
        List<Cell> cellsB = playerB.getCells();

        boolean validA = check_valid(cellsA, cellsB);
        boolean validB = check_valid(cellsB, cellsA);
        if (!validA || !validB) {
            status = "finished";
            if (!validA && !validB) {
                loser = "all";
            } else if (!validA) {
                loser = "A";
            } else {
                loser = "B";
            }
        }
    }

    private void sendAllMessage(String message) {  // 向两个Client发送消息
        WebSocketServer.users.get(playerA.getId()).sendMessage(message);
        WebSocketServer.users.get(playerB.getId()).sendMessage(message);
    }

    private void sendMove() {  // 向两个Client发送移动消息
        lock.lock();
        try {
            JSONObject resp = new JSONObject();
            resp.put("event", "move");
            resp.put("a_direction", nextStepA);
            resp.put("b_direction", nextStepB);
            sendAllMessage(resp.toJSONString());
            nextStepA = nextStepB = null;
        } finally {
            lock.unlock();
        }
    }

    private String getStringMap() {  // 将g转换成01字符串
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                res.append(g[i][j] ? 1 : 0);
            }
        }
        return res.toString();
    }

    private void saveRecord() {  // 将对局信息存到数据库中
        Record record = new Record(
                null,
                playerA.getId(),
                playerA.getSx(),
                playerA.getSy(),
                playerB.getId(),
                playerB.getSx(),
                playerB.getSy(),
                playerA.getStringSteps(),
                playerB.getStringSteps(),
                getStringMap(),
                loser,
                new Date()
        );
        WebSocketServer.recordMapper.insert(record);
    }

    private void sendResult() {  // 向两个Client公布结果
        JSONObject resp = new JSONObject();
        resp.put("event", "result");
        resp.put("loser", loser);
        saveRecord();  // 在发送结束消息给前端之前先将游戏记录存下来
        sendAllMessage(resp.toJSONString());
    }

    @Override
    public void run() {
        for (int i = 0; i < 1000; i++) {  // 游戏最多走的步数不会超过1000
            if (nextStep()) {  // 是否获取了两条蛇的下一步操作
                judge();
                if ("playing".equals(status)) {  // 如果游戏还在进行中则需要将两名玩家的操作广播给两个Client
                    sendMove();
                } else {
                    sendResult();
                    break;
                }
            } else {
                status = "finished";
                lock.lock();
                try {
                    if (nextStepA == null && nextStepB == null) {
                        loser = "all";
                    } else if (nextStepA == null) {
                        loser = "A";
                    } else {
                        loser = "B";
                    }
                } finally {
                    lock.unlock();
                }
                sendResult();  // 这一步结束后需要给两个Client发送消息
                break;
            }
        }
    }
}
