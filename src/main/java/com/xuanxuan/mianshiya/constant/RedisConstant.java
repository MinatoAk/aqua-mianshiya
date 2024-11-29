package com.xuanxuan.mianshiya.constant;

public interface RedisConstant {
    String USER_SIGN_IN_KEY_PREFIX = "user:signin:";

    String INC_SYNC_QUESTION_TO_ES = "IncSync:QuestionToEs:lock";

    String FULL_SYNC_QUESTION_TO_ES = "FullSync:QuestionToEs:lock";

    static String generateUserSignInKey(long userId, int year) {
        return String.format("%s:%s:%s", USER_SIGN_IN_KEY_PREFIX, year, userId);
    }
}
