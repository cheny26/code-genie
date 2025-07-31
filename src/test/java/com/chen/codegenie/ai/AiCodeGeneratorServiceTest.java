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
        HtmlCodeResult res=aiCodeGeneratorService.generateHtmlCode("做个留言板网站，不超过50行代码");
        Assertions.assertNotNull(res);
    }

    @Test
    void generateMultiFileCode() {
        //String res=aiCodeGeneratorService.generateMultiFileCode("做个留言板网站，不超过50行代码");
        MultiFileCodeResult res=aiCodeGeneratorService.generateMultiFileCode("做个留言板网站，不超过50行代码");
        Assertions.assertNotNull(res);
    }
}