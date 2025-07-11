package com.springcloud.personalfollower.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GptService {

    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GptService(@Value("${openai.api.key}") String apiKey) {
        this.openAiService = new OpenAiService(apiKey);
    }

    /**
     * 관심 카테고리를 1개 이상 받아 GPT에게 추천 키워드를 JSON 배열로 요청합니다.
     * @param categories 관심 카테고리 (보통 1개씩 요청)
     * @return 추천 키워드 리스트
     */
    public List<String> recommendKeywords(List<String> categories) {
        String prompt = String.format("""
            너는 유튜브 추천 시스템이야.
            사용자의 관심 카테고리는 다음과 같아: %s
        
            이 카테고리를 기반으로 오늘 보면 좋을 유튜브 영상 키워드 3개를 추천해줘.
            형식은 무조건 다음처럼 JSON 배열만 줘야 해. 설명 절대 하지마.
        
            예시:
            ["감정 조절", "마케팅 심리학", "무의식 설득"]
        """, String.join(", ", categories));

        ChatMessage systemMessage = new ChatMessage("system", prompt);
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(systemMessage))
                .maxTokens(300)
                .build();

        try {
            ChatCompletionResult result = openAiService.createChatCompletion(request);
            String jsonResponse = result.getChoices().get(0).getMessage().getContent();

            // GPT 응답을 JSON 배열로 파싱
            return objectMapper.readValue(jsonResponse, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("GPT 응답 파싱 실패: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}