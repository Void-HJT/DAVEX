package DveAgent.module.comparison;

import DveBase.common.R;
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

    // agent解析csv文件表头
    @PostMapping("/getcsvheader")
    public R<TableHeader> getcsvheader(@RequestParam("fileId") Integer fileId,
                                       @RequestParam("folderId") Integer folderId,
                                             @RequestParam("agentId") Integer agentId) {

        return comparisonService.getCsvHeader(fileId, folderId, agentId);
    }

    // agent计算哈希
//    @PostMapping("/gethash")
//    public R<List<String>> gethash(@RequestParam Integer seed,
//                                   @RequestParam List<String> attribute) {
//
//        return comparisonService.gethash(seed, attribute);
//    }
}
