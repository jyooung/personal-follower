package com.springcloud.personalfollower.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GptService {

    private final OpenAiService openAiService;

    public GptService(@Value("${openai.api.key}") String apiKey) {
        this.openAiService = new OpenAiService(apiKey);
    }

    public List<String> recommendKeywords(List<String> categories) {
        String systemPrompt = "너는 유튜브 콘텐츠 큐레이터야. 사용자의 관심사는 " +
                String.join(", ", categories) + "이야. 이 사용자에게 오늘 보면 좋을 유튜브 영상 키워드나 제목 3개를 추천해줘.";

        ChatMessage systemMessage = new ChatMessage("system", systemPrompt);
        ChatMessage userMessage = new ChatMessage("user", "추천해줘");

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(systemMessage, userMessage))
                .maxTokens(300)
                .build();

        ChatCompletionResult result = openAiService.createChatCompletion(request);
        String response = result.getChoices().get(0).getMessage().getContent();

        return Arrays.stream(response.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> s.replaceAll("^\\d+\\.|^-", "").trim())
                .collect(Collectors.toList());
    }
}