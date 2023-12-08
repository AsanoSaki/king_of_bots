package com.kob.backend.controller.user.account;

import com.kob.backend.service.user.account.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class LoginController {
    @Autowired  // 将接口注入进来，这就是Spring的IoC依赖注入特性
    private LoginService loginService;

    @PostMapping("/user/account/login/")  // 登录采用POST请求，不是明文传输，较为安全
    public Map<String, String> login(@RequestParam Map<String, String> data) {  // 将POST参数放在一个Map中
        String username = data.get("username");
        String password = data.get("password");
        return loginService.login(username, password);
    }
}
