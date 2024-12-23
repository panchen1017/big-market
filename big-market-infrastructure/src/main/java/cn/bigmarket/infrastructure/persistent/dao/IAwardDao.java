package cn.bigmarket.infrastructure.persistent.dao;


import cn.bigmarket.infrastructure.persistent.po.Award;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 奖品
 */
@Mapper
public interface IAwardDao {

    List<Award> queryAwardList();
    String queryAwardConfigByAwardId(Integer awardId);

    String queryAwardKeyByAwardId(Integer awardId);
}
