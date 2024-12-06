package cn.bigmarket.domain.strategy.repository;


import cn.bigmarket.domain.strategy.model.entity.StrategyAwardEntity;
import cn.bigmarket.domain.strategy.model.entity.StrategyEntity;
import cn.bigmarket.domain.strategy.model.entity.StrategyRuleEntity;
import cn.bigmarket.domain.strategy.model.valobj.RuleTreeVO;
import cn.bigmarket.domain.strategy.model.valobj.RuleWeightVO;
import cn.bigmarket.domain.strategy.model.valobj.StrategyAwardRuleModelVO;
import cn.bigmarket.domain.strategy.model.valobj.StrategyAwardStockKeyVO;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    void cacheStrategyAwardCount(String cacheKey, Integer awardCount);

    Boolean subtractionAwardStock(String cacheKey);
    Boolean subtractionAwardStock(String cacheKey, Date endDateTime);


    void awardStockConsumeSendQueue(StrategyAwardStockKeyVO strategyAwardStockKeyVO);

    StrategyAwardStockKeyVO takeQueueValue();

    void updateStrategyAwardStock(Long strategyId, Integer awardId);
    /**
     * 根据策略ID+奖品ID的唯一值组合，查询奖品信息
     *
     * @param strategyId 策略ID
     * @param awardId    奖品ID
     * @return 奖品信息
     */

    StrategyAwardEntity queryStrategyAwardEntity(Long strategyId, Integer awardId);

    Long queryStrategyIdByActivityId(Long activityId);

    Integer queryTodayUserRaffleCount(String userId, Long strategyId);

    /**
     * 根据规则树ID集合查询奖品中加锁数量的配置「部分奖品需要抽奖N次解锁」
     *
     * @param treeIds 规则树ID值
     * @return key 规则树，value rule_lock 加锁值
     */
    Map<String, Integer> queryAwardRuleLockCount(String[] treeIds);

    /**
     * 根据用户ID、策略ID，查询用户活动账户总使用量
     *
     * @param userId     用户ID
     * @param strategyId 策略ID
     * @return 使用总量
     */
    Integer queryActivityAccountTotalUseCount(String userId, Long strategyId);
    /**
     * 查询奖品权重配置
     *
     * @param strategyId 策略ID
     * @return 权重规则
     */
    List<RuleWeightVO> queryAwardRuleWeight(Long strategyId);
}
