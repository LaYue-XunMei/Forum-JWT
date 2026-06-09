package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Account;
import com.example.entity.dto.AccountDetails;
import com.example.entity.vo.request.DetailsSaveVO;
import com.example.mapper.AccountDetailsMapper;
import com.example.service.AccountDetailsService;
import com.example.service.AccountService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountDetailsServiceImpl extends ServiceImpl<AccountDetailsMapper, AccountDetails> implements AccountDetailsService {

    @Resource
    AccountService accountService;


    @Override
    public AccountDetails findAccountDetailsById(int id) {
        return this.getById(id);
    }

    @Override
    @Transactional //saveOrUpdate事务操作
    public synchronized boolean saveAccountDetails(int id, DetailsSaveVO vo) {//可能两个人同时改成一个名字，同时判断没用过，所以加锁
        Account account =accountService.findAccountByNameOrEmail(vo.getUsername());//通过前端的用户名查找目标用户
        if(account == null || account.getId() == id){
            accountService.update()
                    .eq("id",id)
                    .set("username",vo.getUsername())
                    .update();
            this.saveOrUpdate(new AccountDetails(
                    id, vo.getGender(), vo.getPhone(),
                    vo.getQq(), vo.getWechat(), vo.getDesc()));
            return true;
        }
        return false;
    }
}
