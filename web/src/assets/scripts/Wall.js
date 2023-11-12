import { AcGameObject } from "./AcGameObject";

export class Wall extends AcGameObject {
  constructor(r, c, gamemap) {  // 需要传入第几行第几列以及GameMap
    super();

    this.r = r;
    this.c = c;
    this.gamemap = gamemap;
    this.color = "#B47226";
  }

  update() {
    this.render();
  }

  render() {
    const L = this.gamemap.L, ctx = this.gamemap.ctx;
    ctx.fillStyle = this.color;
    ctx.fillRect(this.c * L, this.r * L, L, L);
  }
}
