package DavexBase.common;

import DavexBase.entity.JwtMetadata;
import DavexBase.info.TokenResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtCache {
    private final Map<String, String> authTokenMap = new ConcurrentHashMap<>();
    private final Map<String, JwtMetadata> authMetaMap = new ConcurrentHashMap<>();

    public String getToken(String centerId) {
        return authTokenMap.get(centerId);
    }
    public JwtMetadata getMeta(String centerId) {
        return authMetaMap.get(centerId);
    }

    public void putToken(String centerId, String jwt) {
        authTokenMap.put(centerId, jwt);
    }
    public void putMeta(String centerId, JwtMetadata jwtMetadata) {
        authMetaMap.put(centerId, jwtMetadata);
    }

    public void removeToken(String centerId) {
        authTokenMap.remove(centerId);
    }
    public void removeMeta(String centerId) {
        authMetaMap.remove(centerId);
    }

    public Set<Map.Entry<String, String>> getAllTokenEntries() {
        return authTokenMap.entrySet();
    }
    public Set<Map.Entry<String, JwtMetadata>> getAllMetaEntries() {
        return authMetaMap.entrySet();
    }
    // 可选：如果需要，可以添加清空缓存的方法
    public void clearTokenCache() {
        authTokenMap.clear();
    }
    public void clearMetaCache() {
        authMetaMap.clear();
    }
}
