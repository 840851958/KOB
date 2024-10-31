package org.kob.backend.service.impl.bot;

import org.kob.backend.mapper.BotMapper;
import org.kob.backend.pojo.Bot;
import org.kob.backend.pojo.User;
import org.kob.backend.service.impl.util.UserDetailsImpl;
import org.kob.backend.service.user.bot.UpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UpdateServiceImpl implements UpdateService {
    // 取bot涉及到bot的查询，通过mapper实现bot相关操作
    @Autowired
    private BotMapper botMapper;
    @Override
    public Map<String, String> update(Map<String, String> data) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) usernamePasswordAuthenticationToken.getPrincipal();
        User user = userDetails.getUser();

        int bot_id = Integer.parseInt(data.get("bot_id"));

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
        Bot bot = botMapper.selectById(bot_id);
        if (bot == null){
            map.put("error_message", "bot不存在或已被删除");
            return map;
        }
        if (!bot.getUserId().equals(user.getId())){
            map.put("error_message","没有权限修改Bot");
            return map;
        }

        Bot new_bot = new Bot(
                bot.getId(),
                user.getId(),
                title,
                description,
                content,
                bot.getRating(),
                bot.getCreatetime(),
                new Date()
        );
        botMapper.updateById(new_bot);
        map.put("error_message", "success");
        return map;
    }
}
