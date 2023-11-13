import { AcGameObject } from "./AcGameObject";
import { Cell } from "./Cell";

export class Snake extends AcGameObject {
  constructor(info, gamemap) {
    super();

    this.id = info.id; // 每条蛇有唯一的id进行区分
    this.color = info.color; // 颜色
    this.gamemap = gamemap;

    this.cells = [new Cell(info.r, info.c)];  // 初始化时只有一个点即cells[0]表示蛇头
    this.radius = 0.4;  // 蛇中每个节点的半径

    this.speed = 2;  // 蛇每秒走2个格子
    this.direction = -1;  // 下一步移动的指令，-1表示没有指令，0、1、2、3分别表示上、右、下、左
    this.status = "idle";  // 蛇的状态，idle表示静止，move表示正在移动，die表示死亡
    this.next_cell = null;  // 下一步的目标位置
    this.step = 0;  // 回合数

    this.dr = [-1, 0, 1, 0];
    this.dc = [0, 1, 0, -1];

    this.eps = 0.01;  // 误差

    this.eye_color = "black";
    this.eye_radius = 0.05;
    this.eye_direction = 0;  // 蛇头朝向的方向，默认左下角的蛇朝上，右上角的蛇朝下
    if (this.id === 1) this.eye_direction = 2;
    this.eye_dx = [  // 四个方向蛇眼睛的偏移量，x是横轴
      [-1, 1],
      [1, 1],
      [1, -1],
      [-1, -1],
    ];
    this.eye_dy = [
      [-1, -1],
      [-1, 1],
      [1, 1],
      [1, -1],
    ];
  }

  start() {}

  next_step() {  // 将蛇的状态变为走下一步
    const d = this.direction;
    this.next_cell = new Cell(this.cells[0].r + this.dr[d], this.cells[0].c + this.dc[d]);
    this.eye_direction = d;  // 更新蛇头朝向
    this.direction = -1;  // 复原
    this.status = "move";
    this.step++;

    const k = this.cells.length;
    for (let i = k; i > 0; i--) {  // 将所有节点向后移动一位，因为要在头节点前面插入新的头节点
      this.cells[i] = JSON.parse(JSON.stringify(this.cells[i - 1]));  // 注意要深层复制一份，还有一个细节是JS的数组越界不会出错
    }

    if (!this.gamemap.check_next_valid(this.next_cell)) {  // 下一步不合法
      this.status = "die";
      this.color = "white";
    }
  }

  set_direction(d) {  // 由于未来不一定只会从键盘获取输入，因此实现一个接口修改direction
    this.direction = d;
  }

  check_tail_increasing() {  // 检测当前回合蛇的长度是否增加
    if (this.step <= 7) return true;  // 前7回合每一回合长度都增加
    if (this.step % 3 === 1) return true;  // 之后每3回合增加一次长度
    return false;
  }

  update_move() {  // 将头节点cells[0]向目标节点next_cell移动
    const dx = this.next_cell.x - this.cells[0].x;  // 在x方向上与目的地的偏移量
    const dy = this.next_cell.y - this.cells[0].y;  // 在y方向上与目的地的偏移量
    const distance = Math.sqrt(dx * dx + dy * dy);  // 与目的地的距离
    if (distance < this.eps) {  // 已经走到目标点
      this.status = "idle";  // 状态变为静止
      this.cells[0] = this.next_cell;  // 将头部更新为目标点
      this.next_cell = null;

      if (!this.check_tail_increasing()) {  // 尾部没有变长则移动完成后要删去尾部
        this.cells.pop();
      }
    } else {
      const move_length = this.speed * this.timedelta / 1000;  // 每一帧移动的距离
      const cos_theta = dx / distance;  // cos值
      const sin_theta = dy / distance;  // sin值
      this.cells[0].x += move_length * cos_theta;
      this.cells[0].y += move_length * sin_theta;

      if (!this.check_tail_increasing()) {
        const k = this.cells.length;
        const tail = this.cells[k - 1], tail_target = this.cells[k - 2];
        const tail_dx = tail_target.x - tail.x;
        const tail_dy = tail_target.y - tail.y;
        tail.x += move_length * tail_dx / distance;  // 此处就不分开计算cos和sin了
        tail.y += move_length * tail_dy / distance;
      }
    }
  }

  update() {
    if (this.status === "move") {  // 只有移动状态才执行update_move函数
      this.update_move();
    }
    this.render();
  }

  render() {
    const L = this.gamemap.L;
    const ctx = this.gamemap.ctx;
    ctx.fillStyle = this.color;
    for (const cell of this.cells) {
      ctx.beginPath();
      ctx.arc(cell.x * L, cell.y * L, L * this.radius, 0, Math.PI * 2);
      ctx.fill();
    }

    // 将相邻的两个球连在一起
    for (let i = 1; i < this.cells.length; i++) {
      const a = this.cells[i - 1], b = this.cells[i];
      if (Math.abs(a.x - b.x) < this.eps && Math.abs(a.y - b.y) < this.eps)
        continue;
      if (Math.abs(a.x - b.x) < this.eps) {  // 上下排列，即x相同，左上角的点的y值为两者的最小值，因为越往上y越小
        ctx.fillRect((a.x - this.radius) * L, Math.min(a.y, b.y) * L, 2 * this.radius * L, Math.abs(a.y - b.y) * L);
      } else {  // 左右排列，画法同理
        ctx.fillRect(Math.min(a.x, b.x) * L, (a.y - this.radius) * L, Math.abs(a.x - b.x) * L, 2 * this.radius * L);
      }
    }

    ctx.fillStyle = this.eye_color;
    for (let i = 0; i < 2; i++) {  // 绘制两个眼睛
      const eye_x = this.cells[0].x + this.eye_dx[this.eye_direction][i] * this.radius * 0.4;  // 偏移距离为半径长度的0.4倍
      const eye_y = this.cells[0].y + this.eye_dy[this.eye_direction][i] * this.radius * 0.4;
      ctx.beginPath();
      ctx.arc(eye_x * L, eye_y * L, this.eye_radius * L, 0, Math.PI * 2);
      ctx.fill();
    }
  }
}
