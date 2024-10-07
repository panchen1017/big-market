package cn.bigmarket.domain.strategy.repository;


import cn.bigmarket.domain.strategy.model.entity.StrategyAwardEntity;
import cn.bigmarket.domain.strategy.model.entity.StrategyEntity;
import cn.bigmarket.domain.strategy.model.entity.StrategyRuleEntity;
import cn.bigmarket.domain.strategy.model.valobj.RuleTreeVO;
import cn.bigmarket.domain.strategy.model.valobj.StrategyAwardRuleModelVO;

import java.util.HashMap;
import java.util.List;

/**
 * @description 策略装实现，负责初始化策略计算
 */
public interface IStrategyRepository {
    List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId);

    void storeStrategySearchRateTables(String key, Integer rateRange, HashMap<Integer, Integer> shufflestrategyAwardSearchRateTables);

    // 获取rateRange
    int getRateRange(Long strategyId);

    int getRateRange(String key);

    Integer getStrategyAwardAssemble(String key, int rateKey);

    StrategyEntity queryStrategyEntityByStrategyId(Long strategyId);

    StrategyRuleEntity queryStrategyRule(Long strategyId, String ruleModel);

    String queryStrategyRuleValue(Long strategyId,  String ruleModel);

    String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel);

    StrategyAwardRuleModelVO queryStrategyAwardRuleModel(Long strategyId, Integer awardId);

    RuleTreeVO queryRuleTreeVOByTreeId(String treeId);
}
