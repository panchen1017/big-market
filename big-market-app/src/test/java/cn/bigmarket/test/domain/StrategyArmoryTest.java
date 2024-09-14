package cn.bigmarket.test.domain;

import cn.bigmarket.domain.strategy.service.armory.IStrategyArmory;
import lombok.extern.slf4j.Slf4j;
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

    @Test
    public void test_strategyArmory() {
        strategyArmory.assembleLotteryStrategy(100002L);
    }

    @Test
    public void  test_getAssembleRandomVal() {
        log.info("抽奖装配测试结果：{}", strategyArmory.getRandomAward(100002L));
        log.info("抽奖装配测试结果：{}", strategyArmory.getRandomAward(100002L));
        log.info("抽奖装配测试结果：{}", strategyArmory.getRandomAward(100002L));
        log.info("抽奖装配测试结果：{}", strategyArmory.getRandomAward(100002L));
        log.info("抽奖装配测试结果：{}", strategyArmory.getRandomAward(100002L));
        log.info("抽奖装配测试结果：{}", strategyArmory.getRandomAward(100002L));
        log.info("抽奖装配测试结果：{}", strategyArmory.getRandomAward(100002L));
        log.info("抽奖装配测试结果：{}", strategyArmory.getRandomAward(100002L));
    }

}
