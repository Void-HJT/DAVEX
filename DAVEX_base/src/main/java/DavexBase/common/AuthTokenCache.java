package DavexBase.common;

import DavexBase.info.TokenResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthTokenCache {
    private final Map<String, TokenResult> authTokenMap = new ConcurrentHashMap<>();

    public TokenResult getToken(String KeycloakUrl) {
        return authTokenMap.get(KeycloakUrl);
    }

    public void putToken(String KeycloakUrl, TokenResult tokenResult) {
        authTokenMap.put(KeycloakUrl, tokenResult);
    }

    public void removeToken(String KeycloakUrl) {
        authTokenMap.remove(KeycloakUrl);
    }

    // 可选：如果需要，可以添加清空缓存的方法
    public void clearCache() {
        authTokenMap.clear();
    }
}
