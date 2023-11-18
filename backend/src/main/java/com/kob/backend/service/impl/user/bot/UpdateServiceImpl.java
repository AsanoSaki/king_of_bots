package com.kob.backend.service.impl.user.bot;

import com.kob.backend.mapper.BotMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import com.kob.backend.service.impl.utils.UserDetailsImpl;
import com.kob.backend.service.user.bot.UpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UpdateServiceImpl implements UpdateService {
    @Autowired
    private BotMapper botMapper;

    @Override
    public Map<String, String> update(Map<String, String> data) {
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = loginUser.getUser();

        int bot_id = Integer.parseInt(data.get("bot_id"));
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

        Bot bot = botMapper.selectById(bot_id);

        if (bot == null) {
            res.put("result", "Bot doesn't exist!");
            return res;
        }
        if (!bot.getUserId().equals(user.getId())) {
            res.put("result", "No permission to update the bot!");
            return res;
        }

        Bot new_bot = new Bot(bot.getId(), user.getId(), title, description, content, bot.getRating(), bot.getCreatetime(), new Date());
        botMapper.updateById(new_bot);

        res.put("result", "success");
        return res;
    }
}
