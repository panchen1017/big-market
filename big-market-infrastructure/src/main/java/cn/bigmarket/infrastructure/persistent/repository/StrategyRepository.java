package cn.bigmarket.infrastructure.persistent.repository;


import cn.bigmarket.domain.strategy.model.entity.StrategyAwardEntity;
import cn.bigmarket.domain.strategy.model.entity.StrategyEntity;
import cn.bigmarket.domain.strategy.model.entity.StrategyRuleEntity;
import cn.bigmarket.domain.strategy.model.valobj.*;
import cn.bigmarket.domain.strategy.repository.IStrategyRepository;
import cn.bigmarket.infrastructure.persistent.dao.*;
import cn.bigmarket.infrastructure.persistent.po.*;
import cn.bigmarket.infrastructure.persistent.redis.IRedisService;
import cn.bigmarket.types.common.Constants;
import cn.bigmarket.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RMap;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static cn.bigmarket.types.enums.ResponseCode.UN_ASSEMBLED_STRATEGY_ARMORY;

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
    @Resource
    private IRuleTreeDao ruleTreeDao;
    @Resource
    private IRuleTreeNodeDao ruleTreeNodeDao;
    @Resource
    private IRuleTreeNodeLineDao ruleTreeNodeLineDao;

    @Override
    public List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId) {

        // 优先从缓存中提取，如果没有，那就去数据库获取，获取了之后存入redis

        // 这边做了一个优先从redis缓存中获取的操作，big_market_strategy_award_key_"strategyId"
        // 在types层的Constants当中有许多关于redis的缓存
        // 这个cacheKey是从redis中拿的，这样子下次如果还是同样的strategyId,就不用做对应的Dao操作了
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_LIST_KEY + strategyId;

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
                        .awardTitle(strategyAward.getAwardTitle())
                        .sort(strategyAward.getSort())
                        .awardSubtitle(strategyAward.getAwardSubtitle())
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
        String cacheKey = Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key;
        if (!redisService.isExists(cacheKey)) {
            throw new AppException(UN_ASSEMBLED_STRATEGY_ARMORY.getCode(), cacheKey + Constants.COLON + UN_ASSEMBLED_STRATEGY_ARMORY.getInfo());
        }
        return redisService.getValue(cacheKey);
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
    public String queryStrategyRuleValue(Long strategyId, String ruleModel) {
        // 忽略awardId
        return queryStrategyRuleValue(strategyId, null, ruleModel);
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

    @Override
    public RuleTreeVO queryRuleTreeVOByTreeId(String treeId) {

        // 优先从缓存中获取
        String cacheKey = Constants.RedisKey.RULE_TREE_VO_KEY + treeId;
        RuleTreeVO ruleTreeVO = redisService.getValue(cacheKey);
        if(null != ruleTreeVO) return ruleTreeVO;

        RuleTree ruleTree = ruleTreeDao.queryRuleTreeByTreeId(treeId);
        List<RuleTreeNode> ruleTreeNodes = ruleTreeNodeDao.queryRuleTreeNodeListByTreeId(treeId);
        List<RuleTreeNodeLine> ruleTreeNodeLines = ruleTreeNodeLineDao.queryRuleTreeNodeLineListByTreeId(treeId);
        // 1. tree node line 转换Map结构
        Map<String, List<RuleTreeNodeLineVO>> ruleTreeNodeLineMap = new HashMap<>();
        for (RuleTreeNodeLine ruleTreeNodeLine : ruleTreeNodeLines) {
            RuleTreeNodeLineVO ruleTreeNodeLineVO = RuleTreeNodeLineVO.builder()
                    .treeId(ruleTreeNodeLine.getTreeId())
                    .ruleNodeFrom(ruleTreeNodeLine.getRuleNodeFrom())
                    .ruleNodeTo(ruleTreeNodeLine.getRuleNodeTo())
                    .ruleLimitType(RuleLimitTypeVO.valueOf(ruleTreeNodeLine.getRuleLimitType()))
                    .ruleLimitValue(RuleLogicCheckTypeVO.valueOf(ruleTreeNodeLine.getRuleLimitValue()))
                    .build();

            List<RuleTreeNodeLineVO> ruleTreeNodeLineVOList = ruleTreeNodeLineMap.computeIfAbsent(ruleTreeNodeLine.getRuleNodeFrom(), k -> new ArrayList<>());
            ruleTreeNodeLineVOList.add(ruleTreeNodeLineVO);
        }

        // 2. tree node 转换为Map结构
        Map<String, RuleTreeNodeVO> treeNodeMap = new HashMap<>();
        for (RuleTreeNode ruleTreeNode : ruleTreeNodes) {
            RuleTreeNodeVO ruleTreeNodeVO = RuleTreeNodeVO.builder()
                    .treeId(ruleTreeNode.getTreeId())
                    .ruleKey(ruleTreeNode.getRuleKey())
                    .ruleDesc(ruleTreeNode.getRuleDesc())
                    .ruleValue(ruleTreeNode.getRuleValue())
                    .treeNodeLineVOList(ruleTreeNodeLineMap.get(ruleTreeNode.getRuleKey()))
                    .build();
            treeNodeMap.put(ruleTreeNode.getRuleKey(), ruleTreeNodeVO);
        }

        // 3. 构建 Rule Tree
        RuleTreeVO ruleTreeVODB = RuleTreeVO.builder()
                .treeId(ruleTree.getTreeId())
                .treeName(ruleTree.getTreeName())
                .treeDesc(ruleTree.getTreeDesc())
                .treeRootRuleNode(ruleTree.getTreeRootRuleKey())
                .treeNodeMap(treeNodeMap)
                .build();


        // 在缓存中添加
        redisService.setValue(cacheKey, ruleTreeVODB);
        return ruleTreeVODB;
    }

    @Override
    public void cacheStrategyAwardCount(String cacheKey, Integer awardCount) {
        // 为了方便后面 incr decr 新增和减少redis当中的long值，使用setAtomicLong
        if(null != redisService.getValue(cacheKey)) return;
        // 如果当前 redis 中已经存了对应 cacheKey 的奖品数量，就不用重复装配了
        // 如果 redis 未存 cacheKey 的奖品数量，就装配
        redisService.setAtomicLong(cacheKey, awardCount);
    }

    @Override
    public Boolean subtractionAwardStock(String cacheKey) {
        // surplus 指的是当前剩余库存数量减一之后的数量
        long surplus = redisService.decr(cacheKey);
        if(surplus < 0) {
            // 如果剩余值小于 0 ，那当前就没库存了
            redisService.setValue(cacheKey, 0);
            return false;
        }
        String lockKey = cacheKey + Constants.UNDERLINE + surplus;
        // 1. 按照cacheKey decr 后的值，如 99、98、97 和 key 组成为库存锁的key进行使用。
        // 2. 加锁为了兜底，如果后续有恢复库存，手动处理等，也不会超卖。因为所有的可用库存key，都被加锁了。
        // setNx加锁操作
        // setNX 是 "Set if Not Exists"（如果不存在则设置）的缩写。
        // 其中 key 是要设置的键名，value 是要设置的值。
        // 如果键 key 不存在，则将键 key 的值设置为 value，并返回 1 表示设置成功。如果键 key 已经存在，则不进行任何操作，返回 0 表示设置失败

        Boolean lock = redisService.setNx(lockKey);
        // 如果加锁不成功的话，就会返回一个日志
        if(!lock) {
            log.info("策略奖品库存加锁失败 {}", lockKey);
        }
        return lock;
    }

    @Override
    public void awardStockConsumeSendQueue(StrategyAwardStockKeyVO strategyAwardStockKeyVO) {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_QUERY_KEY;
        // 创建队列信息
        RBlockingQueue<StrategyAwardStockKeyVO> blockingQueue = redisService.getBlockingQueue(cacheKey);
        // 放到延迟队列中去
        RDelayedQueue<StrategyAwardStockKeyVO> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        // 3 秒过后再加到延迟队列中去
        delayedQueue.offer(strategyAwardStockKeyVO, 3, TimeUnit.SECONDS);
    }

    @Override
    public StrategyAwardStockKeyVO takeQueueValue() {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_QUERY_KEY;
        RBlockingQueue<StrategyAwardStockKeyVO> blockingQueue = redisService.getBlockingQueue(cacheKey);
        // 使用 poll 弹出，如果弹不出 返回 null
        return blockingQueue.poll();
    }

    @Override
    public void updateStrategyAwardStock(Long strategyId, Integer awardId) {
        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setStrategyId(strategyId);
        strategyAward.setAwardId(awardId);
        strategyAwardDao.updateStrategyAwardStock(strategyAward);
    }

    @Override
    public StrategyAwardEntity queryStrategyAwardEntity(Long strategyId, Integer awardId) {
        // 优先从缓存获取
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_KEY + strategyId + Constants.UNDERLINE + awardId;
        StrategyAwardEntity strategyAwardEntity = redisService.getValue(cacheKey);
        if (null != strategyAwardEntity) return strategyAwardEntity;
        // 查询数据
        StrategyAward strategyAwardReq = new StrategyAward();
        strategyAwardReq.setStrategyId(strategyId);
        strategyAwardReq.setAwardId(awardId);
        StrategyAward strategyAwardRes = strategyAwardDao.queryStrategyAward(strategyAwardReq);
        // 转换数据
        strategyAwardEntity = StrategyAwardEntity.builder()
                .strategyId(strategyAwardRes.getStrategyId())
                .awardId(strategyAwardRes.getAwardId())
                .awardTitle(strategyAwardRes.getAwardTitle())
                .awardSubtitle(strategyAwardRes.getAwardSubtitle())
                .awardCount(strategyAwardRes.getAwardCount())
                .awardCountSurplus(strategyAwardRes.getAwardCountSurplus())
                .awardRate(strategyAwardRes.getAwardRate())
                .sort(strategyAwardRes.getSort())
                .build();
        // 缓存结果
        redisService.setValue(cacheKey, strategyAwardEntity);
        // 返回数据
        return strategyAwardEntity;

    }


}
