package cn.bigmarket.domain.strategy.service.rule.chain.impl;

import cn.bigmarket.domain.strategy.repository.IStrategyRepository;
import cn.bigmarket.domain.strategy.service.rule.chain.AbstractLogicChain;
import cn.bigmarket.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import cn.bigmarket.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 *  黑名单方法 责任链 实现类
 */
@Slf4j
@Component("rule_blacklist")
public class BackListLogicChain extends AbstractLogicChain {
    @Resource
    private IStrategyRepository strategyRepository;
    @Override
    public DefaultChainFactory.StrategyAwardVO logic(String userId, Long strategyId) {
        log.info("抽奖责任链-黑名单开始 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModels());
        // 查询规则配置
        // 只使用 strategyId 和 ruleModel 去获取 RuleValue，让提供方提供差异化的方法，这边不需要奖品id
        String ruleValue = strategyRepository.queryStrategyRuleValue(strategyId, ruleModels());
        if(null == ruleValue)
            return next().logic(userId, strategyId);
        // 用冒号 ” : “ 进行拆分 101 : user001,user002,user003
        String[] splitRuleValue = ruleValue.split(Constants.COLON);
        // splitRuleValue[0] 是 awardId 101
        Integer awardId = Integer.parseInt(splitRuleValue[0]);

        // 过滤其他规则
        // 2. 个人 id 是否在添加的 id 里面，如果在的话，就加入黑名单
        String[] userBlackIds = splitRuleValue[1].split(Constants.SPLIT);
        for (String userBlackId : userBlackIds) {
            // 如果匹配上黑名单id，那就走黑名单接管
            if(userBlackId.equals(userId)) {
                log.info("抽奖责任链-黑名单接管 userId: {} strategyId: {} ruleModel: {} awardId: {}", userId, strategyId, ruleModels(), awardId);
                return DefaultChainFactory.StrategyAwardVO.builder()
                        .awardId(awardId)
                        .logicModel(ruleModels())
                        .awardRuleValue("0.01,1")// 给一个默认的固定值，只要是黑名单，就给这么点
                        .build();
            }
        }
        // 过滤其他责任链
        log.info("抽奖责任链-黑名单放行 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModels());

        return next().logic(userId, strategyId);
    }
    @Override
    protected String ruleModels() {
        return DefaultChainFactory.LogicModel.RULE_BLACKLIST.getCode();
    }
}
