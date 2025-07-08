package com.springcloud.personalfollower.common.util;

import org.openkoreantext.processor.KoreanTokenJava;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class NounExtractor {

    // 조사, 불용어 정리
    private static final Set<String> STOP_WORDS = Set.of(
            // 조사 및 형식어
            "이", "가", "을", "를", "은", "는", "에", "에게", "에서", "보다", "로", "으로", "의", "과", "와", "도", "만", "까지",
            "것", "수", "때", "듯", "더", "좀", "또", "또는", "등", "들", "하여", "해서", "하면", "하여도", "같은", "처럼", "만큼",
            "뿐", "만에", "조차", "이랑", "이요", "이라", "이나", "든지",

            // 감탄/형용 표현
            "진짜", "정말", "대박", "헐", "헉", "음", "어머", "세상에", "진심", "진심으로", "완전", "역시", "완벽", "충격", "소름", "엄청",
            "최고", "최악", "대체", "결국", "드디어", "설마", "진정", "과연", "그냥", "무려", "사실", "갑자기", "이게", "이런",

            // YouTube 특유 홍보/어그로 표현
            "보세요", "보시죠", "보자", "보니까", "보면서", "봐야", "봐야지", "봤더니", "주의보", "주의필요",
            "주의하세요", "클릭", "필수", "당신", "여러분", "당장", "꼭", "지금", "오늘", "내일", "최초", "최신", "공개", "전격"
             );


    public static List<String> extractNouns(String text) {
        // 정규화
        CharSequence normalized = OpenKoreanTextProcessorJava.normalize(text);
        // 토큰화
        scala.collection.Seq<org.openkoreantext.processor.tokenizer.KoreanTokenizer.KoreanToken> tokens =
                OpenKoreanTextProcessorJava.tokenize(normalized);
        // Java용 토큰 변환
        List<KoreanTokenJava> tokenList = OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens);

        // 명사만 필터링
        return tokenList.stream()
                .filter(token -> {
                    String pos = String.valueOf(token.getPos());
                    // 일반 명사 고유 명사 포함
                    return pos.equals("Noun") || pos.equals("ProperNoun");
                })
                .map(KoreanTokenJava::getText)
                .filter(word ->
                        word.length() > 1 &&                          // 글자 수 제한 (1글자 제외)
                        !STOP_WORDS.contains(word) &&                // 불용어 제외
                        word.matches(".*[가-힣a-zA-Z0-9]+.*")         // 의미 없는 특수기호 필터
                )
                .distinct()
                .collect(Collectors.toList());
    }
}
