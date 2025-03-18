package DavexCenter.module.auth.service;

import java.util.List;
import java.util.Map;

import DavexBase.service.auth.CenterWebClientService;
import DavexCenter.common.AuthException;
import DavexCenter.common.JwtUtils;
import DavexBase.entity.JwtMetadata;
import DavexCenter.mapper.JwtMetadataMapper;
import com.auth0.jwt.interfaces.DecodedJWT;
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
    private JwtUtils jwtUtils;
    @Autowired
    private JwtMetadataMapper jwtMetadataMapper;
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

    /**
     * Agent 登录并返回 JWT
     */
    public Map<String,Object> login(String agentUid, String password) {
        // 1. 验证 Agent 身份
        Agent agent = agentMapper.selectById(agentUid);
        if (agent == null || !password.equals(agent.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        // 2. 生成 JWT
        return jwtUtils.generateToken(agentUid);
    }

    public String getSecret(){
        return jwtUtils.generateSecret();
    }
    //
    // 验证 Token 有效性（包含吊销检查）
    public boolean validateToken(String token) {
        try {
            DecodedJWT jwt = jwtUtils.verifyToken(token);
            JwtMetadata metadata = jwtMetadataMapper.selectById(jwt.getId());
            return metadata != null && !metadata.isRevoked();
        } catch (Exception e) {
            return false;
        }
    }

    // 刷新 Token
    public Map<String, Object> refreshToken(String oldToken) {
        DecodedJWT oldJwt = jwtUtils.verifyToken(oldToken);
        if (isTokenRevoked(oldJwt.getId())) {
            throw new AuthException("Token is revoked");
        }
        revokeToken(oldToken); // 吊销旧 Token
        return jwtUtils.generateToken(oldJwt.getSubject());
    }

    // 吊销 Token
    public void revokeToken(String token) {
        try {
            DecodedJWT jwt = jwtUtils.verifyToken(token);
            JwtMetadata metadata = jwtMetadataMapper.selectById(jwt.getId());
            if (metadata != null&&!metadata.isRevoked()) {
                metadata.setRevoked(true);
                jwtMetadataMapper.updateById(metadata);
            }
        } catch (Exception e) {
            throw new AuthException("Invalid token");
        }
    }


    public boolean deleteAllRevokedToken(){
        LambdaQueryWrapper<JwtMetadata> queryWrapper = new LambdaQueryWrapper<JwtMetadata>()
                .eq(JwtMetadata::isRevoked, true);
        int deletedRows = jwtMetadataMapper.delete(queryWrapper);
        return deletedRows > 0; // 返回是否成功删除了记录
    }
    //

    private boolean isTokenRevoked(String jti) {
        JwtMetadata metadata = jwtMetadataMapper.selectById(jti);
        return metadata != null && metadata.isRevoked();
    }

}
