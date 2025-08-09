package com.young.aicustomer.controller;

import com.young.aicustomer.dto.LoginFormDTO;
import com.young.aicustomer.dto.RegisterFormDTO;
import com.young.aicustomer.dto.Result;
import com.young.aicustomer.entity.User;
import com.young.aicustomer.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final IUserService userService;

    @GetMapping("/test")
    public User testAPI() {
        return new User().setNickName("jacky");
    }

    /**
     * 获取验证码
     * @param phone 手机号
     */
//    @PostMapping("/code")
//    public Result code(@RequestParam("phone") String phone) {
    @PostMapping("/code/{phone}")
    public Result code(@PathVariable("phone") String phone) {
        // 发送短信验证码并保存验证码
        return userService.sendCode(phone);
    }

    /**
     * 登录功能
     * @param loginForm 登录参数：手机号、验证码；或者手机号、密码
     */
    @PostMapping("/login")
    public Result longin(@RequestBody LoginFormDTO loginForm) {
        // 实现登录功能
        return userService.login(loginForm);
    }

    /**
     * 注册功能
     * @param RegisterForm 登录参数：手机号、密码、昵称
     */
    @PostMapping("/register")
    public Result register(@RequestBody RegisterFormDTO RegisterForm) {
        // 实现注册功能
        return userService.register(RegisterForm);
    }

}
