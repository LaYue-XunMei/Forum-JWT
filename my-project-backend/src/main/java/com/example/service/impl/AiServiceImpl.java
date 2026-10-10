package com.example.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.service.AiService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

@Service
public class AiServiceImpl implements AiService {


    @Resource
    DeepSeekChatModel chatModel;



    @Override
    public SseEmitter chatWithAi(JSONArray context) {

        SseEmitter sseEmitter = new SseEmitter(3000L);

        List<? extends AbstractMessage> list = context.stream().map(item -> {
            JSONObject obj = JSONObject.from(item);
            return switch (obj.getString("type")) {
                case "user" -> new UserMessage(obj.getString("text"));
                case "assistant" -> new AssistantMessage(obj.getString("text"));//给AI的提示词
                default -> throw new RuntimeException();
            };
        }).toList();
        Prompt prompt = new Prompt(list.toArray(new Message[0]));
        Flux<ChatResponse> flux = chatModel.stream(prompt);//响应式开发相关对象，类似生产者消费者模型
        flux.subscribe( response -> {//只要flux有了新东西就会调用一次，一小段回复ChatResponse
            String text = response.getResult().getOutput().getText();
            try {
                sseEmitter.send(text);
            } catch (Exception e) {
                sseEmitter.completeWithError(e);//关闭emitter
            }
        },sseEmitter::completeWithError, sseEmitter::complete);

        return sseEmitter;//先给客户端一个SSE，让他等待输出结果
    }
}
