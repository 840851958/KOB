import { AcGameObject } from "@/assets/scripts/AcGameObjects";
import { Wall } from "./Wall";
import { Snake } from "./Snake";

export class GameMap extends AcGameObject {
    constructor(ctx, parent){
        super();
        this.ctx = ctx;
        this.parent = parent;
        this.L = 0;

        // 这样做是为了防止两条蛇同时进入一个点中造成误判。通过判断坐标和奇偶的方式调整，使得一条蛇的运动轨迹为奇偶奇，另一条蛇为偶奇偶。
        this.cols = 13;
        this.rows = 14;
        
        this.inner_walls = 20; 
        this.walls = [];

        this.snakes = [
            new Snake({id: 0, color: "#4876EC", r: this.rows-2, c: 1},this),
            new Snake({id: 1, color: "#F94848", r: 1, c: this.cols-2},this),    
        ];
    }

    //检查连通性
    check_connectivity(g,sx,sy,tx,ty){
        // 递归终止条件
        if(sx == tx && sy == ty) return true;
        // 标记已经走过的位置
        g[sx][sy] = true;
        let dx = [-1,0,1,0], dy = [0,1,0,-1];
        for(let i = 0; i < 4; i++){
            // 求当前位置的运动可能性
            let x = sx + dx[i];
            let y = sy + dy[i];
            // 递归
            if(!g[x][y] && this.check_connectivity(g,x,y,tx,ty)) 
                return true;
        }
        return false;
    }

    creat_walls(){
        // 布尔数组的作用,存储并判断是否有墙
        const g = [];
        for(let r = 0; r < this.rows; r++){
            g[r] = [];
            for(let c = 0; c < this.cols; c++){
                g[r][c] = false;
            }
        }
        //将四周加入布尔数组中
        for(let r = 0; r < this.rows; r++){
            g[r][0] = g[r][this.cols-1] = true;
        }
        for (let c = 0; c < this.cols; c++) {
            g[0][c] = g[this.rows-1][c] = true;
        }
        //创建随机障碍物
        for(let i = 0; i < this.inner_walls / 2; i++){
            for(let j = 0; j < 1000; j++){
                let r = parseInt(Math.random() * this.rows);
                let c = parseInt(Math.random() * this.cols);
                // 中心对称创建
                if(g[r][c] || g[this.rows-1-r][this.cols-1-c]) continue;
                // 轴对称创建 if(g[r][c] || g[c][r]) continue;
                // 防止出生点被墙覆盖
                if(r == 1 && c == this.cols-2 || r == this.rows-2 && c == 1) continue;
                // 将两个对称位置创建墙
                g[r][c] = g[this.rows-1-r][this.cols-1-c] = true;
                break;
            }
        }
        const copy_g = JSON.parse(JSON.stringify(g));
        if(!this.check_connectivity(copy_g,this.rows-2,1,1,this.cols-2)) return false;
        //加上障碍物
        for(let r = 0; r < this.rows; r++){
            for(let c = 0; c < this.cols; c++){
                if (g[r][c]) {
                    this.walls.push(new Wall(r,c,this)); 
                }
            }
        }
        return true;


    }
    add_listening_events(){
        this.ctx.canvas.focus(); // 聚焦画布
        const [snake0, snake1] = this.snakes;
        this.ctx.canvas.addEventListener("keydown", e => {
            if(e.key === 'w') snake0.set_direction(0);
            else if(e.key === 'd') snake0.set_direction(1);
            else if(e.key === 's') snake0.set_direction(2);
            else if(e.key === 'a') snake0.set_direction(3);
            else if(e.key === 'ArrowUp') snake1.set_direction(0);
            else if(e.key === 'ArrowRight') snake1.set_direction(1);
            else if(e.key === 'ArrowDown') snake1.set_direction(2);
            else if(e.key === 'ArrowLeft') snake1.set_direction(3);
        });

    }

    start(){
        for(let i = 0; i < 1000; i++){
            if (this.creat_walls())
                break; 
        }
        this.add_listening_events();
        
    }

    update_size(){
        // 取Int是为了让后面计算宽高时是整像素，防止出现分割缝隙；
        this.L = parseInt(Math.min(this.parent.clientWidth / this.cols, this.parent.clientHeight / this.rows));
        this.ctx.canvas.width = this.L * this.cols;
        this.ctx.canvas.height = this.L * this.rows;
    }
    check_ready() {
        // 判断两条蛇是否都准备好下一步了
        for(const snake of this.snakes){
            // js要用三个 "=" 判断
            if(snake.status !== "idle") return false;
            if(snake.direction === -1) return false;

        }
        return true;
    }
    next_step(){
        // 遍历让两条蛇进入下一回合
        for(const snake of this.snakes){
            snake.next_step();
        }
    }

    check_valid(cell){
        // 检测目标位置是否合法：不撞墙、不相撞、不衔尾
        for(const wall of this.walls){
            if(wall.r === cell.r && wall.c === cell.c)
                return false;
        }

        for(const snake of this.snakes){
            let k = snake.cells.length;
            if(!snake.check_tail_increasing){
                // ? k--保证当尾部移动时（总长不增加），蛇头蛇尾不会相撞，不需去遍历蛇尾？
                k--;
            }
            // ?
            for(let i = 0; i < k; i++){
                if(snake.cells[i].r === cell.r && snake.cells[i].c === cell.c)
                    return false;
            }
        }
        return true;
    }

    update(){
        this.update_size(); 
        if(this.check_ready()){
            this.next_step();
        }
        // 每一帧都要渲染
        this.render();
        
    }

    render(){
        const color_even = "#AAD751", color_odd = "#A2D149";
        for (let r = 0; r < this.rows; r++) {
            for(let c = 0; c < this.cols; c++){
                if ((r+c) % 2 == 0) {
                    this.ctx.fillStyle = color_even;
                }else{
                    this.ctx.fillStyle = color_odd;
                }
                this.ctx.fillRect(r * this.L, c * this.L, this.L, this.L);
            }
            
        }
    }
}