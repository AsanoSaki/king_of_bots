import { AcGameObject } from "./AcGameObject";
import { Wall } from "./Wall";
import { Snake } from "./Snake";

export class GameMap extends AcGameObject {
  constructor(ctx, parent, store) {  // ctx表示画布，parent表示画布的父元素
    super();

    this.ctx = ctx;
    this.parent = parent;
    this.L = 0;  // 一个单位的绝对长度
    this.rows = 13;  // 地图的行数
    this.cols = 14;  // 地图的列数

    this.inner_walls_count = 20;  // 地图内部的随机障碍物数量，需要是偶数
    this.walls = [];  // 所有的障碍物

    this.snakes = [
      new Snake({ id: 0, color: "#4876EC", r: this.rows - 2, c: 1 }, this),
      new Snake({ id: 1, color: "#F94848", r: 1, c: this.cols - 2 }, this),
    ];

    this.store = store;  // vuex中的全局变量
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
        if (g[r][c] || g[this.rows - 1 - r][this.cols - 1 - c]) continue;
        if (r == this.rows - 2 && c == 1 || r == 1 && c == this.cols - 2) continue;  // 判断是否覆盖到出生地
        g[r][c] = g[this.rows - 1 - r][this.cols - 1 - c] = true;
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

  create_walls_online() {  // 通过后端生成的数据创建地图
    const g = this.store.state.pk.game_map;

    for (let r = 0; r < this.rows; r++) {
      for (let c = 0; c < this.cols; c++) {
        if (g[r][c]) {
          this.walls.push(new Wall(r, c, this));
        }
      }
    }
  }

  add_listening_events() {
    this.ctx.canvas.focus();  // 使Canvas聚焦

    this.ctx.canvas.addEventListener("keydown", e => {
      if (this.store.state.pk.status === "local") {  // 本地对战
        const [snake0, snake1] = this.snakes;

        if (e.key === "w") snake0.set_direction(0);
        else if (e.key === "d") snake0.set_direction(1);
        else if (e.key === "s") snake0.set_direction(2);
        else if (e.key === "a") snake0.set_direction(3);
        else if (e.key === "ArrowUp") snake1.set_direction(0);
        else if (e.key === "ArrowRight") snake1.set_direction(1);
        else if (e.key === "ArrowDown") snake1.set_direction(2);
        else if (e.key === "ArrowLeft") snake1.set_direction(3);
      } else {  // 匹配对战
        let d = -1;

        if (e.key === "w") d = 0;
        else if (e.key === "d") d = 1;
        else if (e.key === "s") d = 2;
        else if (e.key === "a") d = 3;

        if (d !== -1) {
          this.store.state.pk.socket.send(JSON.stringify({
            event: "move",
            direction: d,
          }));
        }
      }
    });
  }

  start() {
    if (this.store.state.pk.status === "local") {
      for (let i = 0; i < 10000; i++) {
        if (this.create_walls())
          break;
      }
    } else {
      this.create_walls_online();  // 在线生成地图
    }
    this.add_listening_events();
  }

  update_size() {  // 每一帧更新地图大小
    this.L = parseInt(Math.min(this.parent.clientWidth / this.cols, this.parent.clientHeight / this.rows));
    this.ctx.canvas.width = this.L * this.cols;
    this.ctx.canvas.height = this.L * this.rows;
  }

  check_ready() {  // 判断两条蛇是否都准备好下一回合了
    for (const snake of this.snakes) {
      if (snake.status !== "idle" || snake.direction === -1) return false;
    }
    return true;
  }

  next_step() {  // 让两条蛇进入下一回合
    for (const snake of this.snakes) {
      snake.next_step();
    }
  }

  check_next_valid(cell) {  // 检测目标格子是否合法
    for (const wall of this.walls) {  // 枚举障碍物
      if (wall.r == cell.r && wall.c == cell.c)
        return false;
    }

    for (const snake of this.snakes) {  // 枚举蛇的身体
      let k = snake.cells.length;
      if (!snake.check_tail_increasing()) {  // 蛇尾会前进时不检测碰撞蛇尾
        k--;
      }
      for (let i = 0; i < k; i++) {
        if (snake.cells[i].r === cell.r && snake.cells[i].c === cell.c)
          return false;
      }
    }

    return true;
  }

  update() {
    this.update_size();

    if (this.check_ready()) {
      this.next_step();
    }

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
