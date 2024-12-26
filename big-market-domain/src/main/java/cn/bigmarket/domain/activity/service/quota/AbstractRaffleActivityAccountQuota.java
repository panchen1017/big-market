package cn.bigmarket.domain.activity.service.quota;

import cn.bigmarket.domain.activity.model.aggregate.CreateQuotaOrderAggregate;
import cn.bigmarket.domain.activity.model.entity.*;
import cn.bigmarket.domain.activity.repository.IActivityRepository;
import cn.bigmarket.domain.activity.service.IRaffleActivityAccountQuotaService;
import cn.bigmarket.domain.activity.service.quota.policy.ITradePolicy;
import cn.bigmarket.domain.activity.service.quota.rule.IActionChain;
import cn.bigmarket.domain.activity.service.quota.rule.factory.DefaultActivityChainFactory;
import cn.bigmarket.types.enums.ResponseCode;
import cn.bigmarket.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖活动抽象类，定义标准的流程
 * @create 2024-03-16 08:42
 */
@Slf4j
public abstract class AbstractRaffleActivityAccountQuota extends RaffleActivityAccountQuotaSupport implements IRaffleActivityAccountQuotaService {

    // 不同类型的交易策略实现类，通过构造函数注入到 Map 中，教程；https://bugstack.cn/md/road-map/spring-dependency-injection.html
    private final Map<String, ITradePolicy> tradePolicyGroup;

    public AbstractRaffleActivityAccountQuota(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory, Map<String, ITradePolicy> tradePolicyGroup) {
        super((DefaultActivityChainFactory) activityRepository, (IActivityRepository) defaultActivityChainFactory);
        this.tradePolicyGroup = tradePolicyGroup;
    }


    @Override
    public String createOrder(SkuRechargeEntity skuRechargeEntity) {
        // 1. 参数校验
        String userId = skuRechargeEntity.getUserId();
        Long sku = skuRechargeEntity.getSku();
        String outBusinessNo = skuRechargeEntity.getOutBusinessNo();
        if (null == sku || StringUtils.isBlank(userId) || StringUtils.isBlank(outBusinessNo)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }

        // 2. 查询基础信息
        // 2.1 通过sku查询活动信息
        ActivitySkuEntity activitySkuEntity = queryActivitySku(sku);
        // 2.2 查询活动信息
        ActivityEntity activityEntity = queryRaffleActivityByActivityId(activitySkuEntity.getActivityId());
        // 2.3 查询次数信息（用户在活动上可参与的次数）
        ActivityCountEntity activityCountEntity = queryRaffleActivityCountByActivityCountId(activitySkuEntity.getActivityCountId());

        // 3. 活动动作规则校验 「过滤失败则直接抛异常」- 责任链扣减sku库存
        IActionChain actionChain = defaultActivityChainFactory.openActionChain();
        actionChain.action(activitySkuEntity, activityEntity, activityCountEntity);

        // 4. 构建订单聚合对象
        CreateQuotaOrderAggregate createOrderAggregate = buildOrderAggregate(skuRechargeEntity, activitySkuEntity, activityEntity, activityCountEntity);

        // 5. 交易策略 - 【积分兑换，支付类订单】【返利无支付交易订单，直接充值到账】【订单状态变更交易类型策略】
        ITradePolicy tradePolicy = tradePolicyGroup.get(skuRechargeEntity.getOrderTradeType().getCode());
        tradePolicy.trade(createOrderAggregate);

        // 6. 返回单号
        return createOrderAggregate.getActivityOrderEntity().getOrderId();
    }

    protected abstract CreateQuotaOrderAggregate buildOrderAggregate(SkuRechargeEntity skuRechargeEntity, ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity);

}
//public abstract class AbstractRaffleActivityAccountQuota extends RaffleActivityAccountQuotaSupport implements IRaffleActivityAccountQuotaService {
//
//
//    private final Map<String, ITradePolicy> tradePolicyGroup;
//
//    public AbstractRaffleActivityAccountQuota(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory, Map<String, ITradePolicy> tradePolicyGroup) {
//        super(activityRepository, defaultActivityChainFactory);
//        this.tradePolicyGroup = tradePolicyGroup;
//    }
//
//
//    @Override
//    public String createOrder(SkuRechargeEntity skuRechargeEntity) {
//        /**
//         * 做 sku 扣减
//         */
//        // 1. 参数校验 （传来的 skuRechargeEntity 中的信息是否完整 userId，sku，outBusinessNo）
//        String userId = skuRechargeEntity.getUserId();
//        Long sku = skuRechargeEntity.getSku();
//        String outBusinessNo = skuRechargeEntity.getOutBusinessNo();
//        if(null == sku || StringUtils.isBlank(userId) || StringUtils.isBlank(outBusinessNo)) {
//            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
//        }
//        // 2. 查询基础信息 （这边继承了RaffleActivitySupport，具体的repository代码在支持类中编写，这个抽象类就不写repository的东西了）
//        // 通过sku查询活动信息（通过用户id和sku获取raffle_activity_sku表中的数据）
//        ActivitySkuEntity activitySkuEntity = queryActivitySku(skuRechargeEntity.getSku());
//        // 查询活动信息（通过刚刚的raffle_activity_sku表中的 activityId 数据 获取 raffle_activity 表中数据）
//        ActivityEntity activityEntity = queryRaffleActivityByActivityId(activitySkuEntity.getActivityId());
//        // 查询次数信息（用户在活动上可参与的次数，通过刚刚的raffle_activity_sku表中的 activityCountId 数据 获取 RaffleActivityCount 表中数据）
//        ActivityCountEntity activityCountEntity = queryRaffleActivityCountByActivityCountId(activitySkuEntity.getActivityCountId());
//
//        // 3. 活动动作规则校验（创建责任链）
//        IActionChain actionChain = defaultActivityChainFactory.openActionChain();
//        // 这里不用返回值了，如果是异常，就直接抛出去了
//        actionChain.action(activitySkuEntity, activityEntity, activityCountEntity);
//
//        // 4. 构建订单聚合对象（模版模式，子类具体实现）
//        CreateQuotaOrderAggregate createOrderAggregate = buildOrderAggreagate(skuRechargeEntity, activitySkuEntity, activityEntity, activityCountEntity);
//
//        // 5. 交易策略 - 【积分兑换，支付类订单】【返利无支付交易订单，直接充值到账】【订单状态变更交易类型策略】
//        ITradePolicy tradePolicy = tradePolicyGroup.get(skuRechargeEntity.getOrderTradeType().getCode());
//        tradePolicy.trade(createOrderAggregate);
//
//        // 6. 返回单号
//        return createOrderAggregate.getActivityOrderEntity().getOrderId();
//    }
//
//
//    protected abstract CreateQuotaOrderAggregate buildOrderAggreagate(SkuRechargeEntity skuRechargeEntity, ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity);
//
//
//}
