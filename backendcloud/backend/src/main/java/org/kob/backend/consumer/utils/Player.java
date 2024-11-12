package org.kob.backend.consumer.utils;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    private Integer id;
    private Integer sx, sy;
    private List<Integer> steps;


    private boolean check_tail_increasing(int step){ // 检测当前回合蛇的长度是否增加
        if (step <= 10) {
            return true;
        }
        return step % 3 == 1;
    }
    public List<Cell> getCells(){
        List<Cell> res = new ArrayList<>(); // 定义蛇的身体
        int[] dx = {-1,0,1,0}, dy = {0,1,0,-1};
        int x = sx, y = sy;
        int step = 0; // 回合数
        res.add(new Cell(x,y));
        for(int d : steps){
            x += dx[d];
            y += dy[d];
            res.add(new Cell(x,y));
            if (!check_tail_increasing(++step)){
                res.remove(0);
            }
        }
        return res;
    }
    public String getStepsString() {
        StringBuilder res = new StringBuilder();
        for (int d : steps){
            res.append(d);
        }
        return res.toString();
    }
}
