package org.kob.backend.consumer.utils;

import io.jsonwebtoken.Claims;
import org.kob.backend.utils.JwtUtil;

public class JwtAuthentication {
    public static Integer getuserId(String token){
        int userId = -1;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            userId = Integer.parseInt(claims.getSubject());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return userId;
    }

}
