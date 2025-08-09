package com.young.aicustomer.model;

import com.young.aicustomer.dto.UserDTO;

import java.util.function.Consumer;

/**
 * 大模型抽象接口：各大模型均能getAnswer进行问答
 */
public interface CustomAIModel {
    void getAnswer(String question, UserDTO user, String sessionId, Consumer<String> onAnswer);
}
