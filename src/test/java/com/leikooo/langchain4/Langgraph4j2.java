package com.leikooo.langchain4;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.leikooo.langchain4.manager.CosManager;
import com.leikooo.langchain4.model.ExecutionStatus;
import com.leikooo.langchain4.service.GenerateImage;
import com.leikooo.langchain4.service.ImageService;
import com.leikooo.langchain4.tool.SearchTool;
import com.qcloud.cos.model.PutObjectResult;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * @author <a href="https://github.com/lieeew">leikooo</a>
 * @date 2025/6/30
 * @description
 */
@Slf4j
@SpringBootTest
public class Langgraph4j2 {
    @Resource
    private ChatModel chatModel;

    @Resource
    private GenerateImage generateImage;

    @Resource
    private CosManager cosManager;

    private String text = """
            当你一边忙着加班，一边觉得生活还挺有趣。
            
            “当我看到同事们在加班时，我想，我是不是也该‘努力’一下？”
            “然后我决定继续‘努力’喝咖啡。”
            
            晚上十点，办公楼灯光依旧明亮，键盘的敲击声比午休时还要清晰，耳边是一个接一个的会议通知。
            但我并没有焦虑——毕竟，每次有人说“加班很辛苦”时，我总是微笑着点点头，心里想：这不就是生活的意义吗？
            
            “或许，工作就是为了让自己更‘忙’，然后找到一种‘宁静’。”
            
            尽管如此，我还是不敢抛下我的任务，毕竟，时间就是金钱，虽然我也知道那只是老板的金钱。
            
            “等一下，是不是该检查下这个表格了？”
            “哦，不，我可以再处理一份报告。”
            突然间，时间已经到了凌晨两点，屏幕前闪烁的代码依然让人有些模糊，但我依然在坚持。
            没有什么能够阻挡我追求工作的决心，除了... 眼皮越来越重。
            
            """;

    private String promote = """
            You are an expert in making articles funny and engaging by adding memes (表情包).
            Your task is to analyze the following text, identify the best places to insert meme images, and create corresponding search prompts for them.
            
            Rules:
            1. Insert placeholders like `[IMAGE_1]`, `[IMAGE_2]`, etc., into the text.
            2. For each placeholder, create a funny and descriptive prompt that can be used to either search for a meme or generate one. The prompt should capture the emotion and context of that part of the text.
            3. Return the modified text and a dictionary mapping placeholders to their prompts.
            4. use chinses language to response result json map for value 
            """;
    private String aiResponse = """
            {
                "modifiedText": "当你一边忙着加班，一边觉得生活还挺有趣。[IMAGE_1]\\n\\n“当我看到同事们在加班时，我想，我是不是也该‘努力’一下？”[IMAGE_2]\\n“然后我决定继续‘努力’喝咖啡。”[IMAGE_3]\\n\\n晚上十点，办公楼灯光依旧明亮，键盘的敲击声比午休时还要清晰，耳边是一个接一个的会议通知。[IMAGE_4]\\n但我并没有焦虑——毕竟，每次有人说“加班很辛苦”时，我总是微笑着点点头，心里想：这不就是生活的意义吗？[IMAGE_5]\\n\\n“或许，工作就是为了让自己更‘忙’，然后找到一种‘宁静’。”[IMAGE_6]\\n\\n尽管如此，我还是不敢抛下我的任务，毕竟，时间就是金钱，虽然我也知道那只是老板的金钱。[IMAGE_7]\\n\\n“等一下，是不是该检查下这个表格了？”[IMAGE_8]\\n“哦，不，我可以再处理一份报告。”[IMAGE_9]\\n突然间，时间已经到了凌晨两点，屏幕前闪烁的代码依然让人有些模糊，但我依然在坚持。[IMAGE_10]\\n没有什么能够阻挡我追求工作的决心，除了... 眼皮越来越重。[IMAGE_11]",
                "imagePrompts": {
                    "IMAGE_1": "哈哈哈",
                    "IMAGE_2": "搞笑图片"
                }
            }
            """;

    private String urlPromote = """
            Only return valid URLs. Do not return any other information. If no relevant URL is found, return an empty string.
            """;

    static AsyncNodeAction<MessagesState<String>> makeNode(String message) {
        return node_async(state -> Map.of("messages", message));
    }

    AsyncNodeAction<MessagesState<String>> makeNodeSleep(String key, String message) {
        return node_async(state -> Map.of(key, message));
    }

    AsyncNodeAction<MessagesState<String>> searchImage(String key, String message) {
        return node_async(state -> {
            // 调用 tools 获取到表情包链接
            log.info("请求的 key {}  请求的 value {}", key, message);
            ImageService imageService = AiServices.builder(ImageService.class)
                    .chatModel(chatModel)
                    .systemMessageProvider(s -> urlPromote)
                    .tools(new SearchTool()).build();
            String ask = imageService.ask("帮我找一下关于 熊猫 的表情包");
            // ask 收否包含对应图片的 url
            if (StrUtil.isBlank(ask)) {
                return Map.of("messages",  ExecutionStatus.builder().isSuccess(false).message(message).build());
            }
            return Map.of(key, ExecutionStatus.builder().isSuccess(true).message(message).build());
        });
    }

    AsyncNodeAction<MessagesState<String>> generateImage(String key, String message) {
        return node_async(state -> {
            ExecutionStatus executionStatus = (ExecutionStatus) state.data().get(key);
            if (Objects.isNull(executionStatus) || !executionStatus.isSuccess()) {
                return Map.of("messages", executionStatus.getMessage());
            }
            log.info("generate 请求的 key {}  请求的 value {}", key, message);
            ImageSynthesisResult result = generateImage.generateImage(message);
            if (Objects.isNull(result)) {
                return Map.of(key, message);
            }
            PutObjectResult putObjectResult = cosManager.putObject(key, HttpUtil.downloadFileFromUrl(result.getOutput().getResults().getFirst().get("url"),
                    System.getProperty("user.dir")) + File.separator + UUID.randomUUID().toString().substring(0, 5) + ".png");
            return Map.of(key, "对应的 url");
        });
    }

