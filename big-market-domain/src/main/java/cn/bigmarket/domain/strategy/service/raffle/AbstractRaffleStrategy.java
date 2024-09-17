package cn.bigmarket.domain.strategy.service.raffle;

import cn.bigmarket.domain.strategy.model.entity.RaffleAwardEntity;
import cn.bigmarket.domain.strategy.model.entity.RaffleFactorEntity;
import cn.bigmarket.domain.strategy.model.entity.RuleActionEntity;
import cn.bigmarket.domain.strategy.model.entity.StrategyEntity;
import cn.bigmarket.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import cn.bigmarket.domain.strategy.repository.IStrategyRepository;
import cn.bigmarket.domain.strategy.service.IRaffleStrategy;
import cn.bigmarket.domain.strategy.service.armory.IStrategyDispatch;
import cn.bigmarket.domain.strategy.service.rule.factory.DefaultLogicFactory;
import cn.bigmarket.types.enums.ResponseCode;
import cn.bigmarket.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 *  抽象策略实现类
 */
@Slf4j
public abstract class AbstractRaffleStrategy implements IRaffleStrategy {

    // 策略仓储服务 -> domain层像一个大厨，仓储层提供米面粮油
    protected IStrategyRepository repository;
    // 策略调度服务 -> 只负责抽奖处理，通过新增接口的方式，隔离职责，不需要使用方关心或者调用抽奖的初始化
    protected IStrategyDispatch strategyDispatch;

    // 通过构造函数注入
    public AbstractRaffleStrategy(IStrategyRepository repository, IStrategyDispatch strategyDispatch) {
        this.repository = repository;
        this.strategyDispatch = strategyDispatch;
    }

    @Override
    public RaffleAwardEntity performRaffle(RaffleFactorEntity raffleFactorEntity) {

        // 1. 参数校验
        // 校验 userId 和 strategyId 是不是为空，为空就返回异常
        String userId = raffleFactorEntity.getUserId();
        Long strategyId = raffleFactorEntity.getStrategyId();
        if (null == strategyId || StringUtils.isBlank(userId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }

        // 2. 策略查询
        // 查询策略表的一行数据，通过策略表数据去看是否要去过滤规则
        StrategyEntity strategy = repository.queryStrategyEntityByStrategyId(strategyId);

        // 3. 抽奖前 - 规则过滤
        // 将刚刚找到的这行策略表的数据 放入 raffleFactor，主要是 userId,strategyId（“xiaofuge”， 100001L）
        RaffleFactorEntity raffleFactor = RaffleFactorEntity.builder()
                                                            .userId(userId)
                                                            .strategyId(strategyId)
                                                            .build();
        // 放到这个子类的抽象方法中去（raffleFactor， strategy一行数据中的 ruleModels
        RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> ruleActionEntity = this.doCheckRaffleBeforeLogic(raffleFactor, strategy.ruleModels());

        // 4. 判断返回的 ruleActionEntity
        // ruleActionEntity 返回的就是规则已经过滤完的对象，里面有存 哪个规则，哪些数据
        // 如果返回的 ruleActionEntity 显示是被“接管”了，就是被 TAKE_OVER 了，那就进入循环
        if (RuleLogicCheckTypeVO.TAKE_OVER.getCode().equals(ruleActionEntity.getCode())) {
            // 被黑名单接管了，那就直接返回了，都不用抽奖了，因为黑名单有一个特地做好的路径，返回固定的积分即可，他不配抽奖
            if (DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode().equals(ruleActionEntity.getRuleModel())) {
                // 黑名单返回固定的奖品ID
                return RaffleAwardEntity.builder()
                        .awardId(ruleActionEntity.getData().getAwardId())
                        .build();
            } else if(DefaultLogicFactory.LogicModel.RULE_WHITELIST.getCode().equals(ruleActionEntity.getRuleModel()))  {
                // 白名单返回固定的奖品ID
                return RaffleAwardEntity.builder()
                        .awardId(ruleActionEntity.getData().getAwardId())
                        .build();
            } else if (DefaultLogicFactory.LogicModel.RULE_WIGHT.getCode().equals(ruleActionEntity.getRuleModel())) {
                // 但是如果是不是黑名单接管的，那就是 权重的规则，那还是要执行抽奖的，只是抽奖的奖品池有不同（奖品更好了）
                // 权重根据返回的信息进行抽奖
                RuleActionEntity.RaffleBeforeEntity raffleBeforeEntity = ruleActionEntity.getData();
                String ruleWeightValueKey = raffleBeforeEntity.getRuleWeightValueKey();
                // strategyDispatch.getRandomAward 就是去抽奖， 获取抽奖策略装配的随机结果
                // 收回伏笔（）
                Integer awardId = strategyDispatch.getRandomAward(strategyId, ruleWeightValueKey);
                // 返回的 奖品id 就是经过上节课做的抽奖得到的
                return RaffleAwardEntity.builder()
                        .awardId(awardId)
                        .build();
            }
        }
        // 5. 默认抽奖流程（如果返回的 ruleActionEntity  没有 被 “接管” ）
        //  那就直接放行，这个就类似于上节课，在 第5节：抽奖前置规则过滤 之前，直接仅仅是 getRandomAward 执行抽奖罢了。
        Integer awardId = strategyDispatch.getRandomAward(strategyId);

        return RaffleAwardEntity.builder()
                .awardId(awardId)
                .build();

    }
    protected abstract RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> doCheckRaffleBeforeLogic(RaffleFactorEntity raffleFactorEntity, String... logics);

}
