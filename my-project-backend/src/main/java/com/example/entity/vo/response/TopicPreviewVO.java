package com.example.entity.vo.response;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TopicPreviewVO {

    int id;
    int type;
    String title;
    String text;
    List<String> images;
    Date time;

    Integer uid;
    String username;
    String avatar;

    int like;
    int collect;
    int top;//是否置顶
    int locked;//帖子是否锁定，默认0不锁定，1为锁定
    int invisible;//默认0表示不封禁
}
