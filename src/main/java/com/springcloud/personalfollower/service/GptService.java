package com.springcloud.personalfollower.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class GptService {

    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GptService(@Value("${openai.api.key}") String apiKey) {
        this.openAiService = new OpenAiService(apiKey);
    }

    /**
     * 관심 카테고리 및 유사 키워드를 기반으로 GPT에게 추천 키워드를 요청합니다.
     * @param combinedKeywords 관심 키워드 + 유사 키워드 목록
     * @return 추천 키워드 리스트 (최대 3개)
     */
    public List<String> recommendKeywords(List<String> combinedKeywords) {
        String joined = String.join(", ", combinedKeywords);

        String prompt = String.format("""
            너는 유튜브 추천 키워드를 생성하는 추천 시스템이야.
            사용자의 관심 키워드와 이와 유사한 키워드는 다음과 같아:
            [%s]

            위 키워드를 참고해서 오늘 보면 좋을 유튜브 영상 주제 키워드 3개를 추천해줘. 최근 유행도 반영해도 좋아.
            형식은 반드시 JSON 배열로 줘. 예시는 아래와 같아. 설명 붙이지 마.

            예시:
            ["감정 조절", "마케팅 심리학", "무의식 설득"]
        """, joined);

        ChatMessage systemMessage = new ChatMessage("system", prompt);
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(systemMessage))
                .maxTokens(300)
                .build();

        try {
            ChatCompletionResult result = openAiService.createChatCompletion(request);
            String jsonResponse = result.getChoices().get(0).getMessage().getContent();

            // JSON 파싱
            return objectMapper.readValue(jsonResponse, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("GPT 응답 파싱 실패: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}