package cn.bigmarket.domain.strategy.service.armory;


import cn.bigmarket.domain.strategy.model.entity.StrategyAwardEntity;
import cn.bigmarket.domain.strategy.model.entity.StrategyEntity;
import cn.bigmarket.domain.strategy.model.entity.StrategyRuleEntity;
import cn.bigmarket.domain.strategy.repository.IStrategyRepository;
import cn.bigmarket.types.enums.ResponseCode;
import cn.bigmarket.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.*;

/**
 * @description 策略装配库(兵工厂)，负责初始化策略计算
 */
@Service
@Slf4j
public class StrategyArmoryDispatch implements  IStrategyArmory, IStrategyDispatch {

    @Resource
    private IStrategyRepository repository;

    @Override
    public boolean assembleLotteryStrategy(Long strategyId) {

        // 根据策略id去查他的值
        // 1. 查询策略配置
        List<StrategyAwardEntity> strategyAwardEntities = repository.queryStrategyAwardList(strategyId);//
        assembleLotteryStrategy(String.valueOf(strategyId), strategyAwardEntities);

        // 2. 权重策略配置 - 适用于 rule_weight 权重规则配置
        // 查询出我要的权重配置
        StrategyEntity strategyEntity = repository.queryStrategyEntityByStrategyId(strategyId);
        // 判断strategy表中的有没有rule_weight
        String ruleWeight = strategyEntity.getRuleWeight();// strategyEntity 实体类中定义的方法
        if(null == ruleWeight) // 如果strategy表中的 rule_weight 为空，这说明它没有规则约束
            return true;

        // 接下去根据strategyId，和 权重 来查询具体的 strategy_rule 的 id
        StrategyRuleEntity strategyRuleEntity = repository.queryStrategyRule(strategyId, ruleWeight);
        if(null == strategyRuleEntity) {
            // 抛出一个异常
            throw new AppException(ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getCode(), ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getInfo());
        }

        // 拿到  4000:102,103,104,105
        //      5000:102,103,104,105,106,107
        //      6000:102,103,104,105,106,107,108,109
        Map<String, List<Integer>> ruleWeightValuesMap = strategyRuleEntity.getRuleWeightValues();

        // 拿到map中的 KEY序列
        Set<String> keys = ruleWeightValuesMap.keySet();
        for (String key : keys) {
            // 拿到每个 key 对应的 可以抽到的奖品编号
            List<Integer> ruleWeightValues = ruleWeightValuesMap.get(key);

            // 再用深拷贝 去拷贝一份
            ArrayList<StrategyAwardEntity> strategyAwardEntityClone = new ArrayList<>(strategyAwardEntities);
            // 进行删数据的操作， 判断如果是我存储进来的值那就保存，反之就删掉
            strategyAwardEntityClone.removeIf(entity->!ruleWeightValues.contains(entity.getAwardId()));
            //该代码的作用是：从 strategyAwardEntityClone 集合中移除那些其 AwardId 不在 ruleWeightValues 集合中的 entity 对象。
            // 也就是说，最终保留下来的 entity 对象的 AwardId 必须是存在于 ruleWeightValues 集合中的。
            assembleLotteryStrategy(String.valueOf(strategyId).concat("_").concat(key), strategyAwardEntityClone);
        }
        return true;
    }
    private void assembleLotteryStrategy(String key,  List<StrategyAwardEntity> strategyAwardEntities) {

        // 1. 获取表中概率的最小值
        BigDecimal minAwardRate = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // 2. 获取表中概率的总和
        BigDecimal totalAwardRate = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add); // 选择 BigDecimal 是为了确保计算过程中不发生精度丢失或舍入误差。

        // 3. 用 1 / 0.0001 获得概率范围， 百分位， 千分位， 万分位。
        BigDecimal rateRange = totalAwardRate.divide(minAwardRate, 0, RoundingMode.CEILING);
        // divide就是total精确地除以min，0 这个参数指定结果应该保留的小数位数。0 代表不保留小数位，即结果必须是整数。
        // RoundingMode.CEILING是指向上取整，就是往比自己数字大的取整。

