package org.kob.backend.consumer;
import org.kob.backend.mapper.UserMapper;
import org.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾
public class WebSocketServer {
    // 需要根据用户ID找到每一个用户对应的链接是谁，所以需要定义一个全局变量存储所有的链接，将userId映射到每一个链接
    private static ConcurrentHashMap<Integer,WebSocketServer> users = new ConcurrentHashMap<>(); // 线程安全的哈希表
    private User user;
    private Session session;

    // 查询数据库要用到mapper层
    private static UserMapper userMapper;
    @Autowired
    public void setUserMapper(UserMapper userMapper){
        WebSocketServer.userMapper = userMapper;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        // 建立连接
        this.session = session;
        System.out.println("connected!");
        // 根据token获取用户id
        int userId = Integer.parseInt(token);
    }

    @OnClose
    public void onClose() {
        // 关闭链接
        System.out.println("disconnected!");
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 从Client接收消息
        System.out.println("receive message!");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

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
