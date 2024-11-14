package DavexBase.service.auth;

import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import DavexBase.common.AuthTokenCache;
import DavexBase.common.R;
import DavexBase.entity.Keycloak;
import DavexBase.entity.KeycloakCredentials;
import DavexBase.info.TokenResult;
import DavexBase.mapper.KeycloakCredentialsMapper;
import DavexBase.mapper.KeycloakMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.security.KeyFactory;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;


@Service
public class TokenValidationService {

    @Autowired
    AuthTokenCache authTokenCache;

    @Autowired
    private KeycloakMapper keycloakMapper;

    @Autowired
    private KeycloakCredentialsMapper keycloakCredentialsMapper;


    public boolean validateToken(String authenticationId, String token) {

        LambdaQueryWrapper<Keycloak> keycloakLambdaQueryWrapper = Wrappers.<Keycloak>lambdaQuery().eq(Keycloak::getAuthenticationId, authenticationId);
        Keycloak keycloak = keycloakMapper.selectOne(keycloakLambdaQueryWrapper);

        LambdaQueryWrapper<KeycloakCredentials> keycloakCredentialsLambdaQueryWrapper = Wrappers.<KeycloakCredentials>lambdaQuery()
                .eq(KeycloakCredentials::getTargetId, authenticationId);
        KeycloakCredentials keycloakCredentials = keycloakCredentialsMapper.selectOne(keycloakCredentialsLambdaQueryWrapper);

        if (keycloak == null) {
            throw new RuntimeException("Invalid Authentication ID");
        }

        if(keycloakCredentials == null){
            throw new RuntimeException("Invalid Public Key");
        }

        // 通过公钥验证Token，并提取Claims
        String publicKeyPem = keycloakCredentials.getPublicKey();
        try {
            byte[] decodedKey = Base64.getDecoder().decode(publicKeyPem);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(keySpec);

            // 解析JWT，提取claims
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            //String userIdInToken = claims.getSubject();
            // 假设用户名存储在preferred_username字段中
            String usernameInToken = claims.get("preferred_username", String.class);
//            System.err.println("TokenSub: " + userIdInToken);
//            System.err.println("TokenUsername: " + usernameInToken);
            
            if (!authenticationId.equalsIgnoreCase(usernameInToken)) {
                throw new RuntimeException("TokenId does not match authId : " + " TokenId："+authenticationId+" authId："+usernameInToken);
                //return false; // 用户ID不匹配的情况
            }
        } catch (Exception e) {
            throw new RuntimeException("Token parsing failed: " + e.getMessage());
        }


         // 使用Keycloak的OpenID Connect token验证端点手动进行验证
        String introspectionUrl = keycloak.getServerUrl() + "/realms/" + keycloak.getRealm() + "/protocol/openid-connect/token/introspect";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(keycloak.getClientId(), keycloak.getClientSecret());

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("token", token);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(introspectionUrl, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Boolean active = (Boolean) response.getBody().get("active");
            return active != null && active;
        }

        return false;
    }

