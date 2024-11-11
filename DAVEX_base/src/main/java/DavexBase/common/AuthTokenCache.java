package DavexBase.common;

import DavexBase.info.TokenResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthTokenCache {
    private final Map<String, TokenResult> authTokenMap = new ConcurrentHashMap<>();

    public TokenResult getToken(String authId) {
        return authTokenMap.get(authId);
    }

    public void putToken(String authId, TokenResult tokenResult) {
        authTokenMap.put(authId, tokenResult);
    }

    public void removeToken(String authId) {
        authTokenMap.remove(authId);
    }

    // 可选：如果需要，可以添加清空缓存的方法
    public void clearCache() {
        authTokenMap.clear();
    }
}
