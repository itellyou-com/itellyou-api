package com.itellyou.service.user;

import com.itellyou.model.common.OperationalModel;
import com.itellyou.model.sys.EntityAction;
import com.itellyou.model.sys.EntityType;
import com.itellyou.model.user.UserBankLogModel;
import com.itellyou.model.user.UserBankModel;
import com.itellyou.model.user.UserBankType;

public interface UserBankService {
    UserBankModel findByUserId(Long userId);

    UserBankLogModel update(Double amount, UserBankType type, EntityAction action, EntityType dataType, String dataKey, Long userId, String remark, Long clientIp) throws Exception;

    int insert(UserBankModel bankModel);

    void updateByOperational(UserBankType bankType,OperationalModel model);
}
