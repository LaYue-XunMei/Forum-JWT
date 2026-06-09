package com.example.entity.vo.response;

import lombok.Data;

import java.util.Date;

@Data
public class AccountVO {
    String username;//用户名
    String email;//邮箱
    String role;//角色
    //String avatar;//头像
    Date registerTime;//注册时间
}
