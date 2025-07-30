package com.springcloud.personalfollower.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class YoutubeVisitId {
    private String url;
    private String visitTime;
    private String videoId;  // 새로 추가

    // 어노테이션으로 대체
//    public YoutubeVisitId(String url, String visitTime, String videoId) {
//        this.url = url;
//        this.visitTime = visitTime;
//        this.videoId = videoId;
//    }

}

