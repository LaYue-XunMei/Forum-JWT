package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.BaseData;
import com.fasterxml.jackson.databind.ser.Serializers;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("db_account_privacy")
public class AccountPrivacy implements BaseData {

    @TableId(type = IdType.AUTO)
    final Integer id;//构造时一定要有

    boolean email = true;//默认都展示
    boolean phone = true;
    boolean qq = true;
    boolean wechat = true;
    boolean gender = true;

}
