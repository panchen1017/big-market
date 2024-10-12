package cn.bigmarket.trigger.api;

import cn.bigmarket.trigger.api.dto.RaffleAwardListRequestDTO;
import cn.bigmarket.trigger.api.dto.RaffleAwardListResponseDTO;
import cn.bigmarket.trigger.api.dto.RaffleRequestDTO;
import cn.bigmarket.trigger.api.dto.RaffleResponseDTO;
import cn.bigmarket.types.model.Response;

import java.util.List;

/**
 * 抽奖服务接口
 */
public interface IRaffleService {

    /**
     * 策略装配接口
     * @param strategyId 策略id
     * @return  装配结果
     */
    Response<Boolean> strategyArmory(Long strategyId);

    /**
     * 查询抽奖奖品列表配置
     *
     * @param requestDTO 抽奖奖品列表请求参数
     * @return 奖品列表数据
     */
    Response<List<RaffleAwardListResponseDTO>> queryRaffleAwardList(RaffleAwardListRequestDTO requestDTO);

    /**
     * 随机抽奖接口
     * @param requestDTO 请求参数
     * @return 抽奖结果
     */
    Response<RaffleResponseDTO> randomRaffle(RaffleRequestDTO requestDTO);
}
