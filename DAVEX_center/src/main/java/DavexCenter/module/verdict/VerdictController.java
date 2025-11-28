package DavexCenter.module.verdict;

import DavexBase.common.Body;
import DavexBase.info.VerdictFilterDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/verdict")
public class VerdictController {
    @Autowired
    VerdictService verdictService;

    /**
     * Center端发送查询条件到Agent端，调用Agent的query2Embeddings接口生成Embedding
     * @param filterDTO 筛选条件（同Agent端接口参数）
     * @return Agent端返回的pkl文件路径或错误信息
     */
    @PostMapping("/sendQuery")
    public Body<String> sendQuery(@RequestParam String agentId,
            @RequestBody(required = false) VerdictFilterDTO filterDTO) {
        return verdictService.sendQuery(agentId, filterDTO);
    }
}
