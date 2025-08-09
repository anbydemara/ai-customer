package com.young.aicustomer.service.impl;

import com.young.aicustomer.dto.UserDTO;
import com.young.aicustomer.model.impl.XFModel;
import com.young.aicustomer.service.AIQAService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class AIQAServiceImpl implements AIQAService {

    private final ApplicationContext context;

    @Override
    public void getAnswer(String question, UserDTO user, String sessionId, Consumer<String> consumer) {
        XFModel xunfei = context.getBean("xunfei", XFModel.class);
        xunfei.getAnswer(question, user, sessionId, consumer);
    }
}
