package DveAgent.module.directory;


import java.time.LocalDateTime;
import java.util.List;

import DveBase.service.directory.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import DveBase.common.Body;
import DveBase.entity.Application;
import DveBase.entity.Group;
import DveBase.entity.Rule;

@RestController // @RestController的作用等同于@Controller + @ResponseBody。
// 相当于@Controller+@ResponseBody两个注解的结合，返回json数据不需要在方法前面加@ResponseBody注解了，但使用@RestController这个注解，就不能返回jsp,html页面，视图解析器无法解析jsp,html页面
@RequestMapping("/directory/group")
public class GroupController {

    private static final Logger customLogger = LoggerFactory.getLogger("CustomLogger");

    @Autowired
    GroupService groupService;


    @PostMapping("/getApplication")
    public Body<List<Application>> getApplication(){
        return groupService.getApplication();

    }
    @PostMapping("/getGroup")
    public Body<List<Group>> getGroup(@RequestParam("agentId") Long agentId,
                                      @RequestParam("centerId") Long centerId){
        // 记录输入参数、调用方法、请求方、接收方和时间
        String requestTime = LocalDateTime.now().toString();
        customLogger.info("Custom Log - Input: agentId={}, centerId={}, Method: getGroup, Requester: {}, Responder: {}, Time: {}",
                agentId, centerId, "RequesterInfo", "ResponderInfo", requestTime);

        Body<List<Group>> response = groupService.getGroup(agentId, centerId);

        // 记录输出结果、请求方、接收方和时间
        String responseTime = LocalDateTime.now().toString();
        customLogger.info("Custom Log - Output: {}, Requester: {}, Responder: {}, Time: {}",
                response, "RequesterInfo", "ResponderInfo", responseTime);

        return response;
    }
    @PostMapping("/addGroup")
    public Body<String> addGroup(@RequestParam("agentId") Long agentId,
                                 @RequestParam("centerId") Long centerId,
                                 @RequestParam("name") String name){
        return groupService.addGroup(agentId,centerId,name);
    }
    @PostMapping("/deleteGroup")
    public Body<String> deleteGroup(@RequestParam("agentId") Long agentId,
                                    @RequestParam("centerId") Long centerId,
                                    @RequestParam("groupId") Long groupId){
        return groupService.deleteGroup(agentId,centerId,groupId);
    }

    @PostMapping("/getGroupByApplicationId")
    public Body<List<Group>> getGroupByApplicationId(@RequestParam("agentId") Long agentId,
                                                     @RequestParam("centerId") Long centerId,
                                                     @RequestParam("applicationId") Long applicationId){
        return groupService.getGroupByApplicationId(agentId,centerId,applicationId);

    }

    @PostMapping("/addApplicationGroup")
    public Body<String> addApplicationGroup(@RequestParam("agentId") Long agentId,
                                            @RequestParam("centerId") Long centerId,
                                            @RequestParam("applicationId") Long applicationId,
                                            @RequestParam("groupId") Long groupId){
        return groupService.addApplicationGroup(agentId,centerId,applicationId,groupId);

    }
    @PostMapping("/deleteApplicationGroup")
    public Body<String> deleteApplicationGroup(@RequestParam("agentId") Long agentId,
                                            @RequestParam("centerId") Long centerId,
                                               @RequestParam("applicationId") Long applicationId,
                                               @RequestParam("groupId") Long groupId){
        return groupService.deleteApplicationGroup(agentId,centerId,applicationId,groupId);

    }

    @PostMapping("/addRule")
    public Body<String> addRule(@RequestParam("agentId") Long agentId,
                                @RequestParam("groupId") Long groupId,
                                @RequestParam("allowMethod")String allowMethod){
        return groupService.addRule(agentId,groupId,allowMethod);

    }
    @PostMapping("/getRuleByGroup")
    public Body<List<Rule>> getRuleByGroup(@RequestParam("agentId") Long agentId,
                                           @RequestParam("groupId") Long groupId){
        return groupService.getRuleByGroup(agentId,groupId);
    }
    @PostMapping("/deleteRule")
    public Body<String> deleteRule(@RequestParam("agentId") Long agentId,
                                   @RequestParam("groupId") Long groupId,
                                   @RequestParam("allowMethod")String allowMethod){
        return groupService.deleteRule(agentId,groupId,allowMethod);
    }
    
}
