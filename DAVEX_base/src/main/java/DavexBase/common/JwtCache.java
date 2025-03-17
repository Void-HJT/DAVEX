package DavexBase.common;

import DavexBase.info.TokenResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtCache {
    private final Map<String, String> authTokenMap = new ConcurrentHashMap<>();

    public String getToken(String centerId) {
        return authTokenMap.get(centerId);
    }

    public void putToken(String centerId, String jwt) {
        authTokenMap.put(centerId, jwt);
    }

    public void removeToken(String centerId) {
        authTokenMap.remove(centerId);
    }

    public Set<Map.Entry<String, String>> getAllEntries() {
        return authTokenMap.entrySet();
    }

    // 可选：如果需要，可以添加清空缓存的方法
    public void clearCache() {
        authTokenMap.clear();
    }
}
