package cn.bigmarket.infrastructure.persistent.dao;


import cn.bigmarket.domain.strategy.model.entity.StrategyAwardEntity;
import cn.bigmarket.infrastructure.persistent.po.Award;
import cn.bigmarket.infrastructure.persistent.po.StrategyAward;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 策略奖品
 */
@Mapper
public interface IStrategyAwardDao {

    List<StrategyAward> queryStrategyAwardList();

    List<StrategyAward> queryStrategyAwardListByStrategyID(Long strategyId);

    String queryStrategyAwardRuleModels(StrategyAward strategyAward);

    void updateStrategyAwardStock(StrategyAward strategyAward);

    StrategyAward queryStrategyAward(StrategyAward strategyAwardReq);
}
