package DveAgent.module.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DveAgent.common.My;
import DveAgent.entity.Agent;
import DveAgent.entity.Center;
import DveAgent.mapper.AgentMapper;
import DveAgent.mapper.CenterMapper;
import reactor.netty.http.client.HttpClient;

@Service
public class AgentWebClientService {
        @Autowired
        private CenterMapper centerMapper;
        @Autowired
        private AgentMapper agentMapper;
        // @Autowired
        // private SslContextBuilder sslBuilder;

        @Autowired
        My my;

        public WebClient agent2CenterWebClient(long center_id) throws Exception {
                LambdaQueryWrapper<Center> queryWrapper = Wrappers.<Center>lambdaQuery().eq(Center::getUid, center_id);
                Center center = centerMapper.selectOne(queryWrapper);
                HttpClient httpClient = HttpClient.create();
                return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
                                .baseUrl("http://" + center.getIp() + ":" + center.getPort()).build();
        }

        public WebClient agent2AgentWebClient(long agent_id) throws Exception {
                LambdaQueryWrapper<Agent> queryWrapper = Wrappers.<Agent>lambdaQuery().eq(Agent::getUid, agent_id);
                Agent agent = agentMapper.selectOne(queryWrapper);
                HttpClient httpClient = HttpClient.create();
                return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
                                .baseUrl("http://" + agent.getIp() + ":" + agent.getPort())
                                // .filter(signRequest())
                                .build();
        }
}
