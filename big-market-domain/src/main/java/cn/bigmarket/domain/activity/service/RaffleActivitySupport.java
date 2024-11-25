package cn.bigmarket.domain.activity.service;

import cn.bigmarket.domain.activity.model.entity.ActivityCountEntity;
import cn.bigmarket.domain.activity.model.entity.ActivityEntity;
import cn.bigmarket.domain.activity.model.entity.ActivitySkuEntity;
import cn.bigmarket.domain.activity.repository.IActivityRepository;
import cn.bigmarket.domain.activity.service.rule.factory.DefaultActivityChainFactory;

/**
 * 抽奖活动的支撑类（都是一些查询信息的步骤）
 *
 */
public class RaffleActivitySupport {

    protected DefaultActivityChainFactory defaultActivityChainFactory;
    protected IActivityRepository activityRepository;

    public RaffleActivitySupport(DefaultActivityChainFactory defaultActivityChainFactory, IActivityRepository activityRepository) {
        this.defaultActivityChainFactory = defaultActivityChainFactory;
        this.activityRepository = activityRepository;
    }

    public ActivitySkuEntity queryActivitySku(Long sku) {
        // 1. 通过sku查询活动信息（通过用户id和sku获取raffle_activity_sku表中的数据）
        return activityRepository.queryActivitySku(sku);
    }
    public ActivityEntity queryRaffleActivityByActivityId(Long activityId) {
        // 2. 查询活动信息（通过刚刚的raffle_activity_sku表中的 activityId 数据 获取 raffle_activity 表中数据）
        return activityRepository.queryRaffleActivityByActivityId(activityId);
    }

    public ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId) {
        // 3. 查询次数信息（用户在活动上可参与的次数，通过刚刚的raffle_activity_sku表中的 activityCountId 数据 获取 RaffleActivityCount 表中数据）
        return activityRepository.queryRaffleActivityCountByActivityCountId(activityCountId);
    }

}
