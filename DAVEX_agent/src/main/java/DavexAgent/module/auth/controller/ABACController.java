package DavexAgent.module.auth.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import DavexBase.service.auth.ABACService;
import DavexBase.entity.Application;
import DavexBase.entity.File;
import DavexBase.entity.Folder;
import DavexBase.mapper.ApplicationMapper;
import DavexBase.mapper.FileMapper;
import DavexBase.mapper.FolderMapper;

@RestController
@RequestMapping("/abac")
public class ABACController {
    @Autowired
    private ABACService abacService;
    @Autowired
    private ApplicationMapper applicationMapper;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private FolderMapper folderMapper;

    @GetMapping("/file_test")
    public List<Boolean> fileTest(@RequestParam String appid, @RequestParam String fid, @RequestParam String action) {
        Application app = applicationMapper.selectById(appid);
        File file = fileMapper.selectById(fid);
        return abacService.fileTest(app, file, action);
    }

    @GetMapping("/folder_test")
    public List<Boolean> folderTest(@RequestParam String appid, @RequestParam String fid) {
        Application app = applicationMapper.selectById(appid);
        Folder folder = folderMapper.selectById(fid);
        return abacService.folderTest(app, folder);
    }

}
