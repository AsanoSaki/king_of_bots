package com.kob.backend.service.impl.user.account;

import com.kob.backend.pojo.User;
import com.kob.backend.service.impl.utils.UserDetailsImpl;
import com.kob.backend.service.user.account.LoginService;
import com.kob.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service  // 注入到Spring中，未来可以用@Autowired注解将该类注入到某个其他类中
public class LoginServiceImpl implements LoginService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public Map<String, String> login(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);  // 需要先封装一下，因为数据库中存的不是明文

        Authentication authenticate = authenticationManager.authenticate(authenticationToken);  // 验证是否能登录，如果失败会自动处理

        UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
        User user = loginUser.getUser();  // 将用户取出来
        String jwt_token = JwtUtil.createJWT(user.getId().toString());  // 将用户的ID转换成jwt_token

        Map<String, String> res = new HashMap<>();
        res.put("result", "success");
        res.put("jwt_token", jwt_token);

        return res;
    }
}
