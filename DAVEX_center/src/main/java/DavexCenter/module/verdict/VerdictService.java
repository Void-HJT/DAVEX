package DavexCenter.module.verdict;

import DavexBase.common.Body;
import DavexBase.common.My;
import DavexBase.info.VerdictFilterDTO;
import DavexBase.service.auth.CenterWebClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class VerdictService {
    private static final Logger logger = LoggerFactory.getLogger(VerdictService.class);

    @Autowired
    private My my;
    @Autowired
    private CenterWebClientService centerWebClientService;

    public Body<String> sendQuery(String agentId, VerdictFilterDTO filterDTO) {
        try {
            logger.info("Center端开始转发查询条件到Agent端，筛选条件：{}", filterDTO);

            if (agentId == null || agentId.trim().isEmpty()) {
                logger.error("Center端发送查询失败：目标AgentId未配置");
                return Body.error("目标Agent未配置，无法发起预处理请求");
            }

            WebClient webClient = centerWebClientService.center2AgentWebClient(agentId);
            Body<String> agentResponse = webClient.post()
                    .uri("/verdict/query2Embeddings") // Agent端目标接口路径
                    .bodyValue(filterDTO) // 透传筛选条件（JSON格式）
                    .retrieve() // 接收响应
                    // 解析Agent端返回的Body<String>格式（与系统统一返回格式一致）
                    .bodyToMono(new ParameterizedTypeReference<Body<String>>() {})
                    .block(); // 同步阻塞等待结果（Center端接口需同步返回，适配RESTful调用）

            // 5. 透传Agent端结果（成功/失败状态均返回给前端）
            if (agentResponse.getCode() == 1) {
                logger.info("Center端发送查询成功：Agent端预处理完成，pkl文件路径={}", agentResponse.getData());
                return Body.success(agentResponse.getData(), "Agent端目标集合预处理成功：" + agentResponse.getMessage());
            } else {
                logger.error("Center端发送查询失败：Agent端处理异常，错误信息={}", agentResponse.getMessage());
                return Body.error("Agent端预处理失败：" + agentResponse.getMessage());
            }

        } catch (Exception e) {
            logger.error("Center端转发查询到Agent端时发生系统异常", e);
            return Body.error("跨Center-Agent通信异常：" + e.getMessage());
        }
    }
}
