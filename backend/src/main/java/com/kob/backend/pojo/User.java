package com.kob.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data  // Lombok能够帮我们实现get/set之类的方法
@NoArgsConstructor  // 无参构造函数
@AllArgsConstructor  // 带所有参数的构造函数
public class User {
    @TableId(value = "id", type = IdType.AUTO)  // 声明id为自增类型
    private Integer id;
    private String username;
    private String password;
}
