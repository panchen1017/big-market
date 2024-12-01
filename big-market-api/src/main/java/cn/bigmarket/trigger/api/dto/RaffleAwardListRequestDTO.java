package cn.bigmarket.trigger.api.dto;

import lombok.Data;

/**
 * 抽奖奖品列表，请求对象
 */
@Data
public class RaffleAwardListRequestDTO {


    // 用户ID
    private String userId;
    // 抽奖活动ID
    private Long activityId;

}
