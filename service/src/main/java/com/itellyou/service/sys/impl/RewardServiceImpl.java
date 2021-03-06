package com.itellyou.service.sys.impl;

import com.itellyou.model.common.OperationalModel;
import com.itellyou.model.event.AnswerEvent;
import com.itellyou.model.event.ArticleEvent;
import com.itellyou.model.event.OperationalEvent;
import com.itellyou.model.event.SoftwareEvent;
import com.itellyou.model.question.QuestionAnswerDetailModel;
import com.itellyou.model.question.QuestionDetailModel;
import com.itellyou.model.sys.EntityAction;
import com.itellyou.model.sys.EntityDataModel;
import com.itellyou.model.sys.EntityType;
import com.itellyou.model.sys.RewardLogModel;
import com.itellyou.model.user.UserBankLogModel;
import com.itellyou.model.user.UserBankType;
import com.itellyou.service.article.ArticleCommentService;
import com.itellyou.service.event.OperationalPublisher;
import com.itellyou.service.question.QuestionAnswerCommentService;
import com.itellyou.service.software.SoftwareCommentService;
import com.itellyou.service.sys.EntityService;
import com.itellyou.service.sys.RewardLogService;
import com.itellyou.service.sys.RewardService;
import com.itellyou.service.user.bank.UserBankService;
import com.itellyou.util.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;

@Service
public class RewardServiceImpl implements RewardService {

    private final RewardLogService logService;
    private final UserBankService bankService;
    private final EntityService entityService;
    private final OperationalPublisher operationalPublisher;
    private final ArticleCommentService articleCommentService;
    private final QuestionAnswerCommentService answerCommentService;
    private final SoftwareCommentService softwareCommentService;

    public RewardServiceImpl(RewardLogService logService, UserBankService bankService, EntityService entityService, OperationalPublisher operationalPublisher, ArticleCommentService articleCommentService, QuestionAnswerCommentService answerCommentService, SoftwareCommentService softwareCommentService) {
        this.logService = logService;
        this.bankService = bankService;
        this.entityService = entityService;
        this.operationalPublisher = operationalPublisher;
        this.articleCommentService = articleCommentService;
        this.answerCommentService = answerCommentService;
        this.softwareCommentService = softwareCommentService;
    }

    @Override
    @Transactional
    public RewardLogModel doReward(UserBankType bankType, Double amount, EntityType dataType, Long dataKey, Long userId, Long ip) throws Exception {
        try{
            EntityDataModel dataModel = entityService.search(dataType,new HashMap<String,Object>(){{
                put("hasContent",false);
                put("ids",new HashSet<Long>(){{ add(dataKey);}});
            }});
            Object targetData = dataModel.get(dataType,dataKey);
            if(targetData == null) throw new Exception("错误的打赏的目标 ");
            Class clazz = targetData.getClass();
            Field field = null;
            while (clazz != null) {
                try {
                    field = clazz.getDeclaredField("createdUserId");
                    break;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            if(field == null) new Exception("错误的打赏的目标");
            field.setAccessible(true);
            Long targetUserId = Long.valueOf(field.get(targetData).toString());
            if(targetUserId.equals(userId)) throw new Exception("不能给自己打赏");

            UserBankLogModel bankLogModel = bankService.update(-Math.abs(amount),bankType, EntityAction.REWARD,dataType,dataKey.toString(),userId,"打赏支出",ip);
            if(bankLogModel == null) throw new Exception("扣款失败");
            UserBankLogModel targetBankLogModel = bankService.update(Math.abs(amount),bankType,EntityAction.REWARD,dataType,dataKey.toString(),targetUserId,"收到打赏",ip);
            if(targetBankLogModel == null) throw new Exception("收款失败");

            RewardLogModel logModel = new RewardLogModel(null,bankType,dataType,dataKey,amount,targetUserId,userId, DateUtils.toLocalDateTime(),ip);
            int result = logService.insert(logModel);
            if(result != 1) throw new Exception("记录日志失败");

            String typePrefix = "";
            String typeSuffix = "";
            if(bankType.equals(UserBankType.CASH)) {
                typePrefix = " ¥";
                typeSuffix = " ";
            }
            if(bankType.equals(UserBankType.CREDIT)){
                typeSuffix = " 积分";
                typePrefix = " ";
            }

            OperationalModel operationalModel = new OperationalModel(EntityAction.REWARD, dataType, dataKey, targetUserId, userId, DateUtils.toLocalDateTime(), ip);
            OperationalEvent event = new OperationalEvent(this, operationalModel,"amount",amount);
            if(dataType.equals(EntityType.ARTICLE)){
                String html = new StringBuilder("<p>我刚刚打赏了这篇文章").append(typePrefix).append(amount).append(typeSuffix).append("，也推荐给你。</p>").toString();
                articleCommentService.insert(dataKey,0l,0l,html,html,userId,ip,false);
                event = new ArticleEvent(this,EntityAction.REWARD,dataKey,targetUserId, userId, DateUtils.toLocalDateTime(), ip);
            }
            else if(dataType.equals(EntityType.ANSWER)){
                String html = new StringBuilder("<p>我刚刚打赏了这个回答").append(typePrefix).append(amount).append(typeSuffix).append("，也推荐给你。</p>").toString();
                answerCommentService.insert(dataKey,0l,0l,html,html,userId,ip,false);
                QuestionAnswerDetailModel answerDetailModel = (QuestionAnswerDetailModel)targetData;
                QuestionDetailModel questionDetailModel = answerDetailModel.getQuestion();
                event = new AnswerEvent(this,EntityAction.REWARD,answerDetailModel.getQuestionId(),questionDetailModel != null ? questionDetailModel.getCreatedUserId() : 0l,dataKey,targetUserId, userId, DateUtils.toLocalDateTime(), ip);
            }else if(dataType.equals(EntityType.SOFTWARE)){
                String html = new StringBuilder("<p>我刚刚打赏了").append(typePrefix).append(amount).append(typeSuffix).append("，也推荐给你。</p>").toString();
                softwareCommentService.insert(dataKey,0l,0l,html,html,userId,ip,false);
                event = new SoftwareEvent(this,EntityAction.REWARD,dataKey,targetUserId, userId, DateUtils.toLocalDateTime(), ip);
            }
            if(bankType.equals(UserBankType.CASH)) {
                event.setArgs(new HashMap<String, Object>(){{
                    put("amount",amount);
                }});
                operationalPublisher.publish(event);
            }
            return logModel;
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw e;
        }
    }
}
