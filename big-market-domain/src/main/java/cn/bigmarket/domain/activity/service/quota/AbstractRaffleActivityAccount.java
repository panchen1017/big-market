package cn.bigmarket.domain.activity.service.quota;

import cn.bigmarket.domain.activity.model.aggregate.CreateQuotaOrderAggregate;
import cn.bigmarket.domain.activity.model.entity.*;
import cn.bigmarket.domain.activity.repository.IActivityRepository;
import cn.bigmarket.domain.activity.service.IRaffleActivityAccountQuotaService;
import cn.bigmarket.domain.activity.service.quota.rule.IActionChain;
import cn.bigmarket.domain.activity.service.quota.rule.factory.DefaultActivityChainFactory;
import cn.bigmarket.types.enums.ResponseCode;
import cn.bigmarket.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖活动抽象类，定义标准的流程
 * @create 2024-03-16 08:42
 */
@Slf4j
public abstract class AbstractRaffleActivityAccount extends RaffleActivityAccountQuotaSupport implements IRaffleActivityAccountQuotaService {


    public AbstractRaffleActivityAccount(DefaultActivityChainFactory defaultActivityChainFactory, IActivityRepository activityRepository) {
        super(defaultActivityChainFactory, activityRepository);
    }

//    @Override
//    public ActivityOrderEntity createRaffleActivityOrder(ActivityShopCartEntity activityShopCartEntity) {
//        // activityShopCartEntity 包括 userId 和 sku
//        // 1. 通过sku查询活动信息（通过用户id和sku获取raffle_activity_sku表中的数据）
//        ActivitySkuEntity activitySkuEntity = activityRepository.queryActivitySku(activityShopCartEntity.getSku());
//        // 2. 查询活动信息（通过刚刚的raffle_activity_sku表中的 activityId 数据 获取 raffle_activity 表中数据）
//        ActivityEntity activityEntity = activityRepository.queryRaffleActivityByActivityId(activitySkuEntity.getActivityId());
//        // 3. 查询次数信息（用户在活动上可参与的次数，通过刚刚的raffle_activity_sku表中的 activityCountId 数据 获取 RaffleActivityCount 表中数据）
//        ActivityCountEntity activityCountEntity = activityRepository.queryRaffleActivityCountByActivityCountId(activitySkuEntity.getActivityCountId());
//
//        log.info("查询结果：{} {} {}", JSON.toJSONString(activitySkuEntity), JSON.toJSONString(activityEntity), JSON.toJSONString(activityCountEntity));
//
//        return ActivityOrderEntity.builder().build();
//    }

    @Override
    public String createOrder(SkuRechargeEntity skuRechargeEntity) {
        // 1. 参数校验 （传来的 skuRechargeEntity 中的信息是否完整 userId，sku，outBusinessNo）
        String userId = skuRechargeEntity.getUserId();
        Long sku = skuRechargeEntity.getSku();
        String outBusinessNo = skuRechargeEntity.getOutBusinessNo();
        if(null == sku || StringUtils.isBlank(userId) || StringUtils.isBlank(outBusinessNo)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        // 2. 查询基础信息 （这边继承了RaffleActivitySupport，具体的repository代码在支持类中编写，这个抽象类就不写repository的东西了）
        // 通过sku查询活动信息（通过用户id和sku获取raffle_activity_sku表中的数据）
        ActivitySkuEntity activitySkuEntity = queryActivitySku(skuRechargeEntity.getSku());
        // 查询活动信息（通过刚刚的raffle_activity_sku表中的 activityId 数据 获取 raffle_activity 表中数据）
        ActivityEntity activityEntity = queryRaffleActivityByActivityId(activitySkuEntity.getActivityId());
        // 查询次数信息（用户在活动上可参与的次数，通过刚刚的raffle_activity_sku表中的 activityCountId 数据 获取 RaffleActivityCount 表中数据）
        ActivityCountEntity activityCountEntity = queryRaffleActivityCountByActivityCountId(activitySkuEntity.getActivityCountId());

        // 3. 活动动作规则校验（创建责任链）
        IActionChain actionChain = defaultActivityChainFactory.openActionChain();
        // 这里不用返回值了，如果是异常，就直接抛出去了
        actionChain.action(activitySkuEntity, activityEntity, activityCountEntity);

        // 4. 构建订单聚合对象（模版模式，子类具体实现）
        CreateQuotaOrderAggregate createOrderAggregate = buildOrderAggreagate(skuRechargeEntity, activitySkuEntity, activityEntity, activityCountEntity);
        // 5. 保存订单
        doSaveOrder(createOrderAggregate);
        // 6. 返回单号
        return createOrderAggregate.getActivityOrderEntity().getOrderId();
    }

    protected abstract void doSaveOrder(CreateQuotaOrderAggregate createOrderAggregate);

    protected abstract CreateQuotaOrderAggregate buildOrderAggreagate(SkuRechargeEntity skuRechargeEntity, ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity);


}
