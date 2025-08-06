package com.chen.codegenie.ai;

import com.chen.codegenie.ai.model.HtmlCodeResult;
import com.chen.codegenie.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;
    @Test
    void generateHtmlCode() {
        //String res=aiCodeGeneratorService.generateHtmlCode("做个留言板网站，不超过50行代码");
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(1, "做个留言板网站，总代码量不超过 20 行");
        Assertions.assertNotNull(result);
        result = aiCodeGeneratorService.generateHtmlCode(1, "不要生成网站，告诉我你刚刚做了什么？");
        Assertions.assertNotNull(result);
        result = aiCodeGeneratorService.generateHtmlCode(2, "做个todolist网站，总代码量不超过 20 行");
        Assertions.assertNotNull(result);
        result = aiCodeGeneratorService.generateHtmlCode(2, "不要生成网站，告诉我你刚刚做了什么？");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        //String res=aiCodeGeneratorService.generateMultiFileCode("做个留言板网站，不超过50行代码");
        MultiFileCodeResult res=aiCodeGeneratorService.generateMultiFileCode("做个留言板网站，不超过50行代码");
        Assertions.assertNotNull(res);
    }
}