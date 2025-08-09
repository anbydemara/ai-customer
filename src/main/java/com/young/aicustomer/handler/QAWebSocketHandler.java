package com.young.aicustomer.handler;

import com.young.aicustomer.dto.UserDTO;
import com.young.aicustomer.service.AIQAService;
import com.young.aicustomer.service.IQaKnowledgeService;
import com.young.aicustomer.service.QaCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class QAWebSocketHandler extends TextWebSocketHandler {

    private final AIQAService qaService;
    private final QaCacheService qaCacheService;
    private final IQaKnowledgeService qaKnowledgeService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String, Object> attributes = session.getAttributes();

        // 检查前端是否已传会话ID
        String sessionId = getQueryParam(session.getUri(), "sessionId");
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString(); // 第一次连接生成新的会话ID
            // 返回会话ID给前端
            session.sendMessage(new TextMessage(buildInitMsg(sessionId)));
        }

//        // 返回会话ID给前端
//        String initMsg = String.format("{\"type\":\"init\",\"content\":\"%s\"}", sessionId);
//        session.sendMessage(new TextMessage(initMsg));

        // 可存入属性，后续提问时获取
        attributes.put("sessionId", sessionId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        super.handleTextMessage(session, message);
        String msg = message.getPayload();

        // 从session获取用户，握手阶段
        UserDTO user = (UserDTO) session.getAttributes().get("user");

        if (user == null) {
            session.sendMessage(new TextMessage(buildCommonMsg("未登录")));
            return;
        }

        String sessionId = (String) session.getAttributes().get("sessionId");

//        // 拼接最终回答
//        StringBuilder fullAnswer = new StringBuilder();

        // 先查缓存
        String cachedMsg = qaCacheService.getQa(user.getId(), msg);
        if (cachedMsg != null) {
            session.sendMessage(new TextMessage(buildCommonMsg("[缓存]")));
            session.sendMessage(new TextMessage(buildCommonMsg(cachedMsg)));
            return;
        }

        // 再走知识库
        Optional<String> knowledge = qaKnowledgeService.findKnowledge(msg);

        if (knowledge.isPresent()) {
            session.sendMessage(new TextMessage(buildCommonMsg("[知识库回答]")));
            session.sendMessage(new TextMessage(buildCommonMsg(knowledge.get())));
            return;
        }

        // 缓存没有再走请求
        CompletableFuture.runAsync(() -> {
            qaService.getAnswer(msg, user, sessionId, answerPart -> {
                try {
                    session.sendMessage(new TextMessage(buildCommonMsg(answerPart)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    private String getQueryParam(URI uri, String key) {
        if (uri == null || uri.getQuery() == null) return null;
        String[] params = uri.getQuery().split("&");
        for (String param : params) {
            String[] kv = param.split("=");
            if (kv.length == 2 && kv[0].equals(key)) {
                return kv[1];
            }
        }
        return null;
    }


    public String buildInitMsg(String content) {
        return String.format("{\"type\":\"%s\", \"content\":\"%s\"}", "init", content);
    }
    public String buildCommonMsg(String content) {
        return String.format("{\"type\":\"%s\", \"content\":\"%s\"}", "common", content.replace("\n", "\\n"));
    }
//
}
