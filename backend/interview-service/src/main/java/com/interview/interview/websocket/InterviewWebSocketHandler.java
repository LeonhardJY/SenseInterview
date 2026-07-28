package com.interview.interview.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 面试房间 WebSocket 处理器
 * <p>
 * 职责：
 * 1. 管理每个面试任务的 WebSocket 连接（userId -> session）
 * 2. 接收客户端的 ping 心跳
 * 3. 向指定面试房间推送实时消息（进度、评估结果、表情分析等）
 * <p>
 * 连接路径：ws://host:port/ws/interview/{taskId}?userId={userId}
 */
@Slf4j
@Component
public class InterviewWebSocketHandler extends TextWebSocketHandler {

    /**
     * 在线会话表：taskId -> (userId -> session)
     * ConcurrentHashMap + 不会频繁遍历，性能足够
     */
    private final Map<String, Map<Long, WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String taskId = getTaskId(session);
        Long userId = getUserId(session);

        if (taskId == null || userId == null) {
            closeSession(session, "缺少 taskId 或 userId 参数");
            return;
        }

        // 加入房间
        roomSessions.computeIfAbsent(taskId, k -> new ConcurrentHashMap<>())
                .put(userId, session);

        log.info("WebSocket 连接建立 — taskId: {}, userId: {}, sessionId: {}", taskId, userId, session.getId());
        log.info("当前在线房间数: {}, 连接数: {}", roomSessions.size(), countOnlineSessions());

        // 回复连接成功
        sendMessage(session, buildMessage("CONNECTED", "连接成功", null));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        String taskId = getTaskId(session);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> msg = objectMapper.readValue(payload, Map.class);
            String type = (String) msg.getOrDefault("type", "");

            switch (type) {
                case "PING" -> sendMessage(session, buildMessage("PONG", "pong", null));
                case "TYPING" -> {
                    // 用户正在输入中，可广播给面试官（未来扩展）
                    log.debug("用户输入中 — taskId: {}", taskId);
                }
                default -> log.warn("未知消息类型: {}, taskId: {}", type, taskId);
            }
        } catch (Exception e) {
            log.warn("WebSocket 消息解析失败, taskId: {}", taskId, e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String taskId = getTaskId(session);
        Long userId = getUserId(session);

        if (taskId != null && userId != null) {
            Map<Long, WebSocketSession> room = roomSessions.get(taskId);
            if (room != null) {
                room.remove(userId);
                if (room.isEmpty()) {
                    roomSessions.remove(taskId);
                }
            }
        }

        log.info("WebSocket 连接关闭 — taskId: {}, userId: {}, status: {}", taskId, userId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket 传输异常 — sessionId: {}, error: {}", session.getId(), exception.getMessage());
        closeSession(session, "传输异常");
    }

    // ========== 对外推送接口 ==========

    /**
     * 向指定面试房间的所有人推送消息
     */
    public void broadcastToRoom(String taskId, String type, Object data) {
        Map<Long, WebSocketSession> room = roomSessions.get(taskId);
        if (room == null || room.isEmpty()) {
            log.debug("房间 {} 无在线连接，跳过推送", taskId);
            return;
        }

        String message = buildMessage(type, null, data);
        room.values().forEach(session -> {
            if (session.isOpen()) {
                sendMessage(session, message);
            }
        });
    }

    /**
     * 向指定用户推送消息
     */
    public void sendToUser(String taskId, Long userId, String type, Object data) {
        Map<Long, WebSocketSession> room = roomSessions.get(taskId);
        if (room == null) return;

        WebSocketSession session = room.get(userId);
        if (session != null && session.isOpen()) {
            sendMessage(session, buildMessage(type, null, data));
        }
    }

    /**
     * 判断房间是否有在线连接
     */
    public boolean hasOnlineConnections(String taskId) {
        Map<Long, WebSocketSession> room = roomSessions.get(taskId);
        return room != null && !room.isEmpty();
    }

    // ========== 内部方法 ==========

    private void sendMessage(WebSocketSession session, String message) {
        try {
            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            }
        } catch (IOException e) {
            log.error("WebSocket 发送消息失败, sessionId: {}", session.getId(), e);
        }
    }

    private void closeSession(WebSocketSession session, String reason) {
        try {
            if (session.isOpen()) {
                session.close(CloseStatus.POLICY_VIOLATION.withReason(reason));
            }
        } catch (IOException e) {
            log.warn("关闭 WebSocket 异常, sessionId: {}", session.getId());
        }
    }

    private String buildMessage(String type, String message, Object data) {
        try {
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("type", type);
            if (message != null) result.put("message", message);
            if (data != null) result.put("data", data);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            log.error("构建消息 JSON 失败", e);
            return "{}";
        }
    }

    /**
     * 从 WebSocket URI 中提取 taskId
     * 路径格式：/ws/interview/{taskId}
     */
    private String getTaskId(WebSocketSession session) {
        String path = session.getUri() != null ? session.getUri().getPath() : "";
        String prefix = "/ws/interview/";
        if (path.startsWith(prefix)) {
            return path.substring(prefix.length());
        }
        return null;
    }

    /**
     * 从查询参数中提取 userId
     */
    private Long getUserId(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : "";
        if (query == null || query.isEmpty()) return null;

        for (String param : query.split("&")) {
            String[] parts = param.split("=", 2);
            if (parts.length == 2 && "userId".equals(parts[0])) {
                try {
                    return Long.parseLong(parts[1]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private int countOnlineSessions() {
        return roomSessions.values().stream()
                .mapToInt(Map::size)
                .sum();
    }
}
