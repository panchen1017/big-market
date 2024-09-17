package cn.bigmarket.domain.strategy.service;

import cn.bigmarket.domain.strategy.model.entity.RaffleAwardEntity;
import cn.bigmarket.domain.strategy.model.entity.RaffleFactorEntity;

/**
 * 抽奖策略接口
 */
public interface IRaffleStrategy {

    RaffleAwardEntity performRaffle(RaffleFactorEntity raffleFactorEntity);
}
