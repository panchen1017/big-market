package cn.bigmarket.infrastructure.persistent.repository;


import cn.bigmarket.domain.strategy.model.entity.StrategyAwardEntity;
import cn.bigmarket.domain.strategy.repository.IStrategyRepository;
import cn.bigmarket.infrastructure.persistent.dao.IStrategyAwardDao;
import cn.bigmarket.infrastructure.persistent.po.StrategyAward;
import cn.bigmarket.infrastructure.persistent.redis.IRedisService;
import cn.bigmarket.types.common.Constants;
import org.redisson.api.RMap;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @description 策略装实现，负责初始化策略计算
 */
@Repository
public class StrategyRepository implements IStrategyRepository {

    @Resource
    private IStrategyAwardDao strategyAwardDao;
    @Resource
    private IRedisService redisService;
    @Override
    public List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId) {

        // 优先从缓存中提取，如果没有，那就去数据库获取，获取了之后存入redis

        // 这边做了一个优先从redis缓存中获取的操作，big_market_strategy_award_key_"strategyId"
        // 在types层的Constants当中有许多关于redis的缓存
        // 这个cacheKey是从redis中拿的，这样子下次如果还是同样的strategyId,就不用做对应的Dao操作了
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_KEY + strategyId;

        // 通过cacheKey去获取value
        List<StrategyAwardEntity> strategyAwardEntities = redisService.getValue(cacheKey);

        if( null != strategyAwardEntities && !strategyAwardEntities.isEmpty()) return strategyAwardEntities;
            // 取出来的 既不为null，也不为空时,返回即可

        // 如果redis中没有，那么我们去Dao中获取
        // 这边要注意，如果说从库中获取，不是StrategyAwardEntity实体对象，是StrategyAward对象，库里面没有StrategyAwardEntity实体对象
        List<StrategyAward> strategyAwards =  strategyAwardDao.queryStrategyAwardListByStrategyID(strategyId);
        // 返回结果列表
        ArrayList<StrategyAwardEntity> awardEntities = new ArrayList<>(strategyAwards.size());
        for(StrategyAward strategyAward: strategyAwards) {
            // 复制上方的StrategyAward strategyAward，然后 Alt + insert，便捷化建立builder
            StrategyAwardEntity strategyAwardEntity = StrategyAwardEntity.builder()
                        .strategyId(strategyAward.getStrategyId())
                        .awardId(strategyAward.getAwardId())
                        .awardCount(strategyAward.getAwardCount())
                        .awardCountSurplus(strategyAward.getAwardCountSurplus())
                        .awardRate(strategyAward.getAwardRate())
                        .build();
            awardEntities.add(strategyAwardEntity);
        }
        // 这边也是把刚刚提取出的这个strategyId对应的cacheKey存入redis中，下次就能减少Dao的操作了
        redisService.setValue(cacheKey, awardEntities);
        return awardEntities;
    }

    @Override
    public void storeStrategySearchRateTables(Long strategyId, Integer rateRange, HashMap<Integer, Integer> shufflestrategyAwardSearchRateTables) {
        // 1. 存储抽奖策略范围值，如6000，用于生成6000以内的随机数
        // 这里只是去存一个key-value键值对，strategy-6000
        redisService.setValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + strategyId, rateRange.intValue());
        // 2. 存储打乱后的 概率查找表
        RMap<Integer, Integer> map = redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + strategyId);
        map.putAll(shufflestrategyAwardSearchRateTables);
    }

    @Override
    public int getRateRange(Long strategyId) {
        // 从redis中拿出键值对的 6000
        return  redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + strategyId);
    }

    @Override
    public Integer getStrategyAwardAssemble(Long strategyId, int rateKey) {
        // 直接从map中拿回上面存的数据
        return redisService.getFromMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + strategyId, rateKey);
    }
}
