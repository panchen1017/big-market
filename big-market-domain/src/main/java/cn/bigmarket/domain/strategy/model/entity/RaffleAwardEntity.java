package cn.bigmarket.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 抽奖奖品实体类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RaffleAwardEntity {
    /** 策略ID */
    private Long strategyId;
    /** 奖品ID */
    private Integer awardId;
    /**奖品对接标识-每一个都是一个对应的发奖策略 */
    private String awardKey;
    /** 奖品配置信息光人*/
    private String awardConfig;
    /**奖品内容描述 */
    private String awardDesc;
}
