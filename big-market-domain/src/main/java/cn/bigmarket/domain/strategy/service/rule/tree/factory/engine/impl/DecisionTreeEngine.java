package cn.bigmarket.domain.strategy.service.rule.tree.factory.engine.impl;

import cn.bigmarket.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import cn.bigmarket.domain.strategy.model.valobj.RuleTreeNodeLineVO;
import cn.bigmarket.domain.strategy.model.valobj.RuleTreeNodeVO;
import cn.bigmarket.domain.strategy.model.valobj.RuleTreeVO;
import cn.bigmarket.domain.strategy.service.rule.tree.ILogicTreeNode;
import cn.bigmarket.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import cn.bigmarket.domain.strategy.service.rule.tree.factory.engine.IDecisionTreeEngine;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 决策树引擎（工厂来帮我创建，我自己不创建了）
 */
@Slf4j
public class DecisionTreeEngine implements IDecisionTreeEngine {

    private final Map<String, ILogicTreeNode> logicTreeNodeGroup;

    // 整个树对象
    private final RuleTreeVO ruleTreeVO;

    public DecisionTreeEngine(Map<String, ILogicTreeNode> logicTreeNodeGroup, RuleTreeVO ruleTreeVO) {
        this.logicTreeNodeGroup = logicTreeNodeGroup;
        this.ruleTreeVO = ruleTreeVO;
    }

    @Override
    public DefaultTreeFactory.StrategyAwardVO process(String userId, Long strategyId, Integer awardId) {

        // process方法 就是去处理二叉树当中的走向
        // 首先，创建一个要返回的对象
        DefaultTreeFactory.StrategyAwardVO strategyAwardData = null;

        // 获取当前树对象基本信息（根节点，子节点map）
        String nextNode = ruleTreeVO.getTreeRootRuleNode();
        Map<String, RuleTreeNodeVO> treeNodeMap = ruleTreeVO.getTreeNodeMap();

        // 通过 nextNode 指针获取当前树根节点对象 ruleTreeNode
        // 获取起始节点（执行节点）「根节点记录了第一个要执行的规则」，用指针去获取真正树根节点
        // ruleTreeNode 这是一个真正的根节点，不是树对象了
        RuleTreeNodeVO ruleTreeNode = treeNodeMap.get(nextNode);

        // nextNode：当前指针
        // ruleTreeNode：当前节点
        while (null != nextNode) {// 直至下一个节点没有了，当前的节点就是最终节点
            // 1. 获取ruleKey（“rule_lock”,“rule_luck_award”,“rule_stock”）
            ILogicTreeNode logicTreeNode = logicTreeNodeGroup.get(ruleTreeNode.getRuleKey());

            // 2. 决策节点计算
            // 根据 logicTreeNode（"rule_stock"，"rule_luck_award"，"rule_lock"）去过对应的逻辑 logic
            DefaultTreeFactory.TreeActionEntity logicEntity = logicTreeNode.logic(userId, strategyId, awardId, ruleTreeNode.getRuleValue());
            // 获取 logicEntity 的结果（"TAKE_OVER" / "ALLOW"）
            // ruleLogicCheckTypeVO：是否能走到下一个节点的信息
            // strategyAwardData：当前对象的数据信息
            RuleLogicCheckTypeVO ruleLogicCheckTypeVO = logicEntity.getRuleLogicCheckType();
            strategyAwardData = logicEntity.getStrategyAwardVO();// awardId / awardRuleValue
            log.info("决策树引擎【{}】treeId:{} node:{} code:{}", ruleTreeVO.getTreeName(), ruleTreeVO.getTreeId(), nextNode, ruleLogicCheckTypeVO.getCode());

            // 3. 获取下个节点
            // ruleLogicCheckTypeVO.getCode()走左子树还是右子树，根据其中的 "TAKE_OVER" / "ALLOW" 去判断
            // ruleTreeNode.getTreeNodeLineVOList 是获取存放节点的连线
            nextNode = nextNode(ruleLogicCheckTypeVO.getCode(), ruleTreeNode.getTreeNodeLineVOList());
            ruleTreeNode = treeNodeMap.get(nextNode);
        }
        // 返回最终结果
        return strategyAwardData;
    }
    private String nextNode(String matterValue, List<RuleTreeNodeLineVO> ruleTreeNodeLineVOList) {
        // treeNodeLineVOList 就是指当前节点两条（左，右子树）节点连线的信息
        // 如果为空，就说明当前就是叶子节点
//        if(null == ruleTreeNodeLineVOList || ruleTreeNodeLineVOList.size() == 0)
        if(null == ruleTreeNodeLineVOList || ruleTreeNodeLineVOList.isEmpty())
            return null;
        for (RuleTreeNodeLineVO nodeLine : ruleTreeNodeLineVOList) {
            if(decisionLogic(matterValue, nodeLine)) {
                // decisionLogic 判断是走左子树的 “ALLOW” ，还是右子树的 “TAKE_OVER”
                return nodeLine.getRuleNodeTo();
            }
        }
        return null;
//        throw new RuntimeException("决策树引擎，nextNode 计算失败，未找到可执行节点！");
    }

