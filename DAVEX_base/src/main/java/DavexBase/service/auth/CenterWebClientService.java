package DavexBase.service.auth;

import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;

import DavexBase.entity.Agent;
import DavexBase.mapper.AgentMapper;
import reactor.netty.http.client.HttpClient;

@Service
public class CenterWebClientService {

    private AgentMapper agentMapper;

    private ExchangeStrategies strategies;


    public CenterWebClientService(ObjectMapper objectMapper, AgentMapper agentMapper) {
        this.agentMapper = agentMapper;
        strategies = ExchangeStrategies
                .builder()
                .codecs(clientDefaultCodecsConfigurer -> {
                    clientDefaultCodecsConfigurer.defaultCodecs()
                            .jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper,
                                    MediaType.APPLICATION_JSON));
                    clientDefaultCodecsConfigurer.defaultCodecs()
                            .jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper,
                                    MediaType.APPLICATION_JSON));

                }).build();
    }

    public WebClient center2AgentWebClient(String agent_id) throws Exception {
        LambdaQueryWrapper<Agent> queryWrapper = Wrappers.<Agent>lambdaQuery().eq(Agent::getUid, agent_id);
        Agent agent = agentMapper.selectOne(queryWrapper);
        HttpClient httpClient = HttpClient.create();
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl("http://" + agent.getIp() + ":" + agent.getPort())
                .exchangeStrategies(strategies)
                .build();
    }

    public WebClient center2AgentWebClientCustomize(String agentId) {
        // 1. 自定义缓冲区大小（根据实际文件大小调整，比如设置为100MB）
        int bufferSize = 100 * 1024 * 1024; // 100MB
        ExchangeStrategies customizeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    // 增大默认的响应体缓存限制
                    configurer.defaultCodecs().maxInMemorySize(bufferSize);
                    // 禁用默认的DataBuffer缓存，改用流式传输
                    configurer.defaultCodecs().enableLoggingRequestDetails(true);
                })
                .build();

        LambdaQueryWrapper<Agent> queryWrapper = Wrappers.<Agent>lambdaQuery().eq(Agent::getUid, agentId);
        Agent agent = agentMapper.selectOne(queryWrapper);
        HttpClient httpClient = HttpClient.create();

        // 3. 创建WebClient并应用配置
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl("http://" + agent.getIp() + ":" + agent.getPort())
                .exchangeStrategies(customizeStrategies)
                .build();
    }

//    public WebClient center2AgentWebClientInAuth(String agent_id,String username,String password,String authId) throws Exception {
//
//        //判断是否有token
//        LambdaQueryWrapper<Keycloak> queryWrapperKeycloak = Wrappers.lambdaQuery(Keycloak.class).eq(Keycloak::getAuthenticationId, agent_id);
//        Keycloak keycloak = keycloakMapper.selectOne(queryWrapperKeycloak);
//        if (keycloak == null) {
//            throw new RuntimeException("Invalid Authentication ID");
//        }
//
//        TokenResult tokenResult = authTokenCache.getToken(keycloak.getServerUrl());
//
//        if(tokenResult==null){
//            //没有token就申请token
//            tokenResult = tokenValidationService.getToken(username,password,agent_id);
//        }
//        else
//        {
//            //验证该token是否过期
//            String isExpired = tokenValidationService.checkTokenStatus(agent_id);
//            if(!isExpired.equals("Token is valid")){
//                if (isExpired.equals("Token expired but Refresh Token is valid"))
//                {
//                    //如果refreshToken没过期则更新
//                    try {
//                        tokenValidationService.updateToken(agent_id);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                else if(isExpired.equals("Token expired and Refresh Token is invalid"))
//                {
//                    //如果过期重新申请
//                    tokenResult = tokenValidationService.getToken(username,password,agent_id);
//                }
//                else
//                {
//                    throw new RuntimeException(isExpired);
//                }
//            }
//        }
//        //这时token已存在并未过期
//        LambdaQueryWrapper<Agent> queryWrapper = Wrappers.<Agent>lambdaQuery().eq(Agent::getUid, agent_id);
//        Agent agent = agentMapper.selectOne(queryWrapper);
//        HttpClient httpClient = HttpClient.create();
//        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
//                .baseUrl("http://" + agent.getIp() + ":" + agent.getPort())
//                .defaultHeader("Authentication-ID", authId)
//                .defaultHeader("Token", tokenResult.getAccessToken())
//                .exchangeStrategies(strategies)
//                .build();
//    }
}
