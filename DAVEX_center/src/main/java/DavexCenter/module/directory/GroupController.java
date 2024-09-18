package DavexCenter.module.directory;


import DavexBase.common.Body;
import DavexBase.entity.Agent;
import DavexBase.entity.Center;
import DavexBase.entity.Group;
import DavexBase.entity.Rule;
import DavexBase.info.ApplicationAndCenterName;
import DavexBase.info.GroupAndCenterName;
import DavexBase.service.directory.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController // @RestController的作用等同于@Controller + @ResponseBody。
// 相当于@Controller+@ResponseBody两个注解的结合，返回json数据不需要在方法前面加@ResponseBody注解了，但使用@RestController这个注解，就不能返回jsp,html页面，视图解析器无法解析jsp,html页面
@RequestMapping("/directory/group")
public class GroupController {

    private static final Logger customLogger = LoggerFactory.getLogger("CustomLogger");

    @Autowired
    GroupService groupService;


    @PostMapping("/getApplication")
    public Body<List<ApplicationAndCenterName>> getApplication(){
        return groupService.getApplication();

    }

    @PostMapping("/getGroupList")
    public Body<List<GroupAndCenterName>> getGroupList(@RequestParam("agentId") String agentId,
                                      @RequestParam("centerId") String centerId){
//        // 记录输入参数、调用方法、请求方、接收方和时间
//        String requestTime = LocalDateTime.now().toString();
//        customLogger.info("Custom Log - Input: agentId={}, centerId={}, Method: getGroup, Requester: {}, Responder: {}, Time: {}",
//                agentId, centerId, "RequesterInfo", "ResponderInfo", requestTime);

        Body<List<GroupAndCenterName>> response = groupService.getGroupList(agentId, centerId);

//        // 记录输出结果、请求方、接收方和时间
//        String responseTime = LocalDateTime.now().toString();
//        customLogger.info("Custom Log - Output: {}, Requester: {}, Responder: {}, Time: {}",
//                response, "RequesterInfo", "ResponderInfo", responseTime);

        return response;
    }
    @PostMapping("/addGroup")
    public Body<String> addGroup(@RequestParam("agentId") String agentId,
                                 @RequestParam("centerId") String centerId,
                                 @RequestParam("name") String name){
        return groupService.addGroup(agentId,centerId,name);
    }
    @PostMapping("/deleteGroup")
    public Body<String> deleteGroup(@RequestParam("agentId") String agentId,
                                    @RequestParam("centerId") String centerId,
                                    @RequestParam("groupId") Long groupId){
        return groupService.deleteGroup(agentId,centerId,groupId);
    }

    @PostMapping("/getGroupByApplicationId")
    public Body<List<GroupAndCenterName>> getGroupByApplicationId(@RequestParam("agentId") String agentId,
                                                                  @RequestParam("centerId") String centerId,
                                                                  @RequestParam("applicationId") String applicationId){
        return groupService.getGroupByApplicationId(agentId,centerId,applicationId);

    }

    @PostMapping("/addApplicationGroup")
    public Body<String> addApplicationGroup(@RequestParam("agentId") String agentId,
                                            @RequestParam("centerId") String centerId,
                                            @RequestParam("applicationId") String applicationId,
                                            @RequestParam("groupId") Long groupId){
        return groupService.addApplicationGroup(agentId,centerId,applicationId,groupId);

    }
    @PostMapping("/deleteApplicationGroup")
    public Body<String> deleteApplicationGroup(@RequestParam("agentId") String agentId,
                                            @RequestParam("centerId") String centerId,
                                               @RequestParam("applicationId") String applicationId,
                                               @RequestParam("groupId") Long groupId){
        return groupService.deleteApplicationGroup(agentId,centerId,applicationId,groupId);

    }

    @PostMapping("/addRule")
    public Body<String> addRule(@RequestParam("agentId") String agentId,
                                @RequestParam("groupId") Long groupId,
                                @RequestParam("allowMethod")String allowMethod){
        return groupService.addRule(agentId,groupId,allowMethod);

    }
    @PostMapping("/getRuleByGroup")
    public Body<List<Rule>> getRuleByGroup(@RequestParam("agentId") String agentId,
                                           @RequestParam("groupId") Long groupId){
        return groupService.getRuleByGroup(agentId,groupId);
    }
    @PostMapping("/deleteRule")
    public Body<String> deleteRule(@RequestParam("agentId") String agentId,
                                   @RequestParam("groupId") Long groupId,
                                   @RequestParam("allowMethod")String allowMethod){
        return groupService.deleteRule(agentId,groupId,allowMethod);
    }
    @PostMapping("/getAllAllowedMethod")
    public Body<List<String>>getAllAllowedMethod(){
        return groupService.getAllAllowedMethod();
    }

    @PostMapping("/getAgent")
    public Body<Agent> getAgent(@RequestParam("agentId") String agentId){
        return groupService.getAgent(agentId);
    }

    @PostMapping("/getCenter")
    public Body<Center> getCenter(@RequestParam("centerId") String centerId){
        return groupService.getCenter(centerId);
    }

    @PostMapping("/getGroup")
    public Body<Group> getGroup(@RequestParam("groupId") Long groupId,
                                @RequestParam("agentId") String agentId,
                                @RequestParam("centerId") String centerId){
        return groupService.getGroup(groupId,agentId,centerId);
    }
    
}
