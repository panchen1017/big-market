package cn.bigmarket.domain.strategy.service.rule.tree;

import cn.bigmarket.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;

/**
 * 规则树接口
 *
 */
public interface ILogicTreeNode {

    // 如果库存没有了，如果加锁了，那奖品都不能到手，只能兜底。如果能到手，走库存扣减的逻辑。
    DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId, String ruleValue);
}
