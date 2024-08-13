package DavexCenter.module.task.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/MpcTasksOutput")
public class MpcTaskOutputController {

    @GetMapping("/select")
    public String getMethodName(@RequestParam String param) {
        return new String();
    }
    

}
