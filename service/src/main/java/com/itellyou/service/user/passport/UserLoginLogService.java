package com.itellyou.service.user.passport;

import com.itellyou.model.user.UserLoginLogModel;

public interface UserLoginLogService {
    int insert(UserLoginLogModel userLoginLogModel);

    UserLoginLogModel find(String token);

    int setDisabled(Boolean status,String token);
}
