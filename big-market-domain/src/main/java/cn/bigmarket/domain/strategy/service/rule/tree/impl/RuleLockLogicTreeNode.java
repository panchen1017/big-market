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
    @Override
    public DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId) {

//        StrategyAwardRuleModelVO strategyAwardRuleModelVO = repository.queryStrategyAwardRuleModel(strategyId, awardId);
//        String strategyRuleValue = repository.queryStrategyRuleValue(strategyId, awardId, strategyAwardRuleModelVO.getRuleModels());

        return DefaultTreeFactory.TreeActionEntity.builder()
                .ruleLogicCheckType(RuleLogicCheckTypeVO.ALLOW)
                .build();
    }
}
