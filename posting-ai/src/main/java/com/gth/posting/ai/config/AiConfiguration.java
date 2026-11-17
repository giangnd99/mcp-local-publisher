package com.gth.posting.ai.config;

import com.gth.posting.ai.adapter.PassThroughContentCritic;
import com.gth.posting.ai.adapter.SpringAiContentCritic;
import com.gth.posting.ai.adapter.SpringAiCopywriter;
import com.gth.posting.ai.adapter.TemplateCopywriter;
import com.gth.posting.application.port.out.ai.ContentCritic;
import com.gth.posting.application.port.out.ai.Copywriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Chon hien thuc AI hay fallback dua tren cau hinh, khong dua tren su co mat
 * cua bean luc chay. Nho vay tat AI la mot dong yml, khong phai sua code.
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AiConfiguration.class);

    @Bean
    public Copywriter copywriter(AiProperties properties, ObjectProvider<ChatClient.Builder> builderProvider) {
        ChatClient.Builder builder = properties.isEnabled() ? builderProvider.getIfAvailable() : null;
        if (builder == null) {
            log.info("Lop AI dang tat hoac chua co ChatClient: dung TemplateCopywriter");
            return new TemplateCopywriter();
        }
        log.info("Lop AI dang bat: dung SpringAiCopywriter");
        return new SpringAiCopywriter(builder.build(), properties);
    }

    @Bean
    public ContentCritic contentCritic(AiProperties properties, ObjectProvider<ChatClient.Builder> builderProvider) {
        ChatClient.Builder builder = properties.isEnabled() ? builderProvider.getIfAvailable() : null;
        if (builder == null) {
            return new PassThroughContentCritic();
        }
        return new SpringAiContentCritic(builder.build());
    }
}
