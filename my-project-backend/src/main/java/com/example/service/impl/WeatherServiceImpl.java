package com.example.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.entity.vo.response.WeatherVO;
import com.example.service.WeatherService;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

@Service
public class WeatherServiceImpl implements WeatherService {

    @Resource
    RestTemplate restTemplate;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Value("${spring.weather.key}")
    String key;

    @Value("${spring.weather.api-host}")
    String apiHost;

    @Override
    public WeatherVO fetchWeather(double longitude, double latitude) {

        return fetchFromCache(longitude,latitude);
    }


    private WeatherVO fetchFromCache(double longitude, double latitude) {
        JSONObject geo = this.decompressStringToJSON(restTemplate.getForObject(
                "https://" + apiHost + "/geo/v2/city/lookup?location="+longitude+","+latitude+"&key="+key,
                byte[].class));
        if(geo == null) return null;
        JSONObject location = geo.getJSONArray("location").getJSONObject(0);//只查出一个
        int id = location.getInteger("id");//查地区ID
        String key = Const.FORUM_WEATHER_CACHE+id;
        String cache = stringRedisTemplate.opsForValue().get(key);
        if(cache != null)
            return JSONObject.parseObject(cache).to(WeatherVO.class);
        WeatherVO vo = this.fetchFromAPI(id,location);
        if(vo == null) return null;
        stringRedisTemplate.opsForValue().set(key,JSONObject.from(vo).toJSONString(),1, TimeUnit.HOURS);//缓存1个小时
        return vo;
    }

    private WeatherVO fetchFromAPI(int id,JSONObject location) {
        WeatherVO vo = new WeatherVO();
        vo.setLocation(location);
        JSONObject now = this.decompressStringToJSON(restTemplate.getForObject(//获取实时天气
                "https://" + apiHost + "/v7/weather/now?location="+id+"&key="+key,
                byte[].class));
        if(now == null) return null;
        vo.setNow(now.getJSONObject("now"));
        JSONObject hourly = this.decompressStringToJSON(restTemplate.getForObject(
                "https://" + apiHost + "/v7/weather/24h?location="+id+"&key="+key,
                byte[].class));
        if(hourly == null) return null;
        vo.setHourly(new JSONArray(hourly.getJSONArray("hourly").stream().limit(5).toList()));//只保存5个数据
        return vo;
    }


    //返回数据是JSON格式并进行了Gzip压缩
    private JSONObject decompressStringToJSON(byte[] data){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try{
            GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data));
            byte[] buffer = new byte[1024];
            int read;
            while ((read = gzip.read(buffer)) != -1) {
                stream.write(buffer, 0, read);//写入解压后的数据
            }
            gzip.close();
            stream.close();
            return JSONObject.parseObject(stream.toString());
        }catch (IOException e){
            return null;
        }
    }



}
