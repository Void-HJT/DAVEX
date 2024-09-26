package DavexCenter.module.application;


import DavexBase.common.Body;
import DavexBase.common.GetMaxUid;
import DavexBase.entity.Application;
import DavexBase.mapper.ApplicationMapper;
import DavexBase.service.MQ.MessageService;
import DavexCenter.module.MQ.AdminMQPublishService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {
    @Autowired
    ApplicationMapper applicationMapper;

    @Autowired
    AdminMQPublishService adminMQPublishService;

    @Autowired
    MessageService messageService;

    @Value("${my.id}")
    private String uid;

    // 使用 Jackson ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ApplicationService() {
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public Body<String> addApplication(Application application) {
        //设置application
        GetMaxUid getMaxUid = new GetMaxUid();
        int maxTailNumber = getMaxUid.getApplicationMaxUid(uid,applicationMapper);
        application.setCenterId(uid);
        application.setUid(uid+"-AXX"+(maxTailNumber+1));
        application.setLastUpdated(LocalDateTime.now());
        applicationMapper.insert(application);
        //通信
        String exchange = "applicationExchange";
        String message = null;
        String operation = "Create";
        String entity = "application";
        message = messageService.getMessage(operation,entity,objectMapper,application);
        adminMQPublishService.publishMessageToFanout(exchange,message);
        return Body.success("插入application成功");
    }


    public Body<String> deleteApplication(String applicationId) {
        Application application1 = applicationMapper.selectById(applicationId);
        if(!uid.equals(application1.getCenterId())){return Body.error("uid不正确");}
        applicationMapper.deleteById(applicationId);
        Application application = new Application();
        application.setUid(applicationId);
        //通信
        String exchange = "applicationExchange";
        String message = null;
        String operation = "Delete";
        String entity = "application";
        message = messageService.getMessage(operation,entity,objectMapper,application);
        adminMQPublishService.publishMessageToFanout(exchange,message);
        return Body.success("删除application成功");
    }


    public Body<String> updateApplication(Application application) {
        if (!uid.equals(application.getCenterId())){return Body.error("uid不正确");}
        applicationMapper.updateById(application);
        //通信
        String exchange = "applicationExchange";
        String message = null;
        String operation = "Update";
        String entity = "application";
        message = messageService.getMessage(operation,entity,objectMapper,application);
        adminMQPublishService.publishMessageToFanout(exchange,message);
        return Body.success("更新application成功");
    }

    public Body<List<Application>> getApplicationList() {
        List<Application> applications = applicationMapper.selectList(null);
        return Body.success(applications,"成功获取application表");
    }
}
