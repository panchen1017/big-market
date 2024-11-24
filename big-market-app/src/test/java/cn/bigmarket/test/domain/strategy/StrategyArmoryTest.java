package cn.bigmarket.test.domain.strategy;

import cn.bigmarket.domain.strategy.service.armory.IStrategyArmory;
import cn.bigmarket.domain.strategy.service.armory.IStrategyDispatch;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * 抽奖装配测试
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class StrategyArmoryTest {

    @Resource
    private IStrategyArmory strategyArmory;
    @Resource
    private IStrategyDispatch strategyDispatch;

    @Before
    public void test_strategyArmory() {
        boolean res = strategyArmory.assembleLotteryStrategy(100001L);
        log.info("测试结果：{}", res);
    }

    @Test
    public void  test_getRandomAwardID() {
        log.info("抽奖装配测试结果：{}", strategyDispatch.getRandomAward(100001L));
//        log.info("抽奖装配测试结果：{}", strategyDispatch.getRandomAward(100002L));
    }
    @Test
    public void  test_getRandomAwardID_ruleWeight() {
        log.info("抽奖装配测试结果：{} ---4000策略", strategyDispatch.getRandomAward(100001L, "4000:102,103,104,105"));
        log.info("抽奖装配测试结果：{} ---5000策略", strategyDispatch.getRandomAward(100001L, "5000:102,103,104,105,106,107"));
        log.info("抽奖装配测试结果：{} ---6000策略", strategyDispatch.getRandomAward(100001L, "6000:102,103,104,105,106,107,108,109"));
//        log.info("抽奖装配测试结果：{}", strategyDispatch.getRandomAward(100002L));
    }

}
