package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.EmailRecord;


import java.util.List;

public interface EmailService {

    void sendVerifyEmail(String type, String email,int code);
    Page<EmailRecord> listEmailRecord(int page, int size);
    boolean resendEmailRecord(int id);
}
