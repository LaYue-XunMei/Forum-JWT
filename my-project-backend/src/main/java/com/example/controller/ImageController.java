package com.example.controller;

import com.example.entity.RestBean;
import com.example.service.ImageService;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/image")
public class ImageController {

    @Resource
    ImageService imageService;


    @PostMapping("/cache")
    public RestBean<String> uploadImage(@RequestParam("file")MultipartFile file,
                                        @RequestAttribute(Const.ATTR_USER_ID)int id,
                                        HttpServletResponse response) throws IOException {
        int fileSize = 5*1024*1024;
        if(file.getSize() > fileSize)//文件大于5M
            return RestBean.failure(400,"文件大小不能超过5MB");
        log.info("正在上传图片...");
        String url = imageService.uploadImage(file,id);
        if(url != null){
            log.info("图片上传成功，大小：{}", file.getSize());
            return RestBean.success(url);
        }else {
            response.setStatus(400);
            return RestBean.failure(400,"图片上传失败，请联系管理员！");
        }
    }

    @PostMapping("/avatar")
    public RestBean<String> uploadAvatar(@RequestParam("file") MultipartFile file,
                                         @RequestAttribute(Const.ATTR_USER_ID)int id) throws IOException {//上传头像后返回一个ID
        int fileSize = 5*1024*1024;
        if(file.getSize() > fileSize)//文件大于5M
            return RestBean.failure(400,"文件大小不能超过5MB");
        log.info("正在上传头像文件...");
        String url = imageService.upLoadAvatar(file,id);
        if(url != null){
            log.info("头像上传成功，大小：{}", file.getSize());
            return RestBean.success(url);
        }else {
            return RestBean.failure(400,"头像上传失败，请联系管理员！");
        }

    }
}
