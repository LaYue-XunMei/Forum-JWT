package com.example.utils;


import com.example.entity.RestBean;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class ControllerUtils {

    public  <T> RestBean<T> messageHandle(Supplier<String> action){
        String message = action.get();
        if(message == null)//如果message为空则正常发送
            return RestBean.success();
        else
            return RestBean.failure(400,message);
    }
}
