package org.kob.backend.service.impl.bot;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.kob.backend.mapper.BotMapper;
import org.kob.backend.pojo.Bot;
import org.kob.backend.pojo.User;
import org.kob.backend.service.impl.util.UserDetailsImpl;
import org.kob.backend.service.user.bot.RemoveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RemoveServiceImpl implements RemoveService {
    @Autowired
    private BotMapper botMapper;

    @Override
    public Map<String, String> remove(Map<String, String> data) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) usernamePasswordAuthenticationToken.getPrincipal();
        User user = userDetails.getUser();
        // 接受前端过来的想要删除的bot id
        int bot_id = Integer.parseInt(data.get("bot_id"));
        // 通过id找到对应的bot
        Bot bot = botMapper.selectById(bot_id);

        Map<String,String> map = new HashMap<>();
        // 判断bot有没有、是不是
        if (bot == null){
            map.put("error_message", "您的Bot不存在或已被删除");
            return map;
        }
        if (!bot.getUserId().equals(user.getId())){
            map.put("error_message", "没有权限删除");
            return map;
        }
        botMapper.deleteById(bot_id);
        map.put("error_message", "success");
        return map;
    }
}
