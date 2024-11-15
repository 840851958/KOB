package com.kob.botrunningsystem.Service.impl;

import com.kob.botrunningsystem.Service.BotRunningService;
import com.kob.botrunningsystem.Service.impl.utils.BotPool;
import org.springframework.stereotype.Service;

@Service
public class BotRunningServiceImpl implements BotRunningService {
    public final static BotPool botpool = new BotPool();
    @Override
    public String addBot(Integer userId, String botCode, String input) {
        System.out.println("add bot: " + userId + " " + botCode + " " + input);
        botpool.addBot(userId, botCode, input);
        return "add bot success";
    }
}
