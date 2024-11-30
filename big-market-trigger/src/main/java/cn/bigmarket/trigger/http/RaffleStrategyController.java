package cn.bigmarket.trigger.http;

import cn.bigmarket.domain.strategy.model.entity.RaffleAwardEntity;
import cn.bigmarket.domain.strategy.model.entity.RaffleFactorEntity;
import cn.bigmarket.domain.strategy.model.entity.StrategyAwardEntity;
import cn.bigmarket.domain.strategy.service.IRaffleAward;
import cn.bigmarket.domain.strategy.service.IRaffleStrategy;
import cn.bigmarket.domain.strategy.service.armory.IStrategyArmory;
import cn.bigmarket.trigger.api.IRaffleStrategyService;
import cn.bigmarket.trigger.api.dto.RaffleAwardListRequestDTO;
import cn.bigmarket.trigger.api.dto.RaffleAwardListResponseDTO;
import cn.bigmarket.trigger.api.dto.RaffleStrategyRequestDTO;
import cn.bigmarket.trigger.api.dto.RaffleStrategyResponseDTO;
import cn.bigmarket.types.enums.ResponseCode;
import cn.bigmarket.types.exception.AppException;
import cn.bigmarket.types.model.Response;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 抽奖服务
 *
 */
@Slf4j
@RestController()
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/raffle/")
public class RaffleStrategyController implements IRaffleStrategyService {

    @Resource
    private IStrategyArmory strategyArmory;
    @Resource
    private IRaffleAward raffleAward;
    @Resource
    private IRaffleStrategy raffleStrategy;

    /**
     * 策略装配，将策略信息装配到缓存中
     * <a href="http://localhost:8091/api/v1/raffle/strategy_armory">/api/v1/raffle/strategy_armory</a>
     *
     * @param strategyId 策略ID
     * @return 装配结果
     */
    @RequestMapping(value = "strategy_armory", method = RequestMethod.GET)
    @Override
    public Response<Boolean> strategyArmory(@RequestParam Long strategyId) {

        try {
            log.info("抽奖策略装配开始 strategyId：{}", strategyId);
            boolean status = strategyArmory.assembleLotteryStrategy(strategyId);
            Response<Boolean> response = Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(status)
                    .build();
            log.info("抽奖策略装配完成 strategyId：{} response:{}", strategyId, response);
            return response;

        } catch (Exception e) {
            log.info("抽奖策略装配失败！！！ strategyId：{}", strategyId);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 查询奖品列表
     * <a href="http://localhost:8091/api/v1/raffle/query_raffle_award_list">/api/v1/raffle/query_raffle_award_list</a>
     * 请求参数 raw json
     *
     * @param requestDTO {"strategyId":1000001}
     * @return 奖品列表
     */
    @RequestMapping(value = "query_raffle_award_list", method = RequestMethod.POST)
    @Override
    public Response<List<RaffleAwardListResponseDTO>> queryRaffleAwardList(@RequestBody RaffleAwardListRequestDTO requestDTO) {

        try {
            log.info("查询抽奖奖品列表配开始 strategyId：{}", requestDTO.getStrategyId());
            // 查询奖品配置
            Long strategyId = requestDTO.getStrategyId();
            List<StrategyAwardEntity> strategyAwardEntities = raffleAward.queryRaffleStrategyAwardList(strategyId);
            List<RaffleAwardListResponseDTO> responseDTOS = new ArrayList<>(strategyAwardEntities.size());
            for (StrategyAwardEntity strategyAwardEntity : strategyAwardEntities) {
                    responseDTOS.add(RaffleAwardListResponseDTO.builder()
                                    .awardId(strategyAwardEntity.getAwardId())
                                    .awardSubtitle(strategyAwardEntity.getAwardSubtitle())
                                    .awardTitle(strategyAwardEntity.getAwardTitle())
                                    .sort(strategyAwardEntity.getSort())
                                    .build());
            }
            Response<List<RaffleAwardListResponseDTO>> response =  Response.<List<RaffleAwardListResponseDTO>>builder()
                    .data(responseDTOS)
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
            log.info("查询抽奖奖品列表配置完成 strategyId：{} response: {}", requestDTO.getStrategyId(), JSON.toJSONString(response));
            // 返回结果
            return response;
        } catch (Exception e) {
            log.error("查询抽奖奖品列表配置失败 strategyId：{}", requestDTO.getStrategyId(), e);
            return Response.<List<RaffleAwardListResponseDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
    /**
     * 随机抽奖接口
     * <a href="http://localhost:8091/api/v1/raffle/random_raffle">/api/v1/raffle/random_raffle</a>
     *
     * @param requestDTO 请求参数 {"strategyId":1000001}
     * @return 抽奖结果
     */
    @RequestMapping(value = "random_raffle", method = RequestMethod.POST)
    @Override
    public Response<RaffleStrategyResponseDTO> randomRaffle(@RequestBody RaffleStrategyRequestDTO requestDTO) {
        try {
            log.info("随机抽奖开始 strategyId: {}", requestDTO.getStrategyId());
            RaffleFactorEntity raffleFactor = new RaffleFactorEntity();
            raffleFactor.setStrategyId(requestDTO.getStrategyId());
            raffleFactor.setUserId("system");
            RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(raffleFactor);

            Response<RaffleStrategyResponseDTO> response = Response.<RaffleStrategyResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(RaffleStrategyResponseDTO.builder()
                            .awardId(raffleAwardEntity.getAwardId())
                            .awardIndex(raffleAwardEntity.getSort())
                            .build())
                    .build();
            log.info("随机抽奖完成 strategyId: {} response: {}", requestDTO.getStrategyId(), JSON.toJSONString(response));
            return response;
        } catch (AppException e) {
            log.error("随机抽奖失败 strategyId：{} {}", requestDTO.getStrategyId(), e.getInfo());
            return Response.<RaffleStrategyResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("随机抽奖失败 strategyId：{}", requestDTO.getStrategyId(), e);
            return Response.<RaffleStrategyResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }








}
