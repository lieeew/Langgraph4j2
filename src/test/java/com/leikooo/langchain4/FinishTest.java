package com.leikooo.langchain4;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.leikooo.langchain4.manager.CosManager;
import com.leikooo.langchain4.model.ExecutionStatus;
import com.leikooo.langchain4.service.GenerateImage;
import dev.langchain4j.model.chat.ChatModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
public class FinishTest {
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
                    "IMAGE": "熊猫"
                 }
            }
            """;

    private String urlPromote = """
            Only return valid URLs. Do not return any other information. If no relevant URL is found, return an empty string.
            """;

    static AsyncNodeAction<MessagesState<String>> makeNode(String key, String message) {
        return node_async(state -> Map.of());
    }


    /**
     * 搜索节点 (第一步):
     * 尝试查找图片，如果找不到，则返回一个 "失败" 状态，并将原始 prompt 传递下去。
     *
     * @param key     状态图中唯一的键
     * @param message 搜索的提示词
     * @return 异步节点动作
     */
    AsyncNodeAction<MessagesState<String>> searchImage(String key, String message) {
        return node_async(state -> {
            log.info("22222222222");

            log.info("【搜索节点】开始搜索, key: {}, prompt: {}", key, message);
            // --- 模拟调用 tool ---
            String foundUrl = null;
            if (message.contains("熊猫")) { // 模拟只有"熊猫"能被找到
                foundUrl = "http://example.com/found_panda.png";
            }
            // --- 模拟结束 ---

            if (StrUtil.isNotBlank(foundUrl)) {
                log.info("【搜索节点】成功找到图片, key: {}, url: {}", key, foundUrl);
                // 成功，将带有URL的成功状态放入state
                return Map.of(key, ExecutionStatus.builder().isSuccess(true).message(foundUrl).build());
            } else {
                log.warn("【搜索节点】未找到图片, key: {}, 将触发生成流程", key);
                // 失败，将失败状态和原始 prompt 放入state，以便后续生成节点使用
                return Map.of(key, ExecutionStatus.builder().isSuccess(false).message(message).build());
            }
        });
    }

    /**
     * 生成节点 (第二步，备用方案):
     * 此节点只在搜索失败时被触发。
     *
     * @param key 状态图中唯一的键
     * @return 异步节点动作
     */
    AsyncNodeAction<MessagesState<String>> generateImage(String key) {
        return node_async(state -> {

            log.info("111111111111111");
            ExecutionStatus previousStatus = (ExecutionStatus) state.data().get(key);

            // 安全检查：理论上只有搜索失败才会进入此节点
            if (previousStatus == null || previousStatus.isSuccess()) {
                log.warn("【生成节点】跳过，因为没有找到失败的搜索状态或状态为成功, key: {}", key);
                return Map.of();
            }

            String promptForGeneration = previousStatus.getMessage();
            log.info("【生成节点】开始生成图片, key: {}, prompt: {}", key, promptForGeneration);

            // --- 模拟调用生成接口 ---
            String generatedUrl = "http://example.com/generated_" + promptForGeneration + ".png";
            log.info("【生成节点】成功生成并上传图片, key: {}, url: {}", key, generatedUrl);
            // --- 模拟结束 ---

            // 更新state，用成功生成的结果替换掉之前的失败状态
            return Map.of(key, ExecutionStatus.builder().isSuccess(true).message(generatedUrl).build());
        });
    }

    @Test
    public void finalTestMatchingTheDiagram() throws GraphStateException, InterruptedException {
        Langgraph4j2.Content content = JSONUtil.toBean(aiResponse, Langgraph4j2.Content.class);

        MessagesStateGraph<String> workflow = new MessagesStateGraph<>();

        // 定义起始节点和汇聚节点 (对应图中的 A 和 B)
        final String START_NODE = "A_Start";
        final String GATHER_NODE = "B_Gather";

//        workflow.addNode(START_NODE, makeNode("A_Start", "开始并行处理..."));
//        workflow.addNode(GATHER_NODE, makeNode("B_Gather", "所有分支汇聚完成。"));

        // 定义整个工作流的入口和出口
        workflow.addEdge(START, START_NODE);
        workflow.addEdge(GATHER_NODE, END);
        List<CompiledGraph<MessagesState<String>>> list = new ArrayList<>();
        // --- 这里是关键：创建并行分支，对应图中的 A -> A1, A3 ---
        for (Map.Entry<String, String> entry : content.getImagePrompts().entrySet()) {
            String value = entry.getValue();
            String key = entry.getKey();
            CompiledGraph<MessagesState<String>> compile = new MessagesStateGraph<String>()
                    .addNode(key + "1)", searchImage(key, value))
                    .addNode(key + "2)", makeNode(key, value))
                    .addEdge(START, key + "1)")
                    .addEdge(key + "1)", key + "2)")
                    .addEdge(key + "2)", GATHER_NODE)
                    .compile();
            list.add(compile);
            workflow.addNode(START_NODE, compile);
        }
        // 编译图
        CompiledGraph<MessagesState<String>> compiledGraph = workflow
//                .addEdge(START_NODE, GATHER_NODE)
                .compile();

        System.out.println("------ 工作流定义 (最终版: Search -> Generate) ------");
        System.out.println(compiledGraph.getGraph(GraphRepresentation.Type.MERMAID)); // 这个会打印出类似您图片的结构
        System.out.println("------ 开始执行 ------");


        for (var item : compiledGraph.stream(Map.of())) {
            System.out.println(item);
        }

        System.out.println("------ 执行完毕 ------");

        Thread.sleep(10000);
    }

    /**
     * 路由判断函数 (新):
     * 检查搜索节点的结果，并返回一个表示结果的【路由键】 (例如 "SUCCESS" 或 "FAILURE")。
     * 这个函数本身不关心要去哪个节点，只负责判断当前状态。
     *
     * @param state 当前的工作流状态
     * @param key   要检查的状态的 key
     * @return 代表逻辑分支的路由键 (String)
     */
    private CompletableFuture<String> asyncRouteAfterSearch(MessagesState<String> state, String key) {
        ExecutionStatus status = (ExecutionStatus) state.data().get(key);

        String routeKey = (status != null && status.isSuccess()) ? "SUCCESS" : "FAILURE";

        // Wrap the result in a an already-completed CompletableFuture.
        return CompletableFuture.completedFuture(routeKey);
    }

}
