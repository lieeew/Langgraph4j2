package com.leikooo.langchain4.demo;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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

    }
}
