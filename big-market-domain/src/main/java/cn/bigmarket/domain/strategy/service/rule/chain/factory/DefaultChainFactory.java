package cn.bigmarket.domain.strategy.service.rule.chain.factory;

import cn.bigmarket.domain.strategy.model.entity.StrategyEntity;
import cn.bigmarket.domain.strategy.repository.IStrategyRepository;
import cn.bigmarket.domain.strategy.service.rule.chain.ILogicChain;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 默认工厂
 *
 */
@Service
public class DefaultChainFactory {

     private final Map<String, ILogicChain> logicChainGroup;

     private final IStrategyRepository repository;

    public DefaultChainFactory(Map<String, ILogicChain> logicChainGroup, IStrategyRepository repository) {
        this.logicChainGroup = logicChainGroup;
        this.repository = repository;
    }

    public ILogicChain openLogicChain(Long strategyId) {

        // 通过 strategyId 获取 strategy 实体
        StrategyEntity strategy = repository.queryStrategyEntityByStrategyId(strategyId);
        // 获取到 strategy 实体中 ruleModels：rule_weight, rule_blacklist
        String[] ruleModels = strategy.ruleModels();
        if (null == ruleModels || ruleModels.length == 0) {
            // 如果没有ruleModels，那么直接返回 default 兜底奖品
            return logicChainGroup.get("default");
        }

        // 要先把第 0 个责任链（第一个ruleModels）拿出来
        // 因为要从第 0 个链上挂节点
        // 这就好比，黑名单，白名单，权重......一个个按照顺序挂上责任链，一个个执行看看能不能接管
        ILogicChain logicChain = logicChainGroup.get(ruleModels[0]);
        // current 就是类似指针的东西
        ILogicChain current = logicChain;
        for (int i = 1; i < ruleModels.length; i++) {
            // 一个个往后面挂结点
            ILogicChain nextChain = logicChainGroup.get(ruleModels[i]);
            current = current.appendNext(nextChain);
        }
        // 最后添加一个兜底链路
        current.appendNext(logicChainGroup.get("default"));
        return logicChain;

    }

}
