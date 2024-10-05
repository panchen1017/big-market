package cn.bigmarket.domain.strategy.service.rule.filter.impl;

import cn.bigmarket.domain.strategy.model.entity.RuleActionEntity;
import cn.bigmarket.domain.strategy.model.entity.RuleMatterEntity;
import cn.bigmarket.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import cn.bigmarket.domain.strategy.repository.IStrategyRepository;
import cn.bigmarket.domain.strategy.service.annotation.LogicStrategy;
import cn.bigmarket.domain.strategy.service.rule.filter.ILogicFilter;
import cn.bigmarket.domain.strategy.service.rule.filter.factory.DefaultLogicFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 用户抽奖n次后，对应奖品可解锁抽奖 抽奖中
 */
@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.RULE_LOCK) // 中置 rule_lock 规则
public class RuleLockLogicFilter implements ILogicFilter<RuleActionEntity.RaffleCenterEntity> {

    @Resource
    private IStrategyRepository repository;

    // test 默认设置成 0
    private Long userRaffleCount = 0L;
    @Override
    public RuleActionEntity<RuleActionEntity.RaffleCenterEntity> filter(RuleMatterEntity ruleMatterEntity) {

        log.info("规则过滤-次数锁 userId:{} strategyId:{} ruleModel:{}", ruleMatterEntity.getUserId(), ruleMatterEntity.getStrategyId(), ruleMatterEntity.getRuleModel());
        // 根据 strategyId，awardId，ruleModel 去查询表中 ruleValue（1， 2， 6）
        String ruleValue = repository.queryStrategyRuleValue(ruleMatterEntity.getStrategyId(), ruleMatterEntity.getAwardId(), ruleMatterEntity.getRuleModel());
        // 将字符串转换成数字型
        long raffleCount = Long.parseLong(ruleValue);
        // 将当前用户抽奖次数与 raffleCount 对比
        // 例如：如果当期那用户抽奖次数已经大于 1 / 2 / 6 次，那就可以抽到更好的商品
        // 那么这边就 放行，不做拦截了 直接 ALLOW
        if(userRaffleCount >= raffleCount) {
            return RuleActionEntity.<RuleActionEntity.RaffleCenterEntity>builder()
                    .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                    .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                    .build();
        }
        // 如果不匹配，拦截就行 TAKE_OVER
        return RuleActionEntity.<RuleActionEntity.RaffleCenterEntity>builder()
                .info(RuleLogicCheckTypeVO.TAKE_OVER.getInfo())
                .code(RuleLogicCheckTypeVO.TAKE_OVER.getCode())
                .build();

    }














}
