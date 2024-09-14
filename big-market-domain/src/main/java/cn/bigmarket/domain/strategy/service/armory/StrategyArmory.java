package cn.bigmarket.domain.strategy.service.armory;


import cn.bigmarket.domain.strategy.model.entity.StrategyAwardEntity;
import cn.bigmarket.domain.strategy.repository.IStrategyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.ranges.Range;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @description 策略装配库(兵工厂)，负责初始化策略计算
 */
@Service
@Slf4j
public class StrategyArmory implements  IStrategyArmory{

    @Resource
    private IStrategyRepository repository;

    @Override
    public void assembleLotteryStrategy(Long strategyId) {

        // 根据策略id去查他的值
        // 1. 查询策略配置
        List<StrategyAwardEntity> strategyAwardEntities = repository.queryStrategyAwardList(strategyId);

        // 2. 获取表中概率的最小值
        BigDecimal minAwardRate = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // 3. 获取表中概率的总和
        BigDecimal totalAwardRate = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add); // 选择 BigDecimal 是为了确保计算过程中不发生精度丢失或舍入误差。

        // 4. 用 1 / 0.0001 获得概率范围， 百分位， 千分位， 万分位。
        BigDecimal rateRange = totalAwardRate.divide(minAwardRate, 0, RoundingMode.CEILING);
        // divide就是total精确地除以min，0 这个参数指定结果应该保留的小数位数。0 代表不保留小数位，即结果必须是整数。
        // RoundingMode.CEILING是指向上取整，就是往比自己数字大的取整。

        // 5.现在得到了一个范围值，就是抽奖在这个范围当中  strategyAwardSearchRateTables 存奖品id
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
        // 6. 乱序 将存好的 11111111......11111112222222......222222......333333....444444......555555 打乱
        Collections.shuffle(strategyAwardSearchRateTables);

        // 7.乱序之后存到 hashmap 中 <0, 5>,<1, 5>,<2, 1>,<3, 3>,<4, 2>......
        // 这样子就方便之后一去查找就能够从 redis 中返回对应奖品id
        // 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 ......  key
        // 2 4 7 1 4 2 5 1 4 1 4 1 4 1  2  5  3  6  2  6  4  1  1  ...... value
        HashMap<Integer, Integer> shufflestrategyAwardSearchRateTables = new HashMap<>();
        for (int i = 0; i < strategyAwardSearchRateTables.size(); i++) {
            // 对应的奖品是啥，存起来
            shufflestrategyAwardSearchRateTables.put(i, strategyAwardSearchRateTables.get(i));
        }
        // 8. 存储到redis
        // 传入策略id， map大小， map
        repository.storeStrategySearchRateTables(strategyId, shufflestrategyAwardSearchRateTables.size(), shufflestrategyAwardSearchRateTables);

    }

    @Override
    public Integer getRandomAward(Long strategyId) {

        int rateRange = repository.getRateRange(strategyId);
        // 通过随机值去查找一下奖品信息
        // 这边去一个随机数，范围就是 0 ~ rateRange
        // SecureRandom 是一种更强大的随机数生成器，专为安全场景设计。SecureRandom().nextInt(rateRange)就是给一个 0 ~ 6000 的随机数
        // 它使用加密算法作为随机数生成的基础，生成的随机数是不可预测的，即使你知道了部分的种子或之前的输出，也无法预测下一个随机数。
        return repository.getStrategyAwardAssemble(strategyId, new SecureRandom().nextInt(rateRange));
    }


}
