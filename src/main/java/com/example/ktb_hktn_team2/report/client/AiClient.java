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
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        this.objectMapper = new ObjectMapper();
        this.aiServerUrl = aiServerUrl;
    }

    public AiAnalyzeResult analyzeDaily(String userId, String date, List<HourlyStatDto> hourly, int stretchSuggested, int stretchDone) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("date", date);
            body.put("user_id", userId);
            body.put("stretch_suggested", stretchSuggested);
            body.put("stretch_done", stretchDone);

            List<Map<String, Object>> hourlyList = new ArrayList<>();
            for (HourlyStatDto h : hourly) {
                Map<String, Object> item = new HashMap<>();
                item.put("hour", h.getHour());
                item.put("good_ratio", h.getGoodRatio() != null ? h.getGoodRatio() : 0.0);
                item.put("monitored_min", h.getMonitoredMin() != null ? h.getMonitoredMin() : 0.0);
                item.put("alerts", h.getAlerts() != null ? h.getAlerts() : 0);
                hourlyList.add(item);
            }
            body.put("hourly", hourlyList);

            String requestJson = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiServerUrl + "/api/report/daily/analyze"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
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
            }
        } catch (Exception e) {
            log.warn("AI 서버 분석 호출 실패 (규칙 기반 폴백 적용): {}", e.getMessage());
        }

        return fallbackAnalyze(hourly);
    }

    private AiAnalyzeResult fallbackAnalyze(List<HourlyStatDto> hourly) {
        double avg = hourly.stream().mapToDouble(h -> h.getGoodRatio() != null ? h.getGoodRatio() : 0.0).average().orElse(0.8);
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
