package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.BaseData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@TableName("db_topic_type")
@AllArgsConstructor
@NoArgsConstructor
public class TopicType implements BaseData {

    Integer id;
    String name;
    @TableField("`desc`")
    String desc;
    String color;
}