    public TokenResult getToken(String username, String password, String authenticationId) {
        LambdaQueryWrapper<Keycloak> queryWrapper = Wrappers.<Keycloak>lambdaQuery().eq(Keycloak::getAuthenticationId, authenticationId);
        Keycloak keycloak = keycloakMapper.selectOne(queryWrapper);
        if(keycloak==null){throw new RuntimeException("no authId");}
        // 使用Keycloak的OpenID Connect token验证端点手动进行验证
        String tokenUrl = keycloak.getServerUrl() + "/realms/" + keycloak.getRealm() + "/protocol/openid-connect/token";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", keycloak.getClientId());
        formData.add("client_secret", keycloak.getClientSecret());
        formData.add("username", username);
        formData.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("access_token")) {
                String accessToken = (String) responseBody.get("access_token");
                String refreshToken = (String) responseBody.get("refresh_token");
                TokenResult tokenResult = new TokenResult(accessToken, refreshToken);
                authTokenCache.putToken(keycloak.getServerUrl(),tokenResult);
                return tokenResult;
            }
        }

        throw new RuntimeException("Failed to retrieve token from Keycloak");
    }


    public boolean isTokenExpired(String authId) {

        // 查询数据库获取 Keycloak 信息
        LambdaQueryWrapper<Keycloak> queryWrapper = Wrappers.lambdaQuery(Keycloak.class).eq(Keycloak::getAuthenticationId, authId);
        Keycloak keycloak = keycloakMapper.selectOne(queryWrapper);

        if (keycloak == null) {
            throw new RuntimeException("Invalid Authentication ID");
        }

        // 从缓存中获取 Token
        TokenResult tokenResult = authTokenCache.getToken(keycloak.getServerUrl());

        // 如果缓存中没有 Token
        if (tokenResult == null) {
            throw new RuntimeException("No Token");
        }

        // 使用 Introspection Endpoint 验证 Token 是否有效
        String introspectionUrl = keycloak.getServerUrl() + "/realms/" + keycloak.getRealm() + "/protocol/openid-connect/token/introspect";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(keycloak.getClientId(), keycloak.getClientSecret());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("token", tokenResult.getAccessToken());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(introspectionUrl, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            Boolean active = (Boolean) responseBody.get("active");
            return active == null || !active; // 返回 true 表示 Token 已过期或无效
        }

        // 如果请求失败，视为 Token 无效
        return true;
    }

    public void updatePublicKey(String username, String password, String targetId) throws Exception {

        LambdaQueryWrapper<Keycloak> queryWrapper = Wrappers.lambdaQuery(Keycloak.class).eq(Keycloak::getAuthenticationId, targetId);
        Keycloak keycloak = keycloakMapper.selectOne(queryWrapper);
        if (keycloak == null) {
            throw new RuntimeException("no authId");
        }

        // 获取Access Token
        TokenResult tokenResult = getToken(username, password, targetId);
        String accessToken = tokenResult.getAccessToken();

        // 请求JWKS端点
        String jwksUrl = keycloak.getServerUrl() + "/realms/" + keycloak.getRealm() + "/protocol/openid-connect/certs";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(jwksUrl, HttpMethod.GET, entity, Map.class);

        List<Map<String, Object>> keys = (List<Map<String, Object>>) response.getBody().get("keys");
        if (keys.isEmpty()) {
            throw new RuntimeException("No keys found in JWKS");
        }

        // 筛选出`RS256`算法且`use`为`SIG`的密钥
        Map<String, Object> signingKey = keys.stream()
                .filter(key -> "RS256".equals(key.get("alg")) && "sig".equals(key.get("use")))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No RS256 signing key found in JWKS"));

        List<String> x5cList = (List<String>) signingKey.get("x5c");
        if (x5cList == null || x5cList.isEmpty()) {
            throw new RuntimeException("No certificate chain (x5c) found in key");
        }

        // 提取并解析第一个证书
        String cert = x5cList.get(0);
        byte[] decoded = Base64.getDecoder().decode(cert);

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate x509Cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(decoded));
        PublicKey publicKey = x509Cert.getPublicKey();

        // 将公钥存储到数据库
        KeycloakCredentials credentials = new KeycloakCredentials();
        credentials.setTargetId(targetId);
        credentials.setPublicKey(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        if(keycloakCredentialsMapper.selectById(targetId)==null){
            keycloakCredentialsMapper.insert(credentials);
        }
        else {
            keycloakCredentialsMapper.updateById(credentials);
        }

        // 登出以销毁session
        logout(keycloak.getServerUrl(), keycloak.getRealm(), keycloak.getClientId(), tokenResult.getRefreshToken(), keycloak.getClientSecret());
    }


    private void logout(String keycloakServerUrl, String realm, String clientId, String refreshToken,String clientSecret) {
        String logoutUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/logout";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret); // 添加client_secret，这很重要
        formData.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        try {
            ResponseEntity<Void> response = restTemplate.exchange(logoutUrl, HttpMethod.POST, request, Void.class);
            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                System.out.println("Successfully logged out. No additional content returned.");
            } else if (response.getStatusCode() != HttpStatus.OK) {
                System.err.println("Unexpected response status: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            System.err.println("Failed to log out session: " + e.getMessage());
            }
    }

}
