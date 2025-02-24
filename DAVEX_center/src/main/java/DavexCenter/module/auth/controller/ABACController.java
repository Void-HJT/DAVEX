package DavexCenter.module.auth.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import DavexBase.common.Body;
import DavexBase.entity.Application;
import DavexBase.entity.File;
import DavexBase.entity.FileRule;
import DavexBase.entity.Folder;
import DavexBase.entity.FolderVisibility;
import DavexBase.entity.Rule;
import DavexBase.entity.Visibility;
import DavexBase.info.RelationshipInfo;
import DavexBase.info.RuleOrVisibility;
import DavexBase.mapper.ApplicationMapper;
import DavexBase.mapper.FileMapper;
import DavexBase.mapper.FolderMapper;
import DavexBase.service.auth.ABACService;

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

    @PostMapping("/getRules")
    public Body<List<Rule>> getRules() {
        return abacService.getRules();
    }

    @PostMapping("/createRule")
    public Body<String> createRule(@RequestBody RuleOrVisibility rov) {
        return abacService.createRule(rov.getExpression(), rov.getDescription());
    }

    @PostMapping("/updateRule")
    public Body<String> updateRule(@RequestBody Rule rule) {
        return abacService.updateRule(rule.getUid(), rule.getExpression(), rule.getDescription());
    }

    @PostMapping("/deleteRule")
    public Body<String> deleteRule(@RequestBody Rule rule) {
        return abacService.deleteRule(rule.getUid());
    }

    @PostMapping("/getFileRule")
    public Body<List<Rule>> getFileRules(@RequestBody String fid) {
        return abacService.getFileRule(fid);
    }

    @PostMapping("/createFileRule")
    public Body<String> createFileRule(@RequestBody RelationshipInfo relation) {
        return abacService.createFileRule(relation.getIdA(), relation.getIdB());
    }

    @PostMapping("/updateFileRule")
    public Body<String> updateFileRule(@RequestBody FileRule fileRule) {
        return abacService.updateFileRule(fileRule.getUid(), fileRule.getFileId(), fileRule.getRuleId());
    }

    @PostMapping("/deleteFileRule")
    public Body<String> deleteFileRule(@RequestBody String fileRuleId) {
        return abacService.deleteFileRule(fileRuleId);
    }

    @PostMapping("/getVisibilities")
    public Body<List<Visibility>> getVisibilities() {
        return abacService.getVisibilities();
    }

    @PostMapping("/createVisibility")
    public Body<String> createVisibility(@RequestBody RuleOrVisibility rov) {
        return abacService.createVisibility(rov.getExpression(), rov.getDescription());
    }

    @PostMapping("/updateVisibility")
    public Body<String> updateVisibility(@RequestBody Visibility visibility) {
        return abacService.updateVisibility(visibility.getUid(), visibility.getExpression(),
                visibility.getDescription());
    }

    @PostMapping("/deleteVisibility")
    public Body<String> deleteVisibility(@RequestBody Visibility visibility) {
        return abacService.deleteVisibility(visibility.getUid());
    }

    @PostMapping("/getFolderVisibility")
    public Body<List<Visibility>> getFolderVisibility(@RequestBody String fid) {
        return abacService.getFolderVisibility(fid);
    }

    @PostMapping("/createFolderVisibility")
    public Body<String> createFolderVisibility(@RequestBody RelationshipInfo relation) {
        return abacService.createFolderVisibility(relation.getIdA(), relation.getIdB());
    }

    @PostMapping("/updateFolderVisibility")
    public Body<String> updateFolderVisibility(@RequestBody FolderVisibility folderVisibility) {
        return abacService.updateFolderVisibility(folderVisibility.getUid(), folderVisibility.getFolderId(),
                folderVisibility.getVisibilityId());
    }

    @PostMapping("/deleteFolderVisibility")
    public Body<String> deleteFolderVisibility(@RequestBody String folderVisibilityId) {
        return abacService.deleteFolderVisibility(folderVisibilityId);
    }

    @PostMapping("/syncRule")
    public Body<String> syncRule(@RequestBody Rule rule,@RequestParam("target")String target){
        return abacService.syncRule(rule,target);
    }

    @PostMapping("/syncFileRule")
    public Body<String> syncFileRule(@RequestBody FileRule fileRule,@RequestParam("target")String target){
        return abacService.syncFileRule(fileRule,target);
    }

}
