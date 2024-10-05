package cn.bigmarket.domain.strategy.service.raffle;


import cn.bigmarket.domain.strategy.model.entity.RaffleFactorEntity;
import cn.bigmarket.domain.strategy.model.entity.RuleActionEntity;
import cn.bigmarket.domain.strategy.model.entity.RuleMatterEntity;
import cn.bigmarket.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import cn.bigmarket.domain.strategy.repository.IStrategyRepository;
import cn.bigmarket.domain.strategy.service.AbstractRaffleStrategy;
import cn.bigmarket.domain.strategy.service.armory.IStrategyDispatch;
import cn.bigmarket.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import cn.bigmarket.domain.strategy.service.rule.filter.ILogicFilter;
import cn.bigmarket.domain.strategy.service.rule.filter.factory.DefaultLogicFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy {

    @Resource
    private DefaultLogicFactory logicFactory;

    public DefaultRaffleStrategy(IStrategyRepository repository, IStrategyDispatch strategyDispatch, DefaultChainFactory defaultChainFactory) {
        super(repository, strategyDispatch, defaultChainFactory);
    }


    // 抽奖前置条件判断
    @Override
    protected RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> doCheckRaffleBeforeLogic(RaffleFactorEntity raffleFactorEntity, String... logics) {
        // 如果传过来的对应策略id和奖品id的rule_model为空，那么直接放行即可
        if(logics == null || 0 == logics.length)
            return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                    .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                    .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                    .build();

        // 1. 获取规则引擎工厂提供的部分
        Map<String, ILogicFilter<RuleActionEntity.RaffleBeforeEntity>> logicFilterGroup = logicFactory.openLogicFilter();

        // 2. 黑名单规则优先过滤，logics传过来的是 " strategy 中的 rule_models"
        // （（userId， strategyId）， rule_models）
        //  RULE_BLACKLIST("rule_blacklist","【抽奖前规则】黑名单规则过滤，命中黑名单则直接返回"),
        String ruleBackList = Arrays.stream(logics)
                .filter(str -> str.contains(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode()))
                // 类比.filter(str -> str.contains("rule_blacklist"))
                .findFirst()
                .orElse(null);

        // 3. 如果配置了黑名单的规则(rule_blacklist 命中 “rule_blacklist”)，
        // 那就先过滤黑名单（其实意思就是 strategy 的 rule_models 当中有没有“rule_blacklist”字段）
        if (StringUtils.isNotBlank(ruleBackList)) {
            // 先拿黑名单的类型值，去获取它对应的 filter 对象
            // 这里是 "rule_blacklist" 黑名单的类型，所以它获取的 filter 对象是 RuleBackListLogicFilter
            ILogicFilter<RuleActionEntity.RaffleBeforeEntity> logicFilter = logicFilterGroup.get(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode());
            RuleMatterEntity ruleMatterEntity = new RuleMatterEntity();
            // 将传过来的 UserId, AwardId（似乎为空）, StrategyId 装进实体 RuleMatterEntity 中
            // ruleModel也是 "rule_blacklist" 黑名单
            ruleMatterEntity.setUserId(raffleFactorEntity.getUserId());
            ruleMatterEntity.setAwardId(raffleFactorEntity.getAwardId());
            ruleMatterEntity.setStrategyId(raffleFactorEntity.getStrategyId());
            ruleMatterEntity.setRuleModel(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode());

            RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> ruleActionEntity = logicFilter.filter(ruleMatterEntity);
            // 这里返回的就是 RuleBackListLogicFilter 的返回值了
            // 看看 RuleLogicCheckTypeVO 里面到底是 ALLOW 还是 TAKE_OVER
            // 如果不是黑名单 ALLOW 允许，那就直接返回黑名单结果就完事了，黑名单不允许，那接下来也不用过滤了
            if (!RuleLogicCheckTypeVO.ALLOW.getCode().equals(ruleActionEntity.getCode())) {
                return ruleActionEntity;
            }
        }

        // ----------------------------------------------------------------------------------------------------------------------
        // 2. 白名单规则优先过滤，logics传过来的是 " strategy 一行数据中的 rule_models"
        // TO_DO:白名单也可以加，就类似于 RuleWeightLogicFilter 去做一个新的白名单实现类，继承 ILogicFilter
        // （（userId， strategyId）， rule_models）
        //  RULE_BLACKLIST("rule_blacklist","【抽奖前规则】黑名单规则过滤，命中黑名单则直接返回"),
        String ruleWhiteList = Arrays.stream(logics)
                .filter(str -> str.contains(DefaultLogicFactory.LogicModel.RULE_WHITELIST.getCode()))
                // 类比.filter(str -> str.contains("rule_blacklist"))
                .findFirst()
                .orElse(null);

        // 3. 如果配置了黑名单 rule_blacklist，那就先过滤黑名单（其实意思就是 strategy 的 rule_models 当中有没有“rule_blacklist”字段）
        if (StringUtils.isNotBlank(ruleWhiteList)) {
            // 先拿黑名单的类型值，去获取它对应的 filter 对象
            // 这里是 "rule_blacklist" 黑名单的类型，所以它获取的 filter 对象是 RuleBackListLogicFilter
            ILogicFilter<RuleActionEntity.RaffleBeforeEntity> logicFilter = logicFilterGroup.get(DefaultLogicFactory.LogicModel.RULE_WHITELIST.getCode());
            RuleMatterEntity ruleMatterEntity = new RuleMatterEntity();
            // 将传过来的 UserId, AwardId（似乎为空）, StrategyId 装进实体 RuleMatterEntity 中
            // ruleModel也是 "rule_blacklist" 黑名单
            ruleMatterEntity.setUserId(raffleFactorEntity.getUserId());
            ruleMatterEntity.setAwardId(raffleFactorEntity.getAwardId());
            ruleMatterEntity.setStrategyId(raffleFactorEntity.getStrategyId());
            ruleMatterEntity.setRuleModel(DefaultLogicFactory.LogicModel.RULE_WHITELIST.getCode());

            RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> ruleActionEntity = logicFilter.filter(ruleMatterEntity);

            // 这里返回的就是 RuleBackListLogicFilter 的返回值了
            // 看看 RuleLogicCheckTypeVO 里面到底是 ALLOW 还是 TAKE_OVER
            // 如果不是黑名单 ALLOW 允许，那就直接返回黑名单结果就完事了，黑名单不允许，那接下来也不用过滤了
            if (!RuleLogicCheckTypeVO.ALLOW.getCode().equals(ruleActionEntity.getCode())) {
                return ruleActionEntity;
            }
        }
        // ----------------------------------------------------------------------------------------------------------------------


        // 4. 顺序过滤剩余规则（如果是黑名单允许的，那就继续过滤剩余规则）
        // 从 logics 字符串中排除 "rule_blacklist" 并将剩余的逻辑转换为 List<String>
        // logics例：rule_blacklist,rule_weight
        List<String> ruleList = Arrays.stream(logics)
                .filter(s -> !s.equals(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode()))
                .filter(s -> !s.equals(DefaultLogicFactory.LogicModel.RULE_WHITELIST.getCode()))
                // 类比 .filter(s -> !s.equals("rule_blacklist"))
                .collect(Collectors.toList());


        RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> ruleActionEntity = null;
        for (String ruleModel : ruleList) {
            // 刚刚这里是 "rule_blacklist" 黑名单的类型，所以它获取的 filter 对象是 RuleBackListLogicFilter
            // 现在已经把黑名单的类型排除出去了，就是剩下的 filter 对象了，例如 RuleWeightLogicFilter 权重规则过滤
            ILogicFilter<RuleActionEntity.RaffleBeforeEntity> logicFilter = logicFilterGroup.get(ruleModel);
            RuleMatterEntity ruleMatterEntity = new RuleMatterEntity();
            // 将传过来的 UserId, AwardId（似乎为空）, StrategyId 装进实体 RuleMatterEntity 中
            ruleMatterEntity.setUserId(raffleFactorEntity.getUserId());
            ruleMatterEntity.setAwardId(raffleFactorEntity.getAwardId());
            ruleMatterEntity.setStrategyId(raffleFactorEntity.getStrategyId());
            ruleMatterEntity.setRuleModel(ruleModel);

            ruleActionEntity = logicFilter.filter(ruleMatterEntity);
            // 非放行结果则顺序过滤
            log.info("抽奖前规则过滤 userId: {} ruleModel: {} code: {} info: {}", raffleFactorEntity.getUserId(), ruleModel, ruleActionEntity.getCode(), ruleActionEntity.getInfo());

            // 看看 RuleLogicCheckTypeVO 里面到底是 ALLOW 还是 TAKE_OVER
            // 如果是不允许，那就直接返回了，已经没必要往下走了，这边已经不满足什么抽奖条件了
            if (!RuleLogicCheckTypeVO.ALLOW.getCode().equals(ruleActionEntity.getCode()))
                return ruleActionEntity;
        }

        return ruleActionEntity;

    }

    // 抽奖中置条件判断
    @Override
    protected RuleActionEntity<RuleActionEntity.RaffleCenterEntity> doCheckRaffleCenterLogic(RaffleFactorEntity raffleFactorEntity, String... logics) {
        // 如果传过来的对应策略id和奖品id的rule_model为空，那么直接放行即可
        if(logics == null || 0 == logics.length)
            return RuleActionEntity.<RuleActionEntity.RaffleCenterEntity>builder()
                    .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                    .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                    .build();

        // 1. 获取规则引擎工厂提供的部分
        Map<String, ILogicFilter<RuleActionEntity.RaffleCenterEntity>> logicFilterGroup = logicFactory.openLogicFilter();

        RuleActionEntity<RuleActionEntity.RaffleCenterEntity> ruleActionEntity = null;
        // logics 传进去的就是string[]数组，将ruleModel按照 逗号 拆分开
        for (String ruleModel : logics) {
            // 抽奖中置规则判断，装配完之后放到filter中
            ILogicFilter<RuleActionEntity.RaffleCenterEntity> logicFilter = logicFilterGroup.get(ruleModel);
            RuleMatterEntity ruleMatterEntity = new RuleMatterEntity();
            ruleMatterEntity.setUserId(raffleFactorEntity.getUserId());
            ruleMatterEntity.setAwardId(raffleFactorEntity.getAwardId());
            ruleMatterEntity.setStrategyId(raffleFactorEntity.getStrategyId());
            ruleMatterEntity.setRuleModel(ruleModel);

            // 抽奖中的filter就是去判断，根据strategyId，awardId（没咋用到）,ruleModel，去strategy_rule表中
            // 找对应的rule_value字段，字段是指的抽奖 x 次可以解锁
            ruleActionEntity = logicFilter.filter(ruleMatterEntity);
            // 非放行结果则顺序过滤
            log.info("抽奖中 规则过滤 userId: {} ruleModel: {} code: {} info: {}", raffleFactorEntity.getUserId(), ruleModel, ruleActionEntity.getCode(), ruleActionEntity.getInfo());

            // 看看 RuleLogicCheckTypeVO 里面到底是 ALLOW 还是 TAKE_OVER
            // 如果是不允许，那就直接返回了，已经没必要往下走了，这边已经不满足什么抽奖条件了
            if (!RuleLogicCheckTypeVO.ALLOW.getCode().equals(ruleActionEntity.getCode()))
                return ruleActionEntity;
        }

        return ruleActionEntity;
    }
}
