package com.springcloud.personalfollower.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.*;
import java.text.SimpleDateFormat;

@Service
public class HistoryService {

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

    public void extractHistory() throws IOException, ClassNotFoundException, SQLException {
        // 1. 사용 중인 Chrome 방문기록 DB는 직접 열 수 없으므로, 안전하게 분석하기 위해 원본 DB를 복사해서 사용
        copyDatabase(new File(ORIGINAL_HISTORY_PATH), new File(COPIED_HISTORY_PATH));

        // 2. SQLite JDBC 드라이버를 로드 (클래스가 메모리에 적재되어야 DB 연결 가능)
        Class.forName("org.sqlite.JDBC");

        // 3. 복사된 SQLite DB에 연결
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + COPIED_HISTORY_PATH);
             Statement stmt = conn.createStatement()) {

            // 3-1. DB가 잠긴 경우를 대비해 5초간 대기 설정
            stmt.execute("PRAGMA busy_timeout = 5000;");

            // 4. 조회 기준 시간 계산
            //    - 크롬의 방문 기록은 1601년 1월 1일을 기준으로 마이크로초 단위로 저장됨
            //    - Unix epoch 기준(1970년)으로부터 1601년까지의 시간: 11644473600000L ms
            //    - 현재 시간에서 7일 전을 계산하고 크롬 포맷(마이크로초)으로 변환
            long DaysAgoMillis = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
            long visitTimeMicro = (DaysAgoMillis + 11644473600000L) * 1000;

            // 5. 최근 N일간의 전체 방문 기록 조회 (유튜브 필터링은 자바에서 따로 수행)
            String query = "SELECT urls.url, visits.visit_time " +
                    "FROM urls INNER JOIN visits ON urls.id = visits.url " +
                    "WHERE visits.visit_time > " + visitTimeMicro + " " +
                    "ORDER BY visits.visit_time ASC";

            ResultSet rs = stmt.executeQuery(query);
            // 날짜 형식 변환용
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // 6. 결과 출력
            while (rs.next()) {
                String url = rs.getString("url");
                // 유튜브 주소만 필터링
                // 유튜브 비디오 URL인지 확인
                if ((url.contains("youtube.com/watch?v=") || url.contains("youtu.be/"))) {
                    long visitTimeMicroSec = rs.getLong("visit_time");
                    // 크롬 시간 형식을 일반 시간(ms)로 변환
                    long visitTimeMillis = (visitTimeMicroSec / 1000L) - 11644473600000L;
                    // 날짜 포맷 변경
                    String formatted = sdf.format(new Date(visitTimeMillis));
                    // 콘솔 출력
                    System.out.println("[YouTube] " + formatted + " - " + url);
                }
            }

            System.out.println("최근 7일간 유튜브 방문기록 출력 완료");

        } catch (SQLException e) {
            System.out.println("SQLite 오류: " + e.getMessage());
            throw e;
        }
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
