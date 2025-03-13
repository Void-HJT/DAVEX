package DavexCenter.module.query.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import DavexBase.common.Body;
import DavexBase.entity.OutsideDatabase;
import DavexBase.entity.OutsideDatabaseTable;
import DavexBase.info.QueryRequest;
import DavexBase.service.query.DatabaseService;

@RestController // @RestController的作用等同于@Controller + @ResponseBody。
// 相当于@Controller+@ResponseBody两个注解的结合，返回json数据不需要在方法前面加@ResponseBody注解了，但使用@RestController这个注解，就不能返回jsp,html页面，视图解析器无法解析jsp,html页面
@RequestMapping("/query/database")
public class DatabaseController {
    @Autowired
    DatabaseService databaseService;

    @PostMapping("/addDatabase")
    public Body<String> addDatabase(@RequestBody OutsideDatabase outsideDatabase) {
        return databaseService.addDatabase(outsideDatabase);
    }

    @PostMapping("/getDatabase")
    public Body<List<OutsideDatabase>> getDatabase() {
        return databaseService.getDatabase();
    }

    @PostMapping("/getTable")
    public Body<List<OutsideDatabaseTable>> getTable(@RequestParam("databaseId") Long databaseId) {
        return databaseService.getTable(databaseId);
    }

    @PostMapping("/locateQuery")
    public Body<byte[]> executeQuery(@RequestBody QueryRequest request,
                                     @RequestParam("databaseId") Long databaseId,
                                     @RequestParam(value = "chainMaker", defaultValue = "false") Boolean chainMaker,
                                     @RequestParam(value = "requestHash", defaultValue = "defaultHash") String requestHash,
                                     @RequestParam(value = "requestId", defaultValue = "defaultId") String requestId) throws Exception {
        return databaseService.executeQuery(request, databaseId,chainMaker,requestHash,requestId);
    }

    @PostMapping("/query2Agent")
    public Body<String> query2Agent(@RequestBody QueryRequest request,
            @RequestParam("applicationId") String applicationId,
            @RequestParam("agentId") String agentId,
            @RequestParam("databaseId") Long databaseId) {
        return databaseService.query2Agent(request, applicationId, agentId, databaseId);
    }

}
