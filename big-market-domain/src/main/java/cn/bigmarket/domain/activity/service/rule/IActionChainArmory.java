package cn.bigmarket.domain.activity.service.rule;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 下单
 * @create 2024-03-23 10:15
 */
public interface IActionChainArmory {

    IActionChain next();

    IActionChain appendNext(IActionChain next);

}
