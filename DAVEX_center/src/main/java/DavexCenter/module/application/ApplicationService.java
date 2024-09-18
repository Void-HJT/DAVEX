package DavexCenter.module.application;


import DavexBase.common.Body;
import DavexBase.entity.Application;
import DavexBase.mapper.ApplicationMapper;
import DavexCenter.common.HandleUid;
import DavexCenter.module.MQ.CenterPublishService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
    CenterPublishService centerPublishService;

    @Value("${service.id}")
    private String uid;

    // 使用 Jackson ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ApplicationService() {
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public Body<String> addApplication(Application application) {
        //设置application
        List<Object> uidList = applicationMapper.selectObjs(new QueryWrapper<Application>().select("uid"));
        // 遍历uidList中的每个uid
        int maxTailNumber = -1; // 初始化最大尾部数字
        for (Object obj : uidList) {
            if (obj instanceof String) {
                String uid = (String) obj;
                // 利用UidParser解析uid并获取数字
                HandleUid parser = new HandleUid(uid);
                int[] numbers = parser.getNumbers();

                // 获取最后一个数字（尾部数字）
                if (numbers.length > 0) {
                    int tailNumber = numbers[numbers.length - 1];  // 尾部的数字
                    // 比较更新最大尾部数字
                    if (tailNumber > maxTailNumber) {
                        maxTailNumber = tailNumber;
                    }
                }
            }
        }
        if(maxTailNumber==-1){return Body.error("自动获取uid列表失败");}
        application.setCenterId(uid);
        application.setUid(uid+"-AXX"+(maxTailNumber+1));
//        application.setLastUpdated(LocalDateTime.now());
        applicationMapper.insert(application);
        //通信
        String exchange = "applicationExchange";
        String message = null;
        try {
            message = "Create:" + objectMapper.writeValueAsString(application);
        } catch (JsonProcessingException e) {
            return Body.error("message 生成失败 "+e);
        }
        centerPublishService.publishMessageToFanout(exchange,message);
        return Body.success("插入application成功");
    }


    public Body<String> deleteApplication(String applicationId) {
        Application application1 = applicationMapper.selectById(applicationId);
        if(application1.getUid()!=uid){return Body.error("uid不正确");}
        applicationMapper.deleteById(applicationId);
        Application application = new Application();
        application.setUid(applicationId);
        //通信
        String exchange = "applicationExchange";
        String message = null;
        try {
            message = "Delete:" + objectMapper.writeValueAsString(application);
        } catch (JsonProcessingException e) {
            return Body.error("message 生成失败"+e);
        }
        centerPublishService.publishMessageToFanout(exchange,message);
        return Body.success("删除application成功");
    }


    public Body<String> updateApplication(Application application) {
        if (application.getCenterId()!=uid){return Body.error("uid不正确");}
        applicationMapper.updateById(application);
        //通信
        String exchange = "applicationExchange";
        String message = null;
        try {
            message = "Update:" + objectMapper.writeValueAsString(application);
        } catch (JsonProcessingException e) {
            return Body.error("message 生成失败"+e);
        }
        centerPublishService.publishMessageToFanout(exchange,message);
        return Body.success("更新application成功");
    }

    public Body<List<Application>> getApplicationList() {
        List<Application> applications = applicationMapper.selectList(null);
        return Body.success(applications,"成功获取application表");
    }
}