    public boolean decisionLogic(String matterValue, RuleTreeNodeLineVO nodeLine) {
        switch (nodeLine.getRuleLimitType()) {
            case EQUAL:
                return matterValue.equals(nodeLine.getRuleLimitValue().getCode());
            // 以下规则暂时不需要实现
            case GT:
            case LT:
            case GE:
            case LE:
            default:
                return false;
        }
    }
//        // 要返回的对象
//        DefaultTreeFactory.StrategyAwardData strategyAwardData = null;
//
//        // 获取基础信息 ruleTreeVO：树对象
//        // 根节点指针 nextNode
//        String nextNode = ruleTreeVO.getTreeRootRuleNode();
//        // 树所有节点存在 map 中
//        Map<String, RuleTreeNodeVO> treeNodeMap = ruleTreeVO.getTreeNodeMap();
//
//        // 获取起始节点（执行节点）「根节点记录了第一个要执行的规则」，用指针去获取真正树根节点
//        // ruleTreeNode 这是一个真正的根节点，不是树对象了
//        RuleTreeNodeVO ruleTreeNode = treeNodeMap.get(nextNode);
//
//        // nextNode：指针
//        // ruleTreeNode：当前节点
//        while (null != nextNode) {  // 直至下一个节点没有了，当前的节点就是最终节点
//            // 1. 获取决策节点 ，例如："rule_stock"，"rule_luck_award"，"rule_lock"
//            ILogicTreeNode logicTreeNode = logicTreeNodeGroup.get(ruleTreeNode.getRuleKey());
//
//            // 2. 决策节点计算
//            // 根据 logicTreeNode（"rule_stock"，"rule_luck_award"，"rule_lock"）去过对应的逻辑 logic
//            DefaultTreeFactory.TreeActionEntity logicEntity = logicTreeNode.logic(userId, strategyId, awardId);// 一遍遍去过
//            // 获取 logicEntity 的结果（"TAKE_OVER" / "ALLOW"）
//            // ruleLogicCheckTypeVO：是否能走到下一个节点的信息
//            // strategyAwardData：当前对象的数据信息
//            RuleLogicCheckTypeVO ruleLogicCheckTypeVO = logicEntity.getRuleLogicCheckType();
//            strategyAwardData = logicEntity.getStrategyAwardData();// awardId / awardRuleValue
//            log.info("决策树引擎【{}】treeId:{} node:{} code:{}", ruleTreeVO.getTreeName(), ruleTreeVO.getTreeId(), nextNode, ruleLogicCheckTypeVO.getCode());
//
//            // 3. 获取下个节点
//            // ruleLogicCheckTypeVO.getCode()走左子树还是右子树，根据其中的 "TAKE_OVER" / "ALLOW" 去判断
//            // ruleTreeNode.getTreeNodeLineVOList 是获取存放节点的连线
//            nextNode = nextNode(ruleLogicCheckTypeVO.getCode(), ruleTreeNode.getTreeNodeLineVOList());
//            ruleTreeNode = treeNodeMap.get(nextNode);
//        }
//
//        // 返回最终结果
//        return strategyAwardData;
//    }
//    public String nextNode(String matterValue, List<RuleTreeNodeLineVO> treeNodeLineVOList) {
//        // treeNodeLineVOList 就是指当前节点两条（左，右子树）节点连线的信息
//        // 如果为空，就说明当前就是叶子节点
//        if (null == treeNodeLineVOList || treeNodeLineVOList.isEmpty()) return null;
//        for (RuleTreeNodeLineVO nodeLine : treeNodeLineVOList) {
//            if (decisionLogic(matterValue, nodeLine)) {
//                return nodeLine.getRuleNodeTo();
//            }
//        }
//        throw new RuntimeException("决策树引擎，nextNode 计算失败，未找到可执行节点！");
//    }





}
