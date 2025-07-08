package com.springcloud.personalfollower.common.util;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class YoutubeCategoryMapper {

    private static final Map<String, String> YOUTUBE_CATEGORY_MAP = Map.ofEntries(
            Map.entry("1", "Film & Animation"),
            Map.entry("2", "Autos & Vehicles"),
            Map.entry("10", "Music"),
            Map.entry("15", "Pets & Animals"),
            Map.entry("17", "Sports"),
            Map.entry("20", "Gaming"),
            Map.entry("22", "People & Blogs"),
            Map.entry("23", "Comedy"),
            Map.entry("24", "Entertainment"),
            Map.entry("25", "News & Politics"),
            Map.entry("26", "Howto & Style"),
            Map.entry("27", "Education"),
            Map.entry("28", "Science & Technology"),
            Map.entry("29", "Nonprofits & Activism")
    );

    public static String getCategoryName(String categoryId) {
        return YOUTUBE_CATEGORY_MAP.getOrDefault(categoryId, "Unknown");
    }
}
