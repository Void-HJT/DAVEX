package DavexBase.service.application;


import DavexBase.common.Body;
import DavexBase.common.GetMaxUid;
import DavexBase.entity.Agent;
import DavexBase.entity.Application;
import DavexBase.entity.Center;
import DavexBase.entity.Rule;
import DavexBase.mapper.AgentMapper;
import DavexBase.mapper.ApplicationMapper;
import DavexBase.mapper.CenterMapper;
import DavexBase.service.MQ.MessageService;
import DavexBase.service.auth.CenterWebClientService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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

//    @Autowired
//    AdminMQPublishService adminMQPublishService;

    @Autowired
    MessageService messageService;
    @Autowired
    AgentMapper agentMapper;
    @Autowired
    CenterWebClientService centerWebClientService;


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
        application.setPassword(uid);
        applicationMapper.insert(application);
        //通信
//        String exchange = "ApplicationExchange";
//        String message = null;
//        String operation = "Create";
//        String entity = "application";
//        message = messageService.getMessage(operation,entity,objectMapper,application);
//        adminMQPublishService.publishMessageToFanout(exchange,message);
        List<Agent> agentList = agentMapper.selectList(new LambdaQueryWrapper<>());
        for(Agent agent:agentList){
            try {
                // 准备 target 参数，可以根据实际情况选择 add, delete 或 update
                String target = "add";
                if(agent.getUid().equals(uid)){//跳过自己
                    continue;
                }
                // 构建 WebClient 请求并发送 POST 请求
                centerWebClientService.center2AgentWebClient(agent.getUid())
                        .post()
                        .uri(UriBuilder -> UriBuilder.path("/application/management/syncApplication").queryParam("target", target).build())// 请求 URL
                        .bodyValue(application)
                        .retrieve() // 发起请求
                        .bodyToMono(String.class) // 处理返回响应，假设返回的 Body 是 String 类型
                        .block();
            } catch (Exception e) {
                // 处理可能的异常
                System.err.println("Error processing agent with UID: " + agent.getUid());
                e.printStackTrace();
            }
        }
        return Body.success("插入application成功");
    }


    public Body<String> deleteApplication(String applicationId) {
        Application application1 = applicationMapper.selectById(applicationId);
        if(!uid.equals(application1.getCenterId())){return Body.error("uid不正确");}
        applicationMapper.deleteById(applicationId);
        Application application = new Application();
        application.setUid(applicationId);
        //通信
//        String exchange = "ApplicationExchange";
//        String message = null;
//        String operation = "Delete";
//        String entity = "application";
//        message = messageService.getMessage(operation,entity,objectMapper,application);
//        adminMQPublishService.publishMessageToFanout(exchange,message);
        List<Agent> agentList = agentMapper.selectList(new LambdaQueryWrapper<>());
        for(Agent agent:agentList){
            try {
                // 准备 target 参数，可以根据实际情况选择 add, delete 或 update
                String target = "delete";
                if(agent.getUid().equals(uid)){//跳过自己
                    continue;
                }
                // 构建 WebClient 请求并发送 POST 请求
                centerWebClientService.center2AgentWebClient(agent.getUid())
                        .post()
                        .uri(UriBuilder -> UriBuilder.path("/application/management/syncApplication").queryParam("target", target).build())// 请求 URL
                        .bodyValue(application)
                        .retrieve() // 发起请求
                        .bodyToMono(String.class) // 处理返回响应，假设返回的 Body 是 String 类型
                        .block();
            } catch (Exception e) {
                // 处理可能的异常
                System.err.println("Error processing agent with UID: " + agent.getUid());
                e.printStackTrace();
            }
        }
        return Body.success("删除application成功");
    }


    public Body<String> updateApplication(Application application) {
        if (!uid.equals(application.getCenterId())){return Body.error("uid不正确");}
        applicationMapper.updateById(application);
        //通信
//        String exchange = "ApplicationExchange";
//        String message = null;
//        String operation = "Update";
//        String entity = "application";
//        message = messageService.getMessage(operation,entity,objectMapper,application);
//        adminMQPublishService.publishMessageToFanout(exchange,message);
        List<Agent> agentList = agentMapper.selectList(new LambdaQueryWrapper<>());
        for(Agent agent:agentList){
            try {
                // 准备 target 参数，可以根据实际情况选择 add, delete 或 update
                String target = "update";
                if(agent.getUid().equals(uid)){//跳过自己
                    continue;
                }
                // 构建 WebClient 请求并发送 POST 请求
                centerWebClientService.center2AgentWebClient(agent.getUid())
                        .post()
                        .uri(UriBuilder -> UriBuilder.path("/application/management/syncApplication").queryParam("target", target).build())// 请求 URL
                        .bodyValue(application)
                        .retrieve() // 发起请求
                        .bodyToMono(String.class) // 处理返回响应，假设返回的 Body 是 String 类型
                        .block();
            } catch (Exception e) {
                // 处理可能的异常
                System.err.println("Error processing agent with UID: " + agent.getUid());
                e.printStackTrace();
            }
        }
        return Body.success("更新application成功");
    }

    public Body<List<Application>> getApplicationList() {
        List<Application> applications = applicationMapper.selectList(null);
        return Body.success(applications,"成功获取application表");
    }
    //
    public Body<String> syncApplication(Application application, String target){

        // 检查输入参数
        if (application == null || target == null || target.isEmpty()) {
            return Body.error("Invalid input: application or target is null or empty.");
        }

        LambdaQueryWrapper<Application> queryApplicationWrapper = Wrappers.<Application>lambdaQuery()
                .eq(Application::getUid, application.getUid());
        //查看是否存在
        Application existingApplication = applicationMapper.selectOne(queryApplicationWrapper);
        //根据target不同进行不同操作 add delete update
        try {
            switch (target.toLowerCase()) {
                case "add":
                    if (existingApplication != null) {
                        return Body.error("Application with the same UID already exists.");
                    }
                    applicationMapper.insert(application); // 插入新的
                    return Body.success("Application added successfully.");

                case "delete":
                    if (existingApplication == null) {
                        return Body.error("Application not found. Cannot delete.");
                    }
                    applicationMapper.delete(queryApplicationWrapper); // 删除目标
                    return Body.success("Application deleted successfully.");

                case "update":
                    if (existingApplication == null) {
                        return Body.error("Application not found. Cannot update.");
                    }
                    applicationMapper.update(application, queryApplicationWrapper); // 更新
                    return Body.success("Application updated successfully.");

                default:
                    return Body.error("Invalid target action: " + target);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Body.error("An error occurred: " + e.getMessage());
        }
    }

}
