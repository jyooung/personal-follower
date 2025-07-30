package com.springcloud.personalfollower.service;

import com.springcloud.personalfollower.domain.youtube.YoutubeVideoMetadata;
import com.springcloud.personalfollower.dto.SessionUser;
import com.springcloud.personalfollower.dto.request.YoutubeVisitId;
import com.springcloud.personalfollower.repository.YoutubeMetadataRepository;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

@AllArgsConstructor
@Service
public class HistoryYtApiService {

    private final YoutubeMetadataRepository youtubeMetadataRepository;
    private final YoutubeApiService youtubeApiService;
    private final HttpSession httpSession;
    private final YoutubeMetadataService youtubeMetadataService;
    private final UserNounStatsService userNounStatsService;
    public static final String ORIGINAL_HISTORY_PATH;
    public static final String COPIED_HISTORY_PATH;


    static {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();
        String path;

        if (os.contains("win")) {
            path = userHome + "/AppData/Local/Google/Chrome/User Data/Default/History";
        } else if (os.contains("mac")) {
            path = userHome + "/Library/Application Support/Google/Chrome/Default/History";
        } else {
            path = userHome + "/.config/google-chrome/Default/History";
        }

        ORIGINAL_HISTORY_PATH = path;
        COPIED_HISTORY_PATH = path + "_copy";
    }

    public void runYoutubeExtraction() throws Exception {
        // 크롬 방문기록에서 유튜브 방문기록, 비디오ID 만 추출해서 visits 리스트에 YoutubeVisit 형태로 저장
        List<YoutubeVisitId> visits = extractYoutubeHistoryFromChrome();
        System.out.println("유튜브 방문기록 추출 완료");

        // 반복문으로 유튜브 영상마다 메타데이터 요청
        for (YoutubeVisitId visit : visits) {
            String videoId = visit.getVideoId();
            String visitedAt = visit.getVisitTime();
            String originalUrl = visit.getUrl(); // SQLite에서 나온 원본 URL

            // userId 가져오기 (로그인된 사용자)
            String userId = getCurrentUserId();

            // videoId를 가지고 YouTube API 요청 + metadatata 생성
            YoutubeVideoMetadata metadata = youtubeApiService.fetchVideoMetadata(
                    videoId, userId, visitedAt, originalUrl
            );

            if (metadata != null) {
                youtubeMetadataService.saveOrUpdateMetadata(metadata);
                userNounStatsService.updateNounStats(userId, metadata.getNouns());
            }

        }

    }

    // HttpSession에 저장된 SessionUser 객체에서 가져오기 거기서 유저ID꺼내기 (이메일)
    public String getCurrentUserId() {
        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("user");
        if (sessionUser == null) {
            throw new IllegalStateException("로그인 정보가 없습니다.");
        }
        return sessionUser.getEmail(); // 또는 name (ID로 삼는 기준에 따라)
    }

    // 유저의 저장된 마지막 영상 시청 시간
    public String getLastVisitedTimeOrNull() {
        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("user");
        if (sessionUser == null) throw new IllegalStateException("로그인 정보 없음");

        return youtubeMetadataRepository
                .findTopByUserIdOrderByVisitedAtDesc(sessionUser.getEmail())
                .map(YoutubeVideoMetadata::getVisitedAt)
                .orElse(null); // 기록이 없으면 null
    }

