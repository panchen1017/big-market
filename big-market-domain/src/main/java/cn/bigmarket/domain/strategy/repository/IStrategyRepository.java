package cn.bigmarket.domain.strategy.repository;


import cn.bigmarket.domain.strategy.model.entity.StrategyAwardEntity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
 * @description 策略装实现，负责初始化策略计算
 */
public interface IStrategyRepository {
    List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId);

    void storeStrategySearchRateTables(Long strategyId, Integer rateRange, HashMap<Integer, Integer> shufflestrategyAwardSearchRateTables);

    // 获取rateRange
    int getRateRange(Long strategyId);


    Integer getStrategyAwardAssemble(Long strategyId, int rateKey);
}
