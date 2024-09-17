package cn.bigmarket.test.infrastructure;

import cn.bigmarket.infrastructure.persistent.dao.IAwardDao;
import cn.bigmarket.infrastructure.persistent.dao.IStrategyAwardDao;
import cn.bigmarket.infrastructure.persistent.dao.IStrategyDao;
import cn.bigmarket.infrastructure.persistent.dao.IStrategyRuleDao;
import cn.bigmarket.infrastructure.persistent.po.Award;
import cn.bigmarket.infrastructure.persistent.po.Strategy;
import cn.bigmarket.infrastructure.persistent.po.StrategyAward;
import cn.bigmarket.infrastructure.persistent.po.StrategyRule;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * 奖品Dao测试
 */

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class AwardDaoTest {

    @Resource
    private IAwardDao awardDao;
    @Resource
    private IStrategyRuleDao strategyRuleDao;
    @Resource
    private IStrategyDao strategyDao;
    @Resource
    private IStrategyAwardDao strategyAwardDao;

    @Test
    public void test_queryAwardList() {

//        List<Award> awards = awardDao.queryAwardList();
//        log.info("测试结果：{}", JSON.toJSONString(awards));

//        List<StrategyRule> rules = strategyRuleDao.queryStrategyRuleList();
//        log.info("测试结果：{}", JSON.toJSONString(rules));

//        List<Strategy> strategies = strategyDao.queryStrategyList();
//        log.info("测试结果：{}", JSON.toJSONString(strategies));

        List<StrategyAward> strategyAwards = strategyAwardDao.queryStrategyAwardList();
//        log.info("测试结果：{}", JSON.toJSONString(strategyAwards));

    }
}
