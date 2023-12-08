package com.kob.backend.consumer.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private Integer id;
    private Integer sx;
    private Integer sy;
    private List<Integer> steps;  // 记录历史走过的每一步方向

    private boolean check_tail_increasing(int step) {  // 检测当前回合蛇的长度是否增加
        if (step <= 7) return true;  // 前7回合每一回合长度都增加
        return step % 3 == 1;  // 之后每3回合增加一次长度
    }

    public List<Cell> getCells() {  // 返回蛇的身体，每次都根据蛇历史走的方向将其每一格找出来
        List<Cell> cells = new ArrayList<>();
        int[] dx = { -1, 0, 1, 0 }, dy = { 0, 1, 0, -1 };
        int x = sx, y = sy;
        int step = 0;
        cells.add(new Cell(x, y));
        for (int d: steps) {
            x += dx[d];
            y += dy[d];
            cells.add(new Cell(x, y));
            if (!check_tail_increasing(++step)) {
                cells.remove(0);  // 删掉蛇尾，即第一个起始的位置
            }
        }
        return cells;
    }

    public String getStringSteps() {  // 将steps转换成字符串
        StringBuilder res = new StringBuilder();
        for (int d: steps) {
            res.append(d);
        }
        return res.toString();
    }
}
