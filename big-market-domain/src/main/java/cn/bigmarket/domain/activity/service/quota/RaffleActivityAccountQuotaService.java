package cn.bigmarket.domain.activity.service.quota;

import cn.bigmarket.domain.activity.model.aggregate.CreateQuotaOrderAggregate;
import cn.bigmarket.domain.activity.model.entity.*;
import cn.bigmarket.domain.activity.model.valobj.ActivitySkuStockKeyVO;
import cn.bigmarket.domain.activity.repository.IActivityRepository;
import cn.bigmarket.domain.activity.service.IRaffleActivitySkuStockService;
import cn.bigmarket.domain.activity.service.quota.policy.ITradePolicy;
import cn.bigmarket.domain.activity.service.quota.rule.factory.DefaultActivityChainFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖活动服务
 * @create 2024-03-16 08:41
 */
@Service
public class RaffleActivityAccountQuotaService extends AbstractRaffleActivityAccountQuota implements IRaffleActivitySkuStockService {

    public RaffleActivityAccountQuotaService(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory, Map<String, ITradePolicy> tradePolicyGroup) {
        super(activityRepository, defaultActivityChainFactory, tradePolicyGroup);
    }

    @Override
    protected CreateQuotaOrderAggregate buildOrderAggregate(SkuRechargeEntity skuRechargeEntity, ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity) {
        // 订单实体对象
        ActivityOrderEntity activityOrderEntity = new ActivityOrderEntity();
        activityOrderEntity.setUserId(skuRechargeEntity.getUserId());
        activityOrderEntity.setSku(skuRechargeEntity.getSku());
        activityOrderEntity.setActivityId(activityEntity.getActivityId());
        activityOrderEntity.setActivityName(activityEntity.getActivityName());
        activityOrderEntity.setStrategyId(activityEntity.getStrategyId());
        // 公司里一般会有专门的雪花算法UUID服务，我们这里直接生成个12位就可以了。
        activityOrderEntity.setOrderId(RandomStringUtils.randomNumeric(12));
        activityOrderEntity.setOrderTime(new Date());
        activityOrderEntity.setTotalCount(activityCountEntity.getTotalCount());
        activityOrderEntity.setDayCount(activityCountEntity.getDayCount());
        activityOrderEntity.setMonthCount(activityCountEntity.getMonthCount());
        activityOrderEntity.setPayAmount(activitySkuEntity.getProductAmount());
        activityOrderEntity.setPayAmount(activitySkuEntity.getProductAmount());
        activityOrderEntity.setOutBusinessNo(skuRechargeEntity.getOutBusinessNo());

        // 构建聚合对象
        return CreateQuotaOrderAggregate.builder()
                .userId(skuRechargeEntity.getUserId())
                .activityId(activitySkuEntity.getActivityId())
                .totalCount(activityCountEntity.getTotalCount())
                .dayCount(activityCountEntity.getDayCount())
                .monthCount(activityCountEntity.getMonthCount())
                .activityOrderEntity(activityOrderEntity)
                .build();
    }

    @Override
    public ActivitySkuStockKeyVO takeQueueValue() throws InterruptedException {
        return activityRepository.takeQueueValue();
    }

    @Override
    public void clearQueueValue() {
        activityRepository.clearQueueValue();
    }

    @Override
    public void updateActivitySkuStock(Long sku) {
        activityRepository.updateActivitySkuStock(sku);
    }

    @Override
    public void clearActivitySkuStock(Long sku) {
        activityRepository.clearActivitySkuStock(sku);
    }

    @Override
    public void updateOrder(DeliveryOrderEntity deliveryOrderEntity) {
        activityRepository.updateOrder(deliveryOrderEntity);
    }

    @Override
    public Integer queryRaffleActivityAccountPartakeCount(Long activityId, String userId) {
        return activityRepository.queryRaffleActivityAccountPartakeCount(activityId, userId);
    }

