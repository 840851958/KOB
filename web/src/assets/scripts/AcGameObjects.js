const Ac_Game_Objects = [];
// 实现运动的基类
export class AcGameObject {
    // 构造函数，传递值
    constructor() {
        Ac_Game_Objects.push(this);
        this.timedelta = 0;
        this.has_called_start = false;

    }
    // 启动只执行一次
    start(){

    }
    // 除第一帧外，每一帧都执行一次刷新
    update() {
        
    }
    // 删除之前执行
    on_destroy(){

    }

    destroy(){
        this.on_destroy();

        for(let i in Ac_Game_Objects){
            // 取出当前元素，匹配的话就删除
            const obj = Ac_Game_Objects[i];
            if (obj == this) {
                Ac_Game_Objects.splice(i);
                break;
            }
        }
    } 
}
// 更新时间
let last_timestamp = null; 
const step = timestamp => {
    // in遍历下标 of遍历值
    for(let obj of Ac_Game_Objects){
        if(!obj.has_called_start){
            obj.start();
            obj.has_called_start = true;
        }else{
            obj.update();
            obj.timedelta = timestamp - last_timestamp;
        }
    }
    // 迭代时间
    last_timestamp = timestamp;
    // 每一步都执行一次请求
    requestAnimationFrame(step);
}
// 在下一帧执行step函数。
requestAnimationFrame(step);