package DavexCenter.goSdk.client;

import DavexCenter.goSdk.config.GoProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class GoClient {
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String baseUrl;
    private final Duration requestTimeout;

    public GoClient(ObjectMapper objectMapper, GoProperties properties) {
        this.objectMapper = objectMapper;
        this.baseUrl = normalizeBaseUrl(properties.getBaseUrl());
        this.requestTimeout = Duration.ofMillis(properties.getRequestTimeoutMs());
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                .build();
    }

    public JsonNode getHealth() {
        return get("/api/v1/health");
    }

    public JsonNode postEcho(JsonNode body) {
        return post("/api/v1/echo", body);
    }

    public JsonNode didGenerate() {
        return post("/api/v1/did/generate", objectMapper.createObjectNode());
    }

    public JsonNode didRegister(JsonNode body) {
        return post("/api/v1/did/register", body);
    }

    public JsonNode didQuery(String did) {
        String encoded = URLEncoder.encode(did, StandardCharsets.UTF_8);
        return get("/api/v1/did/query?did=" + encoded);
    }

    public JsonNode vcIssue(JsonNode body) {
        return post("/api/v1/vc/issue", body);
    }

    public JsonNode vcVerify(JsonNode body) {
        return post("/api/v1/vc/verify", body);
    }

    public JsonNode vpGenerate(JsonNode body) {
        return post("/api/v1/vp/generate", body);
    }

    public JsonNode vpVerify(JsonNode body) {
        return post("/api/v1/vp/verify", body);
    }

    public JsonNode privacyGroupCreate(JsonNode body) {
        return post("/api/v1/privacy/group/create", body);
    }

    public JsonNode privacyGroupMember(JsonNode body) {
        return post("/api/v1/privacy/group/member", body);
    }

    public JsonNode privacyGroupQuery(String groupId) {
        String encoded = URLEncoder.encode(groupId, StandardCharsets.UTF_8);
        return get("/api/v1/privacy/group?groupId=" + encoded);
    }

    public JsonNode privacyVPGenerate(JsonNode body) {
        return post("/api/v1/privacy/vp/generate", body);
    }

    public JsonNode privacyVPVerify(JsonNode body) {
        return post("/api/v1/privacy/vp/verify", body);
    }

    public JsonNode privacyVPVerifyLocal(JsonNode body) {
        return post("/api/v1/privacy/vp/verify-local", body);
    }

    public JsonNode privacyClaimsBuild(JsonNode body) {
        return post("/api/v1/privacy/claims/build", body);
    }

    public JsonNode privacyKeyImage(JsonNode body) {
        return post("/api/v1/privacy/keyimage", body);
    }

    public JsonNode privacyVerifyRequestCreate(JsonNode body) {
        return post("/api/v1/privacy/verify-request/create", body);
    }

    private JsonNode get(String path) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(requestTimeout)
                .GET()
                .build();
        return send(request);
    }

    private JsonNode post(String path, JsonNode body) {
        String json;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (IOException e) {
            throw new GoClientException("serialize request failed", e);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(requestTimeout)
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return send(request);
    }

    private JsonNode send(HttpRequest request) {
        HttpResponse<String> resp;
        try {
            resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new GoClientException("go backend unavailable: " + e.getMessage(), e);
        }

        try {
            return objectMapper.readTree(resp.body());
        } catch (IOException e) {
            throw new GoClientException("invalid json from go backend, status=" + resp.statusCode(), e);
        }
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "http://localhost:8081";
        }
        String s = baseUrl.trim();
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }
}
