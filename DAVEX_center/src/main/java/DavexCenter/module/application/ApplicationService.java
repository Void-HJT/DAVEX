package DavexCenter.module.application;


import DavexBase.common.Body;
import DavexBase.entity.Application;
import DavexBase.mapper.ApplicationMapper;
import DavexCenter.module.MQ.CenterPublishService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {
    @Autowired
    ApplicationMapper applicationMapper;

    @Autowired
    CenterPublishService centerPublishService;

    // 使用 Jackson ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Body<String> addApplication(Application application) {
        applicationMapper.insert(application);
        //通信
        String exchange = "applicationExchange";
        String message = null;
        try {
            message = "Create:" + objectMapper.writeValueAsString(application);
        } catch (JsonProcessingException e) {
            return Body.error("message 生成失败");
        }
        centerPublishService.publishMessageToFanout(exchange,message);
        return Body.success("插入application成功");
    }


    public Body<String> deleteApplication(String applicationId) {
        applicationMapper.deleteById(applicationId);
        Application application = new Application();
        application.setUid(applicationId);
        //通信
        String exchange = "applicationExchange";
        String message = null;
        try {
            message = "Delete:" + objectMapper.writeValueAsString(application);
        } catch (JsonProcessingException e) {
            return Body.error("message 生成失败");
        }
        centerPublishService.publishMessageToFanout(exchange,message);
        return Body.success("删除application成功");
    }


    public Body<String> updateApplication(Application application) {
        applicationMapper.updateById(application);
        //通信
        String exchange = "applicationExchange";
        String message = null;
        try {
            message = "Update:" + objectMapper.writeValueAsString(application);
        } catch (JsonProcessingException e) {
            return Body.error("message 生成失败");
        }
        centerPublishService.publishMessageToFanout(exchange,message);
        return Body.success("更新application成功");
    }

}
