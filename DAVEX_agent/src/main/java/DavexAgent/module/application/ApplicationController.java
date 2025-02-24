package DavexAgent.module.application;

import DavexBase.common.Body;
import DavexBase.entity.Application;
import DavexBase.entity.FileRule;
import DavexBase.entity.Rule;
import DavexBase.service.application.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/application/management")
public class ApplicationController {
    @Autowired
    ApplicationService applicationService;

    @PostMapping("/addApplication")
    public Body<String> addApplication(@RequestBody Application application){

        return applicationService.addApplication(application);
    }

    @PostMapping("/deleteApplication")
    public Body<String> deleteApplication(@RequestParam("applicationId") String applicationId){
        return applicationService.deleteApplication(applicationId);
    }

    @PostMapping("/updateApplication")
    public Body<String> updateApplication(@RequestBody Application application){
        return applicationService.updateApplication(application);
    }

    @PostMapping("/getApplicationList")
    public Body<List<Application>> getApplicationList(){
        return applicationService.getApplicationList();
    }

    @PostMapping("/syncApplication")
    public Body<String> syncApplication(@RequestBody Application application, @RequestParam("target")String target){
        return applicationService.syncApplication(application,target);
    }


}
