package DavexAgent.module.verdict;

import DavexBase.common.Body;
import DavexBase.info.VerdictFilterDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/verdict")
public class VerdictController {

    private static final Logger logger = LoggerFactory.getLogger(VerdictController.class);

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
    public Body<String> getDataEmbeddings(@RequestBody List<String> fileIds,
                                          @RequestParam("outputPrefix") String outputPrefix) {
        return verdictService.getDataEmbeddings(fileIds, outputPrefix);
    }

    /**
     * 一站式服务：筛选文件+生成Embedding，直接返回pkl文件流
     * @param filterDTO 筛选条件
     * @return ResponseEntity<Resource>：data返回文件流，响应头返回文件路径和状态码
     */
    @PostMapping("/query2Embeddings")
    public ResponseEntity<Resource> query2Embeddings(@RequestBody(required = false) VerdictFilterDTO filterDTO,
                                                     @RequestParam("outputPrefix") String outputPrefix) {
        try {
            // 调用Service层方法，获取pkl文件路径（Service层返回格式不变，仅复用原有逻辑）
            Body<String> serviceResult = verdictService.query2Embeddings(filterDTO, outputPrefix);

            // 1. 处理失败场景（沿用原有错误码和提示）
            if (serviceResult.getCode() != 1 || serviceResult.getData() == null) {
                HttpHeaders errorHeaders = new HttpHeaders();
                // 响应头传递错误信息（对应原message）
                errorHeaders.add("X-Message", URLEncoder.encode(serviceResult.getMessage(), StandardCharsets.UTF_8));
                errorHeaders.add("X-Code", String.valueOf(serviceResult.getCode()));
                // 返回空资源+错误状态码
                return new ResponseEntity<>(errorHeaders, HttpStatus.BAD_REQUEST);
            }

            // 2. 处理成功场景：读取pkl文件，返回二进制流
            String pklFilePath = serviceResult.getData(); // Service返回的pkl文件绝对路径
            java.io.File pklFile = new java.io.File(pklFilePath);
            Resource resource = new FileSystemResource(pklFile);

            // 3. 构建响应头：传递文件路径（X-Message）、状态码（X-Code）、下载相关信息
            HttpHeaders headers = new HttpHeaders();
            // 自定义响应头：返回文件路径（对应需求的message字段）
            headers.add("X-Message", URLEncoder.encode(pklFilePath, StandardCharsets.UTF_8));
            // 自定义响应头：返回成功状态码（1=成功）
            headers.add("X-Code", "1");
            // 下载相关头信息（可选：让浏览器直接下载文件，而非预览）
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + URLEncoder.encode(pklFile.getName(), StandardCharsets.UTF_8) + "\"");
            // 设置文件类型（pkl为二进制文件，用application/octet-stream）
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            // 设置文件大小
            headers.setContentLength(pklFile.length());

            // 4. 返回文件流+响应头+200状态码
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);

        } catch (Exception e) {
            // 处理系统异常
            HttpHeaders exceptionHeaders = new HttpHeaders();
            exceptionHeaders.add("X-Message", URLEncoder.encode("系统内部错误：" + e.getMessage(), StandardCharsets.UTF_8));
            exceptionHeaders.add("X-Code", "0");
            return new ResponseEntity<>(exceptionHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 执行P1离线阶段命令
     */
    @PostMapping("/executeP1Offline")
    public Body<String> executeP1Offline(@RequestBody Map<String, String> params) {
        String garnetDir = params.get("garnetDir");
        String dataDir = params.get("dataDir");
        String dataset = params.get("dataset");
        String clusters = params.get("clusters");

        String cmd = String.format(
                "%s/ann-party-offline.x --data-dir %s --dataset %s --clusters %s",
                garnetDir, dataDir, dataset, clusters
        );
        return verdictService.executeLocalCommand(cmd, "P1离线阶段");
    }

    /**
     * 获取P1服务器文件
     */
    @GetMapping("/getP1File")
    public ResponseEntity<Resource> getP1File(@RequestParam("filePath") String filePath) {
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * 生成SSL证书
     */
    @PostMapping("/generateSSL")
    public Body<String> generateSSL(@RequestBody Map<String, String> params) {
        String garnetDir = params.get("garnetDir");
        String cmd = String.format("cd %s && ./Scripts/setup-ssl.sh 2", garnetDir);
        return verdictService.executeLocalCommand(cmd, "SSL证书生成");
    }

    /**
     * 新增：获取P1服务器的文件/目录（目录会自动压缩为ZIP）
     */
    @GetMapping("/getP1FileOrDir")
    public ResponseEntity<Resource> getP1FileOrDir(@RequestParam("filePath") String filePath) throws IOException {
        java.io.File target = new java.io.File(filePath);
        if (!target.exists()) {
            return ResponseEntity.notFound().build();
        }

        // 如果是目录：压缩为ZIP后返回
        if (target.isDirectory()) {
            // 创建临时ZIP文件
            String zipFileName = target.getName() + ".zip";
            java.io.File zipFile = new java.io.File(target.getParent(), zipFileName);
            verdictService.zipDirectory(target, zipFile);

            // 返回ZIP文件流
            Resource resource = new FileSystemResource(zipFile);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } else {
            // 如果是文件：直接返回（复用原有逻辑）
            Resource resource = new FileSystemResource(target);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        }
    }

    /**
     * 新增：列出P1服务器指定目录下的证书文件（pem/key）
     */
    @GetMapping("/listSSLCerts")
    public Body<List<String>> listSSLCerts(@RequestParam("certDir") String certDir) {
        try {
            java.io.File dir = new java.io.File(certDir);
            if (!dir.exists() || !dir.isDirectory()) {
                return Body.error("证书目录不存在或不是目录：" + certDir);
            }

            // 列出所有pem/key文件
            List<String> certFiles = new ArrayList<>();
            java.io.File[] files = dir.listFiles((d, name) -> name.endsWith(".pem") || name.endsWith(".key"));
            if (files != null) {
                for (java.io.File file : files) {
                    certFiles.add(file.getAbsolutePath());
                }
            }

            return Body.success(certFiles, "获取证书文件列表成功");
        } catch (Exception e) {
            logger.error("列出证书文件失败", e);
            return Body.error("列出证书文件异常：" + e.getMessage());
        }
    }

    /**
     * 新增：执行P1在线阶段命令
     */
    @PostMapping("/executeP1Online")
    public Body<String> executeP1Online(@RequestBody Map<String, String> params) {
        String garnetDir = params.get("garnetDir");
        String port = params.get("port");
        String targetIp = params.get("targetIp");
        String dataDir = params.get("dataDir");
        String dataset = params.get("dataset");
        String topK = params.get("topK");

        // 拼接P1在线命令
        String cmd = String.format(
                "cd %s && ./ann-party.x 1 -pn %s -h %s -d %s -n %s -k %s",
                garnetDir, port, targetIp, dataDir, dataset, topK
        );
        return verdictService.executeLocalCommand(cmd, "P1在线阶段");
    }
}
