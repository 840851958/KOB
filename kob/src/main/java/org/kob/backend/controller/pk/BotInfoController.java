package org.kob.backend.controller.pk;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pk/")
public class BotInfoController {
    @RequestMapping("getbotinfo")
    public Map<String,String> getBotInfo(){
        List<Map<String,String>> list = new ArrayList<>();
        Map<String,String> bot1 = new HashMap<>();
        Map<String,String> bot2 = new HashMap<>();
        bot1.put("name", "dogs");
        bot1.put("rating", "1000");
        return bot1;
    }

}
