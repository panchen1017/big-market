package cn.bigmarket.domain.strategy.service.armory;

/**
 * @description 策略抽奖调度
 */

public interface IStrategyDispatch {

    // 获取臭脚策略装配的随机结果
    Integer getRandomAward(Long strategyId);

    // 获取抽奖策略装配的随机结果
    Integer getRandomAward(Long strategyId, String ruleWeightValue);
}
