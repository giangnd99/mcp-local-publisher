package com.gth.posting.ai.adapter;

import com.gth.posting.ai.prompt.CopyPromptFactory;
import com.gth.posting.application.port.out.ai.ContentCritic;
import com.gth.posting.application.port.out.ai.CopyBrief;
import com.gth.posting.application.port.out.ai.CritiqueResult;
import org.springframework.ai.chat.client.ChatClient;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Model thu hai cham bai. Parse JSON bang regex de khong phu thuoc
 * vao viec model co tuan thu format hay khong.
 */
public class SpringAiContentCritic implements ContentCritic {

    private static final Pattern ACCEPTABLE = Pattern.compile("\"acceptable\"\\s*:\\s*(true|false)");
    private static final Pattern ISSUE = Pattern.compile("\"([^\"]{8,200})\"");

    private final ChatClient chatClient;

    public SpringAiContentCritic(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public CritiqueResult critique(String text, CopyBrief brief) {
        String raw = chatClient.prompt()
                .system(CopyPromptFactory.criticSystem())
                .user(CopyPromptFactory.criticUser(text, brief))
                .call()
                .content();

        if (raw == null || raw.isBlank()) {
            return CritiqueResult.rejected(List.of("Critic khong tra ve ket qua"));
        }
        Matcher acceptable = ACCEPTABLE.matcher(raw);
        if (!acceptable.find()) {
            return CritiqueResult.rejected(List.of("Khong doc duoc ket qua cham: " + raw.strip()));
        }
        if ("true".equals(acceptable.group(1))) {
            return CritiqueResult.ok();
        }
        List<String> issues = new ArrayList<>();
        Matcher issue = ISSUE.matcher(raw);
        while (issue.find()) {
            String value = issue.group(1);
            if (!value.equals("acceptable") && !value.equals("issues")) {
                issues.add(value);
            }
        }
        return CritiqueResult.rejected(issues);
    }
}
