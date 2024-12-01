package cn.bigmarket.domain.strategy.service.raffle;


import cn.bigmarket.domain.strategy.model.entity.StrategyAwardEntity;
import cn.bigmarket.domain.strategy.model.valobj.RuleTreeVO;
import cn.bigmarket.domain.strategy.model.valobj.StrategyAwardRuleModelVO;
import cn.bigmarket.domain.strategy.model.valobj.StrategyAwardStockKeyVO;
import cn.bigmarket.domain.strategy.repository.IStrategyRepository;
import cn.bigmarket.domain.strategy.service.AbstractRaffleStrategy;
import cn.bigmarket.domain.strategy.service.IRaffleAward;
import cn.bigmarket.domain.strategy.service.IRaffleRule;
import cn.bigmarket.domain.strategy.service.IRaffleStock;
import cn.bigmarket.domain.strategy.service.armory.IStrategyDispatch;
import cn.bigmarket.domain.strategy.service.rule.chain.ILogicChain;
import cn.bigmarket.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import cn.bigmarket.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import cn.bigmarket.domain.strategy.service.rule.tree.factory.engine.IDecisionTreeEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy implements IRaffleAward, IRaffleStock , IRaffleRule {

    public DefaultRaffleStrategy(IStrategyRepository repository, IStrategyDispatch strategyDispatch, DefaultChainFactory defaultChainFactory, DefaultTreeFactory defaultTreeFactory) {
        super(repository, strategyDispatch, defaultChainFactory, defaultTreeFactory);
    }


    @Override
    public DefaultChainFactory.StrategyAwardVO raffleLogicChain(String userId, Long strategyId) {

        // 调用责任链接口
        ILogicChain logicChain = defaultChainFactory.openLogicChain(strategyId);

        // 使用责任链接口当中的 logic ，一条条去跑，返回 chainAward
        DefaultChainFactory.StrategyAwardVO chainAwardResult = logicChain.logic(userId, strategyId);

        return chainAwardResult;
    }

    @Override
    public DefaultTreeFactory.StrategyAwardVO raffleLogicTree(String userId, Long strategyId, Integer awardId) {
        return raffleLogicTree(userId, strategyId, awardId, null);
    }

    @Override
    public DefaultTreeFactory.StrategyAwardVO raffleLogicTree(String userId, Long strategyId, Integer awardId, Date endDateTime) {
        // 用 strategyId 和 awardId 去 strategy_award 表中找 ruleModel
        StrategyAwardRuleModelVO strategyAwardRuleModelVO = repository.queryStrategyAwardRuleModel(strategyId, awardId);
        // 如果为空，返回默认 awardId
        if(null == strategyAwardRuleModelVO)
            return DefaultTreeFactory.StrategyAwardVO.builder()
                    .awardId(awardId)
                    .build();
        // 用刚刚找到的 ruleModels 例如：“tree_lock”,“tree_luck_award”....
        // 去把这个 ruleModels 这个名字作为 树名treeId 去查找 一个规则树对象
        RuleTreeVO ruleTreeVO = repository.queryRuleTreeVOByTreeId(strategyAwardRuleModelVO.getRuleModels());
        if(null == ruleTreeVO) {
            // 也就是说，当我们在 strategy_award 表中找到了 ruleModel（“tree_lock”,“tree_luck_award”....）
            // 但是 rule_tree 表中并没有对应的树，要抛个异常
            throw new RuntimeException("存在抽奖策略配置的规则模型 Key，未在库表 rule_tree、rule_tree_node、rule_tree_line 配置对应的规则树信息 " + strategyAwardRuleModelVO.getRuleModels());
        }
        // 调用决策树工厂接口，实现其中的 process 方法（上节课做的）
        IDecisionTreeEngine treeEngine = defaultTreeFactory.openLogicTree(ruleTreeVO);
        DefaultTreeFactory.StrategyAwardVO treeProcessResult = treeEngine.process(userId, strategyId, awardId, endDateTime);
        return treeProcessResult;
    }

    @Override
    public StrategyAwardStockKeyVO takeQueueValue() throws InterruptedException {
        return repository.takeQueueValue();
    }

    @Override
    public void updateStrategyAwardStock(Long strategyId, Integer awardId) {
        repository.updateStrategyAwardStock(strategyId, awardId);
    }

    @Override
    public List<StrategyAwardEntity> queryRaffleStrategyAwardList(Long strategyId) {
        return repository.queryStrategyAwardList(strategyId);
    }

    @Override
    public List<StrategyAwardEntity> queryRaffleStrategyAwardListByActivityId(Long activityId) {
        Long strategyId = repository.queryStrategyIdByActivityId(activityId);
        return queryRaffleStrategyAwardList(strategyId);
    }

    @Override
    public Map<String, Integer> queryAwardRuleLockCount(String[] treeIds) {
        return repository.queryAwardRuleLockCount(treeIds);
    }
}
