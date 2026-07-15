package com.example.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.*;
import com.example.entity.vo.request.TopicCreateVO;
import com.example.entity.vo.response.TopicDetailVO;
import com.example.entity.vo.response.TopicPreviewVO;
import com.example.entity.vo.response.TopicTopVO;
import com.example.mapper.*;
import com.example.service.TopicService;
import com.example.utils.CacheUtils;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper,Topic> implements TopicService {

    @Resource
    TopicTypeMapper topicTypeMapper;

    @Resource
    FlowUtils flowUtils;

    @Resource
    CacheUtils cacheUtils;

    @Resource
    AccountMapper accountMapper;

    @Resource
    AccountDetailsMapper accountDetailsMapper;

    @Resource
    AccountPrivacyMapper accountPrivacyMapper;

    @Resource
    StringRedisTemplate template;

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
            cacheUtils.deleteCachePattern(Const.FORUM_TOPIC_PREVIEW_CACHE+"*");//发帖后，帖子缓存清空
            return null;
        }else {
            return "内部错误，请联系管理员。";
        }
    }

    @Override
    public List<TopicPreviewVO> listTopicByPage(int pageNumber, int type) {//规定一页10个帖子
        String key = Const.FORUM_TOPIC_PREVIEW_CACHE + pageNumber+ ":" + type;
        List<TopicPreviewVO> list = cacheUtils.takeListFromCache(key,TopicPreviewVO.class);
        if(list != null) return list;//不为空，直接从缓存中取

        Page<Topic> page = Page.of(pageNumber, 10);//10个一页
        if(type==0)//0表示所以主题
            baseMapper.selectPage(page,Wrappers.<Topic>query().orderByDesc("time"));
        else
            baseMapper.selectPage(page,Wrappers.<Topic>query().eq("type",type).orderByDesc("time"));
        List<Topic> topics = page.getRecords();//现在拿到的topic里面没有用户信息
        if(topics.isEmpty()) return null;
        list = topics.stream().map(this::resolveToPreview).toList();
        //缓存
        cacheUtils.saveListToCache(key,list,60);
        return list;
    }

    @Override
    public List<TopicTopVO> listTopTopics() {
        List<Topic> topics = baseMapper.selectList(Wrappers.<Topic>query()
                .select("id","title","time")
                .eq("top",1));
        return topics.stream().map(topic -> {
            TopicTopVO vo = new TopicTopVO();
            BeanUtils.copyProperties(topic,vo);
            return vo;
        }).toList();
    }

    @Override
    public TopicDetailVO getTopic(int tid) {
        TopicDetailVO vo = new TopicDetailVO();
        Topic topic = baseMapper.selectById(tid);
        BeanUtils.copyProperties(topic,vo);//先把帖子信息拷贝过去
        //还要给用户数据
        TopicDetailVO.User user = new TopicDetailVO.User();
        vo.setUser(this.fillUserDetailByPrivacy(user,topic.getUid()));
        return vo;
    }
    /**
     * 由于论坛交互数据（如点赞、收藏）更新可能非常频繁
     * 更新信息实时到MySQL不太现实，所以用Redis做缓冲并在合适的时机一次性入库一段时间内的全部数据
     * 当数据更新到来时，创建一个新的定时任务，此任务在一段时间后执行
     * 将全部Redis暂时缓存信息一次性加入到数据库，如果
     * 在定时任务已经设定期间又有新的更新到来，则仅仅更新Redis不创建新的延时任务
     */
    @Override
    public void interact(Interact interact, boolean state) {
        String type = interact.getType();
        synchronized (type.intern()){
            template.opsForHash().put(type,interact.toKey(),Boolean.toString(state));
            this.saveInteractSchedule(type);
        }
    }

    //定时任务
    private final Map<String,Boolean> state = new HashMap<>();//判断任务是否开始计时
    ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    private void saveInteractSchedule(String type) {
        if(!state.getOrDefault(type,false)) {
            state.put(type,true);//任务开始
            service.schedule(()->{
                try {
                    this.saveInteract(type);
                } finally {
                    state.put(type,false);
                }
            },3, TimeUnit.SECONDS);
        };
    }

    private void saveInteract(String type){
        synchronized (type.intern()){
            List<Interact> check = new LinkedList<>();//选中
            List<Interact> unCheck = new LinkedList<>();//取消选中
            template.opsForHash().entries(type).forEach((k, v)->{//根据type拿到哈希表
                if(Boolean.parseBoolean(v.toString()))
                    check.add(Interact.parseInteract(k.toString(),type));
                else
                    unCheck.add(Interact.parseInteract(k.toString(),type));
            });
            if(!check.isEmpty())
                baseMapper.addInteract(check,type);
            if(!unCheck.isEmpty())
                baseMapper.deleteInteract(unCheck,type);//批量删除
            template.delete(type);//清楚，等待下一轮操作
        }
    }

    //由于有隐私设置，单独写个方法
    private <T> T fillUserDetailByPrivacy(T target,int uid){
        AccountDetails details = accountDetailsMapper.selectById(uid);
        Account account = accountMapper.selectById(uid);
        AccountPrivacy accountPrivacy = accountPrivacyMapper.selectById(uid);
        //通过accountPrivacy判断哪些信息可以返回，哪些需要隐藏
        String[] ignores = accountPrivacy.hiddenFields();
        BeanUtils.copyProperties(account,target,ignores);
        BeanUtils.copyProperties(details,target,ignores);
        return target;
    }
    private TopicPreviewVO resolveToPreview(Topic topic) {
        TopicPreviewVO vo = new TopicPreviewVO();
        BeanUtils.copyProperties(accountMapper.selectById(topic.getUid()),vo);//单独查询一次，将用户信息与原来的帖子信息分开
        BeanUtils.copyProperties(topic,vo);
        List<String> images = new ArrayList<>();
        StringBuilder previewText = new StringBuilder();
        JSONArray ops = JSONObject.parseObject(topic.getContent()).getJSONArray("ops");
        for(Object op : ops) {
            Object insert = JSONObject.from(op).get("insert");
            if(insert instanceof String text){
                if(previewText.length() >= 300 ) continue;//预览只拿300字
                previewText.append(text);
            }else if(insert instanceof Map<?,?> map){
                Optional.ofNullable(map.get("image"))
                        .ifPresent(obj ->images.add(obj.toString()));
            }
        }
        vo.setText(previewText.length() > 300 ? previewText.substring(0,300) : previewText.toString());
        vo.setImages(images);
        return vo;
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
