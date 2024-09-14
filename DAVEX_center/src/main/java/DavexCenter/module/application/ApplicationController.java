package DavexCenter.module.application;


import DavexBase.common.Body;
import DavexBase.entity.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

}
