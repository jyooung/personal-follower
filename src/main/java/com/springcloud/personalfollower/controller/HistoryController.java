package com.springcloud.personalfollower.controller;

import com.springcloud.personalfollower.common.response.ResponseDto;
import com.springcloud.personalfollower.service.HistoryYtDlpService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
@RequestMapping("/api/history")
public class HistoryController {
    private final HistoryYtDlpService historyYtDlpService;
    /**
     * 브라우저 히스토리를 추출
     * @return 성공 또는 실패 메시지
     * historyYtDlpService yt-dlp 버전 (이후 전환)
     */
    @GetMapping("/extract")
    public ResponseEntity<ResponseDto<String>> extractHistory() {
        try {
            historyYtDlpService.extractHistory();
            return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success("success"));

        } catch (Exception e){
            return ResponseEntity.status(500).body(ResponseDto.failure("Failed to extract browser history: " + e.getMessage()));
        }
    }

}
