package cn.bigmarket.infrastructure.persistent.repository;

import cn.bigmarket.domain.task.model.entity.TaskEntity;
import cn.bigmarket.domain.task.repository.ITaskRepository;
import cn.bigmarket.infrastructure.event.EventPublisher;
import cn.bigmarket.infrastructure.persistent.dao.ITaskDao;
import cn.bigmarket.infrastructure.persistent.po.Task;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 任务服务仓储实现
 * @create 2024-04-06 10:57
 */
@Repository
public class TaskRepository implements ITaskRepository {

    @Resource
    private ITaskDao taskDao;
    @Resource
    private EventPublisher eventPublisher;

    @Override
    public List<TaskEntity> queryNoSendMessageTaskList() {
        List<Task> tasks = taskDao.queryNoSendMessageTaskList();
        List<TaskEntity> taskEntities = new ArrayList<>(tasks.size());
        for (Task task : tasks) {
            TaskEntity taskEntity = new TaskEntity();
            taskEntity.setUserId(task.getUserId());
            taskEntity.setTopic(task.getTopic());
            taskEntity.setMessageId(task.getMessageId());
            taskEntity.setMessage(task.getMessage());
            taskEntities.add(taskEntity);
        }
        return taskEntities;
    }

    @Override
    public void sendMessage(TaskEntity taskEntity) {
        // 消息发送
        eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
    }

    @Override
    public void updateTaskSendMessageCompleted(String userId, String messageId) {
        Task taskReq = new Task();
        taskReq.setUserId(userId);
        taskReq.setMessageId(messageId);
        taskDao.updateTaskSendMessageCompleted(taskReq);
    }

    @Override
    public void updateTaskSendMessageFail(String userId, String messageId) {
        // 发送失败
        Task taskReq = new Task();
        taskReq.setUserId(userId);
        taskReq.setMessageId(messageId);
        taskDao.updateTaskSendMessageFail(taskReq);
    }

}