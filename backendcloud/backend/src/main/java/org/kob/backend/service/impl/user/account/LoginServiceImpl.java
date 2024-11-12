package org.kob.backend.service.impl.user.account;

import org.kob.backend.pojo.User;
import org.kob.backend.service.impl.util.UserDetailsImpl;
import org.kob.backend.service.user.account.LoginService;
import org.kob.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Override
    public Map<String, String> getToken(String username, String password) {
        // 将用户名密码封装成token类
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authenticate = authenticationManager.authenticate(authenticationToken); // 验证身份，登录失败会自动处理
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal(); // 获取用户信息
        User user = loginUser.getUser();

        String jwt = JwtUtil.createJWT(user.getId().toString());
        Map<String,String> map = new HashMap<>();
        map.put("error_message", "success");
        map.put("token", jwt);

        return map;
    }
}
