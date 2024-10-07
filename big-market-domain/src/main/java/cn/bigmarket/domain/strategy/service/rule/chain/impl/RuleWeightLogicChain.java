package cn.bigmarket.domain.strategy.service.rule.chain.impl;

import cn.bigmarket.domain.strategy.repository.IStrategyRepository;
import cn.bigmarket.domain.strategy.service.armory.IStrategyDispatch;
import cn.bigmarket.domain.strategy.service.rule.chain.AbstractLogicChain;
import cn.bigmarket.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import cn.bigmarket.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 *  权重 责任链 实现类
 */
@Slf4j
@Component("rule_weight")
public class RuleWeightLogicChain extends AbstractLogicChain {
    @Resource
    private IStrategyRepository strategyRepository;
    @Resource
    protected IStrategyDispatch strategyDispatch;
    private Long userScore = 0L;
    /**
     * 权重责任链过滤；
     * 1. 权重规则格式；4000:102,103,104,105 5000:102,103,104,105,106,107 6000:102,103,104,105,106,107,108,109
     * 2. 解析数据格式；判断哪个范围符合用户的特定抽奖范围
     */
    @Override
    public DefaultChainFactory.StrategyAwardVO logic(String userId, Long strategyId) {
        log.info("抽奖责任链-权重开始 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModels());
        String ruleValue = strategyRepository.queryStrategyRuleValue(strategyId, ruleModels());
        // 1. 根据用户ID查询用户抽奖消耗的积分值
        // 切分积分对应的奖品 id 用 map 组装，如果返回的 为空，直接返回 null 就行
        Map<Long, String> analyticalValueGroup = getAnalyticalValue(ruleValue);
        if(null == analyticalValueGroup || analyticalValueGroup.isEmpty()) {
            return null;
        }
        // 2. 对 map 中的key做一个排序，因为map使用set类型去存的，目前是无序
        // 转换keys值
        List<Long> analyticalSortedKeys = new ArrayList<>(analyticalValueGroup.keySet());
        Collections.sort(analyticalSortedKeys);

        // 3. 找出最小符合的值，也就是【4500 积分，能找到 4000:102,103,104,105】、【5050 积分，能找到 5000:102,103,104,105,106,107】
        Long nextValue = analyticalSortedKeys.stream()
                .sorted(Comparator.reverseOrder())// 置返数据
                .filter(analyticalSortedKeyValue -> userScore >= analyticalSortedKeyValue)
                .findFirst()
                .orElse(null);

        // 4. 如果 nextValue 不为空，那就接管到权重范围过滤，如果为空，那么放行
        if(null != nextValue) {
            // 这里的 analyticalValueGroup.get(nextValue) 就是 102,103,104,105,106,107（可以抽到的奖品池）
            Integer awardId = strategyDispatch.getRandomAward(strategyId, analyticalValueGroup.get(nextValue));
            log.info("抽奖责任链-权重接管 userId: {} strategyId: {} ruleModel: {} awardId: {}", userId, strategyId, ruleModels(), awardId);
            return DefaultChainFactory.StrategyAwardVO.builder()
                    .awardId(awardId)
                    .logicModel(ruleModels())
                    .build();
        }

        // 如果 nextValue 为空，说明没有匹配值，直接放行
        log.info("抽奖责任链-权重放行 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModels());
        return next().logic(userId, strategyId);

    }
//    private Map<Long, String> getAnalyticalValue(String ruleValue) {
//        // 拆解 4000:102,103,104,105 5000:102,103,104,105,106,107 6000:102,103,104,105,106,107,108,109
//        // 方法返回的是 <4000, 4000:102,103,104,105>
//        //            <5000, 5000:102,103,104,105,106,107>
//        //            <6000, 6000:102,103,104,105,106,107,108,109>
//        String[] splitRuleValueBySpace = ruleValue.split(Constants.SPACE);
//        Map<Long, String> ruleValueMap = new HashMap<>();
//        for (String ruleValueKey : splitRuleValueBySpace) {
//            if(null == ruleValueKey || ruleValueKey.isEmpty())
//                return null;
//            String[] split = ruleValueKey.split(Constants.COLON);
//            if(split.length != 2) {
//                throw new IllegalArgumentException("rule_weight rule_rule invalid input format" + ruleValueKey);
//            }
//            ruleValueMap.put(Long.parseLong(split[0]), ruleValueKey);
//        }
//        return ruleValueMap;
//    }
    private Map<Long, String> getAnalyticalValue(String ruleValue) {
        String[] ruleValueGroups = ruleValue.split(Constants.SPACE);
        Map<Long, String> ruleValueMap = new HashMap<>();
        for (String ruleValueKey : ruleValueGroups) {
            // 检查输入是否为空
            if (ruleValueKey == null || ruleValueKey.isEmpty()) {
                return ruleValueMap;
            }
            // 分割字符串以获取键和值
            String[] parts = ruleValueKey.split(Constants.COLON);
            if (parts.length != 2) {
                throw new IllegalArgumentException("rule_weight rule_rule invalid input format" + ruleValueKey);
            }
            ruleValueMap.put(Long.parseLong(parts[0]), ruleValueKey);
        }
        return ruleValueMap;
    }

    @Override
    protected String ruleModels() {
        return DefaultChainFactory.LogicModel.RULE_WEIGHT.getCode();
    }
}