    @Override
    public Integer queryRaffleActivityAccountDayPartakeCount(Long activityId, String userId) {
        return activityRepository.queryRaffleActivityAccountDayPartakeCount(activityId, userId);
    }

    @Override
    public ActivityAccountEntity queryActivityAccountEntity(Long activityId, String userId) {
        return activityRepository.queryActivityAccountEntity(activityId, userId);
    }

}
//public class RaffleActivityAccountQuotaService extends AbstractRaffleActivityAccountQuota implements IRaffleActivitySkuStockService {
//
//
//    public RaffleActivityAccountQuotaService(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory, Map<String, ITradePolicy> tradePolicyGroup) {
//        super(activityRepository, defaultActivityChainFactory, tradePolicyGroup);
//    }
//
//    @Override
//    protected CreateQuotaOrderAggregate buildOrderAggreagate(SkuRechargeEntity skuRechargeEntity, ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity) {
//        // 订单实体对象
//        // CreateOrderAggregate聚合实体中包含：userId, activityId, totalCount, dayCount, monthCount, activityOrderEntity
//        ActivityOrderEntity activityOrderEntity = new ActivityOrderEntity();
//        activityOrderEntity.setUserId(skuRechargeEntity.getUserId());
//        activityOrderEntity.setSku(skuRechargeEntity.getSku());
//        activityOrderEntity.setActivityId(activityEntity.getActivityId());
//        activityOrderEntity.setActivityName(activityEntity.getActivityName());
//        activityOrderEntity.setStrategyId(activityEntity.getStrategyId());
//        // 公司里一般会有专门的雪花算法UUID服务，我们这里直接生成个12位就可以了。
//        activityOrderEntity.setOrderId(RandomStringUtils.randomNumeric(12));
//        activityOrderEntity.setOrderTime(new Date());
//        activityOrderEntity.setTotalCount(activityCountEntity.getTotalCount());
//        activityOrderEntity.setDayCount(activityCountEntity.getDayCount());
//        activityOrderEntity.setMonthCount(activityCountEntity.getMonthCount());
//        activityOrderEntity.setOutBusinessNo(skuRechargeEntity.getOutBusinessNo());
//
//        // 构建聚合对象
//        return CreateQuotaOrderAggregate.builder()
//                .userId(skuRechargeEntity.getUserId())              // userId
//                .activityId(activitySkuEntity.getActivityId())      // activityId
//                .totalCount(activityCountEntity.getTotalCount())    // totalCount
//                .dayCount(activityCountEntity.getDayCount())        // dayCount
//                .monthCount(activityCountEntity.getMonthCount())    // monthCount
//                .activityOrderEntity(activityOrderEntity)           // activityOrderEntity
//                .build();
//    }
//
//    @Override
//    public ActivitySkuStockKeyVO takeQueueValue() throws InterruptedException {
//        return activityRepository.takeQueueValue();
//    }
//
//    @Override
//    public void clearQueueValue() {
//        activityRepository.clearQueueValue();
//    }
//
//    @Override
//    public void updateActivitySkuStock(Long sku) {
//        activityRepository.updateActivitySkuStock(sku);
//    }
//
//    @Override
//    public void clearActivitySkuStock(Long sku) {
//        activityRepository.clearActivitySkuStock(sku);
//    }
//
//
//    public void updateOrder(DeliveryOrderEntity deliveryOrderEntity) {
//        activityRepository.updateOrder(deliveryOrderEntity);
//
//    }
//
//    @Override
//    public Integer queryRaffleActivityAccountDayPartakeCount(Long activityId, String userId) {
//        return activityRepository.queryRaffleActivityAccountDayPartakeCount(activityId, userId);
//    }
//
//    @Override
//    public ActivityAccountEntity queryActivityAccountEntity(Long activityId, String userId) {
//        return activityRepository.queryActivityAccountEntity(activityId, userId);
//    }
//
//    @Override
//    public Integer queryRaffleActivityAccountPartakeCount(Long activityId, String userId) {
//        return activityRepository.queryRaffleActivityAccountPartakeCount(activityId, userId);
//    }
//}
