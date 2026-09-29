package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("db_email_record")
public class EmailRecord {
    @TableId(type = IdType.AUTO)
    Integer id;
    String email;
    String title;
    String content;
    Date time;
    /*发送状态:0发送中，1成功，2失败*/
    Integer status;//邮件是否发送成功的状态标志

    public EmailRecord(String email, String title, String content) {
        this.email = email;
        this.title = title;
        this.content = content;
        this.status = 0;
        this.time = new Date();
    }

}
