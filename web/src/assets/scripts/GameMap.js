import { AcGameObject } from "./AcGameObject";
import { Wall } from "./Wall";

export class GameMap extends AcGameObject {
  constructor(ctx, parent) {  // ctx表示画布，parent表示画布的父元素
    super();

    this.ctx = ctx;
    this.parent = parent;
    this.L = 0;  // 一个单位的绝对长度
    this.rows = 13;  // 地图的行数
    this.cols = 13;  // 地图的列数

    this.inner_walls_count = 20;  // 地图内部的随机障碍物数量，需要是偶数
    this.walls = [];  // 所有的障碍物
  }

  check_connectivity(g, sx, sy, tx, ty) {  // 用flood fill算法判断两名玩家是否连通
    if (sx == tx && sy == ty) return true;
    g[sx][sy] = true;
    let dx = [-1, 0, 1, 0], dy = [0, 1, 0, -1];
    for (let i = 0; i < 4; i++) {
      let nx = sx + dx[i], ny = sy + dy[i];
      if (!g[nx][ny] && this.check_connectivity(g, nx, ny, tx, ty))  // 地图已经有边界标记了因此无需判断越界
        return true;
    }
    return false;
  }

  create_walls() {
    const g = [];  // 表示每个位置是否是障碍物

    // 初始化障碍物标记数组
    for (let r = 0; r < this.rows; r++) {
      g[r] = [];
      for (let c = 0; c < this.cols; c++) {
        g[r][c] = false;
      }
    }

    // 给地图四周加上障碍物
    for (let r = 0; r < this.rows; r++) {
      g[r][0] = g[r][this.cols - 1] = true;
    }

    for (let c = 0; c < this.cols; c++) {
      g[0][c] = g[this.rows - 1][c] = true;
    }

    // 添加地图内部的随机障碍物，需要有对称性因此枚举一半即可，另一半对称生成
    for (let i = 0; i < this.inner_walls_count / 2; i++) {
      for (let j = 0; j < 10000; j++) {
        let r = parseInt(Math.random() * this.rows);
        let c = parseInt(Math.random() * this.cols);
        if (g[r][c] || g[c][r]) continue;
        if (r == this.rows - 2 && c == 1 || r == 1 && c == this.cols - 2) continue;  // 判断是否覆盖到出生地
        g[r][c] = g[c][r] = true;
        break;
      }
    }

    const g_copy = JSON.parse(JSON.stringify(g));  // 复制一份g，可以先转换成json再转回来就是一份新的了
    if (!this.check_connectivity(g_copy, this.rows - 2, 1, 1, this.cols - 2)) return false;

    // 创建障碍物
    for (let r = 0; r < this.rows; r++) {
      for (let c = 0; c < this.cols; c++) {
        if (g[r][c]) {
          this.walls.push(new Wall(r, c, this));
        }
      }
    }

    return true;
  }

  start() {
    for (let i = 0; i < 10000; i++) {  // 暴力枚举直至生成合法的地图
      if (this.create_walls())
        break;
    }
  }

  update_size() {  // 每一帧更新地图大小
    this.L = parseInt(Math.min(this.parent.clientWidth / this.cols, this.parent.clientHeight / this.rows));
    this.ctx.canvas.width = this.L * this.cols;
    this.ctx.canvas.height = this.L * this.rows;
  }

  update() {
    this.update_size();
    this.render();
  }

  render() {
    const color_even = "#AAD752", color_odd = "#A2D048";
    for (let r = 0; r < this.rows; r++) {
      for (let c = 0; c < this.cols; c++) {
        if ((r + c) % 2 == 0) {
          this.ctx.fillStyle = color_even;
        } else {
          this.ctx.fillStyle = color_odd;
        }
        // canvas坐标系横轴是x，纵轴是y，与数组坐标系不同
        this.ctx.fillRect(c * this.L, r * this.L, this.L, this.L);
      }
    }
  }
}
