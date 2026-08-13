package DavexAgent.module.task.port;

/**
 * Agent 向 Center 回传 MPC/PSI 任务通知的业务端口。
 *
 * 业务层只提供通知内容，不感知 HTTP 地址和查询参数。
 */
public interface CenterTaskNotificationClient {

    void sendNotification(
            String centerId,
            String applicationId,
            String title,
            String content,
            String taskId,
            Integer code,
            String type) throws Exception;
}
