package com.itellyou.service.sys;

import com.itellyou.model.sys.ReportAction;
import com.itellyou.model.sys.ReportModel;
import com.itellyou.model.sys.EntityType;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ReportService {
    int insert(ReportAction action, EntityType type, Long targetId, String description, Long userId, Long ip) throws Exception;

    List<ReportModel> search(Long id,
                                      Map<ReportAction, Collection<EntityType>> actionsMap,
                                      Integer state,
                                      Long targetUserId,
                                      Long userId,
                                      Long beginTime, Long endTime,
                                      Long ip,
                                      Map<String, String> order,
                                      Integer offset,
                                      Integer limit);
    int count(Long id,
              Map<ReportAction, Collection<EntityType>> actionsMap,
              Integer state,
              Long targetUserId,
              Long userId,
              Long beginTime, Long endTime,
              Long ip);
}
