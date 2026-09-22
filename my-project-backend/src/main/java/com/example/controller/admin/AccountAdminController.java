package com.example.controller.admin;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.RestBean;
import com.example.entity.vo.response.AccountVO;
import com.example.service.AccountService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/user")
public class AccountAdminController {

    @Resource
    AccountService accountService;

    @GetMapping("/list")
    public RestBean<JSONObject> accountList(int page,int size) {//默认从1开始，elementUI也是从1开始
        JSONObject object = new JSONObject();
        List<AccountVO> list = accountService.page(Page.of(page,size))
                .getRecords()//拿到该页所有项
                .stream()
                .map(a -> a.asViewObject(AccountVO.class))
                .toList();
        object.put("total",accountService.count());
        object.put("list",list);
        return RestBean.success(object);

    }

}
