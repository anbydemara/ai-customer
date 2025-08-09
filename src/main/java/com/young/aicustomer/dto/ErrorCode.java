package com.young.aicustomer.dto;

/**
 * 统一返回错误码
 */
public enum  ErrorCode {

    // 登录/注册：10开头
    PHONE_INVALID(1001, "手机号格式错误"),
    CODE_ERROR(1002, "验证码错误"),
    USER_EXIST(1003, "该手机号已被注册，请前往登录"),
    PWD_NULL(1004, "密码不能为空"),
    LOGIN_INFO_ERROR(1005, "手机号或密码错误，请重新登录"),
    ACCOUNT_EXIST(10004,"账号已存在"),


    // 文件
    FILE_SIZE_OVERFLOW(5001, "文件超过规定大小"),
    FILE_TYPE_ERROR(5002, "不支持的文件类型"),
    FILE_UPLOAD_ERROR(5003, "文件上传失败"),




    USER_NOT_EXIST(4004, "用户不存在"),
    PWD_ERROR(4005, "原密码错误"),
    IMG_ERROR(4006, "图片上传失败"),

    // 用户 10开头
    NO_LOGIN(1001,"未登录"),
	NO_AUTHEN(1005, "未实名认证");


    private int code;
    private String msg;

    ErrorCode(int code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
