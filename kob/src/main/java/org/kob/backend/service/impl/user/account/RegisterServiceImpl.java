package org.kob.backend.service.impl.user.account;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.kob.backend.mapper.UserMapper;
import org.kob.backend.pojo.User;
import org.kob.backend.service.user.account.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegisterServiceImpl implements RegisterService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Override
    public Map<String, String> register(String username, String password, String confirmedPassword) {
        Map<String,String> map = new HashMap<>();
        if (username == null){
            map.put("error_message", "用户名不能为空");
            return map;
        }
        if (password == null || confirmedPassword == null){
            map.put("error_message", "密码不能为空");
            return map;
        }
        username = username.trim();
        if (username.isEmpty()){
            map.put("error_message", "用户名不能为空");
            return map;
        }
        if (password.isEmpty() || confirmedPassword.isEmpty()){
            map.put("error_message", "密码不能为空");
            return map;
        }
        if (username.length() > 64){
            map.put("error_message", "用户名不能太长");
            return map;
        }
        if (password.length() > 64 || confirmedPassword.length() > 64){
            map.put("error_message", "密码不能太长");
            return map;
        }
        if (!password.equals(confirmedPassword)){
            map.put("error_message", "两次密码不一致");
            return map;
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        List<User> users = userMapper.selectList(queryWrapper); // 查询注册的用户名是否在数据库中
        if (!users.isEmpty()){
            map.put("error_message", "用户名已存在");
            return map;
        }
        // 密码加密,将注册的用户信息加入到数据库中
        String EncodedPassword = passwordEncoder.encode(password);
        String photo = "https://cdn.acwing.com/media/user/profile/photo/352390_lg_6f5cbb974b.jpg";
        User user = new User(null,username,EncodedPassword,photo);
        userMapper.insert(user);

        map.put("error_message", "success");
        return map;
    }
}
