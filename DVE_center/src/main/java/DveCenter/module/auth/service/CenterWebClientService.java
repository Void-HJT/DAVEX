package DveCenter.module.auth.service;

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

import DveAgent.entity.Agent;
import DveAgent.mapper.AgentMapper;
import reactor.netty.http.client.HttpClient;

@Service
public class CenterWebClientService {

        private AgentMapper agentMapper;
        // @Autowired
        // private SslContextBuilder sslBuilder;

        // @Autowired
        // private My my;

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

        public WebClient center2AgentWebClient(long agent_id) throws Exception {
                LambdaQueryWrapper<Agent> queryWrapper = Wrappers.<Agent>lambdaQuery().eq(Agent::getUid, agent_id);
                Agent agent = agentMapper.selectOne(queryWrapper);
                HttpClient httpClient = HttpClient.create();
                return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
                                .baseUrl("http://" + agent.getIp() + ":" + agent.getPort())
                                .exchangeStrategies(strategies)
                                .build();
        }
}
