package com.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class Interact {
    Integer tid;
    Integer uid;
    Date time;
    String type;

    public String toKey(){
        return tid+":"+uid;
    }

    public static Interact parseInteract(String str,String type){//把redis存的还原成interact
        String[] keys = str.split(":");
        return new Interact(Integer.parseInt(keys[0]),Integer.parseInt(keys[1]),new Date(),type);
    }

}
