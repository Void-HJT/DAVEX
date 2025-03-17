package DavexAgent.module.auth.service;

import java.util.HashMap;
import java.util.List;

import DavexBase.common.JwtCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DavexBase.common.R;
import DavexBase.entity.Agent;
import DavexBase.entity.Center;
import DavexBase.mapper.AgentMapper;
import DavexBase.mapper.CenterMapper;
import DavexBase.service.auth.AgentWebClientService;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

@Service
public class AuthService {

    @Autowired
    private CenterMapper centerMapper;
    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    private AgentWebClientService webClientService;
    @Autowired
    private JwtCache jwtCache;

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

    //获取jwt
    public boolean getJwt(String centerId, String agentId, String password) {
        Agent agent = new Agent();
        agent.setUid(agentId);
        agent.setPassword(password);

        try {
            String token = webClientService.agent2CenterWebClient(centerId).post()
                    .uri("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(agent)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (StringUtils.hasText(token)) {
                jwtCache.putToken(centerId, token);
                return true;
            }
            return false;
        } catch (WebClientResponseException e) {
            System.err.println("HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return false;
        }
    }
    //检查jwt
    public boolean checkJwt(String centerId){
        String token = jwtCache.getToken(centerId);
        if (!StringUtils.hasText(token)) {
            return false;
        }
        //
        // 远程严格验证
        try {
            webClientService.agent2CenterWebClient(centerId).post()
                    .uri("/auth/checkJWT")
                    .header("Authorization", token)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (Exception e) {
            System.err.println("JWT检查请求失败"+e.getMessage());
            return false;
        }

    }
    //刷新jwt
    public boolean refreshJwt(String centerId){
        String token = jwtCache.getToken(centerId);
        if (!StringUtils.hasText(token)) {
            return false;
        }
        //访问
        // 远程验证
        try {
            // 发送刷新请求并获取新token
            String newToken = webClientService.agent2CenterWebClient(centerId).post()
                    .uri("/auth/refreshJWT")
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(String.class) // 注意这里改为String类型
                    .block();

            // 保存新token到本地缓存
            if (StringUtils.hasText(newToken)) {
                jwtCache.putToken(centerId, newToken);
                System.out.println("center[{}]令牌刷新成功" + centerId);
                return true;
            }
        } catch (Exception e) {
            System.err.println("JWT刷新请求失败"+e.getMessage());
            return false;
        }
        return false;
    }
    //删去jwt
    public  boolean revokeJwt(String centerId){
        String token = jwtCache.getToken(centerId);
        if (!StringUtils.hasText(token)) {
            return false;
        }
        //访问
        try {
            webClientService.agent2CenterWebClient(centerId).post()
                    .uri("/auth/revokeJWT")
                    .header("Authorization", token)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            System.err.println("JWT远程吊销失败，强制清除本地缓存"+e.getMessage());
        } finally {
            jwtCache.removeToken(centerId); // 无论远程是否成功都清除本地
        }
        return true;
    }
    //展示所有的token
    public java.util.Set<java.util.Map.Entry<String, String>> getAllJwt(){
        return jwtCache.getAllEntries();
    }
}
