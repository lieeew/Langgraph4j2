package com.leikooo.langchain4;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.leikooo.langchain4.manager.CosManager;
import com.leikooo.langchain4.model.UrlResponse;
import com.leikooo.langchain4.service.GenerateImage;
import com.leikooo.langchain4.service.ImageService;
import com.leikooo.langchain4.tool.SearchTool;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.service.AiServices;
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

import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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


    private Langgraph4j2.Content contentData = null;

    private String text = """
                早餐价格又涨价了，
                
                酸菜包 2.8 元 + 烧麦 2.8 元 + 茶叶蛋 2.5 元 + 豆浆 3.5 元。
    
                还记得前几年吃同样的早餐大概是 8 元，这两天发现早餐价格需要 10.8 元了 😂 离谱。
    
                另外上海早餐真的是太少了，不是包子就是煎饼果子，太难了
            """;

    private String text1 = """
            以下是 **去掉所有 URL 和图片链接后的文本内容**，内容结构、语义和格式均保留，图片替换为空行保留布局：
            
            ---
            
            这是一个加载慢慢慢慢慢到不行的网站，据说是由一位低级程序员鱼皮开发的，等了几分钟都没加载完：
            
            你能想到多少种办法，来拯救这个网站的加载速度呢？
            
            我能想到 **至少 12 种**，如果你能想到更多方法，先受我一拜，你真的很厉害；如果你想到的方法比我少，那么这期内容，一定会让你有收获。
            
            下面我们就来聊聊《网站性能优化》。
            
            ## 如何测量网站性能？
            
            衡量网站性能的指标非常多，比如首屏加载时间、白屏时间、可交互时间等等。
            
            但这里为了帮助大家理解，我们主要关注用户最直观能感受到的 **网站加载时长**。
            
            怎么测量网站加载时长呢？
            
            最简单的方法就是按 F12 打开浏览器的开发者工具，切换到 Network 网络面板，刷新页面就能看到每个资源的加载时间了。
            
            当然，还有更专业的网站性能分析工具，在本期的最后会分享。
            
            ## 网站性能优化的关键
            
            虽然网站性能优化的方法非常多，但思路很简单。
            
            问个问题，大家都收过快递吧？快递是怎么送到你家的呢？
            
            首先，商家从仓库把商品打包，然后通过物流网络和快递员配送到你手里，你拆开包裹就能使用了。
            
            访问网站也是一样的：**从服务器获取到网站文件，然后在浏览器中加载**。
            
            要让网站访问更快，我们可以从三个方向来优化：
            
            * 网站传输更快
            * 网站体积更小
            * 网站加载更快
            
            下面我们就按照这些方向，来优化现在这个要加载 3 分多钟的辣鸡网站，看看最后能优化到多少秒。
            
            ## 一、网站传输优化
            
            想要更快获取到网站文件，我们可以按照网站文件传输的路径 **服务器 => 网络传输 => 客户端** 进行优化。
            
            ### 升级服务器配置
            
            毫无疑问，从服务器获取网站文件是需要网络的，服务器带宽越大，网速越快，网站文件下载越快。
            
            所以如果你不知道怎么优化网站性能，最简单粗暴的方法就是加钱！升级服务器的带宽！
            
            比如我把 2M 带宽的小水管升级到 8M，网站加载时长就从 3 分钟优化到了 40 秒，速度优化了 4 倍多！
            
            不过一般来说，对于个人小网站，1-5M 就够用了，毕竟带宽挺贵的。
            
            有同学会问了，光升级带宽就够了么？升级内存、CPU、硬盘有没有用？
            
            这就要看你网站的类别了，对于纯静态网站来说，服务器要做的就是把网站文件发送出去，这个过程主要受带宽限制。但如果你的网站有复杂的后端逻辑，那 CPU 和内存就很重要了。
            
            ### CDN 缓存加速
            
            如果用户离我们的网站服务器较远，传输网站文件的时间就会更长，很影响体验。
            
            如何解决这个问题呢？我们不妨类比一下网购，平台会在全国建立区域仓库，提前把热门商品分配到各地仓库，用户下单后从最近的仓库发货，而不是都从总仓发货，就能更快收货。
            
            这就是 CDN 内容分发网络的原理，提前从源服务器获取到网站文件并缓存到全国各地的节点，用户访问时就可以直接从最近的节点获取资源。不仅延迟更低，而且能同时支持更多用户的访问。
            
            我们使用云服务平台配置一下 CDN，指定原始网站服务器作为源站。
            
            然后设置缓存，可以只缓存图片等媒体资源，也可以缓存整套网站文件，这里我全都要。
            
            试一下效果，首次访问会比较慢，因为 CDN 节点还没有缓存，需要从网站服务器拉取文件；之后速度就飚起来了，直接从 40 秒优化到了 6 秒，性能优化了 6 倍多！效果显著。
            
            不过 CDN 可是把双刃剑，按流量计费，鉴于我被刷了上万元流量费的血泪经验，建议 CDN 能不用就不用，即使要用 CDN 也要做好访问频率限制、用量封顶配置和监控告警。
            
            ### 浏览器缓存
            
            除了 CDN 外，还有一个更彻底的优化方案：**让网站文件根本不用传输**！
            
            这就是 **浏览器缓存** 的作用，将已经请求过的网站文件存储到用户本地，下次再访问网站时，都不用去找服务器了，直接从本地加载资源。
            
            我们可以通过 Web 服务器的 HTTP 缓存头配置或者 CDN 的浏览器缓存过期配置来更改缓存策略，更新不频繁的网站缓存时间可以设置长一些。
            
            我这里设置为 1 小时，效果很明显，直接从 6 秒优化到了 1.69 秒，不过理论上还可以更快。
            
            这样一来，我们就形成了一个完整的网站缓存体系：**CDN 缓存解决地理距离问题，浏览器缓存解决重复访问的问题**。实际情况下两种方法建议结合使用。
            
            ### 升级 HTTP 协议
            
            此外，想要升级网站传输的速度，可以升级请求协议到 HTTP/2。
            
            相比于 HTTP/1.1，HTTP/2 最大的改进是 **多路复用**。HTTP/1.1 虽然可以建立多个连接，但每个连接内的请求必须按顺序处理，容易产生队头阻塞问题。而 HTTP/2 在单个连接上就能同时处理多个请求，真正实现了并行传输。
            
            升级 HTTP/2 的方式很简单，只需要在 Web 服务器（比如 Nginx）添加配置：
            
            ```nginx
            server {
                listen 443 ssl http2;
                server_name your-domain.com;
            
                ssl_certificate /path/to/cert.pem;
                ssl_certificate_key /path/to/key.pem;
            
                # 其他配置...
            }
            ```
            
            如果你用的是 CDN，只需要在 CDN 配置页面一键开启 HTTP/2 即可：
            
            测试一下效果，这次没有用到本地缓存，网站加载时长也从 6 秒缩短到了 1.6 秒，性能优化了 3 倍多！
            
            仅仅点了一下按钮，速度就上来了，是不是没想到？
            
            那你可能问了，现在不是还有 HTTP/3 吗？
            
            HTTP/3 确实更先进，它基于 QUIC 协议，有更快的连接建立速度、更好的多路复用性能和更少的队头阻塞问题，但兼容性和稳定性还需要时间验证，选用 HTTP/2 就足够了。
            
            至此，在没有改变网站本身的情况下，我们就已经把网站加载时间优化到了秒级！
            
            ---
            
            **后续内容还有很多，如需我继续处理去 URL 后的第二部分（体积优化、加载优化等），请告诉我是否继续？**
            
                    
            """;

    private String promote = """
            You are an expert in making articles funny and engaging by adding memes (表情包).
            Your task is to analyze the following text, identify the best places to insert meme images, and create corresponding search prompts for them.
            
            Rules:
            1. Insert placeholders like `[IMAGE_1]`, `[IMAGE_2]`, etc., into the text.
            2. For each placeholder, create a funny and descriptive prompt that can be used to either search for a meme or generate one. The prompt should capture the emotion and context of that part of the text.
            3. Return the modified text and a dictionary mapping placeholders to their prompts.
            4. use chinses language to response result json map for value 
            5. Strictly follow the format below json formate ！

            Example:
            1、modifiedText 修改之后的文本
            2、imagePrompts 是对应的 key 和 value 
            {
                "modifiedText": "",
                "imagePrompts": {
                  "IMAGE_1": ""
                }
            }
            """;
    private String aiResponse = """
            {
                "modifiedText": "当你一边忙着加班，一边觉得生活还挺有趣。[IMAGE_1]\\n\\n“当我看到同事们在加班时，我想，我是不是也该‘努力’一下？”[IMAGE_2]\\n“然后我决定继续‘努力’喝咖啡。”[IMAGE_3]\\n\\n晚上十点，办公楼灯光依旧明亮，键盘的敲击声比午休时还要清晰，耳边是一个接一个的会议通知。[IMAGE_4]\\n但我并没有焦虑——毕竟，每次有人说“加班很辛苦”时，我总是微笑着点点头，心里想：这不就是生活的意义吗？[IMAGE_5]\\n\\n“或许，工作就是为了让自己更‘忙’，然后找到一种‘宁静’。”[IMAGE_6]\\n\\n尽管如此，我还是不敢抛下我的任务，毕竟，时间就是金钱，虽然我也知道那只是老板的金钱。[IMAGE_7]\\n\\n“等一下，是不是该检查下这个表格了？”[IMAGE_8]\\n“哦，不，我可以再处理一份报告。”[IMAGE_9]\\n突然间，时间已经到了凌晨两点，屏幕前闪烁的代码依然让人有些模糊，但我依然在坚持。[IMAGE_10]\\n没有什么能够阻挡我追求工作的决心，除了... 眼皮越来越重。[IMAGE_11]",
                "imagePrompts": {
                  "IMAGE_1": "熊猫",
                  "IMAGE_2": "一只在雨中歌唱的青蛙",
                  "IMAGE_3": "非常离谱的很离谱烟花哦",
                  "IMAGE_3": "找不到图片"
                }
            }
            """;

    private String urlPromote = """
           You are a URL extraction API. Your sole function is to extract relevant URLs based on the user's query.
            
            
           Rule 
           1.  First, get the keyword. Use a simple, separate prompt to have the AI summarize the user's request into a four-character Chinese keyword.
           2.  Then, get the JSON Use the optimized prompt above, inserting the clean keyword you got from the first step into the final.
           3.  If no URLs are found make success false 
           4.  Only return json bellow do not contains any another word
           5.  Strictly follow the format below json formate ！
           6.  if user query is null then success is false 
           7.  if have multiple result just return one url like https://example.com/cat1.gif not return list 
           
           Example：
            
           {
              "success": true,
              "data": "https://example.com/cat1.gif"
           }
            
           """;

    AsyncNodeAction<MessagesState<String>> makeNodeEnd() {
        return node_async(state -> {
            // 汇总最终的结果
            Map<String, Object> resultData = state.data();
            resultData.forEach((key, value) -> {
                if (Objects.isNull(value)) {
                    return;
                }
                if (!(value instanceof UrlResponse urlResponse)) {
                    return;
                }
                if (Objects.isNull(contentData)) {
                    return;
                }
                if(contentData.getModifiedText().contains(key)) {
                    log.info("找到对应的key {} 替换为对应图片的 URL {}", key, String.format("![](%s)", urlResponse.getData()));
                    contentData.setModifiedText(contentData.getModifiedText().replace("[" + key + "]", String.format("![](%s)", urlResponse.getData())));
                }
            });
            // 替换文章里面的占位符
            log.error("最终文章的结果：{}", contentData.getModifiedText());
            return Map.of();
        });
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
            // 调用 tools 获取到表情包链接
            log.info("请求的 key {}  请求的 value {}", key, message);
            ImageService imageService = AiServices.builder(ImageService.class)
                    .chatModel(chatModel)
                    .systemMessageProvider(s -> urlPromote)
                    .tools(new SearchTool()).build();
            String ask = imageService.ask(String.format("帮我找一下关于 %s 的表情包", message));
            log.info("<UNK> ask {}", ask);
            UrlResponse urlResponse = null;
            try {
                urlResponse = JSONUtil.toBean(ask, UrlResponse.class);;
                if (!urlResponse.getSuccess()) {
                    return Map.of(key, UrlResponse.builder().success(false).data(message).build());
                }
            } catch (Exception e) {
                log.error("AI 生成内容出现问题");
                return Map.of(key, UrlResponse.builder().success(false).data(message).build());
            }
            return Map.of(key, urlResponse);
        });
    }

    /**
     * 生成节点 (第二步，备用方案):
     * 此节点只在搜索失败时被触发。
     *
     * @param key     状态图中唯一的键
     * @param message
     * @return 异步节点动作
     */
    AsyncNodeAction<MessagesState<String>> generateImage(String key, String message) {
        return node_async(state -> {
            UrlResponse executionStatus = (UrlResponse) state.data().get(key);
            if (Objects.nonNull(executionStatus) &&  executionStatus.getSuccess()) {
                return Map.of(key, executionStatus);
            }
            log.info("generate 请求的 key {}  请求的 value {}", key, message);
            ImageSynthesisResult result = generateImage.generateImage(message);
            if (Objects.isNull(result)) {
                return Map.of(key, UrlResponse.builder().success(false).build());
            }
            if ("FAILED".equals(result.getOutput().getTaskStatus())) {
                log.error("生成图片失败，失败原因 {}", result);
                return Map.of(key, UrlResponse.builder().success(false).build());
            }
            String imageUrl = result.getOutput().getResults().getFirst().get("url");
            URL url = new URL(imageUrl);
            String fileName = Paths.get(url.getPath()).getFileName().toString();
            cosManager.putObject("langgraph4j/image/" + fileName, HttpUtil.downloadFileFromUrl(imageUrl, FileUtil.createTempFile()));
            return Map.of(key, UrlResponse.builder().success(true).data("https://leeikoooo-1313589692.cos.ap-beijing.myqcloud.com/langgraph4j/image/" + fileName).build());
        });
    }

    @Test
    public void finalTestMatchingTheDiagram() throws GraphStateException, InterruptedException {
        // 使用 AI 返回具体的 JSON 类型的文章插图
        UserMessage userMessage = UserMessage.from(text);
        ChatRequest chatRequest = ChatRequest.builder().messages(SystemMessage.from(promote), userMessage).build();

        String aiResult = chatModel.chat(chatRequest).aiMessage().text();

        contentData = JSONUtil.toBean(aiResult, Langgraph4j2.Content.class);
        log.info("AI 返回的 contentData  {}", contentData);
        MessagesStateGraph<String> workflow = new MessagesStateGraph<>();

        final String START_NODE = "A_Start";
        final String GATHER_NODE = "B_Gather";

        List<CompiledGraph<MessagesState<String>>> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : contentData.getImagePrompts().entrySet()) {
            String value = entry.getValue();
            String key = entry.getKey();
            CompiledGraph<MessagesState<String>> compile = new MessagesStateGraph<String>()
                    .addNode(key + "1)", searchImage(key, value))
                    .addNode(key + "2)", generateImage(key, value))
                    .addEdge(START, key + "1)")
                    .addEdge(key + "1)", key + "2)")
                    .addEdge(key + "2)", END)
                    .compile();
            list.add(compile);
        }

        workflow.addNode(START_NODE, makeNodeStart());
        workflow.addNode(GATHER_NODE, makeNodeEnd());

        workflow.addEdge(START, START_NODE);
        workflow.addEdge(GATHER_NODE, END);

        for (int i = 0; i < list.size(); i++) {
            String subgraphNodeName = "Subgraph_" + i;
            workflow.addNode(subgraphNodeName, list.get(i));
            workflow.addEdge(START_NODE, subgraphNodeName);
            workflow.addEdge(subgraphNodeName, GATHER_NODE);
        }

        CompiledGraph<MessagesState<String>> compile = workflow.compile();
        System.out.println(compile.getGraph(GraphRepresentation.Type.MERMAID));

        for (var item : compile.stream(Map.of())) {
            System.out.println(item);
        }
    }

    private AsyncNodeAction<MessagesState<String>> makeNodeStart() {
        return node_async(state -> Map.of());
    }

}