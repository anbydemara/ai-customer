package com.young.aicustomer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.young.aicustomer.dto.Result;
import com.young.aicustomer.dto.SessionDTO;
import com.young.aicustomer.dto.UserDTO;
import com.young.aicustomer.entity.QaHistory;
import com.young.aicustomer.mapper.QaHistoryMapper;
import com.young.aicustomer.service.IQaHistoryService;
import com.young.aicustomer.service.IQaKnowledgeService;
import com.young.aicustomer.utils.UserThreadLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
//@RequiredArgsConstructor
public class QaHistoryServiceImpl extends ServiceImpl<QaHistoryMapper, QaHistory> implements IQaHistoryService {

//    private final IQaKnowledgeService qaKnowledgeService;
    private IQaKnowledgeService qaKnowledgeService;

    @Autowired
    public void setQaKnowledgeService(IQaKnowledgeService qaKnowledgeService) {
        this.qaKnowledgeService = qaKnowledgeService;
    }

    @Override
    public void saveHistory(Long userId, String sessionId, String content, String role) {
        QaHistory qaHistory = new QaHistory().setUserId(userId).setSessionId(sessionId).setContent(content).setRole(role);
        this.save(qaHistory);
    }

    @Override
    public Result getHistory(int limit) {

        UserDTO user = UserThreadLocal.getUser();

        List<QaHistory> qaHistoryList = this.lambdaQuery()
                .eq(QaHistory::getUserId, user.getId())
                .orderByDesc(QaHistory::getCreateTime)
                .last("limit " + limit)
                .list();

        return Result.success(qaHistoryList);
    }

    @Override
    public Result getSessionHistory(String sessionId) {
        UserDTO user = UserThreadLocal.getUser();

        List<QaHistory> sessionHistoryList = this.lambdaQuery()
                .eq(QaHistory::getSessionId, sessionId)
                .eq(QaHistory::getUserId, user.getId())
                .orderByAsc(QaHistory::getCreateTime).list();

        return Result.success(sessionHistoryList);
    }

    @Override
    public Result getSessionIds() {
        UserDTO user = UserThreadLocal.getUser();
        List<SessionDTO> sessionDTOList = new ArrayList<>();
        // 查询用户历史会话session
        Set<String> sessionIds = this.lambdaQuery()
                .select(QaHistory::getSessionId)  // 选择要查询的字段
                .eq(QaHistory::getUserId, user.getId())
                .list()
                .stream()
                .map(QaHistory::getSessionId)     // 提取字段值
                .collect(Collectors.toSet());
        sessionIds.forEach(sessionId -> {
            QaHistory firstQa = this.lambdaQuery().eq(QaHistory::getSessionId, sessionId).orderByAsc(QaHistory::getCreateTime).last("limit 1").one();
            QaHistory lastQa = this.lambdaQuery().eq(QaHistory::getSessionId, sessionId).orderByDesc(QaHistory::getCreateTime).last("limit 1").one();
            SessionDTO sessionDTO = new SessionDTO(sessionId, firstQa.getCreateTime(), lastQa.getCreateTime(), firstQa.getContent());
            sessionDTOList.add(sessionDTO);
        });
        // 按截至会话时间降序
        Collections.sort(sessionDTOList, Comparator.comparing(SessionDTO::getEndTime).reversed());
        return Result.success(sessionDTOList);
    }

    @Override
    public List<QaHistory> findHistoryBySessionId(String sessionId) {
        return this.lambdaQuery().eq(QaHistory::getSessionId, sessionId).orderByAsc(QaHistory::getCreateTime).list();
    }

    @Override
    public Result deleteSession(String sessionId) {
        this.remove(new LambdaQueryWrapper<QaHistory>().eq(QaHistory::getSessionId, sessionId));
        return Result.success();
    }
}
