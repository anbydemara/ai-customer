package com.young.aicustomer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.young.aicustomer.dto.LoginFormDTO;
import com.young.aicustomer.dto.RegisterFormDTO;
import com.young.aicustomer.dto.Result;
import com.young.aicustomer.entity.User;

public interface IUserService extends IService<User> {
    Result login(LoginFormDTO loginForm);

    Result sendCode(String phone);

    Result register(RegisterFormDTO registerForm);
}
