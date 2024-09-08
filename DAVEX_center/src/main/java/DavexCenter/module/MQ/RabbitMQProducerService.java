package DavexCenter.module.MQ;

import DavexBase.entity.Application;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducerService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void sendApplicationCreatedMessage(Application application) {
        sendApplicationMessage("Application created", application);
    }

    public void sendApplicationUpdatedMessage(Application application) {
        sendApplicationMessage("Application updated", application);
    }

    public void sendApplicationDeletedMessage(Long applicationId) {
        String message = "Application deleted: " + applicationId;
        rabbitTemplate.convertAndSend("application-exchange", "application.deleted", message);
    }

    private void sendApplicationMessage(String action, Application application) {
        // 将 Application 对象转换为 JSON 字符串
        String applicationJson = null;
        try {
            applicationJson = objectMapper.writeValueAsString(application);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String message = action + ": " + applicationJson;
        rabbitTemplate.convertAndSend("application-exchange", "application." + action.toLowerCase(), message);
    }

}
