package com.young.aicustomer.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.young.aicustomer.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.young.aicustomer.utils.RedisConstants.LOGIN_USER_KEY;

@Component
@RequiredArgsConstructor
public class SocketAuthInterceptor implements HandshakeInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
//            servletRequest.get
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String token = httpRequest.getParameter("token");
            if (token != null && !StrUtil.isBlank(token)) {
                String key = LOGIN_USER_KEY + token;
                Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
                if (!userMap.isEmpty()) {
                    // 3. 有效用户，保存用户信息到attributes
                    UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
                    attributes.put("user", userDTO);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
