package cn.bigmarket.domain.task.service;

import cn.bigmarket.domain.task.model.entity.TaskEntity;

import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 消息任务服务接口
 * @create 2024-04-06 10:49
 */
public interface ITaskService {

    /**
     * 查询发送MQ失败和超时 1 分钟未发送的MQ
     *
     * @return 未发送的任务消息列表10条
     */
    List<TaskEntity> queryNoSendMessageTaskList();

    void sendMessage(TaskEntity taskEntity);

    void updateTaskSendMessageCompleted(String userId, String messageId);

    void updateTaskSendMessageFail(String userId, String messageId);

}
