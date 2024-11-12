package org.kob.backend.consumer.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.ConfigurationKeys;
import lombok.Getter;
import lombok.Setter;
import org.kob.backend.consumer.WebSocketServer;
import org.kob.backend.pojo.Record;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Game extends Thread{
    private final Integer rows;
    private final Integer cols;
    private final Integer inner_walls_count;
    private final Player playerA, playerB;

    final private int[][] g;
    final private int[] dx = {-1,0,1,0}, dy = {0,1,0,-1};
    private Integer nextStepA = null;
    private Integer nextStepB = null;
    private ReentrantLock lock = new ReentrantLock();
    private String status = "playing"; // playing -> finished
    private String loser = "";

    public Game(Integer rows,Integer cols, Integer inner_walls_count, Integer idA, Integer idB){
        this.rows = rows;
        this.cols = cols;
        this.inner_walls_count = inner_walls_count;
        this.g = new int[rows][cols];
        playerB = new Player(idB,1,cols-2,new ArrayList<>());
        playerA = new Player(idA,rows-2,1,new ArrayList<>());
    }

    public Player getPlayerA() {
        return playerA;
    }

    public Player getPlayerB() {
        return playerB;
    }

    // 加锁防止多线程读写同一变量出现异常
    public void setNextStepA(Integer nextStepA) {
        lock.lock();
        try {
            this.nextStepA = nextStepA;
        }finally {
            lock.unlock();
        }
    }

    public void setNextStepB(Integer nextStepB) {
        lock.lock();
        try {
            this.nextStepB = nextStepB;
        }finally {
            lock.unlock();
        }
    }

    public int[][] getG(){
        return g;
    }


    private boolean check_connectivity(int sx, int sy, int tx, int ty){
        if (sx == tx && sy == ty){
            return true;
        }
        g[sx][sy] = 1;

        for (int i = 0; i < 4; i++){
            int x = sx+dx[i];
            int y = sy+dy[i];
            if (x >= 0 && x < this.rows && y >= 0 && y < this.cols && g[x][y] == 0){
                if (check_connectivity(x, y, tx, ty)){
                    g[sx][sy] = 0; // 由于g数组是全局变量，所以需要恢复现场
                    return true;
                }
            }
        }
        g[sx][sy] = 0;
        return false;
    }
    private boolean draw(){
        for (int i = 0; i < this.rows; i++){
            for(int j = 0; j < this.cols; j++){
                g[i][j] = 0;
            }
        }
        for(int r = 0; r < this.rows; r++){
            g[r][0] = g[r][this.cols-1] = 1;
        }
        for (int c = 0; c < this.cols; c++){
            g[0][c] = g[this.rows-1][c] = 1;
        }
        Random random = new Random();
        for (int i = 0; i < inner_walls_count / 2; i++){
            for (int j = 0; j < 1000; j++){
                int r = random.nextInt(this.rows);
                int c = random.nextInt(this.cols);
                if (g[r][c] == 1 || g[this.rows-1-r][this.cols-1-c] == 1) {
                    continue;
                }
                if (r == 1 && c == this.cols-2 || r == this.rows-2 && c == 1){
                    continue;
                }
                g[r][c] = g[this.rows-1-r][this.cols-1-c] = 1;
                break;
            }
        }
        return check_connectivity(this.rows-2, 1, 1, this.cols-2);
    }
    public void createMap(){
        for (int i = 0; i < 1000; i++){
            if (draw()){
                break;
            }
        }

    }

    private boolean nextStep(){
        for (int i = 0; i < 50; i++){
            try {
                Thread.sleep(100); // 每0.1秒判断一次玩家是否输入了操作,预留了100*50ms的时间
                lock.lock();
                try {
                    if (nextStepA != null && nextStepB != null){
                        playerA.getSteps().add(nextStepA);
                        playerB.getSteps().add(nextStepB);
                        return true;
                    }
                }finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean check_valid(List<Cell> cellsA, List<Cell> cellsB){
        int n = cellsA.size();
        Cell cell = cellsA.get(n-1);
        if (g[cell.x][cell.y] == 1){
            return false;
        }
        for (int i = 0; i < n-1; i++){
            if (cellsA.get(i).x == cell.x && cellsA.get(i).y == cell.y){ // 自身相撞
                return false;
            }
        }
        for (int i = 0; i < n-1; i++){
            if (cellsB.get(i).x == cell.x && cellsB.get(i).y == cell.y){ // 和对手相撞
                return false;
            }
        }
        return true;
    }

    private void judge(){ // 判断两名玩家操作是否合法
        List<Cell> cellsA = playerA.getCells();
        List<Cell> cellsB = playerB.getCells();
        boolean validA = check_valid(cellsA, cellsB);
        boolean validB = check_valid(cellsB, cellsA);
        if (!validA || !validB){
            status = "finished";
            if (!validA && !validB){
                loser = "all";
            } else if (!validA) {
                loser = "A";
            }else {
                loser = "B";
            }
        }
    }
    private String getMapString(){
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < rows; i++){
            for (int j = 0; j < cols; j++){
                res.append(g[i][j]); // 二维数组转换为String
            }
        }
        return res.toString();
    }
    private void saveToDatabase(){
        Record record = new Record(
                null,
                playerA.getId(),
                playerA.getSx(),
                playerA.getSy(),
                playerB.getId(),
                playerB.getSx(),
                playerB.getSy(),
                playerA.getStepsString(),
                playerB.getStepsString(),
                getMapString(),
                loser,
                new Date()
        );
        WebSocketServer.recordMapper.insert(record);
    }

    private void sendResult(){ // 向双方公布结果
        JSONObject resp = new JSONObject();
        resp.put("event", "result"); // 先发送一个事件，说明传递什么信息
        resp.put("loser", loser);
        resp.put("a_direction", nextStepA);
        resp.put("b_direction", nextStepB);
        saveToDatabase();
        sendAllMessage(resp.toJSONString());

    }

    private void sendMove(){ // 向两个client传递移动信息
        lock.lock(); // 由于读入了nextstep，需要加锁
        try {
            JSONObject resp = new JSONObject();
            resp.put("event", "move");
            resp.put("a_direction", nextStepA);
            resp.put("b_direction", nextStepB);
            sendAllMessage(resp.toJSONString());
            nextStepA = nextStepB = null; // 恢复现场，清空操作
        }finally {
            lock.unlock();
        }
    }

    private void sendAllMessage(String message){
        if (WebSocketServer.users.get(playerA.getId()) != null){
            WebSocketServer.users.get(playerA.getId()).sendMessage(message); // 玩家的链接保存在users中，需要调用id获取，并向其发送信息
        }
        if (WebSocketServer.users.get(playerB.getId()) != null){
            WebSocketServer.users.get(playerB.getId()).sendMessage(message);
        }

    }
    // 新线程的入口函数就是run函数
    // 另起一个线程存储用户的游戏过程（操作执行，每名玩家都有思考时间），防止思考时间造成线程堵塞
    @Override
    public void run() {
        for (int i = 0; i < 1000; i++){ // 玩家的蛇最多填满13*14的地图
            if (nextStep()) {
                // 判断合法
                judge();
                if ("playing".equals(status)){ // 游戏进行且接收到双方的操作之后，需要广播对方的操作
                    sendMove();
                }else {
                    sendResult();
                    break;
                }
            }else {
                // 规定时间双方没有同时行动,产生胜负
                status = "finished";
                // 准备读时加锁，防止读取过程中AB的nextstep变化
                lock.lock();
                try {
                    if (nextStepA == null && nextStepB == null){
                        loser = "all";
                    } else if (nextStepA == null) {
                        loser = "A";
                    } else if (nextStepB == null) {
                        loser = "B";
                    }
                }finally {
                    lock.unlock();
                }
                // 广播结果
                sendResult();
                break;
            }
        }

    }
}
