package cn.bigmarket.domain.strategy.service.rule.tree.factory.engine;

import cn.bigmarket.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;

import java.util.Date;

/**
 *  规则树组合接口（执行引擎）
 *
 */
public interface IDecisionTreeEngine {

    DefaultTreeFactory.StrategyAwardVO process(String userId, Long strategyId, Integer awardId, Date endDateTime);
}
