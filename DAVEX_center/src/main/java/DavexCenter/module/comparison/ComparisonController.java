package DavexCenter.module.comparison;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    // center请求表头信息
    @PostMapping("/getTableHeader")
    public Body<TableHeader> getTableHeader(@RequestParam("agentId") String agentId,
                                            @RequestParam("fileId") String fileId,
                                            @RequestParam("folderId") String folderId) throws Exception {

        return comparisonService.getTableHeader(agentId, fileId, folderId);
    }

    // center请求txt第一行
    @PostMapping("/getTXTExample")
    public Body<List<String>> getTXTExample(@RequestParam("agentId") String agentId,
                                            @RequestParam("fileId") String fileId,
                                            @RequestParam("folderId") String folderId) throws Exception {

        return comparisonService.getTXTExample(agentId, fileId, folderId);
    }

    // center选择属性并获取agent所有数据相应属性的哈希,与己方哈希进行比对
    @PostMapping("/compare")
    public Body<List<Boolean>> compare(@RequestParam("applicationId") String applicationId,
                                       @RequestParam("agentId") String agentId,
                                       @RequestParam("fileId") String fileId,
                                       @RequestParam("folderId") String folderId,
                                       @RequestParam("attributes") List<String> attributes,
                                       @RequestParam("valuesList") List<List<String>> valuesList) throws Exception {

        return comparisonService.compare(applicationId, agentId, fileId, folderId, attributes, valuesList);
    }

    @PostMapping("/compareFromCsv")
    public Body<List<Boolean>> compareFromCsv(@RequestParam("applicationId") String applicationId,
                                              @RequestParam("agentId") String agentId,
                                              @RequestParam("fileId") String fileId,
                                              @RequestParam("folderId") String folderId,
                                              @RequestPart("file") MultipartFile file) throws Exception {

        return comparisonService.compareFromCsv(applicationId, agentId, fileId, folderId, file);
    }

    // 处理前端传值问题
    @PostMapping("/compareFromJson")
    public Body<List<Boolean>> compareFromJson(
            @RequestParam("applicationId") String applicationId,
            @RequestParam("agentId") String agentId,
            @RequestParam("fileId") String fileId,
            @RequestParam("folderId") String folderId,
            @RequestParam("attributes") String attributesJson,
            @RequestParam("valuesList") String valuesListJson
    ) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // 将 JSON 字符串转换为 Java List
        List<String> attributes = objectMapper.readValue(attributesJson, new TypeReference<List<String>>(){});
        List<List<String>> valuesList = objectMapper.readValue(valuesListJson, new TypeReference<List<List<String>>>(){});
        System.out.println("ApplicationId: " + applicationId);
        System.out.println("AgentId: " + agentId);
        System.out.println("FileId: " + fileId);
        System.out.println("FolderId: " + folderId);
        System.out.println("Attributes: " + attributes);
        System.out.println("ValuesList: " + valuesList);

        return comparisonService.compare(applicationId, agentId, fileId, folderId, attributes, valuesList);
    }

    @PostMapping("/compareFromTXT")
    public Body<List<Boolean>> compareFromTXT(@RequestParam("applicationId") String applicationId,
                                              @RequestParam("agentId") String agentId,
                                              @RequestParam("fileId") String fileId,
                                              @RequestParam("folderId") String folderId,
                                              @RequestPart("file") MultipartFile file) throws Exception {

        return comparisonService.compareFromTXT(applicationId, agentId, fileId, folderId, file);
    }

    // 处理前端传值问题
    @PostMapping("/compareTXTFromJson")
    public Body<List<Boolean>> compareTXTFromJson(
            @RequestParam("applicationId") String applicationId,
            @RequestParam("agentId") String agentId,
            @RequestParam("fileId") String fileId,
            @RequestParam("folderId") String folderId,
            @RequestParam("valuesList") String valuesListJson
    ) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // 将 JSON 字符串转换为 Java List
        List<List<String>> valuesList = objectMapper.readValue(valuesListJson, new TypeReference<List<List<String>>>(){});
        System.out.println("ApplicationId: " + applicationId);
        System.out.println("AgentId: " + agentId);
        System.out.println("FileId: " + fileId);
        System.out.println("FolderId: " + folderId);
        System.out.println("ValuesList: " + valuesList);

        return comparisonService.compareTXT(applicationId, agentId, fileId, folderId, valuesList);
    }

//    @PostMapping("/test")
//    public String test() throws Exception {
//
//        return comparisonService.test();
//    }
}
