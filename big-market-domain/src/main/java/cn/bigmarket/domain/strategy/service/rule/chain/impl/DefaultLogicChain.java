package cn.bigmarket.domain.strategy.service.rule.chain.impl;

import cn.bigmarket.domain.strategy.service.armory.IStrategyDispatch;
import cn.bigmarket.domain.strategy.service.rule.chain.AbstractLogicChain;
import cn.bigmarket.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 *  兜底 责任链 实现类
 */
@Slf4j
@Component("default")
public class DefaultLogicChain extends AbstractLogicChain {

    @Resource
    protected IStrategyDispatch strategyDispatch;


    /**
     * 默认的责任链「作为最后一个链」，走兜底
     *
     */
    @Override
    public DefaultChainFactory.StrategyAwardVO logic(String userId, Long strategyId) {
        Integer awardId = strategyDispatch.getRandomAward(strategyId);
        log.info("抽奖责任链-默认处理 userId:{} strategyId:{}  ruleModel:{}  awardId:{}",userId, strategyId, ruleModels(), awardId);
        return DefaultChainFactory.StrategyAwardVO.builder()
                .awardId(awardId)
                .logicModel(ruleModels())
                .build();
    }

    @Override
    protected String ruleModels() {
        return DefaultChainFactory.LogicModel.RULE_DEFAULT.getCode();
    }
}
