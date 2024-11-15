package org.kob.backend.consumer;

import com.alibaba.fastjson.JSONObject;
import org.kob.backend.consumer.utils.Game;
import org.kob.backend.consumer.utils.JwtAuthentication;
import org.kob.backend.mapper.BotMapper;
import org.kob.backend.mapper.RecordMapper;
import org.kob.backend.mapper.UserMapper;
import org.kob.backend.pojo.Bot;
import org.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾
public class WebSocketServer {
    // 需要根据用户ID找到每一个用户对应的链接是谁，所以需要定义一个全局变量存储所有的链接，将userId映射到每一个链接
    final public static ConcurrentHashMap<Integer,WebSocketServer> users = new ConcurrentHashMap<>(); // ConcurrentHashMap是线程安全的哈希表
    // 定义一个匹配池，并用final修饰，即内容可变但地址不可变
// matchpool相关操作迁移到匹配系统中
    //    private final static CopyOnWriteArraySet<User> matchpool = new CopyOnWriteArraySet<>(); // CopyOnWriteArraySet是线程安全的集合
    private User user;
    private Session session;

    // 查询数据库要用到mapper层
    private static UserMapper userMapper;
    public static RecordMapper recordMapper;
    private static BotMapper botMapper;
    public static RestTemplate restTemplate;
    public Game game = null;
    private final static String addPlayerUrl = "http://127.0.0.1:3001/player/add/";
    private final static String removePlayerUrl = "http://127.0.0.1:3001/player/remove/";
    @Autowired
    public void setUserMapper(UserMapper userMapper){
        WebSocketServer.userMapper = userMapper;
    }
    @Autowired
    public void setRecordMapper(RecordMapper recordMapper){
        WebSocketServer.recordMapper = recordMapper;
    }
    @Autowired
    public void setRestTemplate(RestTemplate restTemplate){
        WebSocketServer.restTemplate = restTemplate;
    }
    @Autowired
    public void setBotMapper(BotMapper botMapper){
        WebSocketServer.botMapper = botMapper;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        // 建立连接
        this.session = session;
        System.out.println("connected!");
        // 根据token获取用户id
        Integer userId = JwtAuthentication.getuserId(token);
        this.user = userMapper.selectById(userId);
        if (this.user != null){
            users.put(userId, this);
        }else {
            this.session.close();
        }
        //System.out.println(users);

    }

    @OnClose
    public void onClose() {
        // 关闭链接
        System.out.println("disconnected!");
        if (this.user != null){
            users.remove(this.user.getId());
        }
//        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
//        data.add("user_id", this.user.getId().toString());
//        restTemplate.postForObject(removePlayerUrl, data, String.class);
    }

    public static void startGame(Integer aId, Integer aBotId, Integer bId, Integer bBotId){
        User a = userMapper.selectById(aId), b = userMapper.selectById(bId);
        Bot botA = botMapper.selectById(aBotId), botB = botMapper.selectById(bBotId);
        Game game = new Game(
                13,
                14,
                20,
                a.getId(),
                botA,
                b.getId(),
                botB);
        game.createMap();
        // 初始化两名玩家的game
        if (users.get(a.getId()) != null){
            users.get(a.getId()).game = game; // 当玩家不点击取消而直接结束页面进程时，服务器不会删除匹配信息，但这名玩家已经不在了，因此不会发送消息，进而报错，所以需要判空
        }
        if (users.get(b.getId()) != null){
            users.get(b.getId()).game = game;
        }

        game.start(); // start函数是Thread的API，另起一个线程执行

        JSONObject respGame = new JSONObject();// 由于地图信息越来越多，可以单独封装为一个JSON，
        respGame.put("a_id", game.getPlayerA().getId());
        respGame.put("a_sx", game.getPlayerA().getSx());
        respGame.put("a_sy", game.getPlayerA().getSy());
        respGame.put("b_id", game.getPlayerB().getId());
        respGame.put("b_sx", game.getPlayerB().getSx());
        respGame.put("b_sy", game.getPlayerB().getSy());
        respGame.put("map", game.getG());

        JSONObject respA = new JSONObject();
        respA.put("event", "start_matching");
        respA.put("opponent_username", b.getUsername());
        respA.put("opponent_photo", b.getPhoto());
        respA.put("game", respGame);
        if (users.get(a.getId()) != null){
            users.get(a.getId()).sendMessage(respA.toJSONString());
        }

        JSONObject respB = new JSONObject();
        respB.put("event", "start_matching");
        respB.put("opponent_username", a.getUsername());
        respB.put("opponent_photo", a.getPhoto());
        respB.put("game", respGame);
        if (users.get(b.getId()) != null){
            users.get(b.getId()).sendMessage(respB.toJSONString()); // sendMessage会将后端消息发送给前端socket的onmessage msg中
        }
    }
    private void startMatching(Integer botId){ // 向匹配系统发请求
        System.out.println("start matching");
        MultiValueMap<String,String> data = new LinkedMultiValueMap<>();
        data.add("userId", this.user.getId().toString());
        data.add("rating", this.user.getRating().toString());
        data.add("bot_id", botId.toString());
        restTemplate.postForObject(addPlayerUrl, data, String.class); // 反射，通过该方法可以将Java的String序列化为JSON文件，然后内部反序列为String，使得可以处理多种类型的数据
    }
    private void stopMatching(){
        System.out.println("stop matching");
        MultiValueMap<String,String> data = new LinkedMultiValueMap<>();
        data.add("userId", this.user.getId().toString());
        restTemplate.postForObject(removePlayerUrl, data, String.class);
    }

    private void move(int direction){
        if (game.getPlayerA().getId().equals(user.getId())){
            if (game.getPlayerA().getBotId().equals(-1)){ // 亲自出马
                game.setNextStepA(direction);
            }
        }
        if (game.getPlayerB().getId().equals(user.getId())) {
            if (game.getPlayerB().getBotId().equals(-1)){
            game.setNextStepB(direction);
            }
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) { // 一般将onMessage看作一个路由，即在其中判断将一个任务交给谁
        // 从Client接收消息
        System.out.println("receive message!");
        JSONObject data = JSONObject.parseObject(message);
        String event = data.getString("event");
        if ("start-matching".equals(event)){
            startMatching(data.getInteger("bot_id"));
        }else if ("stop-matching".equals(event)){
            stopMatching();
        } else if ("move".equals(event)) {
            move(data.getInteger("direction"));
        }

    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    //
    public void sendMessage(String message){
        synchronized (this.session){
            try {
                this.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
