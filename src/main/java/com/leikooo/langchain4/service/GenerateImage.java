package com.leikooo.langchain4.service;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author <a href="https://github.com/lieeew">leikooo</a>
 * @date 2025/7/1
 * @description
 */
@Component
@Slf4j
public class GenerateImage {

    @Value("${langchain4j.community.dashscope.chat-model.api-key}")
    private String dashscopeChatModelApiKey;

    /**
     * 生成时间大概是 2 min
     * @param promote
     * @return
     */
    public ImageSynthesisResult generateImage(String promote) {
        ImageSynthesisParam param =
                ImageSynthesisParam.builder()
                        .apiKey(dashscopeChatModelApiKey)
                        .model("wanx2.1-t2i-turbo")
                        .prompt(promote)
                        .n(1)
                        .size("512*512")
                        .build();
        ImageSynthesis imageSynthesis = new ImageSynthesis();
        ImageSynthesisResult result = null;
        try {
            System.out.println("---sync call, please wait a moment----");
            return imageSynthesis.call(param);
        } catch (ApiException | NoApiKeyException e){
            log.error("AI 生成图片报错 {}", e.getMessage());
        }
        return null;
    }
}
