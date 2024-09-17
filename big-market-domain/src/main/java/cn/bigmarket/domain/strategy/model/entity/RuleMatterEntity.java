package cn.bigmarket.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  规则 "物料" 实体对象，用于过滤规则的必要参数信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleMatterEntity {

    private String userId;

    private Long strategyId;

    private Integer awardId;
    /**抽奖规则类型【rule_random随机值计算、rule_lock-抽奖几次后解锁、rule_luck_award -幸运奖(兜底奖品)】*/
    private String ruleModel;
}
