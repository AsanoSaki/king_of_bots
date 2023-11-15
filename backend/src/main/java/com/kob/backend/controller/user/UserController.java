package com.kob.backend.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {
    @Autowired  // 如果要用到数据库里的Mapper需要加上该注解
    UserMapper userMapper;

    @GetMapping("/user/all/")  // 想验证GET/POST类型的请求可以使用GetMapping/PostMapping
    public List<User> getAllUser() {
        return userMapper.selectList(null);  // null表示查询所有数据
    }

    @GetMapping("/user/{id}/")  // 根据id查询用户
    public User getUser(@PathVariable int id) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);  // 根据链接传入的id查询用户
        return userMapper.selectOne(queryWrapper);
    }

    @GetMapping("/user/insert/{username}/{password}/")  // 添加用户
    public String insertUser(@PathVariable String username, @PathVariable String password) {
        if (password.length() < 6) {
            return "The length of password should greater than 6!";
        }
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        userMapper.insert(user);
        return "Add User Successfully!";
    }

    @GetMapping("/user/delete/{id}")  // 删除用户
    public String deleteUser(@PathVariable int id) {
        userMapper.deleteById(id);
        return "Delete User Successfully!";
    }
}
