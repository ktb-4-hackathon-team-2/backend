package com.example.ktb_hktn_team2.report.client;

import com.example.ktb_hktn_team2.report.dto.HourlyStatDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Slf4j
@Component
public class AiClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String aiServerUrl;

    public AiClient(
            @Value("${app.ai-server.url:http://localhost:8000}") String aiServerUrl
    ) {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
        this.aiServerUrl = aiServerUrl;
    }

    /**
     * force=true면 AI 서버의 쿨다운(같은 유저·날짜 재분석 방지)을 건너뛰고 새로 분석한다.
     * 사용자가 리포트에서 '재생성'을 직접 누른 경우에만 true — 자동 호출은 쿨다운으로 과다 청구를 막는다.
     */
    public AiAnalyzeResult analyzeDaily(String userId, String date, List<HourlyStatDto> hourly, int stretchSuggested, int stretchDone, boolean force) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("date", date != null && !date.isBlank() ? date : java.time.LocalDate.now().toString());
            body.put("user_id", userId != null ? userId : "default_user");
            body.put("stretch_suggested", Math.max(0, stretchSuggested));
            body.put("stretch_done", Math.max(0, stretchDone));
            body.put("force", force);

            List<Map<String, Object>> hourlyList = new ArrayList<>();
            if (hourly != null) {
                for (HourlyStatDto h : hourly) {
                    if (h == null) continue;
                    int hour = h.getHour() != null ? Math.min(23, Math.max(0, h.getHour())) : 0;
                    double rawRatio = h.getGoodRatio() != null ? h.getGoodRatio() : 0.0;
                    // 만약 0~100 범위로 들어온 경우 0~1로 정규화
                    if (rawRatio > 1.0) {
                        rawRatio = rawRatio / 100.0;
                    }
                    double goodRatio = Math.min(1.0, Math.max(0.0, rawRatio));
                    double monitoredMin = h.getMonitoredMin() != null ? Math.max(0.0, h.getMonitoredMin()) : 0.0;
                    int alerts = h.getAlerts() != null ? Math.max(0, h.getAlerts()) : 0;

                    Map<String, Object> item = new HashMap<>();
                    item.put("hour", hour);
                    item.put("good_ratio", goodRatio);
                    item.put("monitored_min", monitoredMin);
                    item.put("alerts", alerts);
                    hourlyList.add(item);
                }
            }
            body.put("hourly", hourlyList);

            String requestJson = objectMapper.writeValueAsString(body);
            log.info("AI 서버 분석 요청 페이로드: {}", requestJson);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiServerUrl + "/api/report/daily/analyze"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("AI 서버 분석 응답 성공: {}", response.body());
                JsonNode root = objectMapper.readTree(response.body());
                String summary = root.path("summary").asText("자세 모니터링이 성공적으로 완료되었습니다.");
                String grade = root.path("grade").asText("NORMAL").toUpperCase();
                String source = root.path("source").asText("llm");

                List<String> highlights = new ArrayList<>();
                if (root.has("highlights")) {
                    for (JsonNode n : root.get("highlights")) {
                        highlights.add(n.asText());
                    }
                }

                List<String> advice = new ArrayList<>();
                if (root.has("advice")) {
                    for (JsonNode n : root.get("advice")) {
                        advice.add(n.asText());
                    }
                }

                return new AiAnalyzeResult(summary, grade, highlights, advice, source);
            } else {
                log.warn("AI 서버 응답 실패 [HTTP {}]: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("AI 서버 분석 호출 예외 (규칙 기반 폴백 적용): {}", e.getMessage());
        }

        return fallbackAnalyze(hourly);
    }

    private AiAnalyzeResult fallbackAnalyze(List<HourlyStatDto> hourly) {
        double avg = (hourly != null && !hourly.isEmpty())
                ? hourly.stream().mapToDouble(h -> h.getGoodRatio() != null ? h.getGoodRatio() : 0.0).average().orElse(0.8)
                : 0.8;
        if (avg > 1.0) avg = avg / 100.0;
        String grade = avg >= 0.85 ? "EXCELLENT" : avg >= 0.7 ? "GOOD" : avg >= 0.5 ? "NORMAL" : "BAD";
        String summary = String.format("오늘의 평균 바른 자세 유지율은 %.0f%%입니다. 꾸준히 바른 자세를 유지해 보세요.", avg * 100);
        List<String> highlights = List.of("시간대별 자세를 점검하고 스트레칭을 틈틈이 실천해 보세요.");
        List<String> advice = List.of("50분 작업 후 5분간 가벼운 목·어깨 스트레칭을 권장합니다.");
        return new AiAnalyzeResult(summary, grade, highlights, advice, "rule_based");
    }

    public record AiAnalyzeResult(
            String summary,
            String grade,
            List<String> highlights,
            List<String> advice,
            String source
    ) {}
}
