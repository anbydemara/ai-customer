package com.young.aicustomer.service;

public interface QaCacheService {

    void saveQa(Long userId, String question, String answer);

    String getQa(Long userId, String question);
}
