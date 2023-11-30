package com.kob.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class BackendApplicationTests {
    @Test
    void contextLoads() {
        StringBuilder s = new StringBuilder();
        s.append(true);
        s.append(false);
        System.out.println(s);
    }
}
