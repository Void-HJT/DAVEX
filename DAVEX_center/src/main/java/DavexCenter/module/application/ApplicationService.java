package DavexCenter.module.application;

import DavexBase.common.Body;
import DavexBase.entity.Application;
import DavexBase.mapper.ApplicationMapper;
import DavexCenter.module.MQ.RabbitMQProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {
    @Autowired
    ApplicationMapper applicationMapper;

    @Autowired
    RabbitMQProducerService rabbitMQProducerService;

    public Body<String> addApplication(Application application) {

        applicationMapper.insert(application);
        // 发送创建消息
        rabbitMQProducerService.sendApplicationCreatedMessage(application);
        return Body.success("成功插入");
    }
}
