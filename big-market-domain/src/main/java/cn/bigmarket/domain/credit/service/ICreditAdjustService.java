package cn.bigmarket.domain.credit.service;

import cn.bigmarket.domain.credit.model.entity.TradeEntity;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 积分调额接口【正逆向，增减积分】
 * @create 2024-06-01 09:35
 */
public interface ICreditAdjustService {

    /**
     * 创建增加积分额度订单
     * 积分模块
     * @param tradeEntity 交易实体对象
     * @return 单号
     */
    String createOrder(TradeEntity tradeEntity);

}
