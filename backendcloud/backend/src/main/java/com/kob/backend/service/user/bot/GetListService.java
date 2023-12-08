package com.kob.backend.service.user.bot;

import com.kob.backend.pojo.Bot;

import java.util.List;

public interface GetListService {
    List<Bot> getList();  // 根据用户信息获取Bot，用户信息存放在令牌中，因此不用传参数
}
