package com.young.aicustomer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.young.aicustomer.dto.*;
import com.young.aicustomer.entity.User;
import com.young.aicustomer.mapper.UserMapper;
import com.young.aicustomer.service.IUserService;
import com.young.aicustomer.utils.RegexUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.young.aicustomer.utils.RedisConstants.*;
import static com.young.aicustomer.utils.SystemConstants.USER_NICK_NAME_PREFIX;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone) {
        // 1. 手机号格式校验
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail(ErrorCode.PHONE_INVALID.getCode(), ErrorCode.PHONE_INVALID.getMsg());
        }
        // 2. 生成随机验证码，发送短信（日志模拟）
        String code = RandomUtil.randomNumbers(6);
        log.info("生成验证码：{}", code);
        // 3. code保存到redis，<phone, code>
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        return Result.success();
    }

    @Override
    public Result register(RegisterFormDTO registerForm) {
        System.out.println(registerForm.toString());

        // 1. 手机号格式校验
        if (RegexUtils.isPhoneInvalid(registerForm.getPhone())) {
            return Result.fail(ErrorCode.PHONE_INVALID.getCode(), ErrorCode.PHONE_INVALID.getMsg());
        }
        // 2. 判断用户是否存在（手机号是唯一）
        User user = this.lambdaQuery().eq(User::getPhone, registerForm.getPhone()).one();
        if (user != null) {
            return Result.fail(ErrorCode.USER_EXIST.getCode(), ErrorCode.USER_EXIST.getMsg());
        }

        // 3. 密码校验
        if (StrUtil.isBlank(registerForm.getPassword())) {
            return Result.fail(ErrorCode.PWD_NULL.getCode(), ErrorCode.PWD_NULL.getMsg());
        }
        // 4. 昵称校验
        if (StrUtil.isBlank(registerForm.getNickName())) {
            // 随机生成
            registerForm.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        }
        // 5. 创建用户
        this.save(BeanUtil.copyProperties(registerForm, User.class));

        return Result.success();
    }

    @Override
    public Result login(LoginFormDTO loginForm) {
        // 1. 手机号格式校验
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail(ErrorCode.PHONE_INVALID.getCode(), ErrorCode.PHONE_INVALID.getMsg());
        }
        User user = null;
        // 如果密码不为空，则为密码登录
        if (StrUtil.isNotBlank(loginForm.getPassword())) {
            user = this.lambdaQuery().eq(User::getPhone, phone).eq(User::getPassword, loginForm.getPassword()).one();
            if (user == null) {
                return Result.fail(ErrorCode.LOGIN_INFO_ERROR.getCode(), ErrorCode.LOGIN_INFO_ERROR.getMsg());
            }
        } else {
            // 2. 根据手机号从redis取出code
            String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
            if (cacheCode == null || !cacheCode.equals(loginForm.getCode())) {
                return Result.fail(ErrorCode.CODE_ERROR.getCode(), ErrorCode.CODE_ERROR.getMsg());
            }
            // 3. code验证成功则查询用户
            user = lambdaQuery().eq(User::getPhone, phone).one();
            if (user == null) {
                // 4.1 用户不存在则新建用户
                user = createUser(loginForm);
            }
        }

        // 4.2 为用户生成token
        String token = UUID.randomUUID().toString();
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO,
                new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));    // StringRedisTemplate存储的值需要都为String，这里进行转换
        // 4.3 保存用户信息到redis：<token, UserDto>
        String key = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(key, userMap);
        stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.MINUTES);  // 设置过期时间
        return Result.success(token);
    }

    // 新建用户（默认信息）
    private User createUser(LoginFormDTO loginForm) {
        User user = BeanUtil.copyProperties(loginForm, User.class);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        this.save(user);
        return user;
    }


}
