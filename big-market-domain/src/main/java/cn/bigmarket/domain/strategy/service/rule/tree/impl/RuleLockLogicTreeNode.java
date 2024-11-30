package cn.bigmarket.domain.strategy.service.rule.tree.impl;

import cn.bigmarket.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import cn.bigmarket.domain.strategy.model.valobj.StrategyAwardRuleModelVO;
import cn.bigmarket.domain.strategy.repository.IStrategyRepository;
import cn.bigmarket.domain.strategy.service.rule.tree.ILogicTreeNode;
import cn.bigmarket.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 次数锁节点
 *
 */
@Slf4j
@Component("rule_lock")
public class RuleLockLogicTreeNode implements ILogicTreeNode {
    @Resource
    private IStrategyRepository repository;
    // 用户抽奖次数，后续完成这部分流程开发的时候，从数据库/Redis中读取
    private Long userRaffleCount = 10L;
    @Override
    public DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId, String ruleValue) {
        log.info("规则过滤 - 次数锁 userId:{} strategyId:{} awardId:{} ruleValue:{}", userId, strategyId, awardId, ruleValue);
        long raffleCount = 0L;
        try {
            raffleCount = Long.parseLong(ruleValue);
        } catch (Exception e) {
            throw new RuntimeException("规则过滤 - 次数锁 异常 ruleValue：" + ruleValue + "配置不正确");
        }

        // 查询用户抽奖次数 - 当天的；策略ID:活动ID 1:1 的配置，可以直接用 strategyId 查询。
        Integer userRaffleCount = repository.queryTodayUserRaffleCount(userId, strategyId);

        // 用户抽奖次数大于规则限定值，规则放行
        if (userRaffleCount >= raffleCount) {
            log.info("规则过滤-次数锁【放行】 userId:{} strategyId:{} awardId:{} raffleCount:{} userRaffleCount:{}", userId, strategyId, awardId, userRaffleCount, userRaffleCount);
            return DefaultTreeFactory.TreeActionEntity.builder()
                    .ruleLogicCheckType(RuleLogicCheckTypeVO.ALLOW)
                    .build();
        }

        log.info("规则过滤-次数锁【拦截】 userId:{} strategyId:{} awardId:{} raffleCount:{} userRaffleCount:{}", userId, strategyId, awardId, userRaffleCount, userRaffleCount);
        // 用户抽奖次数小于规则限定值，规则拦截
        return DefaultTreeFactory.TreeActionEntity.builder()
                .ruleLogicCheckType(RuleLogicCheckTypeVO.TAKE_OVER)
                .build();
    }
}
