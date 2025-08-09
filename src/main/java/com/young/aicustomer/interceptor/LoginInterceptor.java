package com.young.aicustomer.interceptor;

import com.young.aicustomer.utils.UserThreadLocal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {


//    private final StringRedisTemplate stringRedisTemplate;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (UserThreadLocal.getUser() == null) {
            response.setStatus(401);    // 401：未授权
            return false;
        }
        return true;    // 用用户则放行
//        String token = request.getHeader("authorization");
//        if (StrUtil.isBlank(token)) {
//            // 1. 未携带token（未登录），拦截
//            response.setStatus(401);
//            return false;
//        }
//
//        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(LOGIN_USER_KEY + token);
//        if (userMap.isEmpty()) {
//            // 2. 无效token（未登录），拦截
//            response.setStatus(401);
//            return false;
//        }
//        // 有效用户，保存用户信息到ThreadLocal，并放行
//        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
//        UserThreadLocal.saveUser(userDTO);
//        return true;
    }


//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
////        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
//        // 请求完成，移除当前用户信息
//        UserThreadLocal.removeUser();
//    }
}
