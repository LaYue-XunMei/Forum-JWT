package com.example.entity.vo.response;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

@Data
public class WeatherVO {

    JSONObject location;//城市地址
    JSONObject now;//当前天气
    JSONArray hourly;//未来天气
}
