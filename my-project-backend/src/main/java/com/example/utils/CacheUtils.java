package com.example.utils;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CacheUtils {

    @Resource
    StringRedisTemplate template;

    //存普通对象
    public <T> void saveToCache(String key, T data, long expireTime){
        template.opsForValue().set(key, JSONObject.from(data).toJSONString(),expireTime, TimeUnit.SECONDS);
    }
    //存List
    public <T> void saveListToCache(String key, List<T> list, long expireTime){
        template.opsForValue().set(key, JSONArray.from(list).toJSONString(),expireTime, TimeUnit.SECONDS);
    }

    public <T> List<T> takeListFromCache(String key, Class<T> itemType){
        String value = template.opsForValue().get(key);

        // 添加空值检查
        if (value == null || value.trim().isEmpty()) {
            return null; // 或者返回 Collections.emptyList()
        }
        return JSONArray.parseArray(value).toList(itemType);
    }

    public <T> T takeFromCache(String key, Class<T> dataType){
        String value = template.opsForValue().get(key);
        // 添加空值检查
        if (value == null || value.trim().isEmpty()) {
            return null; // 或者返回 Collections.emptyList()
        }
        return JSONObject.parseObject(value).to(dataType);
    }

    public void deleteCache(String key){//删除缓存/
        template.delete(key);
    }
}
