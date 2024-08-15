package DavexAgent.module.comparison;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import DavexBase.common.Body;
import DavexBase.info.TableHeader;

// 定义接口路径
@RestController
@RequestMapping("/comparison")
public class ComparisonController {

    @Autowired
    private ComparisonService comparisonService;

    // agent解析csv文件表头
    @PostMapping("/getCsvHeader")
    public Body<TableHeader> getCsvHeader(@RequestParam("fileId") Integer fileId,
                                          @RequestParam("folderId") Integer folderId,
                                          @RequestParam("agentId") Integer agentId) {

        return comparisonService.getCsvHeader(fileId, folderId, agentId);
    }

    // agent计算哈希
    @PostMapping("/getHash")
    public Body<List<String>> getHash(@RequestParam("fileId") Integer fileId,
                                      @RequestParam("folderId") Integer folderId,
                                      @RequestParam("agentId") Integer agentId,
                                      @RequestParam("attributes") List<String> attributes) {

        return comparisonService.getHash(fileId, folderId, agentId, attributes);
    }
}
