package org.infinity.sixtalebackend.domain.chat.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.infinity.sixtalebackend.domain.chat.domain.WaitingChatLog;
import org.infinity.sixtalebackend.domain.chat.dto.ChatMessageRequest;
import org.infinity.sixtalebackend.domain.chat.repository.WaitingChatLogRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisBatchScheduler {
    private final RedisTemplate<String, String> redisTemplate; // Redis 접근
    private final WaitingChatLogRepository waitingChatLogRepository; // MongoDB Repository
    private final ObjectMapper objectMapper; // JSON 직렬화/역직렬화를 위한 ObjectMapper

    @Scheduled(fixedRate = 20000) // 20초마다 실행
    public void saveLogsToMongoDB() {
        System.out.println("Batch Scheduler is running..."); // 디버그용 로그 추가
        Set<String> keys = redisTemplate.keys("chatLogs:waitingRoom:*");
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            // Redis에서 로그 가져오기
            List<String> logs = redisTemplate.opsForList().range(key, 0, -1);
            if (logs == null || logs.isEmpty()) continue;

            try {
                // Redis 로그를 MongoDB Entity로 변환
                List<WaitingChatLog> chatLogs = logs.stream()
                        .map(log -> {
                            try {
                                ChatMessageRequest request = objectMapper.readValue(log, ChatMessageRequest.class);
                                return WaitingChatLog.builder()
                                        .roomID(Long.valueOf(request.getRoomID()))
                                        .memberID(request.getMemberID())
                                        .content(request.getContent())
                                        .createdAt(LocalDateTime.parse(request.getCreatedAt()))
                                        .build();
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to parse log", e);
                            }
                        }).collect(Collectors.toList());

                // MongoDB 저장
                waitingChatLogRepository.saveAll(chatLogs);

                // Redis에서 해당 키 삭제
                redisTemplate.delete(key);
            } catch (Exception e) {
                throw new RuntimeException("Batch processing failed for key: " + key, e);
            }
        }
    }
}
