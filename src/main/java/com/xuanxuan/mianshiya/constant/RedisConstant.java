package com.xuanxuan.mianshiya.constant;

public interface RedisConstant {
    String USER_SIGN_IN_KEY_PREFIX = "user:signin:";

    static String generateUserSignInKey(long userId, int year) {
        return String.format("%s:%s:%s", USER_SIGN_IN_KEY_PREFIX, year, userId);
    }
}
