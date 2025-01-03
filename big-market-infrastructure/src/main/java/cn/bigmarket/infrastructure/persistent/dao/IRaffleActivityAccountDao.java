package cn.bigmarket.infrastructure.persistent.dao;

import cn.bigmarket.infrastructure.persistent.po.RaffleActivityAccount;
import cn.bugstack.middleware.db.router.annotation.DBRouter;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖活动账户表
 * @create 2024-03-09 10:05
 */
@Mapper
public interface IRaffleActivityAccountDao {
    int updateAccountQuota(RaffleActivityAccount raffleActivityAccount);

    void insert(RaffleActivityAccount raffleActivityAccount);
    int updateActivityAccountSubtractionQuota(RaffleActivityAccount raffleActivityAccount);
    void updateActivityAccountMonthSurplusImageQuota(RaffleActivityAccount raffleActivityAccount);

    void updateActivityAccountDaySurplusImageQuota(RaffleActivityAccount raffleActivityAccount);

    @DBRouter
    RaffleActivityAccount queryActivityAccountByUserId(RaffleActivityAccount raffleActivityAccountReq);

    RaffleActivityAccount queryAccountByUserId(RaffleActivityAccount raffleActivityAccount);
}
