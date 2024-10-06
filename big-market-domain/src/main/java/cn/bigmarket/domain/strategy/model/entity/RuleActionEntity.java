package cn.bigmarket.domain.strategy.model.entity;

import cn.bigmarket.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import lombok.*;

/**
 *  规则动作实体，这个是过滤完规则之后返还的，存在： 抽奖前， 抽奖中， 抽奖后
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleActionEntity<T extends RuleActionEntity.RaffleEntity> {
    // 加了<T extends RuleActionEntity.RaffleEntity>
    // 就表明这个实体类传过来的数据必须是继承 RaffleEntity 类的
    private String code = RuleLogicCheckTypeVO.ALLOW.getCode();
    private String info = RuleLogicCheckTypeVO.ALLOW.getInfo();
    // 过滤的是哪个规则
    private String ruleModel;
    // 返回数据
    private T data;

    static public class RaffleEntity {

    }
//      首先 @EqualsAndHashCode 标在子类上
//      1. callSuper = true，根据子类自身的字段值和从父类继承的字段值 来生成hashcode，当两个子类对象比较时，只有子类对象的本身的字段值和继承父类的字段值都相同，equals方法的返回值是true。
//      2. callSuper = false，根据子类自身的字段值 来生成hashcode， 当两个子类对象比较时，只有子类对象的本身的字段值相同，父类字段值可以不同，equals方法的返回值是true。

    @EqualsAndHashCode(callSuper = true)
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    static public class RaffleBeforeEntity extends RaffleEntity{
        /**
         * 抽奖前置规则
         * 权重策略：策略id + ruleWeightValueKey ->去 getRandomAward 抽奖
         * 黑名单：awardId -> 直接返回奖品
         */

        /**
         * 策略id
         */
        private Long strategyId;
        /**
         * 权重值KEY：用于抽奖时可以选择权重抽奖
         */
        private String ruleWeightValueKey;

        /**
         * 如果是黑名单，直接返回奖品，不用抽奖了
         */
        private Integer awardId;
    }
    static public class RaffleAfterEntity extends RaffleEntity{

    }
    static public class RaffleCenterEntity extends RaffleEntity{

    }
}
