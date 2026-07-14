package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.BaseData;
import com.fasterxml.jackson.databind.ser.Serializers;
import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

    //哪些信息是需要隐藏忽略的
    public String [] hiddenFields(){
        List<String> strings = new LinkedList<>();
        //用反射更灵活
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try{
                if(field.getType().equals(boolean.class) && !field.getBoolean(this)){
                    strings.add(field.getName());//记录需要隐藏的
                }
            }catch (Exception ignored){}
        }
        return strings.toArray(String[]::new);
    }

}
