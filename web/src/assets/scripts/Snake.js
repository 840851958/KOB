import { AcGameObject } from "./AcGameObjects";
import { Cell } from "./Cell";

export class Snake extends AcGameObject {
    constructor(info,gamemap){
        super();

        this.id = info.id;
        this.color = info.color;
        this.gamemap = gamemap;

        this.cells  = [new Cell(info.r, info.c)];//存放蛇的身体，cell[0]为蛇头
        this.next_cell = null;
        this.speed = 5;
        this.direction = -1; // -1表示没有动作，0，1，2，3分别表示上右下左
        this.status = "idle"; // idle表示静止，还有move、die两个状态
        
        this.dr = [-1,0,1,0]; // rows移动偏移量
        this.dc = [0,1,0,-1]; // cols移动偏移量

        this.step = 0; // 表示回合数，每过若干回合蛇变长
        this.eps = 1e-2; // 允许0.01的误差 

        this.eye_direction = 0;
        if(this.id === 1) this.eye_direction = 2; // 两条蛇眼睛初始方向一个上一个下


        // 定义不同方向两只眼睛的偏移量
        this.eye_dx = [
            [-1,1],
            [1,1],
            [1,-1],
            [-1,-1],
        ];
        this.eye_dy = [
            [-1,-1],
            [-1,1],
            [1,1],
            [1,-1],
        ]
    }

    start() {

    }

    set_direction(d){
        // 设置接口，获取方向
        this.direction = d;
    }

    check_tail_increasing(){
        // 判断蛇尾是否增加,前十回合每回合加一，后面每三回合加一
        if(this.step <= 10) return true;
        if(this.step % 3 === 1) return true;
        return false;
    }

    next_step(){
        // 将蛇的状态变为移动
        const d = this.direction;
        // 更新眼睛方向
        this.eye_direction = d;
        this.next_cell = new Cell(this.cells[0].r + this.dr[d], this.cells[0].c + this.dc[d]);
        this.direction = -1; // 清空操作
        this.status = "move";
        this.step++;
        
        const k = this.cells.length; // 记录蛇的长度
        for (let i = k; i > 0; i--) {
            this.cells[i] = JSON.parse(JSON.stringify(this.cells[i-1]));
            
        }

        if(!this.gamemap.check_valid(this.next_cell)){// 在下一步操作蛇撞了，死掉
            this.status = "die"; 
        }
    }

    update_move(){
        const dx = this.next_cell.x - this.cells[0].x;
        const dy = this.next_cell.y - this.cells[0].y;
        const distance = Math.sqrt(dx*dx + dy*dy);

        if(distance < this.eps){
            // 走到目标点
            this.cells[0] = this.next_cell; // 添加一个新的蛇头
            this.next_cell = null;
            this.status = "idle";
            if(!this.check_tail_increasing()){ // 走到目标点时如果不变长，直接砍掉
                this.cells.pop(); 
            }
        }else{
            const move_distance = this.speed * this.timedelta / 1000; // 每两帧之间走过的距离
            this.cells[0].x += move_distance * dx/distance;
            this.cells[0].y += move_distance * dy/distance;

            if(!this.check_tail_increasing()){
                // 如果蛇长度不加，需要尾巴移动
                const k = this.cells.length;
                const tail = this.cells[k-1], target_tail = this.cells[k-2];
                const tail_dx = target_tail.x - tail.x;
                const tail_dy = target_tail.y - tail.y;
                // 由于总长度不变、所以md和d不变，而dx和tail_dx在拐弯时会变化
                tail.x += move_distance * tail_dx/distance; 
                tail.y += move_distance * tail_dy/distance;
            }
        }
    }

    update() {  // 每帧更新一次，一秒60次
        if(this.status === "move"){
            this.update_move();
        }
        this.render();
    }

    render() {
        const L = this.gamemap.L;
        const ctx = this.gamemap.ctx;// ? 

        ctx.fillStyle = this.color;
        if(this.status === "die"){
            ctx.fillStyle = "white";
        }
        for(const cell of this.cells){
            ctx.beginPath();
            ctx.arc(cell.x * L, cell.y * L , L/2 * 0.8, 0, Math.PI * 2);
            ctx.fill();
        }
        
        for(let i = 1; i < this.cells.length; i++){
            const a = this.cells[i-1], b = this.cells[i];
            if(Math.abs(a.x - b.x) < this.eps && Math.abs(a.y - b.y) < this.eps)
                continue;
            // 填充y，并且让圆和矩形小点，填满格子不好看
            if(Math.abs(a.x - b.x) < this.eps){
                ctx.fillRect((a.x-0.5 + 0.1)*L, Math.min(a.y, b.y) * L, L*0.8, Math.abs(a.y-b.y) * L);
            } else {
                //填充x部分
                ctx.fillRect(Math.min(a.x, b.x) * L, (a.y-0.5 + 0.1) * L, Math.abs(a.x - b.x) * L, L*0.8);
            }
        }

        ctx.fillStyle = "black";
        for(let i = 0; i < 2; i++){
            // i 表示左眼和右眼，对应上了eye_dx的二维数组
            const eye_x = (this.cells[0].x + this.eye_dx[this.eye_direction][i] * 0.15) * L;
            const eye_y = (this.cells[0].y + this.eye_dy[this.eye_direction][i] * 0.15) * L ;

            ctx.beginPath();
            ctx.arc( eye_x, eye_y, L * 0.05, 0, Math.PI * 2)
            ctx.fill();
        }

    }


}