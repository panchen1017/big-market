package cn.bigmarket.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 抽奖因子实体类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RaffleFactorEntity {
    /**
     * 抽奖的时候只给 奖品id 和 用户id 即可
     */
    private String userId;
    private Long strategyId;

}
