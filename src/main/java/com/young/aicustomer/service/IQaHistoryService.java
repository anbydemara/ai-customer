package com.young.aicustomer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.young.aicustomer.dto.Result;
import com.young.aicustomer.entity.QaHistory;

import java.util.List;

public interface IQaHistoryService extends IService<QaHistory> {
    void saveHistory(Long userId, String sessionId, String content, String role);

    Result getHistory(int limit);

    Result getSessionHistory(String sessionId);

    Result getSessionIds();

    List<QaHistory> findHistoryBySessionId(String sessionId);

    Result deleteSession(String sessionId);
}
