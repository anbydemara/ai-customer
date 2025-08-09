package com.young.aicustomer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.young.aicustomer.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
