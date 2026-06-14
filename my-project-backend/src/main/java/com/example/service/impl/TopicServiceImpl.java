package com.example.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Topic;
import com.example.entity.dto.TopicType;
import com.example.entity.vo.request.TopicCreateVO;
import com.example.mapper.TopicMapper;
import com.example.mapper.TopicTypeMapper;
import com.example.service.TopicService;
import com.example.utils.CacheUtils;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper,Topic> implements TopicService {

    @Resource
    TopicTypeMapper topicTypeMapper;

    @Resource
    FlowUtils flowUtils;

    @Resource
    CacheUtils cacheUtils;

    @Override
    public List<TopicType> listTypes() {
        return topicTypeMapper.selectList(null);
    }


    private Set<Integer> types = null;
    @PostConstruct
    private void initTypes(){
        // 在这里初始化，此时依赖已注入完成
        types = this.listTypes()
                .stream()
                .map(TopicType::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public String createTopic(int uid, TopicCreateVO vo) {
        if(!textLimitCheck(vo.getContent())) return "内容过长，请重新输入。";
        if(!types.contains(vo.getType())) return "文章类型非法";
        String key  = Const.FORUM_TOPIC_CREATE_COUNTER + uid;
        if(!flowUtils.limitPeriodCounterCheck(key,3,3600))
            return "发文频繁，请稍后再试。";
        Topic topic = new Topic();
        BeanUtils.copyProperties(vo,topic);
        topic.setUid(uid);
        topic.setContent(vo.getContent().toJSONString());//发过来的是JSON格式，转换一下
        topic.setTime(new Date());
        if(this.save(topic)){
            cacheUtils.deleteCache(Const.FORUM_TOPIC_PREVIEW_CACHE+"*");//发帖后，帖子缓存清空
            return null;
        }else {
            return "内部错误，请联系管理员。";
        }
    }



    private boolean textLimitCheck(JSONObject content) {
        if(content == null) return false;
        long length = 0;
        for (Object ops : content.getJSONArray("ops")) {
            length += JSONObject.from(ops).getString("insert").length();
            if(length > 20000) return false;
        }
        return true;
    }
}
