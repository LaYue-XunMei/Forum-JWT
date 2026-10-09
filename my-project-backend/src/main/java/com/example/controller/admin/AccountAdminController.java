package com.example.controller.admin;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.dto.AccountDetails;
import com.example.entity.dto.AccountPrivacy;
import com.example.entity.vo.response.AccountVO;
import com.example.mapper.AccountPrivacyMapper;
import com.example.service.AccountDetailsService;
import com.example.service.AccountPrivacyService;
import com.example.service.AccountService;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/admin/user")
public class AccountAdminController {

    @Resource
    AccountService accountService;

    @Resource
    AccountDetailsService accountDetailsService;

    @Resource
    AccountPrivacyService accountPrivacyService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Value("${spring.security.jwt.expire}")
    private int expire;

    //可通过ID或用户名搜索
    @GetMapping("/list")
    public RestBean<JSONObject> accountList(@RequestParam int page,
                                            @RequestParam int size,
                                            @RequestParam(required = false) String keyword) {//默认从1开始，elementUI也是从1开始
        JSONObject object = new JSONObject();
        Page<Account> accountPage = accountService.page(Page.of(page, size), Wrappers.<Account>query()
                .eq(keyword != null,"id", keyword)
                .or()
                .like(keyword != null,"username", "%" + keyword + "%")
        );
        List<AccountVO> list = accountPage
                .getRecords()//拿到该页所有项
                .stream()
                .map(a -> a.asViewObject(AccountVO.class))
                .toList();
        object.put("total",accountPage.getTotal());//只统计搜索后的总数
        object.put("list",list);
        return RestBean.success(object);

    }

    @GetMapping("/detail")
    public RestBean<JSONObject> accountDetail(int id) {
        JSONObject object = new JSONObject();
        object.put("detail",accountDetailsService.findAccountDetailsById(id));
        object.put("privacy",accountPrivacyService.accountPrivacy(id));
        return RestBean.success(object);
    }

    @PostMapping("/save")
    public RestBean<Void> saveAccount(@RequestBody JSONObject object,
                                      @RequestAttribute(Const.ATTR_USER_ID) int uid) {//除了用户基础信息，还会返回其他一些信息，所以用JSON
        int id = object.getInteger("id");
        if(uid == id){
            return RestBean.failure(400,"不能修改自己账号信息");
        }
        Account account = accountService.findAccountById(id);
        Account save = object.toJavaObject(Account.class);//自动赋值转换为对象
        handleBanned(account,save);//先处理一下封禁操作，设置封禁标记，JWT过滤器中使用
        BeanUtils.copyProperties(save,account,"password","registerTime");
        accountService.saveOrUpdate(account);
        AccountDetails detail = accountDetailsService.findAccountDetailsById(id);
        AccountDetails saveDetail = object.getJSONObject("detail").toJavaObject(AccountDetails.class);//返回数据中套了一层，先取出来
        BeanUtils.copyProperties(saveDetail,detail,"id");
        accountDetailsService.saveOrUpdate(detail);
        AccountPrivacy privacy = accountPrivacyService.accountPrivacy(id);
        AccountPrivacy savePrivacy = object.getJSONObject("privacy").toJavaObject(AccountPrivacy.class);
        BeanUtils.copyProperties(savePrivacy,privacy);
        accountPrivacyService.saveOrUpdate(privacy);
        return RestBean.success();
    }

    @PostMapping("/change-password")
    public RestBean<Void> changePassword(@RequestBody JSONObject object){
        accountService.modifyPassword(
                object.getInteger("id"),
                object.getString("newPassword"));
        return RestBean.success();
    }

    private void handleBanned(Account oldAccount,Account currentAccount){
        String key = Const.BANNED_BLOCK+ oldAccount.getId();
        if(!oldAccount.isBanned() && currentAccount.isBanned()){//之前不是封禁而现在被封了，说明管理员就行了封禁操作
            stringRedisTemplate.opsForValue().set(key,"true",expire, TimeUnit.HOURS);
        }else if(oldAccount.isBanned() && !currentAccount.isBanned()){//取消封禁
            stringRedisTemplate.delete(key);
        }
    }

}
