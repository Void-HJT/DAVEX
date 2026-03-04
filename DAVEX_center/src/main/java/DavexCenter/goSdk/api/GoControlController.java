package DavexCenter.goSdk.api;

import DavexCenter.goSdk.client.GoClient;
import DavexCenter.goSdk.client.GoClientException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/v1/control")
public class GoControlController {
    private final GoClient goClient;
    private final ObjectMapper objectMapper;

    public GoControlController(GoClient goClient, ObjectMapper objectMapper) {
        this.goClient = goClient;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/go-health")
    public ApiResponse<Map<String, Object>> goHealth() {
        String traceId = UUID.randomUUID().toString();
        Map<String, Object> data = new HashMap<>();
        data.put("javaTraceId", traceId);
        data.put("javaTime", Instant.now().toString());

        try {
            JsonNode go = goClient.getHealth();
            data.put("go", go);
            data.put("java", Map.of("service", "davex-center-go-sdk"));
            return ApiResponse.ok(data);
        } catch (GoClientException e) {
            return ApiResponse.fail(30001, e.getMessage());
        }
    }

    @PostMapping("/echo")
    public ApiResponse<JsonNode> echo(@RequestBody(required = false) JsonNode body) {
        return forwardWithTrace(() -> goClient.postEcho(nonNullBody(body)));
    }

    @PostMapping("/did/generate")
    public ApiResponse<JsonNode> didGenerate() {
        return forwardWithTrace(goClient::didGenerate);
    }

    @PostMapping("/did/register")
    public ApiResponse<JsonNode> didRegister(@RequestBody(required = false) JsonNode body) {
        return forwardWithTrace(() -> goClient.didRegister(nonNullBody(body)));
    }

    @GetMapping("/did/query")
    public ApiResponse<JsonNode> didQuery(@RequestParam("did") String did) {
        return forwardWithTrace(() -> goClient.didQuery(did));
    }

    @PostMapping("/vc/issue")
    public ApiResponse<JsonNode> vcIssue(@RequestBody(required = false) JsonNode body) {
        return forwardWithTrace(() -> goClient.vcIssue(nonNullBody(body)));
    }

    @PostMapping("/vc/verify")
    public ApiResponse<JsonNode> vcVerify(@RequestBody(required = false) JsonNode body) {
        return forwardWithTrace(() -> goClient.vcVerify(nonNullBody(body)));
    }

    @PostMapping("/vp/generate")
    public ApiResponse<JsonNode> vpGenerate(@RequestBody(required = false) JsonNode body) {
        return forwardWithTrace(() -> goClient.vpGenerate(nonNullBody(body)));
    }

    @PostMapping("/vp/verify")
    public ApiResponse<JsonNode> vpVerify(@RequestBody(required = false) JsonNode body) {
        return forwardWithTrace(() -> goClient.vpVerify(nonNullBody(body)));
    }

    @PostMapping("/privacy/group/create")
    public ApiResponse<JsonNode> privacyGroupCreate(@RequestBody(required = false) JsonNode body) {
        return forwardWithTrace(() -> goClient.privacyGroupCreate(nonNullBody(body)));
    }

    @PostMapping("/privacy/group/member")
    public ApiResponse<JsonNode> privacyGroupMember(@RequestBody(required = false) JsonNode body) {
        return forwardWithTrace(() -> goClient.privacyGroupMember(nonNullBody(body)));
    }

    @GetMapping("/privacy/group")
    public ApiResponse<JsonNode> privacyGroupQuery(@RequestParam("groupId") String groupId) {
        return forwardWithTrace(() -> goClient.privacyGroupQuery(groupId));
    }

    @PostMapping("/privacy/vp/generate")
    public ApiResponse<JsonNode> privacyVpGenerate(@RequestBody(required = false) JsonNode body) {
        return forwardWithTrace(() -> goClient.privacyVPGenerate(nonNullBody(body)));
    }

    @PostMapping("/privacy/vp/verify")
    public ApiResponse<JsonNode> privacyVpVerify(@RequestBody(required = false) JsonNode body) {
        return forwardWithTrace(() -> goClient.privacyVPVerify(nonNullBody(body)));
    }

    @PostMapping("/privacy/vp/verify-local")
    public ApiResponse<JsonNode> privacyVpVerifyLocal(@RequestBody(required = false) JsonNode body) {
        return forwardWithTrace(() -> goClient.privacyVPVerifyLocal(nonNullBody(body)));
    }

    @PostMapping("/privacy/claims/build")
    public ApiResponse<JsonNode> privacyClaimsBuild(@RequestBody(required = false) JsonNode body) {
        return forwardWithTrace(() -> goClient.privacyClaimsBuild(nonNullBody(body)));
    }

    @PostMapping("/privacy/keyimage")
    public ApiResponse<JsonNode> privacyKeyImage(@RequestBody(required = false) JsonNode body) {
        return forwardWithTrace(() -> goClient.privacyKeyImage(nonNullBody(body)));
    }

    @PostMapping("/privacy/verify-request/create")
    public ApiResponse<JsonNode> privacyVerifyRequestCreate(@RequestBody(required = false) JsonNode body) {
        return forwardWithTrace(() -> goClient.privacyVerifyRequestCreate(nonNullBody(body)));
    }

    private ApiResponse<JsonNode> forwardWithTrace(Supplier<JsonNode> request) {
        String traceId = UUID.randomUUID().toString();
        try {
            JsonNode goResp = request.get();
            return enrichGoResponseWithJavaMeta(goResp, traceId);
        } catch (GoClientException e) {
            return ApiResponse.fail(30001, e.getMessage());
        }
    }

    private JsonNode nonNullBody(JsonNode body) {
        return body == null ? objectMapper.createObjectNode() : body;
    }

    private ApiResponse<JsonNode> enrichGoResponseWithJavaMeta(JsonNode goResp, String traceId) {
        if (goResp != null && goResp.isObject()) {
            int code = goResp.path("code").asInt(0);
            String message = goResp.path("message").asText("ok");
            JsonNode goData = goResp.get("data");

            ObjectNode data;
            if (goData != null && goData.isObject()) {
                data = ((ObjectNode) goData).deepCopy();
            } else {
                data = objectMapper.createObjectNode();
                data.set("goData", goData == null ? objectMapper.nullNode() : goData);
            }
            data.put("javaTraceId", traceId);
            data.put("javaTime", Instant.now().toString());

            return new ApiResponse<>(code, message, data);
        }

        ObjectNode data = objectMapper.createObjectNode();
        data.put("javaTraceId", traceId);
        data.put("javaTime", Instant.now().toString());
        data.set("goRaw", goResp);
        return new ApiResponse<>(0, "ok", data);
    }
}