    AsyncNodeAction<MessagesState<String>> makeNodeGetStruct(String message) {
        UserMessage userMessage = UserMessage.from(message);
        ChatRequest chatRequest = ChatRequest.builder().messages(SystemMessage.from(promote), userMessage).build();
        return node_async(state -> {
            System.out.println("chatModel.chat(chatRequest) = " + chatModel.chat(chatRequest));
            Thread.sleep(100000);
            return Map.of("messages", message);
        });
    }


    @Test
    public void test() throws GraphStateException {
        CompiledGraph<MessagesState<String>> workflow = new MessagesStateGraph<String>()
                .addNode("A", makeNodeGetStruct(text))
                .addNode("C", makeNode("C"))
                .addEdge("A", "C")
                .addEdge(START, "A")
                .addEdge("C", END)
                .compile();
        for (var step : workflow.stream(Map.of())) {
            System.out.println(step);
        }
    }

    // todo important
    @Test
    public void finalTest() throws GraphStateException {
        // 提前获取到 json 字符串
        Content content = JSONUtil.toBean(aiResponse, Content.class);
        MessagesStateGraph<String> strngMessagesStateGraph = new MessagesStateGraph<>();
        strngMessagesStateGraph.addNode("A", makeNodeSleep("A", "A"));
        for (Map.Entry<String, String> entry : content.imagePrompts.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            strngMessagesStateGraph.addNode(key, searchImage(key, value));
            strngMessagesStateGraph.addEdge(key, "");
            strngMessagesStateGraph.addEdge("A", key);
        }
        CompiledGraph<MessagesState<String>> workflow = strngMessagesStateGraph
                .addNode("B", makeNodeSleep("B", "B"))
                // 获取到对应的 node 并且还需要排序
                .addEdge(START, "A")
                .addEdge("B", END)
                .compile();
        for (var step : workflow.stream(Map.of())) {
            System.out.println(step);
        }
    }

    @Test
    public void test3() throws GraphStateException {
        ImageService imageService = AiServices.builder(ImageService.class)
                .chatModel(chatModel)
                .systemMessageProvider(s -> "帮我查找对应的关键字的表情包，对应的图片的 URL，给我单纯的 URL 即可不要生成任何多余的内容")
                .tools(new SearchTool()).build();
        String ask = imageService.ask("帮我找一下关于 熊猫 的表情包");
        System.out.println("ask = " + ask);
    }


    /**
     * demo
     */
    public static void main(String[] args) throws GraphStateException {
        var subgraphA3 = new MessagesStateGraph<String>()
                .addNode("A3.1", makeNode("A3.1"))
                .addNode("A3.2", makeNode("A3.2"))
                .addEdge(START, "A3.1")
                .addEdge( "A3.1", "A3.2")
                .addEdge("A3.2", END)
                .compile();
        var subgraphA1 = new MessagesStateGraph<String>()
                .addNode("A1.1", makeNode("A1.1"))
                .addNode("A1.2", makeNode("A1.2"))
                .addEdge(START, "A1.1")
                .addEdge( "A1.1", "A1.2")
                .addEdge("A1.2", END)
                .compile();

        var workflow = new MessagesStateGraph<String>()
                .addNode("A", makeNode("A"))
                .addNode("A1", subgraphA1)
                .addNode("A2", makeNode("A2"))
                .addNode("A3", subgraphA3)
                .addNode("B", makeNode("B"))
                .addEdge("A", "A1")
                .addEdge("A", "A2")
                .addEdge("A", "A3")
                .addEdge("A1", "B")
                .addEdge("A2", "B")
                .addEdge("A3", "B")
                .addEdge(START, "A")
                .addEdge("B", END)
                .compile();
        for (var step : workflow.stream(Map.of())) {
            System.out.println(step);
        }
    }

    @Data
    public class Content implements Serializable {

        private String modifiedText;

        private Map<String, String> imagePrompts;

    }


    /**
     * 未跑通 =》 跑通
     * @throws GraphStateException
     */
    @Test
    public void  lowerLevelApiTest() throws GraphStateException {
        // 调用 tools 获取到表情包链接
        UserMessage userMessage = UserMessage.from("帮我找一下关于 熊猫 的表情包");
        List<ToolSpecification> toolSpecifications = ToolSpecifications.toolSpecificationsFrom(new SearchTool());
        ChatRequest chatMessage = ChatRequest.builder().toolSpecifications(toolSpecifications)
                .messages(userMessage).build();
        ChatResponse chat = chatModel.chat(chatMessage);
        if (chat.aiMessage().hasToolExecutionRequests()) {
            ToolExecutionRequest toolExecutionRequest = chat.aiMessage().toolExecutionRequests().get(0);
            String result = "// 需要自己自己调用相关的方法";
            ToolExecutionResultMessage toolExecutionResultMessage = ToolExecutionResultMessage.from(toolExecutionRequest, result);
            ChatRequest request2 = ChatRequest.builder()
                    .messages(List.of(userMessage, chat.aiMessage(), toolExecutionResultMessage))
                    .toolSpecifications(toolSpecifications)
                    .build();
            ChatResponse response2 = chatModel.chat(request2);
            System.out.println("response2 = " + response2);
        }
        log.info("chat 是 {}", chat.toString());
    }
}
