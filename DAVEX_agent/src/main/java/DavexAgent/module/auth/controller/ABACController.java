package DavexAgent.module.auth.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import DavexAgent.module.auth.service.ABACService;
import DavexBase.entity.Application;
import DavexBase.entity.File;
import DavexBase.mapper.ApplicationMapper;
import DavexBase.mapper.FileMapper;

@RestController
@RequestMapping("/abac")
public class ABACController {
    @Autowired
    private ABACService abacService;
    @Autowired
    private ApplicationMapper applicationMapper;
    @Autowired
    private FileMapper fileMapper;

    @GetMapping("/test")
    public List<Boolean> test(@RequestParam Long appid, @RequestParam Long fid, @RequestParam String action) {
        Application app = applicationMapper.selectById(appid);
        File file = fileMapper.selectById(fid);
        return abacService.test(app, file, action);
    }

}
