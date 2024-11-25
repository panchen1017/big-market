package cn.bigmarket.domain.activity.service.rule;

/**
 * 下单规则责任链抽象类
 *
 */
public abstract class AbstractActionChain implements IActionChain{
    private IActionChain next;

    @Override
    public IActionChain next() {
        return next;
    }

    @Override
    public IActionChain appendNext(IActionChain next) {
        this.next = next;
        return next;
    }
}
