package cn.bigmarket.domain.activity.service.quota.rule;

import cn.bigmarket.domain.activity.model.entity.ActivityCountEntity;
import cn.bigmarket.domain.activity.model.entity.ActivityEntity;
import cn.bigmarket.domain.activity.model.entity.ActivitySkuEntity;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 下单规则过滤接口
 * @create 2024-03-23 09:40
 */
public interface IActionChain extends IActionChainArmory {

    boolean action(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity);

}
