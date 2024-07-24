package DveCenter.module.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DveCenter.common.My;
import DveAgent.entity.Agent;
import DveAgent.mapper.AgentMapper;
import reactor.netty.http.client.HttpClient;

@Service
public class CenterWebClientService {
        @Autowired
        private AgentMapper agentMapper;

        @Autowired
        My my;

        public WebClient center2AgentWebClient(long agent_id) throws Exception {
                LambdaQueryWrapper<Agent> queryWrapper = Wrappers.<Agent>lambdaQuery().eq(Agent::getUid, agent_id);
                Agent agent = agentMapper.selectOne(queryWrapper);
                HttpClient httpClient = HttpClient.create();
                return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
                                .baseUrl("http://" + agent.getIp() + ":" + agent.getPort()).build();
        }
}
