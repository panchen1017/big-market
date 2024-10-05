package cn.bigmarket.domain.strategy.model.valobj;

import cn.bigmarket.domain.strategy.service.rule.filter.factory.DefaultLogicFactory;
import cn.bigmarket.types.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖策略规则规则值对象；值对象，没有唯一ID，仅限于从数据库查询对象
 * @create 2024-01-13 09:30
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyAwardRuleModelVO {
    private String ruleModels;
    public String[] raffleCenterRuleModelList() {
        // 把rule_models拆分 并且返回是 中置抽奖策略的 有哪些
        List<String> ruleModelList = new ArrayList<>();
        if (null == ruleModels)
            return null;
        String[] ruleModelValues = ruleModels.split(Constants.SPLIT);
        // 拆成 例如：rule_lock rule_luck_award
        for (String ruleModelValue : ruleModelValues) {
            if (DefaultLogicFactory.LogicModel.isCenter(ruleModelValue)) {
                // 判断 rule_lock 是不是中置抽奖策略
                // 判断 rule_luck_award 是不是中置抽奖策略
                ruleModelList.add(ruleModelValue);
            }
        }
        // 返回对应的String类型数组
        return ruleModelList.toArray(new String[0]);
    }

}
