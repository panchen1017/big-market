package cn.bigmarket.infrastructure.persistent.repository;


import cn.bigmarket.domain.strategy.model.entity.StrategyAwardEntity;
import cn.bigmarket.domain.strategy.model.entity.StrategyEntity;
import cn.bigmarket.domain.strategy.model.entity.StrategyRuleEntity;
import cn.bigmarket.domain.strategy.model.valobj.StrategyAwardRuleModelVO;
import cn.bigmarket.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import cn.bigmarket.domain.strategy.repository.IStrategyRepository;
import cn.bigmarket.infrastructure.persistent.dao.IStrategyAwardDao;
import cn.bigmarket.infrastructure.persistent.dao.IStrategyDao;
import cn.bigmarket.infrastructure.persistent.dao.IStrategyRuleDao;
import cn.bigmarket.infrastructure.persistent.po.Strategy;
import cn.bigmarket.infrastructure.persistent.po.StrategyAward;
import cn.bigmarket.infrastructure.persistent.po.StrategyRule;
import cn.bigmarket.infrastructure.persistent.redis.IRedisService;
import cn.bigmarket.types.common.Constants;
import com.zaxxer.hikari.util.SuspendResumeLock;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class StrategyRepository implements IStrategyRepository {

    @Resource
    private IStrategyAwardDao strategyAwardDao;
    @Resource
    private IStrategyDao strategyDao;
    @Resource
    private IStrategyRuleDao strategyRuleDao;
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

        if(null != strategyAwardEntities && !strategyAwardEntities.isEmpty()) return strategyAwardEntities;
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
    public void storeStrategySearchRateTables(String key, Integer rateRange, HashMap<Integer, Integer> shufflestrategyAwardSearchRateTables) {
        // 1. 存储抽奖策略范围值，如6000，用于生成6000以内的随机数
        // 这里只是去存一个key-value键值对，strategy-6000
        redisService.setValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key, rateRange.intValue());
        // 2. 存储打乱后的 概率查找表
        RMap<Integer, Integer> map = redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + key);
        map.putAll(shufflestrategyAwardSearchRateTables);
    }

    @Override
    public int getRateRange(Long strategyId) {
        // 从redis中拿出键值对的 6000
//        return  redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + strategyId);
        return getRateRange(String.valueOf(strategyId));
    }

    @Override
    public int getRateRange(String key) {
        log.info("key:{}",key);
        return redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key);
    }

    @Override
    public Integer getStrategyAwardAssemble(String key, int rateKey) {
        // 直接从map中拿回上面存的数据
        return redisService.getFromMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + key, rateKey);
    }

    @Override
    public StrategyEntity queryStrategyEntityByStrategyId(Long strategyId) { // 基本操作都是这些，看看redis有没有，没有去库中找，找到了存redis并返回
        // 通过strategyId 去获取 策略表中的一列数据实体
        // 首先是 正常的，访问一下redis
        StrategyEntity strategyEntity = redisService.getValue(Constants.RedisKey.STRATEGY_KEY + strategyId);
        if(null != strategyEntity) {
            return strategyEntity;
        }
        // 去数据库中查询 策略信息，再转成 策略实体
        Strategy strategy = strategyDao.queryStrategyByStrategyId(strategyId);
        StrategyEntity straEntity = StrategyEntity.builder()
                .strategyId(strategy.getStrategyId())
                .strategyDesc(strategy.getStrategyDesc())
                .ruleModels(strategy.getRuleModels())
                .build();
        // 存入redis中
        redisService.setValue(Constants.RedisKey.STRATEGY_KEY + strategyId, straEntity);
        return straEntity;
    }

    @Override
    public StrategyRuleEntity queryStrategyRule(Long strategyId, String ruleModel) {
        // 通过策略id和规则模式来查找策略规则，然后转为实体并返回

        // 后续做压力测试的时候可以改成 redis 缓存查询先，现在做压测，所以一阶段开发的时候没改
//        StrategyRuleEntity strategyRuleEn = redisService.getValue("Constants.RedisKey.RULEENTITY" + strategyId);
//        if(null != strategyRuleEn)
//            return strategyRuleEn;

        StrategyRule strategyRule = new StrategyRule();
        strategyRule.setStrategyId(strategyId);
        strategyRule.setRuleModel(ruleModel);
        StrategyRule strategyRuleResult = strategyRuleDao.queryStrategyRule(strategyRule);
        StrategyRuleEntity strategyRuleEntity = StrategyRuleEntity.builder()
                .strategyId(strategyRuleResult.getStrategyId())
                .awardId(strategyRuleResult.getAwardId())
                .ruleType(strategyRuleResult.getRuleType())
                .ruleModel(strategyRuleResult.getRuleModel())
                .ruleValue(strategyRuleResult.getRuleValue())
                .ruleDesc(strategyRuleResult.getRuleDesc())
                .build();
//        redisService.setValue("Constants.RedisKey.RULEENTITY" + strategyId, strategyRuleEntity);
        return strategyRuleEntity;
    }

    @Override
    public String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel) {

        StrategyRule strategyRule = new StrategyRule();
        strategyRule.setStrategyId(strategyId);
        strategyRule.setAwardId(awardId);
        strategyRule.setRuleModel(ruleModel);
//        redisService.getValue("Constants.RedisKey.RuleValue" + strategyRule);
        String result = strategyRuleDao.queryStrategyRuleValue(strategyRule);
        return result;
    }

    @Override
    public StrategyAwardRuleModelVO queryStrategyAwardRuleModel(Long strategyId, Integer awardId) {
        // 根据 strategyId 和 awardId
        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setStrategyId(strategyId);
        strategyAward.setAwardId(awardId);

        String ruleModels = strategyAwardDao.queryStrategyAwardRuleModels(strategyAward);
        return StrategyAwardRuleModelVO.builder().ruleModels(ruleModels).build();
    }


}
