package DveAgent.module.auth.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DveAgent.common.R;
import DveAgent.entity.Agent;
import DveAgent.entity.Center;
import DveAgent.mapper.AgentMapper;
import DveAgent.mapper.CenterMapper;
import reactor.core.publisher.Flux;

@Service
public class AuthService {

    @Autowired
    private CenterMapper centerMapper;
    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    private AgentWebClientService webClientService;

    public void broacast(@NonNull Agent me) {
        LambdaQueryWrapper<Agent> agentQuery = Wrappers.<Agent>lambdaQuery().ne(Agent::getUid, me.getUid());
        List<Agent> agentList = agentMapper.selectList(agentQuery);
        try {
            Flux.fromIterable(agentList).flatMap((Agent a) -> {
                try {
                    return webClientService.agent2AgentWebClient(a.getUid()).post().uri("/agents/update").bodyValue(me)
                            .retrieve().bodyToMono(new ParameterizedTypeReference<R<String>>() {
                            });
                } catch (Exception e) {
                    return null;
                }
            }).collectList().block();
        } catch (Exception e) {
            e.printStackTrace();
        }

        LambdaQueryWrapper<Center> centerQuery = Wrappers.<Center>lambdaQuery();
        List<Center> centerList = centerMapper.selectList(centerQuery);

        try {
            Flux.fromIterable(centerList).flatMap((Center c) -> {
                try {
                    return webClientService.agent2CenterWebClient(c.getUid()).post().uri("/agents/update").bodyValue(me)
                            .retrieve().bodyToMono(new ParameterizedTypeReference<R<String>>() {
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }).collectList().block();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
