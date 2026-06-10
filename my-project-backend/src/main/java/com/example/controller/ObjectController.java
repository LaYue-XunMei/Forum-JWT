package com.example.controller;

import com.example.entity.RestBean;
import com.example.service.ImageService;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import io.minio.errors.ErrorResponseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ObjectController {

    @Resource
    ImageService service;

    @GetMapping("/images/**")
    public void imageFetch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setHeader("Content-Type", "image/jpg");
        this.fetchImage(request,response);
    }

    private void fetchImage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String imagePath = request.getServletPath().substring(7); // 去掉 '/images'，这七个字符
        ServletOutputStream stream = response.getOutputStream();

        // '/avatar' 7个字符，再加上 UUID 一定大于13
        if (imagePath.length() <= 13) {
            response.setStatus(404);
            stream.println(RestBean.failure(404, "Not found").asJsonString());
        } else {
            try {
                service.fetchImageFromMinio(stream, imagePath);
                response.setHeader("Cache-Control", "max-age=2592000");// 缓存30天

            } catch (ErrorResponseException e) {
                // 获取 MinIO 的错误代码（字符串）
                String errorCode = e.errorResponse().code();

                // MinIO 的错误代码是字符串，比如 "NoSuchKey"
                if ("NoSuchKey".equals(errorCode)) {
                    response.setStatus(404);
                    stream.println(RestBean.failure(404, "Not found").asJsonString());
                } else {
                    log.error("从Minio获取图片出现异常：{}", e.getMessage(), e);
                    // 其他错误返回 500
                    response.setStatus(500);
                    stream.println(RestBean.failure(500, "获取图片失败").asJsonString());
                }
            } catch (Exception e) {
                log.error("从Minio获取图片出现异常：{}", e.getMessage(), e);
                response.setStatus(500);
                stream.println(RestBean.failure(500, "获取图片失败").asJsonString());
            }
        }
    }
}
