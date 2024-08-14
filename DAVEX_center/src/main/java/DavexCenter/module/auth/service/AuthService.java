package DavexCenter.module.auth.service;

import java.util.List;

import DavexBase.service.auth.CenterWebClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DavexBase.common.R;
import DavexBase.entity.Agent;
import DavexBase.entity.Center;
import DavexBase.mapper.AgentMapper;
import reactor.core.publisher.Flux;

@Service
public class AuthService {

    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    private CenterWebClientService webClientService;

    public void broacast(@NonNull Center me) {
        LambdaQueryWrapper<Agent> agentQuery = Wrappers.<Agent>lambdaQuery();
        List<Agent> agentList = agentMapper.selectList(agentQuery);
        try {
            Flux.fromIterable(agentList).flatMap((Agent a) -> {
                try {
                    return webClientService.center2AgentWebClient(a.getUid()).post().uri("/centers/update")
                            .bodyValue(me)
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
