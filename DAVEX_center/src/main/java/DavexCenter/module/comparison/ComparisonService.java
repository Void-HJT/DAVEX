package DavexCenter.module.comparison;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import DavexBase.common.Body;
import DavexBase.info.DirectoryInfo;
import DavexBase.info.TableHeader;
import DavexCenter.common.CustomMultipartFile;
import DavexBase.service.auth.CenterWebClientService;
import DavexCenter.module.file.service.ComparisonFileService;
import DavexCenter.module.file.service.FileService;

@Service
public class ComparisonService {

        @Autowired
        private CenterWebClientService centerWebClientService;

        @Autowired
        private FileService fileService;

        @Autowired
        private ComparisonFileService comparisonFileService;

        @Value("${file.upload-base-dir}")
        private String uploadBaseDir;

        public Body<DirectoryInfo> getDirectory(Long applicationId, Long agentId) throws Exception {

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

        public Body<TableHeader> getTableHeader(Long agentId, Long fileId, Long folderId) throws Exception {

                WebClient webclient = centerWebClientService.center2AgentWebClient(agentId);
                TableHeader tableHeader = webclient.post()
                                .uri(uriBuilder -> uriBuilder.path("/comparison/getCsvHeader")
                                                .queryParam("fileId", fileId)
                                                .queryParam("folderId", folderId)
                                                .queryParam("agentId", agentId).build())
                                .retrieve()
                                .bodyToMono(new ParameterizedTypeReference<Body<TableHeader>>() {
                                }).block().getData();

                return Body.success(tableHeader, "获取成功");
        }

        public Body<List<Boolean>> compare(Long applicationId, Long agentId, Long fileId, Long folderId,
                        List<String> attributes, List<List<String>> valuesList) throws Exception {

                WebClient webclient = centerWebClientService.center2AgentWebClient(agentId);
                List<String> dataHash = webclient.post()
                                .uri(uriBuilder -> uriBuilder.path("/comparison/getHash")
                                                .queryParam("fileId", fileId)
                                                .queryParam("folderId", folderId)
                                                .queryParam("agentId", agentId)
                                                .queryParam("attributes", attributes).build())
                                .retrieve()
                                .bodyToMono(new ParameterizedTypeReference<Body<List<String>>>() {
                                }).block().getData();
                if (dataHash == null) {
                        return Body.error("属性输入有误");
                }

                // 存储每条数据的比对结果
                List<Boolean> comparisonResults = new ArrayList<>();
                List<Map<String, Object>> jsonResults = new ArrayList<>();

                for (List<String> values : valuesList) {
                        // 计算给定 values 的哈希值
                        String delimiter = "|";  // 使用相同的分隔符
                        StringBuilder sb = new StringBuilder();
//                        System.out.println("Attributes size: " + attributes.size());
//                        System.out.println("Values size: " + values.size());

                        for (int i = 0; i < attributes.size(); i++) {
                                if (i > 0) {
                                        sb.append(delimiter);  // 追加分隔符
                                }
                                if (i < values.size()) {
                                        sb.append(values.get(i));
                                } else {
                                        // 处理错误情况
//                                        throw new IndexOutOfBoundsException("值列表的长度小于属性列表的长度");
                                        return Body.error("输入格式有误");
                                }
                        }

                        String computedHash = DigestUtils.sha256Hex(sb.toString());

                        // 检查计算出的哈希值是否在 dataHash 中
                        boolean exists = dataHash.contains(computedHash);

                        // 添加比对结果
                        comparisonResults.add(exists);

                        // 将每个比对的结果添加到 JSON 结果列表中
                        Map<String, Object> result = new HashMap<>();
                        result.put("exists", exists);
                        result.put("computedHash", computedHash);
                        result.put("attributes", attributes);
                        result.put("values", values);

                        jsonResults.add(result);
                }

                // 将所有比对结果存入一个 JSON 文件
                ObjectMapper objectMapper = new ObjectMapper();
                byte[] jsonBytes = objectMapper.writeValueAsBytes(jsonResults);

                // 使用下划线拼接属性名生成文件名
                String fileName = String.join("_", attributes) + ".json";
                MultipartFile file = new CustomMultipartFile(jsonBytes, fileName);

                // 调用 saveComparisonFile 方法
                comparisonFileService.saveComparison(file, fileService.getSha256(file), applicationId,
                        uploadBaseDir, Timestamp.valueOf(LocalDateTime.now().plusWeeks(1)));

                return Body.success(comparisonResults, "比对成功");
        }

        public Body<List<Boolean>> compareFromCsv(Long applicationId, Long agentId, Long fileId, Long folderId, MultipartFile file) throws Exception {

                // 解析 CSV 文件
                List<String> attributes = new ArrayList<>();
                List<List<String>> valuesList = new ArrayList<>();

                // 指定文件编码格式
                Charset charset = Charset.forName("GB2312"); // 可以替换为 StandardCharsets.UTF_8

                // 解析 CSV 文件
                try (Reader reader = new InputStreamReader(file.getInputStream(), charset);
                     CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

                        for (CSVRecord record : csvParser) {
                                if (attributes.isEmpty()) {
                                        attributes.addAll(record.toMap().keySet());
                                }
                                valuesList.add(new ArrayList<>(record.toMap().values()));
                        }
                }
                System.out.println(attributes);
                System.out.println(valuesList);

                // 调用扩展后的 compare 方法进行多条数据的比对
                return compare(applicationId, agentId, fileId, folderId, attributes, valuesList);
        }
}
