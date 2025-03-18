package DavexCenter.common;

import DavexBase.entity.JwtMetadata;
import DavexCenter.mapper.JwtMetadataMapper;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String secret;       // 密钥（从配置读取）
    @Value("${jwt.expiration-hours}")
    private int expirationHours;

    @Autowired
    private JwtMetadataMapper jwtMetadataMapper;

    /**
     * 生成 JWT（包含元数据存储）
     */
    public Map<String, Object> generateToken(String agentUid) {
        // 1. 生成 JWT
        LocalDateTime now = LocalDateTime.now();
        String jti = UUID.randomUUID().toString(); // 唯一标识

        String token = JWT.create()
                .withJWTId(jti)
                .withSubject(agentUid)
                .withIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .withExpiresAt(Date.from(now.plusHours(expirationHours)
                        .atZone(ZoneId.systemDefault()).toInstant()))
                .sign(Algorithm.HMAC256(secret));

        // 2. 存储 JWT 元数据
        JwtMetadata metadata = new JwtMetadata();
        metadata.setUid(jti);
        metadata.setAgentUid(agentUid);
        metadata.setIssuedTime(now);
        metadata.setExpiresTime(now.plusHours(expirationHours));
        metadata.setRevoked(false);
        jwtMetadataMapper.insert(metadata);

        // 3. 返回 Token 和 Metadata
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("metadata", metadata);


        return result;
    }
    //
    public  String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[32]; // 256位
        random.nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }
    //
    // 验证 Token 签名和过期时间
    public DecodedJWT verifyToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token.replace("Bearer ", ""));
        } catch (Exception e) {
            throw new AuthException("Token verification failed");
        }
    }
    //
    /**
     * 从请求头中解析Token（去掉Bearer前缀）
     */
    public String resolveToken(HttpServletRequest request) {
        String Token = request.getHeader("Authorization");
        if (StringUtils.hasText(Token)) {
            return Token;
        }
        return null;
    }
    /**
     * 解析Token（不验证签名和过期时间）
     */
    public DecodedJWT decodeToken(String token) {
        try {
            return JWT.decode(token.replace("Bearer ", ""));
        } catch (JWTDecodeException e) {
            throw new AuthException("Token decoding failed");
        }
    }
    /**
     * 获取Token中的用户标识（Subject）
     */
    public String getAgentUidFromToken(String token) {
        return decodeToken(token).getSubject();
    }
    /**
     * 获取Token的唯一标识（JTI）
     */
    public String getJtiFromToken(String token) {
        return decodeToken(token).getId();
    }
    /**
     * 综合检查Token是否可用（验证签名 + 未过期 + 未吊销）
     */
    public boolean validateToken(String token) {
        try {
            // 1. 验证签名和过期时间（verifyToken内部已检查）
            DecodedJWT decodedJWT = verifyToken(token);

            // 2. 检查Token是否被主动吊销
            String jti = decodedJWT.getId();
            JwtMetadata metadata = jwtMetadataMapper.selectById(jti);
            if (metadata == null || metadata.isRevoked()) {
                return false; // Token已被吊销或元数据不存在
            }

            // 3. 全部验证通过
            return true;
        } catch (Exception e) {
            return false; // 签名无效、过期或其他异常
        }
    }



}