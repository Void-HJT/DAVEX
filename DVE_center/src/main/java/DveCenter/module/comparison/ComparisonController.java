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
    @PostMapping("/getdirectory")
    public R<DirectoryInfo> getdirectory(@RequestParam("applicationId") Integer applicationId,
                                         @RequestParam("agentId") Integer agentId) throws Exception {

        return comparisonService.getDirectory(applicationId, agentId);
    }

    // center请求表头信息
    @PostMapping("/gettableheader")
    public R<TableHeader> gettableheader(@RequestParam("applicationId") Integer applicationId,
                                               @RequestParam("agentId") Integer agentId,
                                               @RequestParam("fileId") Integer fileId,
                                               @RequestParam("folderId") Integer folderId) throws Exception {

        return comparisonService.getTableHeader(applicationId, agentId, fileId, folderId);
    }

    // center选择属性、生成随机数并获取agent所有数据相应属性的哈希
//    @PostMapping("/gethash")
}
