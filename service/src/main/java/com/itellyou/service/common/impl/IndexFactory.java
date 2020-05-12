package com.itellyou.service.common.impl;

import com.itellyou.model.sys.EntityType;
import com.itellyou.service.article.impl.ArticleIndexServiceImpl;
import com.itellyou.service.column.impl.ColumnIndexServiceImpl;
import com.itellyou.service.common.IndexService;
import com.itellyou.service.question.impl.QuestionAnswerIndexServiceImpl;
import com.itellyou.service.question.impl.QuestionIndexServiceImpl;
import com.itellyou.service.tag.impl.TagIndexServiceImpl;
import com.itellyou.service.user.impl.UserIndexServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class IndexFactory {

    private ArticleIndexServiceImpl articleIndexService;
    private QuestionIndexServiceImpl questionIndexService;
    private QuestionAnswerIndexServiceImpl answerIndexService;
    private TagIndexServiceImpl tagIndexService;
    private ColumnIndexServiceImpl columnIndexService;
    private UserIndexServiceImpl userIndexService;

    private static IndexFactory instance = new IndexFactory();

    public IndexFactory(){}

    @Autowired
    public IndexFactory(ArticleIndexServiceImpl articleIndexService, QuestionIndexServiceImpl questionIndexService, QuestionAnswerIndexServiceImpl answerIndexService, TagIndexServiceImpl tagIndexService,ColumnIndexServiceImpl columnIndexService,UserIndexServiceImpl userIndexService) {
        this.articleIndexService = articleIndexService;
        this.questionIndexService = questionIndexService;
        this.answerIndexService = answerIndexService;
        this.tagIndexService = tagIndexService;
        this.columnIndexService = columnIndexService;
        this.userIndexService = userIndexService;
    }

    @PostConstruct
    public void init(){
        instance = this;
        instance.articleIndexService = this.articleIndexService;
        instance.answerIndexService = this.answerIndexService;
        instance.questionIndexService = this.questionIndexService;
        instance.tagIndexService = this.tagIndexService;
        instance.columnIndexService = this.columnIndexService;
        instance.userIndexService = this.userIndexService;
    }

    public static IndexFactory getInstance(){
        return instance;
    }

    public static IndexService create(EntityType type){
        IndexFactory indexFactory = IndexFactory.getInstance();
        if(type.equals(EntityType.ARTICLE))
            return indexFactory.articleIndexService;
        if(type.equals(EntityType.QUESTION))
            return indexFactory.questionIndexService;
        if(type.equals(EntityType.TAG))
            return indexFactory.tagIndexService;
        if(type.equals(EntityType.ANSWER))
            return indexFactory.answerIndexService;
        if(type.equals(EntityType.COLUMN))
            return indexFactory.columnIndexService;
        if(type.equals(EntityType.USER))
            return indexFactory.userIndexService;
        return null;
    }
}
