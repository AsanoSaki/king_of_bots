const AC_GAME_OBJECTS = []; // 存储所有运动对象

export class AcGameObject {
  constructor() {
    AC_GAME_OBJECTS.push(this);
    this.timedelta = 0;  // 当前帧距离上一帧的时间间隔，浏览器每一帧时间间隔可能有误差不一样因此需要记录
    this.has_called_start = false;  // 是否执行过start函数
  }

  start() {  // 只在对象创建时执行一次
  }

  update() {  // 除了第一帧外每一帧执行一次
  }

  on_destroy() {  // 在删除对象之前可能需要执行的某些操作
  }

  destroy() {  // 将当前对象删除，即从AC_GAME_OBJECTS中移除
    this.on_destroy();

    for (let i in AC_GAME_OBJECTS) {  // 用in遍历下标
      const obj = AC_GAME_OBJECTS[i];
      if (obj == this) {
        AC_GAME_OBJECTS.splice(i);
        break;
      }
    }
  }
}

let last_timestamp;  // 上一帧执行的时刻

const step = (timestamp) => {  // 每次调用会传入当前时刻
  for (let obj of AC_GAME_OBJECTS) {  // 用of遍历值
    if (!obj.has_called_start) {
      obj.start();
      obj.has_called_start = true;
    } else {
      obj.timedelta = timestamp - last_timestamp;
      obj.update();
    }
  }
  last_timestamp = timestamp;
  requestAnimationFrame(step);  // 递归调用
}

requestAnimationFrame(step);
