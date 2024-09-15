package cn.bigmarket.domain.strategy.service.armory;


/**
 * @description 策略装配库(兵工厂)，负责初始化策略计算
 */
public interface IStrategyArmory {

    // 组装抽奖策略
    boolean assembleLotteryStrategy(Long strategyId);

}
