package com.example.entity.vo.response;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
public class TopicDetailVO {
    Integer id;
    String title;
    String content;
    Integer type;
    Date time;
    User user;
    Interact interact; //返回帖子点赞收藏情况
    Long comments; //评论数量
    Integer locked;
    //Integer invisible;//默认0表示不封禁

    @Data
    @AllArgsConstructor
    public static class Interact {
        Boolean like;
        Boolean collect;
    }

    @Data
    public static class User{
        Integer id;
        String username;
        String avatar;
        String desc;
        Integer gender;
        String qq;
        String wechat;
        String phone;
        String email;
    }

}
