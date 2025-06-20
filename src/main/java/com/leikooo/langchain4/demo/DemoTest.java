package com.leikooo.langchain4.demo;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;

import java.lang.annotation.Repeatable;

/**
 * @author <a href="https://github.com/lieeew">leikooo</a>
 * @date 2025/6/20
 * @description
 */
@Component
public class DemoTest implements CommandLineRunner {
    @Resource
    private QwenChatModel qwenChatModel;


    @Override
    public void run(String... args) throws Exception {
        String helloWorld = qwenChatModel.chat("帮我分析一下下面这句话对于程序员的魅力 hello world");
        System.out.println("helloWorld = " + helloWorld);
    }
}
