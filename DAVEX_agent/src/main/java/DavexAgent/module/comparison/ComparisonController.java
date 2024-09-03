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
    public Body<TableHeader> getCsvHeader(@RequestParam("fileId") Long fileId,
                                          @RequestParam("folderId") Long folderId,
                                          @RequestParam("agentId") Long agentId) {

        return comparisonService.getCsvHeader(fileId, folderId, agentId);
    }

    // agent计算哈希
    @PostMapping("/getHash")
    public Body<List<String>> getHash(@RequestParam("fileId") Long fileId,
                                      @RequestParam("folderId") Long folderId,
                                      @RequestParam("agentId") Long agentId,
                                      @RequestParam("attributes") List<String> attributes) {

        return comparisonService.getHash(fileId, folderId, agentId, attributes);
    }

    // 文件名
    @PostMapping("/getFileName")
    public Body<String> getFileName(@RequestParam("fileId") Long fileId,
                                          @RequestParam("folderId") Long folderId,
                                          @RequestParam("agentId") Long agentId) {

        return comparisonService.getFileName(fileId, folderId, agentId);
    }
}
