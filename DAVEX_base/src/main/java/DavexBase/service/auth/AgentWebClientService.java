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
import DavexBase.entity.Center;
import DavexBase.mapper.AgentMapper;
import DavexBase.mapper.CenterMapper;
import reactor.netty.http.client.HttpClient;

@Service
public class AgentWebClientService {

        private CenterMapper centerMapper;

        private AgentMapper agentMapper;

        private ExchangeStrategies strategies;

        public AgentWebClientService(ObjectMapper objectMapper, AgentMapper agentMapper, CenterMapper centerMapper) {
                this.centerMapper = centerMapper;
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

        public WebClient agent2CenterWebClient(String center_id) throws Exception {
                LambdaQueryWrapper<Center> queryWrapper = Wrappers.<Center>lambdaQuery().eq(Center::getUid, center_id);
                Center center = centerMapper.selectOne(queryWrapper);
                HttpClient httpClient = HttpClient.create();
                return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
                                .baseUrl("http://" + center.getIp() + ":" + center.getPort())
                                .exchangeStrategies(strategies)
                                .build();
        }

        public WebClient agent2AgentWebClient(String agent_id) throws Exception {
                LambdaQueryWrapper<Agent> queryWrapper = Wrappers.<Agent>lambdaQuery().eq(Agent::getUid, agent_id);
                Agent agent = agentMapper.selectOne(queryWrapper);
                HttpClient httpClient = HttpClient.create();
                return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
                                .baseUrl("http://" + agent.getIp() + ":" + agent.getPort())
                                .exchangeStrategies(strategies)
                                .build();
        }
}
