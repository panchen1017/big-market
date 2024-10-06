package cn.bigmarket.domain.strategy.service.rule.filter.impl;

import cn.bigmarket.domain.strategy.model.entity.RuleActionEntity;
import cn.bigmarket.domain.strategy.model.entity.RuleMatterEntity;
import cn.bigmarket.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import cn.bigmarket.domain.strategy.repository.IStrategyRepository;
import cn.bigmarket.domain.strategy.service.annotation.LogicStrategy;
import cn.bigmarket.domain.strategy.service.rule.filter.ILogicFilter;
import cn.bigmarket.domain.strategy.service.rule.filter.factory.DefaultLogicFactory;
import cn.bigmarket.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
/**
 *  白名单逻辑处理
 */
@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.RULE_WHITELIST)
public class RuleWhiteListLogicFilter implements ILogicFilter<RuleActionEntity.RaffleBeforeEntity> {

    @Resource
    private IStrategyRepository repository;

    @Override
    public RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> filter(RuleMatterEntity ruleMatterEntity) {

        log.info("规则过滤-白名单 userId:{} strategyId:{} ruleModel:{}", ruleMatterEntity.getUserId(), ruleMatterEntity.getStrategyId(), ruleMatterEntity.getRuleModel());
        String userId = ruleMatterEntity.getUserId();
        Long strategyId = ruleMatterEntity.getStrategyId();

        // 1. 用 策略id，奖品id，rule_model去查对应的rule_value
        String ruleValue = repository.queryStrategyRuleValue(ruleMatterEntity.getStrategyId(), ruleMatterEntity.getAwardId(), ruleMatterEntity.getRuleModel());
        // 用冒号 ” : “ 进行拆分
        //  101 : user001,user002,user003
        String[] splitRuleValue = ruleValue.split(Constants.COLON);
        // splitRuleValue[0] 是 awardId
        Integer awardId = Integer.parseInt(splitRuleValue[0]);

        // 过滤其他规则
        // 2. 个人 id 是否在添加的 id 里面，如果在的话，就加入黑名单
        String[] userBlackIds = splitRuleValue[1].split(Constants.SPLIT);
        for (String userBlackId : userBlackIds) {
            if (userId.equals(userBlackId)) {
                return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                        .ruleModel(DefaultLogicFactory.LogicModel.RULE_WHITELIST.getCode())
                        .data(RuleActionEntity.RaffleBeforeEntity.builder()
                                .strategyId(strategyId)
                                .awardId(awardId)
                                .build())
                        .code(RuleLogicCheckTypeVO.TAKE_OVER.getCode())
                        .info(RuleLogicCheckTypeVO.TAKE_OVER.getInfo())
                        .build();
            }
        }

        // 不被黑名单接管，往下执行就行
        return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                .build();

    }









}
