package com.example.entity.vo.response;

import lombok.Data;

import java.util.Date;

@Data
public class AccountVO {
    int id;
    String username;//用户名
    String email;//邮箱
    String role;//角色
    String avatar;//头像
    Date registerTime;//注册时间
    boolean mute;//禁言
    boolean banned;//封禁
}
