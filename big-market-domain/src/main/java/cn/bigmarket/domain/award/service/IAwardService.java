package cn.bigmarket.domain.award.service;

import cn.bigmarket.domain.award.model.entity.DistributeAwardEntity;
import cn.bigmarket.domain.award.model.entity.UserAwardRecordEntity;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 奖品服务接口
 * @create 2024-04-06 09:03
 */
public interface IAwardService {

    void saveUserAwardRecord(UserAwardRecordEntity userAwardRecordEntity);
    /**
     * 配送发货奖品
     * 调用 giveOutPrizes
     */
    void distributeAward(DistributeAwardEntity distributeAwardEntity);

}
