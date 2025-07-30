package com.springcloud.personalfollower.dto.response;

public record YoutubeMetadata(
        String watchedAt,
        String url,
        String title,
        String thumbnail,
        String categories,
        String tags
) {}

