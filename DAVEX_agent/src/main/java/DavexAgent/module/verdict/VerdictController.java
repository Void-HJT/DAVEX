package DavexAgent.module.verdict;

import DavexBase.common.Body;
import DavexBase.info.VerdictFilterDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/verdict")
public class VerdictController {
    @Autowired
    VerdictService verdictService;

    /**
     * 按条件筛选判决书文件ID
     * 筛选逻辑：
     * 1. 基础条件：judgeTime（判决时间）不为空
     * 2. 可选条件：时间段（judgeTimeStart~judgeTimeEnd）、判决类型（模糊包含）、判决地点（模糊包含）、案由（模糊包含）
     * 3. 所有可选条件均为“且”逻辑，未传则不参与筛选
     *
     * @param filterDTO 筛选条件（所有字段非必填）
     * @return 符合条件的fileId列表
     */
    @PostMapping("/getDestIds")
    public Body<List<String>> getDestIds(@RequestBody(required = false) VerdictFilterDTO filterDTO) {
        // 若前端未传筛选条件（body为空），则传入null（Service层处理为“无筛选”）
        return verdictService.getDestIds(filterDTO);
    }

    @PostMapping("/getDataEmbeddings")
    public Body<String> getDataEmbeddings(@RequestBody List<String> fileIds) {
        return verdictService.getDataEmbeddings(fileIds);
    }
}
