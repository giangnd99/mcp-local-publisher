package com.gth.posting.ai.adapter;

import com.gth.posting.ai.config.AiProperties;
import com.gth.posting.ai.prompt.CopyPromptFactory;
import com.gth.posting.application.port.out.ai.CopyBrief;
import com.gth.posting.application.port.out.ai.Copywriter;
import org.springframework.ai.chat.client.ChatClient;

/**
 * Sinh bai bang model. Khong goi truc tiep OpenAI: dung ChatClient
 * de doi provider (OpenAI, Anthropic, Ollama) chi bang cau hinh.
 */
public class SpringAiCopywriter implements Copywriter {

    private final ChatClient chatClient;
    private final AiProperties properties;

    public SpringAiCopywriter(ChatClient chatClient, AiProperties properties) {
        this.chatClient = chatClient;
        this.properties = properties;
    }

    @Override
    public String write(CopyBrief brief) {
        String content = chatClient.prompt()
                .system(CopyPromptFactory.system(properties.getTone(), brief))
                .user(CopyPromptFactory.user(brief))
                .call()
                .content();

        if (content == null || content.isBlank()) {
            throw new IllegalStateException("Model tra ve noi dung rong");
        }
        String trimmed = content.strip();
        int max = properties.getMaxCharacters();
        if (trimmed.length() > max) {
            trimmed = trimmed.substring(0, max).strip();
        }
        return trimmed;
    }
}
