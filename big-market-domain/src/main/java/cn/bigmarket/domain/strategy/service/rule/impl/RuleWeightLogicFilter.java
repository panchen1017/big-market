package cn.bigmarket.domain.strategy.service.rule.impl;

import cn.bigmarket.domain.strategy.model.entity.RuleActionEntity;
import cn.bigmarket.domain.strategy.model.entity.RuleMatterEntity;
import cn.bigmarket.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import cn.bigmarket.domain.strategy.repository.IStrategyRepository;
import cn.bigmarket.domain.strategy.service.annotation.LogicStrategy;
import cn.bigmarket.domain.strategy.service.rule.ILogicFilter;
import cn.bigmarket.domain.strategy.service.rule.factory.DefaultLogicFactory;
import cn.bigmarket.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
     * 权重规律逻辑处理
 */
@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.RULE_WIGHT)
public class RuleWeightLogicFilter implements ILogicFilter<RuleActionEntity.RaffleBeforeEntity> {

    @Resource
    private IStrategyRepository repository;

    private Long userScore = 4500L;

    /**
     * 权重规则过滤；
     * 1. 权重规则格式；4000:102,103,104,105 5000:102,103,104,105,106,107 6000:102,103,104,105,106,107,108,109
     * 2. 解析数据格式；判断哪个范围符合用户的特定抽奖范围
     *
     * @param ruleMatterEntity 规则物料实体对象
     * @return 规则过滤结果
     */
    @Override
    public RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> filter(RuleMatterEntity ruleMatterEntity) {

        log.info("规则过滤-权重范围 userId:{} strategyId:{} ruleModel:{}", ruleMatterEntity.getUserId(), ruleMatterEntity.getStrategyId(), ruleMatterEntity.getRuleModel());
        Integer awardId = ruleMatterEntity.getAwardId();
        Long strategyId = ruleMatterEntity.getStrategyId();

        // 用 策略id，奖品id，rule_model去查对应的rule_value
        // 得到 4000:102,103,104,105
        //      5000:102,103,104,105,106,107
        //      6000:102,103,104,105,106,107,108,109
        String ruleValue = repository.queryStrategyRuleValue(ruleMatterEntity.getStrategyId(), ruleMatterEntity.getAwardId(), ruleMatterEntity.getRuleModel());

        // 1. 根据用户ID查询用户抽奖消耗的积分值
        // 切分积分对应的奖品 id 用 map 组装
        // 如果返回的 为空，直接返回就行，接下去的过滤没什么用
        Map<Long, String> analyticalValueGroup = getAnalyticalValue(ruleValue);
        if(null == analyticalValueGroup || analyticalValueGroup.isEmpty()) {
            // 返回，不需要接管
            return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                    .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                    .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                    .build();
        }

        // 2. 对 map 中的key做一个排序，因为map使用set类型去存的，目前是无序的
        // 我们的目的就是找到当前用户的例如：4500积分对应最小积分范围
        // 4000:102,103,104,105
        ArrayList<Long> analyticalValueGroupKeys = new ArrayList<>(analyticalValueGroup.keySet());

        Collections.sort(analyticalValueGroupKeys);
        Collections.reverse(analyticalValueGroupKeys);
//        log.info("analyticalValueGroupKeys:{}",analyticalValueGroupKeys);

        // 3. 找出最小符合的值，也就是【4500 积分，能找到 4000:102,103,104,105】、【5000 积分，能找到 5000:102,103,104,105,106,107】
        Long nextValue = analyticalValueGroupKeys.stream()
                .filter(keys -> userScore >= keys)
                .findFirst()
                .orElse(null);// 找不到的话，返回 null
        log.info("nextValue:{}", nextValue);

        // nextValue就是最大的符合 >= keys 的符合值，比如4050就是 4000 ，40500就是6000
        // 4. 如果 nextValue 不为空，那就接管到权重范围过滤，如果为空，那么放行
        if(null != nextValue) {
            return  RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                    .data(RuleActionEntity.RaffleBeforeEntity.builder()
                            .strategyId(strategyId)
                            .ruleWeightValueKey(analyticalValueGroup.get(nextValue))
                            .build())
                    .ruleModel(DefaultLogicFactory.LogicModel.RULE_WIGHT.getCode())
                    .code(RuleLogicCheckTypeVO.TAKE_OVER.getCode())
                    .info(RuleLogicCheckTypeVO.TAKE_OVER.getInfo())
                    .build();
        }
        // 如果 nextValue 为空，说明没有匹配值，直接放行
        return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                .build();

    }

    private Map<Long, String> getAnalyticalValue(String ruleValue) {
        // 拆解 4000:102,103,104,105 5000:102,103,104,105,106,107 6000:102,103,104,105,106,107,108,109
        // 方法返回的是 <4000, 4000:102,103,104,105>
        //            <5000, 5000:102,103,104,105,106,107>
        //            <6000, 6000:102,103,104,105,106,107,108,109>
        String[] splitRuleValueBySpace = ruleValue.split(Constants.SPACE);
        Map<Long, String> ruleValueMap = new HashMap<>();
        for (String ruleValueKey : splitRuleValueBySpace) {
            if(null == ruleValueKey || ruleValueKey.isEmpty())
                return null;
            String[] split = ruleValueKey.split(Constants.COLON);
            if(split.length != 2) {
                throw new IllegalArgumentException("rule_weight rule_rule invalid input format" + ruleValueKey);
            }
            ruleValueMap.put(Long.parseLong(split[0]), ruleValueKey);
        }
        return ruleValueMap;
    }


}
