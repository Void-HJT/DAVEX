package DavexCenter.module.verdict;

import DavexBase.common.Body;
import DavexBase.info.VerdictFilterDTO;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public Body<String> sendQuery(@RequestParam("agentId") String agentId,
            @RequestBody(required = false) VerdictFilterDTO filterDTO) {
        return verdictService.sendQuery(agentId, filterDTO);
    }

    @PostMapping("/computeInput")
    public Body<String> computeInput(@RequestPart("inputFile") MultipartFile inputFile,
                                     @RequestParam("pklPath") String pklPath,
                                     @RequestParam("baseFileName") String baseFileName,
                                     @RequestParam("taskId") Long taskId) {
        return verdictService.computeInput(inputFile, pklPath, baseFileName, taskId);
    }

    @PostMapping("/sendAndCompute")
    public Body<String> sendAndCompute(@RequestParam("agentId") String agentId,
                                       @RequestParam String filterParams,
                                       @RequestPart("inputFile") MultipartFile inputFile) {

        // 将JSON字符串解析为VerdictFilterDTO对象
        VerdictFilterDTO filterDTO = JSON.parseObject(filterParams, VerdictFilterDTO.class);

        return verdictService.sendAndCompute(agentId, filterDTO, inputFile);
    }
}
