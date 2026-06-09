package com.example.entity.vo.response;

import lombok.Data;

@Data
public class AccountPrivacyVO {
    boolean email;
    boolean phone;
    boolean qq;
    boolean wechat;
    boolean gender;
}
