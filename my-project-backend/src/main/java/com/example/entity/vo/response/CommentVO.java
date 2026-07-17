package com.example.entity.vo.response;

import lombok.Data;

import java.util.Date;

@Data
public class CommentVO {
    int id;
    String content;
    Date time;
    String quote;//只要引用评论的内容
    User user;

    @Data
    public static class User{
        Integer id;
        String username;
        String avatar;
        Integer gender;
        String qq;
        String wechat;
        String phone;
        String email;
    }


}
