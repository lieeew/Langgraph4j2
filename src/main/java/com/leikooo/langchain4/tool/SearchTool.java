package com.leikooo.langchain4.tool;

import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import com.google.errorprone.annotations.SuppressPackageLocation;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author <a href="https://github.com/lieeew">leikooo</a>
 * @date 2025/6/23
 * @description
 */
@Slf4j
public class SearchTool {
    @Tool("get memes from website")
    String searchMemes(@P("keyword to search for memes") String key) {
        log.info("正在搜索表情包，关键词：{}", key);
        // 实际的工具执行代码
        String url = String.format("https://api.doutub.com/api/bq/getBqlistByKeyword?keyword=%s&curPage=1&pageSize=20", URLUtil.encode(key));
        return HttpUtil.get(url);
    }
}
