package com.young.aicustomer.service;

import com.young.aicustomer.dto.UserDTO;

import java.util.function.Consumer;

public interface AIQAService {
    void getAnswer(String question, UserDTO user, String sessionId, Consumer<String> consumer);
}
