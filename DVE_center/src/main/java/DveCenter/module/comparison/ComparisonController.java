package DveCenter.module.comparison;

import DveBase.common.R;
import DveBase.info.DirectoryInfo;
import DveBase.info.TableHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 定义接口路径
@RestController
@RequestMapping("/comparison")
public class ComparisonController {

    @Autowired
    private ComparisonService comparisonService;

    // center发起数据目录获取请求
    @PostMapping("/getDirectory")
    public R<DirectoryInfo> getDirectory(@RequestParam("applicationId") Integer applicationId,
                                         @RequestParam("agentId") Integer agentId) throws Exception {

        return comparisonService.getDirectory(applicationId, agentId);
    }

    // center请求表头信息
    @PostMapping("/getTableHeader")
    public R<TableHeader> getTableHeader(@RequestParam("agentId") Integer agentId,
                                               @RequestParam("fileId") Integer fileId,
                                               @RequestParam("folderId") Integer folderId) throws Exception {

        return comparisonService.getTableHeader(agentId, fileId, folderId);
    }

    // center选择属性并获取agent所有数据相应属性的哈希,与己方哈希进行比对
    @PostMapping("/compare")
    public R<Boolean> compare(@RequestParam("applicationId") Long applicationId,
                              @RequestParam("agentId") Integer agentId,
                              @RequestParam("fileId") Integer fileId,
                              @RequestParam("folderId") Integer folderId,
                              @RequestParam("attributes") List<String> attributes,
                              @RequestParam("values") List<String> values) throws Exception {

        return comparisonService.compare(applicationId, agentId, fileId, folderId, attributes, values);
    }
}
