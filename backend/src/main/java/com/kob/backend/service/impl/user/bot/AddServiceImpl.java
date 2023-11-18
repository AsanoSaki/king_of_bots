package com.kob.backend.service.impl.user.bot;

import com.kob.backend.mapper.BotMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import com.kob.backend.service.impl.utils.UserDetailsImpl;
import com.kob.backend.service.user.bot.AddService;
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
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = loginUser.getUser();  // 获取当前登录的用户

        String title = data.get("title");
        String description = data.get("description");
        String content = data.get("content");

        Map<String, String> res = new HashMap<>();

        if (title == null || title.isEmpty()) {
            res.put("result", "The title can't be empty!");
            return res;
        }
        if (title.length() > 100) {
            res.put("result", "Title length can't exceed 100!");
            return res;
        }
        if (description == null || description.isEmpty()) {
            description = "这个用户很懒，什么也没留下~";
        }
        if (description.length() > 300) {
            res.put("result", "Description length can't exceed 300!");
            return res;
        }
        if (content == null || content.isEmpty()) {
            res.put("result", "The content can't be empty!");
            return res;
        }
        if (content.length() > 10000) {
            res.put("result", "Code length can't exceed 10000!");
            return res;
        }

        Date now = new Date();
        Bot bot = new Bot(null, user.getId(), title, description, content, 1500, now, now);
        botMapper.insert(bot);

        res.put("result", "success");
        return res;
    }
}
