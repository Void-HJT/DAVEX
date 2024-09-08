package DavexAgent.module.MQ;

import DavexBase.entity.Application;
import DavexBase.mapper.ApplicationMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQConsumerService {

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = "application-queue")
    public void receiveMessage(String message) {
        System.out.println("Received message: " + message);

        // 根据消息内容处理业务逻辑，例如更新本地的 Application 数据库表
        // 可以解析消息，判断是新增、修改还是删除操作
        if (message.startsWith("Application created")) {
            // 执行相应的新增操作
            handleApplicationCreated(message);
        } else if (message.startsWith("Application updated")) {
            // 执行相应的更新操作
            handleApplicationUpdated(message);
        } else if (message.startsWith("Application deleted")) {
            // 执行相应的删除操作
            handleApplicationDeleted(message);
        }

    }

    private void handleApplicationCreated(String message) {
        // 从消息中提取 Application 信息
        try {
            Application application = extractApplicationFromMessage(message);
            // 保存到数据库
            applicationMapper.insert(application);
            System.out.println("Application created and saved: " + application.getName());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void handleApplicationUpdated(String message) {
        try {
            Application application = extractApplicationFromMessage(message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // 更新本地数据库
    }

    private void handleApplicationDeleted(String message) {
        Long applicationId = extractApplicationIdFromMessage(message);

        // 从数据库删除
    }

    private Application extractApplicationFromMessage(String message) throws JsonProcessingException {
        // 去掉消息中的操作信息，获取 JSON 部分
        String jsonMessage = message.substring(message.indexOf("{"));
        // 将 JSON 转换为 Application 对象
        return objectMapper.readValue(jsonMessage, Application.class);
    }

    private Long extractApplicationIdFromMessage(String message) {
        // 从消息中提取出 Application ID
        return Long.parseLong(message.split(": ")[1].trim());
    }




}
