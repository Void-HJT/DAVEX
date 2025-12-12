package DavexCenter.module.verdict;

import DavexBase.common.Body;
import DavexBase.common.My;
import DavexBase.info.VerdictFilterDTO;
import DavexBase.service.auth.CenterWebClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class VerdictService {
    private static final Logger logger = LoggerFactory.getLogger(VerdictService.class);
    private static final String PKL_SAVE_DIR = "output"; // 本地保存目录（相对于my.core_path）
    private static final String PKL_FILE_NAME_PREFIX = "center_vectorizer_"; // 本地文件前缀（避免重名）

    @Autowired
    private My my;
    @Autowired
    private CenterWebClientService centerWebClientService;

    /**
     * Center端转发查询到Agent端，接收文件流并保存到本地
     * @param agentId 目标AgentID
     * @param filterDTO 筛选条件
     * @return Body<String>：code=1成功（data=Center本地路径），code≠1失败
     */
    public Body<String> sendQuery(String agentId, VerdictFilterDTO filterDTO) {
        try {
            logger.info("Center端开始转发查询条件到Agent端，AgentID：{}，筛选条件：{}", agentId, filterDTO);

            // 1. 校验核心参数
            if (agentId == null || agentId.trim().isEmpty()) {
                logger.error("Center端发送查询失败：目标AgentId未配置");
                return Body.error("目标Agent未配置，无法发起预处理请求");
            }

            // 2. 获取Center→Agent通信WebClient
            WebClient webClient = centerWebClientService.center2AgentWebClient(agentId);
            if (webClient == null) {
                logger.error("Center端发送查询失败：创建Agent通信客户端失败，agentId={}", agentId);
                return Body.error("创建Agent通信连接失败，请检查Agent状态");
            }

            // 3. 调用Agent端接口，接收响应（包含文件流和响应头）
            // 用Mono接收完整响应（响应体+响应头），而非直接解析为Body<String>
            WebClient.ResponseSpec responseSpec = webClient.post()
                    .uri("/verdict/query2Embeddings")
                    .bodyValue(filterDTO)
                    .accept(MediaType.APPLICATION_OCTET_STREAM) // 声明接收二进制流
                    .retrieve();

            // 4. 解析响应头（获取状态码、Agent端文件信息）和响应体（文件流）
            Mono<Body<String>> resultMono = responseSpec.toEntity(Resource.class)
                    .map(responseEntity -> {
                        HttpHeaders headers = responseEntity.getHeaders();
                        Resource fileResource = responseEntity.getBody();

                        // 4.1 解析自定义响应头（X-Code：状态码，X-Message：文件路径/错误信息）
                        String codeStr = headers.getFirst("X-Code");
                        String message = headers.getFirst("X-Message");
                        // 解码中文（避免响应头乱码）
                        if (message != null) {
                            message = decodeURIComponent(message);
                        }
                        int code = (codeStr != null && !codeStr.isEmpty()) ? Integer.parseInt(codeStr) : 0;

                        // 4.2 处理失败场景（状态码≠1或无文件流）
                        if (code != 1 || fileResource == null || !fileResource.exists()) {
                            String errorMsg = message != null ? message : "Agent端预处理失败，未返回有效文件";
                            logger.error("Center端接收Agent文件失败：{}，AgentID：{}", errorMsg, agentId);
                            return Body.error("Agent端预处理失败：" + errorMsg);
                        }

                        // 4.3 处理成功场景：保存文件到Center本地目录
                        try {
                            // 4.3.1 构建本地保存目录（my.core_path + "/output"）
                            String corePath = my.getCore_path();
                            if (corePath == null || corePath.trim().isEmpty()) {
                                throw new RuntimeException("Center端核心路径未配置，无法保存文件");
                            }
                            java.io.File saveDir = new java.io.File(corePath, PKL_SAVE_DIR);
                            // 确保目录存在（不存在则创建）
                            if (!saveDir.exists()) {
                                boolean mkdirsSuccess = saveDir.mkdirs();
                                if (!mkdirsSuccess) {
                                    throw new RuntimeException("创建本地保存目录失败：" + saveDir.getAbsolutePath());
                                }
                            }

                            // 4.3.2 构建本地文件名（添加时间戳避免重名）
                            String timeSuffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                            String fileName = PKL_FILE_NAME_PREFIX + timeSuffix + ".pkl";
                            java.io.File localFile = new java.io.File(saveDir, fileName);

                            // 4.3.3 保存文件流到本地
                            try (OutputStream outputStream = new FileOutputStream(localFile)) {
                                FileCopyUtils.copy(fileResource.getInputStream(), outputStream);
                            }

                            // 4.3.4 构建Center端本地绝对路径（返回给前端）
                            String localFilePath = localFile.getAbsolutePath();
                            logger.info("Center端成功保存Agent文件到本地：{}，Agent端原路径：{}，AgentID：{}",
                                    localFilePath, message, agentId);
                            return Body.success(localFilePath, "Agent端目标集合预处理成功，文件已保存到Center：" + localFilePath);

                        } catch (Exception e) {
                            logger.error("Center端保存Agent文件到本地失败，AgentID：{}", agentId, e);
                            return Body.error("Center端保存文件失败：" + e.getMessage());
                        }
                    });

            // 阻塞获取结果（同步返回给前端）
            return resultMono.block();

        } catch (Exception e) {
            logger.error("Center端转发查询到Agent端时发生系统异常，AgentID：{}", agentId, e);
            return Body.error("跨Center-Agent通信异常：" + e.getMessage());
        }
    }

    /**
     * 解码URL编码的字符串（对应前端encodeURIComponent）
     * 解决响应头中文乱码问题
     */
    private String decodeURIComponent(String encodedStr) {
        try {
            return java.net.URLDecoder.decode(encodedStr, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            logger.warn("解码字符串失败：{}", encodedStr, e);
            return encodedStr; // 解码失败则返回原字符串
        }
    }
}
