package cn.bigmarket.domain.strategy.service.rule.chain;

/**
 * 责任链装配接口
 *
 */
public interface ILogicChainArmory {
    ILogicChain appendNext(ILogicChain next);

    ILogicChain next();
}