    // Java 시간 → Chrome 시간 포맷 변환 함수
    public static long toChromeTime(String visitedAt) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parsed = sdf.parse(visitedAt); // (Date) 캐스팅 불필요
            long millis = parsed.getTime();
            return (millis + 11644473600000L) * 1000L; // 마이크로초 단위로 변환
        } catch (ParseException e) {
            throw new RuntimeException("날짜 파싱 실패", e);
        }
    }



    // 유튜브 방문기록을 크롬의 히스토리 DB에서 추출해 리스트로 반환하는 메서드
    public List<YoutubeVisitId> extractYoutubeHistoryFromChrome() throws Exception {
        // 최종 결과를 담을 리스트 생성
        List<YoutubeVisitId> results = new ArrayList<>();

        // 1. 크롬이 현재 사용 중인 방문기록 DB는 잠겨 있어서 바로 접근 불가
        //    복사본을 만들어 분석에 사용
        copyDatabase(new File(ORIGINAL_HISTORY_PATH), new File(COPIED_HISTORY_PATH));

        // 2. SQLite JDBC 드라이버를 메모리에 로드 (이걸 해야 DB 연결 가능)
        Class.forName("org.sqlite.JDBC");

        // 3. 복사된 SQLite DB에 연결
        try (
                Connection conn = DriverManager.getConnection("jdbc:sqlite:" + COPIED_HISTORY_PATH);
                Statement stmt = conn.createStatement()
        ) {
            // 3-1. 다른 앱이 DB를 잠그고 있을 경우를 대비해, 최대 5초간 대기 설정
            stmt.execute("PRAGMA busy_timeout = 5000;");

            // 4. 방문기록 조회할 기간 구하기
            //    - Chrome은 1601년부터의 마이크로초로 시간을 저장함
            //    - 1970년부터의 시간으로 계산된 현재 시간을 Chrome 포맷으로 변환
            // 4-1. MongoDB에서 가장 최근 visitedAt 가져오기
            String lastVisitedAt = getLastVisitedTimeOrNull(); // 포맷: "yyyy-MM-dd HH:mm:ss"
            System.out.println("🕓 [MongoDB] 가장 마지막 visitedAt = " + lastVisitedAt);

            // 4-2. 크롬 시간 포맷으로 변환
            long visitTimeThreshold = (lastVisitedAt != null)
                    ? toChromeTime(lastVisitedAt)
                    : (System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000 + 11644473600000L) * 1000;

            System.out.println("🧮 [쿼리용] Chrome 시간 기준 threshold = " + visitTimeThreshold);

            // 5. 방문기록에서 최근 7일간의 URL과 방문시간을 조회하는 쿼리 작성
            String query =
                    "SELECT urls.url, visits.visit_time " +
                            "FROM urls INNER JOIN visits ON urls.id = visits.url " +
                            "WHERE visits.visit_time > " + visitTimeThreshold;

            // 6. 쿼리 실행
            ResultSet rs = stmt.executeQuery(query);

            // 날짜 출력을 위해 포맷터 생성 (예: 2024-06-29 20:35:00 형식)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // 7. 결과를 반복하며 유튜브 주소만 필터링
            while (rs.next()) {
                String url = rs.getString("url");

                // 유튜브 동영상 URL인 경우만 필터링 (shorts는 아직 제외)
                if (url.contains("youtube.com/watch?v=") || url.contains("youtu.be/")) {
                    long chromeTime = rs.getLong("visit_time");

                    // 크롬의 시간 포맷(마이크로초) → 일반 시간 포맷(밀리초) 변환
                    long millis = chromeTime / 1000L - 11644473600000L;

                    // 변환한 시간을 보기 좋게 문자열로 포맷
                    String formatted = sdf.format(new Date(millis));

                    // URL에서 videoId 추출
                    String videoId = extractVideoId(url);

                    // null이 아니면 결과 리스트에 추가
                    if (videoId != null) {
                        System.out.println("✅ 유효 기록 → videoId: " + videoId + " | 시청 시간: " + formatted + " | Chrome Time(raw): " + chromeTime);
                        results.add(new YoutubeVisitId(url, formatted, videoId));
                    } else {
                        System.out.println("⏩ 유튜브 아님 → URL: " + url + " | 시간: " + formatted);
                    }

                }
            }
        }

        // 유튜브 방문기록 목록 반환
        return results;
    }

    public String extractVideoId(String url) {
        try {
            if (url.contains("youtu.be/")) {
                // 예: https://youtu.be/VIDEO_ID
                return url.substring(url.indexOf("youtu.be/") + 9, url.indexOf("youtu.be/") + 20);
            }

            // 예: https://www.youtube.com/watch?v=VIDEO_ID
            URI uri = new URI(url);
            String query = uri.getQuery(); // v=VIDEO_ID
            if (query == null) return null;
            for (String param : query.split("&")) {
                if (param.startsWith("v=")) {
                    return param.substring(2);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 크롬이 열고 있는 DB 파일은 락이 걸려 있어서 직접 접근이 어렵기 때문에,
    // 복사본을 만들어 그걸 대상으로 SQLite 분석을 진행함
    private void copyDatabase(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile(); // 대상 파일이 없다면 생성
        }

        // 파일 복사 (입출력 스트림을 통한 채널 방식)
        try (FileChannel source = new FileInputStream(sourceFile).getChannel();
             FileChannel destination = new FileOutputStream(destFile).getChannel()) {
            destination.transferFrom(source, 0, source.size());
        }

        System.out.println("Database copied successfully.");
    }
}
