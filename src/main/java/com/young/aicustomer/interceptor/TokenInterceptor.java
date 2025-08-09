package com.young.aicustomer.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.young.aicustomer.dto.UserDTO;
import com.young.aicustomer.utils.UserThreadLocal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.young.aicustomer.utils.RedisConstants.LOGIN_USER_KEY;
import static com.young.aicustomer.utils.RedisConstants.LOGIN_USER_TTL;

/**
 * 解决redis缓存登录用户信息问题：用户登录后，信息保存到redis，持续访问则进行缓存时间刷新。
 * 但是如果用户一直访问的是LoginInterceptor不拦截的请求，则尽管用户一直在访问，但是过期时间没刷新，到时就退出登录状态了
 * 因此设置全局拦截器进行token刷新
 */
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        return HandlerInterceptor.super.preHandle(request, response, handler);
        // 这里要既然要获取token，就将登录拦截都迁移至此，避免LoginInterceptor再次获取token
        String token = request.getHeader("authorization");
        // 不做登录拦截
        if (StrUtil.isBlank(token)) return true;
        String key = LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        if (userMap.isEmpty())  return true;

        // 3. 有效用户，保存用户信息到ThreadLocal
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        UserThreadLocal.saveUser(userDTO);

        // 4. 刷新token过期时间
        stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.MINUTES);

        response.setHeader("Access-Control-Allow-Origin", "http://localhost:8080");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        return true;    // 放行
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserThreadLocal.removeUser();
    }
}
