package DveAgent.module.directory;


import DveAgent.common.Body;
import DveAgent.entity.Application;
import DveAgent.entity.Group;
import DveAgent.entity.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // @RestController的作用等同于@Controller + @ResponseBody。
// 相当于@Controller+@ResponseBody两个注解的结合，返回json数据不需要在方法前面加@ResponseBody注解了，但使用@RestController这个注解，就不能返回jsp,html页面，视图解析器无法解析jsp,html页面
@RequestMapping("/directory/group")
public class GroupController {

    @Autowired
    GroupService  groupService;


    @PostMapping("/getApplication")
    public Body<List<Application>> getApplication(){
        return groupService.getApplication();

    }
    @PostMapping("/getGroup")
    public Body<List<Group>> getGroup(@RequestParam("agentId") Long agentId,
                                      @RequestParam("centerId") Long centerId){
        return groupService.getGroup(agentId,centerId);
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
                                            @RequestParam("applicationGroupId") Long applicationGroupId){
        return groupService.deleteApplicationGroup(agentId,centerId,applicationGroupId);

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
                                @RequestParam("ruleId") Long ruleId){
        return groupService.deleteRule(agentId,ruleId);
    }
    
}
