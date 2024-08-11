package DveCenter.module.comparison;

import DveBase.common.Body;
import DveBase.common.R;
import DveBase.info.DirectoryInfo;
import DveBase.info.TableHeader;
import DveCenter.common.CustomMultipartFile;
import DveCenter.entity.ComparisonOutput;
import DveCenter.mapper.ComparisonOutputMapper;
import DveCenter.module.auth.service.CenterWebClientService;
import DveCenter.module.file.service.ComparisonFileService;
import DveCenter.module.file.service.FileService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ComparisonService {

    @Autowired
    private CenterWebClientService centerWebClientService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ComparisonFileService comparisonFileService;

    @Autowired
    private ComparisonOutputMapper comparisonOutputMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${file.upload-base-dir}")
    private String uploadBaseDir;

    public Body<DirectoryInfo> getDirectory(Integer applicationId, Integer agentId) throws Exception {

        WebClient webclient = centerWebClientService.center2AgentWebClient(agentId);
        DirectoryInfo directoryInfo = webclient.post()
                .uri(uriBuilder -> uriBuilder.path("/directory/fileFolder/getDirectoryByApplication")
                        .queryParam("rootId", 1)
                        .queryParam("agentId", agentId)
                        .queryParam("applicationId", applicationId).build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Body<DirectoryInfo>>() {
                }).block().getData();

        return Body.success(directoryInfo, "获取数据目录成功");
    }

    public Body<TableHeader> getTableHeader(Integer agentId, Integer fileId, Integer folderId) throws Exception {

        WebClient webclient = centerWebClientService.center2AgentWebClient(agentId);
        TableHeader tableHeader = webclient.post()
                .uri(uriBuilder -> uriBuilder.path("/comparison/getcsvheader")
                        .queryParam("fileId", fileId)
                        .queryParam("folderId", folderId)
                        .queryParam("agentId", agentId).build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Body<TableHeader>>() {
                }).block().getData();


        return Body.success(tableHeader, "获取成功");
    }

    public Body<Boolean> compare(Long applicationId, Integer agentId, Integer fileId, Integer folderId,
                                         List<String> attributes, List<String> values) throws Exception {

        WebClient webclient = centerWebClientService.center2AgentWebClient(agentId);
        List<String> dataHash = webclient.post()
                .uri(uriBuilder -> uriBuilder.path("/comparison/gethash")
                        .queryParam("fileId", fileId)
                        .queryParam("folderId", folderId)
                        .queryParam("agentId", agentId)
                        .queryParam("attributes", attributes).build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Body<List<String>>>() {
                }).block().getData();

        // 计算给定 values 的哈希值
        String delimiter = "|";  // 使用相同的分隔符

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < attributes.size(); i++) {
            if (i > 0) {
                sb.append(delimiter);  // 追加分隔符
            }
            sb.append(values.get(i));
        }

        String computedHash = DigestUtils.sha256Hex(sb.toString());

        // 检查计算出的哈希值是否在 dataHash 中
        boolean exists = dataHash.contains(computedHash);

        // 将结果存入一个 JSON 文件
        Map<String, Object> result = new HashMap<>();
        result.put("exists", exists);
        result.put("computedHash", computedHash);
        result.put("attributes", attributes);
        result.put("values", values);

        ObjectMapper objectMapper = new ObjectMapper();
        byte[] jsonBytes = objectMapper.writeValueAsBytes(result);

        // 使用下划线拼接 values 生成文件名
        String fileName = String.join("_", values) + ".json";
        // 使用 CustomMultipartFile 创建 MultipartFile
        MultipartFile file = new CustomMultipartFile(jsonBytes, fileName);

        // 调用 saveComparisonFile 方法
        comparisonFileService.saveComparisonFile(file, fileService.getSha256(file), applicationId, uploadBaseDir,
                Timestamp.valueOf(LocalDateTime.now().plusWeeks(1)));

        return Body.success(exists, "比对成功");
    }
}
