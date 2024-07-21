package DveAgent.module.task.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import DveAgent.entity.Center;
import DveAgent.module.task.service.CenterService;
import java.util.Optional;

@RestController
@RequestMapping("/centers")
public class CenterController {

    @Autowired
    private CenterService centerService;

    @PostMapping
    public Center createCenter(@RequestBody Center center) {
        return centerService.createCenter(center);
    }
}