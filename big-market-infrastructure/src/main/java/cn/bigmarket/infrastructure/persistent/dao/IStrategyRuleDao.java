package cn.bigmarket.infrastructure.persistent.dao;


import cn.bigmarket.infrastructure.persistent.po.Award;
import cn.bigmarket.infrastructure.persistent.po.StrategyRule;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 策略规则
 */
@Mapper
public interface IStrategyRuleDao {

    List<StrategyRule> queryStrategyRuleList();

    StrategyRule queryStrategyRule(StrategyRule strategyRule);

    String queryStrategyRuleValue(StrategyRule strategyRule);
}
