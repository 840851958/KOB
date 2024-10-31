package org.kob.backend.service.impl.bot;

import org.kob.backend.mapper.BotMapper;
import org.kob.backend.pojo.Bot;
import org.kob.backend.pojo.User;
import org.kob.backend.service.impl.util.UserDetailsImpl;
import org.kob.backend.service.user.bot.AddService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AddServiceImpl implements AddService {
    @Autowired
    private BotMapper botMapper;

    @Override
    public Map<String, String> add(Map<String, String> data) {
        // 将bot添加到用户中，首先要获取user
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = 
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) usernamePasswordAuthenticationToken.getPrincipal();
        User user = userDetails.getUser();

        String title = data.get("title");
        String description = data.get("description");
        String content = data.get("content");
        Map<String,String> map = new HashMap<>();

        if (title.isEmpty()){
            map.put("error_message", "标题不能为空");
            return map;
        }
        if (title.length() > 100){
            map.put("error_message", "标题不能太长");
            return map;
        }
        if (description.isEmpty()){
            description = "用户还未添加任何描述";
        }
        if (description.length() > 300){
            map.put("error_message", "描述信息不能太长");
            return map;
        }
        if (content.isEmpty()){
            map.put("error_message", "代码不能为空");
            return map;
        }
        if (content.length() > 10000){
            map.put("error_message", "代码长度不能超过10000");
            return map;
        }
        Date now = new Date();
        Bot bot = new Bot(null,user.getId(),title,description,content,1500,now,now);

        botMapper.insert(bot);
        map.put("error_message", "success");
        return map;
    }
}
