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
     * Extracts browser history using the yt-dlp-based history extraction service.
     *
     * @return a ResponseEntity containing a success message if extraction succeeds, or a failure message with error details if an exception occurs
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
