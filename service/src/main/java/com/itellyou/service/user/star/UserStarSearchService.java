package com.itellyou.service.user.star;

import com.itellyou.model.sys.PageModel;
import com.itellyou.model.user.UserStarDetailModel;

import java.util.List;
import java.util.Map;

public interface UserStarSearchService {

    int count(Long userId, Long followerId, Long beginTime, Long endTime, Long ip);

    List<UserStarDetailModel> search(Long userId, Long followerId, Long searchId, Long beginTime, Long endTime, Long ip, Map<String, String> order, Integer offset, Integer limit);

    PageModel<UserStarDetailModel> page(Long userId, Long followerId, Long searchId, Long beginTime, Long endTime, Long ip, Map<String, String> order, Integer offset, Integer limit);

    UserStarDetailModel find(Long userId,Long followerId);
}
