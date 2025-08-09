package com.young.aicustomer.service.impl;

import com.young.aicustomer.service.QaCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static com.young.aicustomer.utils.RedisConstants.CACHE_QA_KEY;

@Service
@RequiredArgsConstructor
public class QaCacheServiceImpl implements QaCacheService {

    private final StringRedisTemplate stringRedisTemplate;


    @Override
    public void saveQa(Long userId, String question, String answer) {
        String key = keyBuilder(userId, question);
        stringRedisTemplate.opsForValue().set(key, answer, Duration.ofHours(24));
    }

    @Override
    public String getQa(Long userId, String question) {
        return stringRedisTemplate.opsForValue().get(keyBuilder(userId, question));
    }


    private String keyBuilder(Long userId, String question) {
        String hash = DigestUtils.md5DigestAsHex(question.getBytes(StandardCharsets.UTF_8));
        return CACHE_QA_KEY + userId + ":" + hash;
    }
}
