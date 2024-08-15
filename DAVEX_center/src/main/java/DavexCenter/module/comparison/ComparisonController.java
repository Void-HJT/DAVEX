package DavexCenter.module.comparison;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import DavexBase.common.Body;
import DavexBase.info.DirectoryInfo;
import DavexBase.info.TableHeader;
import org.springframework.web.multipart.MultipartFile;

// 定义接口路径
@RestController
@RequestMapping("/comparison")
public class ComparisonController {

    @Autowired
    private ComparisonService comparisonService;

    // center发起数据目录获取请求
    @PostMapping("/getDirectory")
    public Body<DirectoryInfo> getDirectory(@RequestParam("applicationId") Long applicationId,
                                            @RequestParam("agentId") Long agentId) throws Exception {

        return comparisonService.getDirectory(applicationId, agentId);
    }

    // center请求表头信息
    @PostMapping("/getTableHeader")
    public Body<TableHeader> getTableHeader(@RequestParam("agentId") Long agentId,
                                            @RequestParam("fileId") Long fileId,
                                            @RequestParam("folderId") Long folderId) throws Exception {

        return comparisonService.getTableHeader(agentId, fileId, folderId);
    }

    // center选择属性并获取agent所有数据相应属性的哈希,与己方哈希进行比对
    @PostMapping("/compare")
    public Body<List<Boolean>> compare(@RequestParam("applicationId") Long applicationId,
                                       @RequestParam("agentId") Long agentId,
                                       @RequestParam("fileId") Long fileId,
                                       @RequestParam("folderId") Long folderId,
                                       @RequestParam("attributes") List<String> attributes,
                                       @RequestParam("valuesList") List<List<String>> valuesList) throws Exception {

        return comparisonService.compare(applicationId, agentId, fileId, folderId, attributes, valuesList);
    }

    @PostMapping("/compareFromCsv")
    public Body<List<Boolean>> compareFromCsv(@RequestParam("applicationId") Long applicationId,
                                              @RequestParam("agentId") Long agentId,
                                              @RequestParam("fileId") Long fileId,
                                              @RequestParam("folderId") Long folderId,
                                              @RequestPart("file") MultipartFile file) throws Exception {

        return comparisonService.compareFromCsv(applicationId, agentId, fileId, folderId, file);
    }
}
