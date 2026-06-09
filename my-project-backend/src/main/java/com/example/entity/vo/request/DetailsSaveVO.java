package com.example.entity.vo.request;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class DetailsSaveVO {

    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]+$")
    @Length(min = 1,max = 10)
    String username;
    @Min(0)
    @Max(1)
    int gender;
    @Length(min = 11,max = 11)
    String phone;
    @Length(max = 13)
    String qq;
    @Length(max=20)
    String wechat;//微信
    @Length(max=200)
    String desc;//用户自我简介
}
