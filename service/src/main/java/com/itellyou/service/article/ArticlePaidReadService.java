package com.itellyou.service.article;

import com.itellyou.model.article.ArticlePaidReadModel;
import com.itellyou.model.user.UserBankLogModel;

public interface ArticlePaidReadService {

    int insert(ArticlePaidReadModel model);

    int insertOrUpdate(ArticlePaidReadModel model);

    int deleteByArticleId(Long articleId);

    UserBankLogModel doPaidRead(Long articleId, Long userId, Long ip) throws Exception;
}