        // 4.装配这些奖品序号，放入列表中。现在得到了一个范围值，就是抽奖在这个范围当中  strategyAwardSearchRateTables 存奖品id
        // 比如 6000 个奖品 ，2000个小米台灯对应序号 （1），那就是 存 2000 个（1），xxx 个（2），xxx 个（3）
        ArrayList<Integer> strategyAwardSearchRateTables = new ArrayList<>(rateRange.intValue());
        for (StrategyAwardEntity strategyAward : strategyAwardEntities) {
            // 获取奖品id和奖品概率
            Integer awardId = strategyAward.getAwardId();
            BigDecimal awardRate = strategyAward.getAwardRate();

            // 计算出每个概率值需要存放到的查找表的数量，循环填充
            // rateRange就是一个总量，乘以awardRate，就能算出这个奖品对应的数据量是多少，让 i 去循环
            // 比如101号奖品的概率为0.9，概率范围为100，那么101号在集合中应该占90个位置
            for (int i = 0; i < rateRange.multiply(awardRate).setScale(BigDecimal.ROUND_CEILING).intValue(); i++) {
                strategyAwardSearchRateTables.add(awardId);
            }
        }
        // 5. 乱序 将存好的 11111111......11111112222222......222222......333333....444444......555555 打乱
        Collections.shuffle(strategyAwardSearchRateTables);

        // 6.乱序之后存到 hashmap 中 <0, 5>,<1, 5>,<2, 1>,<3, 3>,<4, 2>......
        // 这样子就方便之后一去查找就能够从 redis 中返回对应奖品id
        // 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 ......  key
        // 2 4 7 1 4 2 5 1 4 1 4 1 4 1  2  5  3  6  2  6  4  1  1  ...... value
        HashMap<Integer, Integer> shufflestrategyAwardSearchRateTables = new HashMap<>();
        for (int i = 0; i < strategyAwardSearchRateTables.size(); i++) {
            // 对应的奖品是啥，存起来
            shufflestrategyAwardSearchRateTables.put(i, strategyAwardSearchRateTables.get(i));
        }
        // 7. 存储到redis
        // 传入策略id， map大小， map
        repository.storeStrategySearchRateTables(key, shufflestrategyAwardSearchRateTables.size(), shufflestrategyAwardSearchRateTables);

    }
    @Override
    public Integer getRandomAward(Long strategyId) {

        int rateRange = repository.getRateRange(strategyId);
        // 通过随机值去查找一下奖品信息
        // 这边去一个随机数，范围就是 0 ~ rateRange
        // SecureRandom 是一种更强大的随机数生成器 ，专为安全场景设计。SecureRandom().nextInt(rateRange)就是给一个 0 ~ 6000 的随机数
        // 它使用加密算法作为随机数生成的基础，生成的随机数是不可预测的，即使你知道了部分的种子或之前的输出，也无法预测下一个随机数。
        return repository.getStrategyAwardAssemble(String.valueOf(strategyId), new SecureRandom().nextInt(rateRange));
    }

    @Override
    public Integer getRandomAward(Long strategyId, String ruleWeightValue) {

        String key = String.valueOf(strategyId).concat("_").concat(ruleWeightValue);

        int rateRange = repository.getRateRange(key);
//        log.info("rateRange:{}", rateRange);
        // 通过随机值去查找一下奖品信息
        // 这边去一个随机数，范围就是 0 ~ rateRange
        // SecureRandom 是一种更强大的随机数生成器 ，专为安全场景设计。SecureRandom().nextInt(rateRange)就是给一个 0 ~ 6000 的随机数
        // 它使用加密算法作为随机数生成的基础，生成的随机数是不可预测的，即使你知道了部分的种子或之前的输出，也无法预测下一个随机数。
        return repository.getStrategyAwardAssemble(key, new SecureRandom().nextInt(rateRange));
    }


}
