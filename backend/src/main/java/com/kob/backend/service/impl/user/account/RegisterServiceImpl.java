package com.kob.backend.service.impl.user.account;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import com.kob.backend.service.user.account.RegisterService;
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
        Map<String, String> res = new HashMap<>();

        if (username == null) {  // 判断是否存在用户名参数
            res.put("result", "The username can't be empty!");
            return res;
        }
        if (password == null || confirmedPassword == null) {  // 判断是否存在密码参数
            res.put("result", "The password can't be empty!");
            return res;
        }

        username = username.trim();  // 删掉用户名首尾的空白字符
        if (username.isEmpty()) {  // 判断删去空格后用户名是否为空
            res.put("result", "The username can't be empty!");
            return res;
        }
        if (password.isEmpty() || confirmedPassword.isEmpty()) {  // 判断密码是否为空
            res.put("result", "The password can't be empty!");
            return res;
        }

        if (username.length() > 100 || password.length() > 100) {  // 判断用户名或密码长度是否超过数据库字段的范围
            res.put("result", "The username or password can't be longer than 100!");
            return res;
        }
        if (!password.equals(confirmedPassword)) {  // 判断两次输入的密码是否一致
            res.put("result", "The inputs of two passwords are different!");
            return res;
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);  // 在数据库中查看是否存在用户名相同的用户
        List<User> users = userMapper.selectList(queryWrapper);
        if (!users.isEmpty()) {
            res.put("result", "The username already exists!");
            return res;
        }

        // 执行数据库插入操作
        String encodedPassword = passwordEncoder.encode(password);
        String photo = "https://cdn.acwing.com/media/user/profile/photo/82581_lg_e9bdbcb8aa.jpg";  // 默认头像
        User user = new User(null, username, encodedPassword, photo);
        userMapper.insert(user);

        res.put("result", "success");
        return res;
    }
}
