package com.example.entity.dto;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.BaseData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@TableName("db_topic")
public class Topic implements BaseData {
    @TableId(type= IdType.AUTO)
    Integer id;

    String title;
    String intro; //只保存文本摘要信息，用于搜索查找
    String content;
    Integer type;
    Integer uid;
    Date time;
    Integer top;
    Integer locked;
    Integer invisible;//默认0表示不封禁

    //静态方法应用再更新时同步更新摘要信息
    public static String recreateIntro(JSONObject content){
        StringBuilder builder = new StringBuilder();
        JSONArray ops = content.getJSONArray("ops");
        for(Object op : ops){
            Object insert = JSONObject.from(op).get("insert");
            if(insert instanceof String text){
                builder.append(text);
            }
        }
        return builder.toString();//提取完文本信息（可做字数限制）
    }

    //每个ops是一个操作，插入或添加新的信息
    public void createIntro(){
        intro = recreateIntro(JSONObject.parseObject(content));

    }
}
