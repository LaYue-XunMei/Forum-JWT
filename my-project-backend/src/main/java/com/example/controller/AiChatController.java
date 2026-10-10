package com.example.controller;

import com.alibaba.fastjson2.JSONArray;
import com.example.entity.RestBean;
import com.example.service.AiService;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.awt.*;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    @Resource
    AiService aiService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatWithAi(@RequestBody JSONArray context) {
        return aiService.chatWithAi(context);
    }

}
