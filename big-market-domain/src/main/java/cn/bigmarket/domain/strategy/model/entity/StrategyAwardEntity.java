package cn.bigmarket.domain.strategy.model.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description 策略奖品实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyAwardEntity {
    /* 抽奖策略ID */
    private Long strategyId;
    /* 抽奖奖品ID */
    private Integer awardId;
    /* 奖品库存总量 */
    private Integer awardCount;
    /* 奖品库存剩余 */
    private Integer awardCountSurplus;
    /* 奖品中奖概率 */
    private BigDecimal awardRate;
    private String awardTitle;
    private String awardSubtitle;
    private Integer sort;


}
