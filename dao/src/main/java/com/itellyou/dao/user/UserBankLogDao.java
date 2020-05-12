package com.itellyou.dao.user;

import com.itellyou.model.sys.EntityAction;
import com.itellyou.model.sys.EntityType;
import com.itellyou.model.user.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface UserBankLogDao {
    int insert(UserBankLogModel userBankLogModel);

    List<UserBankLogModel> search(@Param("id") Long id,
                                        @Param("type") UserBankType type,
                                        @Param("action") EntityAction action,
                                        @Param("dataType") EntityType dataType,
                                        @Param("dataKey") String dataKey,
                                        @Param("userId") Long userId,
                                        @Param("beginTime") Long beginTime, @Param("endTime") Long endTime,
                                        @Param("ip") Long ip,
                                        @Param("order") Map<String, String> order,
                                        @Param("offset") Integer offset,
                                        @Param("limit") Integer limit);

    int count(@Param("id") Long id,
              @Param("type") UserBankType type,
              @Param("action") EntityAction action,
              @Param("dataType") EntityType dataType,
              @Param("dataKey") String dataKey,
              @Param("userId") Long userId,
              @Param("beginTime") Long beginTime, @Param("endTime") Long endTime,
              @Param("ip") Long ip);

    double total(@Param("id") Long id,
              @Param("type") UserBankType type,
              @Param("action") EntityAction action,
              @Param("dataType") EntityType dataType,
                 @Param("dataKey") String dataKey,
              @Param("userId") Long userId,
              @Param("beginTime") Long beginTime, @Param("endTime") Long endTime,
              @Param("ip") Long ip);
}
