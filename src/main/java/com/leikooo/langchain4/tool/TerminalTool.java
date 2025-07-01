package com.leikooo.langchain4.tool;

import dev.langchain4j.agent.tool.Tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author <a href="https://github.com/lieeew">leikooo</a>
 * @date 2025/6/23
 * @description
 */
public class TerminalTool {

    @Tool("execute command")
    void runCommand(String command) throws IOException, InterruptedException {
        Process exec = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        exec.waitFor();
    }

}
