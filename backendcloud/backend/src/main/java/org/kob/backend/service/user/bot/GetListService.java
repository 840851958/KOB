package org.kob.backend.service.user.bot;

import org.kob.backend.pojo.Bot;

import java.util.List;

public interface GetListService {
    // 根据用户的token查找，token保存在local中所以不用传参
    List<Bot> getList();
}
