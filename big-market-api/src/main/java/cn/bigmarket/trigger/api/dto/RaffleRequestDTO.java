package cn.bigmarket.trigger.api.dto;

import lombok.Data;

/**
 * 抽奖请求接口
 */
@Data
public class RaffleRequestDTO {

    // 抽奖策略id
    private  Long strategyId;
}
