package cn.bigmarket.domain.award.service;

import cn.bigmarket.domain.award.event.SendAwardMessageEvent;
import cn.bigmarket.domain.award.model.aggregate.UserAwardRecordAggregate;
import cn.bigmarket.domain.award.model.entity.DistributeAwardEntity;
import cn.bigmarket.domain.award.model.entity.TaskEntity;
import cn.bigmarket.domain.award.model.entity.UserAwardRecordEntity;
import cn.bigmarket.domain.award.model.valobj.TaskStateVO;
import cn.bigmarket.domain.award.repository.IAwardRepository;
import cn.bigmarket.domain.award.service.distribute.IDistributeAward;
import cn.bigmarket.types.event.BaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 奖品服务
 * @create 2024-04-06 09:39
 */
@Service
@Slf4j
public class AwardService implements IAwardService {

    private IAwardRepository awardRepository;
    private SendAwardMessageEvent sendAwardMessageEvent;
    private final Map<String, IDistributeAward> distributeAwardMap;

    public AwardService(IAwardRepository awardRepository, SendAwardMessageEvent sendAwardMessageEvent, Map<String, IDistributeAward> distributeAwardMap) {
        this.awardRepository = awardRepository;
        this.sendAwardMessageEvent = sendAwardMessageEvent;
        this.distributeAwardMap = distributeAwardMap;
    }

    @Override
    public void saveUserAwardRecord(UserAwardRecordEntity userAwardRecordEntity) {
        // 构建消息对象
        SendAwardMessageEvent.SendAwardMessage sendAwardMessage = new SendAwardMessageEvent.SendAwardMessage();
        sendAwardMessage.setUserId(userAwardRecordEntity.getUserId());
        sendAwardMessage.setOrderId(userAwardRecordEntity.getOrderId());
        sendAwardMessage.setUserId(userAwardRecordEntity.getUserId());
        sendAwardMessage.setAwardId(userAwardRecordEntity.getAwardId());
        sendAwardMessage.setAwardTitle(userAwardRecordEntity.getAwardTitle());

        // 这里的 topic 是 spring.rabbitmq.topic.send_award
        BaseEvent.EventMessage<SendAwardMessageEvent.SendAwardMessage> sendAwardMessageEventMessage = sendAwardMessageEvent.buildEventMessage(sendAwardMessage);

        // 构建任务对象
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setUserId(userAwardRecordEntity.getUserId());
        taskEntity.setTopic(sendAwardMessageEvent.topic());
        taskEntity.setMessageId(sendAwardMessageEventMessage.getId());
        taskEntity.setMessage(sendAwardMessageEventMessage);
        taskEntity.setState(TaskStateVO.create);

        // 构建聚合对象
        UserAwardRecordAggregate userAwardRecordAggregate = UserAwardRecordAggregate.builder()
                .taskEntity(taskEntity)
                .userAwardRecordEntity(userAwardRecordEntity)
                .build();

        // 存储聚合对象 - 一个事务下，用户的中奖记录
        awardRepository.saveUserAwardRecord(userAwardRecordAggregate);
    }

    @Override
    public void distributeAward(DistributeAwardEntity distributeAwardEntity) {
        // 奖品Key，查询出来奖品对应那个key
        String awardKey = awardRepository.queryAwardKey(distributeAwardEntity.getAwardId());
        if (null == awardKey) {
            log.error("分发奖品，奖品ID不存在。awardKey:{}", awardKey);
            return;
        }

        // 奖品服务，减少 if...else...
        IDistributeAward distributeAward = distributeAwardMap.get(awardKey);

        if (null == distributeAward) {
            log.error("分发奖品，对应的服务不存在。awardKey:{}", awardKey);
            throw new RuntimeException("分发奖品，奖品" + awardKey + "对应的服务不存在");
        }

        // 发放奖品，调用要发放的奖品信息
        distributeAward.giveOutPrizes(distributeAwardEntity);
    }

}
