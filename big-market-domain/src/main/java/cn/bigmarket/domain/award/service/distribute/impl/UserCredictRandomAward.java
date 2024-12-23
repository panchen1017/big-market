package cn.bigmarket.domain.award.service.distribute.impl;

import cn.bigmarket.domain.award.model.aggregate.GiveOutPrizesAggregate;
import cn.bigmarket.domain.award.model.entity.DistributeAwardEntity;
import cn.bigmarket.domain.award.model.entity.UserAwardRecordEntity;
import cn.bigmarket.domain.award.model.entity.UserCreditAwardEntity;
import cn.bigmarket.domain.award.model.valobj.AwardStateVO;
import cn.bigmarket.domain.award.repository.IAwardRepository;
import cn.bigmarket.domain.award.service.distribute.IDistributeAward;
import cn.bigmarket.types.common.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.MathContext;


/**
 * 这边直接用 map<String, Bean> key 的模式，从对应的 Component 直接获取对应的 Bean 对象
 */
@Component("user_")
public class UserCredictRandomAward implements IDistributeAward {

    @Resource
    private IAwardRepository repository;


    /**
     * 发奖
     * @param distributeAwardEntity
     */
    @Override
    public void giveOutPrizes(DistributeAwardEntity distributeAwardEntity) {
        // 获取奖品id / 奖品配置
        Integer awardId = distributeAwardEntity.getAwardId();
        String awardConfig = distributeAwardEntity.getAwardConfig();

        // 如果奖品配置为空，要去数据库主动获取给我
        // 类似 0.01,1  1,100 一个积分范围值
        if(StringUtils.isBlank(awardConfig)) {
            awardConfig = repository.queryAwardConfig(awardId);
        }

        // 先用 split 拆分，如果不是两份，那就有问题，抛异常
        String[] split = awardConfig.split(Constants.SPLIT);
        if(split.length != 2) {
            throw new RuntimeException("award_config 「" + awardConfig + "」配置不是一个范围值，如 1,100");
        }
        // 在这个范围值之内，生成一个随机值
        // 生成随机积分值
        BigDecimal creditAmount = generateRandom(new BigDecimal(split[0]), new BigDecimal(split[1]));

        // 构建聚合对象
        UserAwardRecordEntity userAwardRecordEntity = GiveOutPrizesAggregate.buildDistributeUserAwardRecordEntity(
                distributeAwardEntity.getUserId(),
                distributeAwardEntity.getOrderId(),
                distributeAwardEntity.getAwardId(),
                AwardStateVO.complete
        );

        UserCreditAwardEntity userCreditAwardEntity = GiveOutPrizesAggregate.buildUserCreditAwardEntity(distributeAwardEntity.getUserId(), creditAmount);

        GiveOutPrizesAggregate giveOutPrizesAggregate = new GiveOutPrizesAggregate();
        giveOutPrizesAggregate.setUserId(distributeAwardEntity.getUserId());
        giveOutPrizesAggregate.setUserAwardRecordEntity(userAwardRecordEntity);
        giveOutPrizesAggregate.setUserCreditAwardEntity(userCreditAwardEntity);

        // 存储发奖对象
        repository.saveGiveOutPrizesAggregate(giveOutPrizesAggregate);
    }

    private BigDecimal generateRandom(BigDecimal min, BigDecimal max) {
        // 如果最小值和最大值是相等的，直接返回一个 min 即可
        if (min.equals(max)) return min;
        BigDecimal randomBigDecimal = min.add(BigDecimal.valueOf(Math.random()).multiply(max.subtract(min)));
        return randomBigDecimal.round(new MathContext(3));
    }
}
