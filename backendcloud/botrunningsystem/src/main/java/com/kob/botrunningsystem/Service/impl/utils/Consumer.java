package com.kob.botrunningsystem.Service.impl.utils;

import com.kob.botrunningsystem.Service.BotRunningService;
import com.kob.botrunningsystem.utils.BotInterface;
import org.joor.Reflect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Random;
import java.util.UUID;
@Component
public class Consumer extends Thread{
    private Bot bot;
    private static RestTemplate restTemplate;
    private final static String receiveBotMoveUrl = "http://127.0.0.1:3000/pk/receive/bot/move/";
    @Autowired
    public void setRestTemplate(RestTemplate restTemplate){
        Consumer.restTemplate = restTemplate;
    }
    public void startTimeout(long timeout, Bot bot){ // 防止用户提供的代码死循环
        this.bot = bot;
        this.start();
        try {
            this.join(timeout); // 最多等待timeout秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            this.interrupt(); // 中断当前线程
        }
    }

    private String addUid(String code, String uid){ // 给每个类加上uid，保证单独性
        int k = code.indexOf(" implements com.kob.botrunningsystem.utils.BotInterface");
        //System.out.println(k);
        return code.substring(0, k) + uid + code.substring(k);
    }

    @Override
    public void run() {
        UUID uuid = UUID.randomUUID();
        String uid = uuid.toString().substring(0,8);

        BotInterface botInterface = Reflect.compile( // 可以动态编译字符串
                "com.kob.botrunningsystem.utils.Bot" + uid,
                addUid(bot.getBotCode(), uid)
                ).create().get();
        Integer direction = botInterface.nextMove(bot.getInput());
        System.out.println("move-direction: " + bot.getUserId() + " " + direction);

        MultiValueMap<String,String> data = new LinkedMultiValueMap<>();
        data.add("user_id", bot.getUserId().toString());
        data.add("direction", direction.toString());
        restTemplate.postForObject(receiveBotMoveUrl, data, String.class);
    }
}
