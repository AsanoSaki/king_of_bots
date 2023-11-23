package com.kob.backend.consumer.utils;

import java.util.Arrays;
import java.util.Random;

public class Game {
    private final Integer rows;
    private final Integer cols;
    private final Integer inner_walls_count;
    private final boolean[][] g;
    private static final int[] dx = { -1, 0, 1, 0 }, dy = { 0, 1, 0, -1 };

    public Game(Integer rows, Integer cols, Integer inner_walls_count) {
        this.rows = rows;
        this.cols = cols;
        this.inner_walls_count = inner_walls_count;
        this.g = new boolean[rows][cols];
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
}
