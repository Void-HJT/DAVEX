package DavexCenter.goSdk.api;

import DavexCenter.goSdk.client.GoClient;
import DavexCenter.goSdk.client.GoClientException;
import DavexCenter.goSdk.service.GoRecordService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.function.Consumer;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/v1/control")
public class GoControlController {
    private static final Logger logger = LoggerFactory.getLogger(GoControlController.class);

    private final GoClient goClient;
    private final ObjectMapper objectMapper;
    private final GoRecordService goRecordService;

    public GoControlController(GoClient goClient, ObjectMapper objectMapper, GoRecordService goRecordService) {
        this.goClient = goClient;
        this.objectMapper = objectMapper;
        this.goRecordService = goRecordService;
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
        JsonNode requestBody = nonNullBody(body);
        return forwardWithTrace(() -> goClient.postEcho(requestBody));
    }

    @PostMapping("/did/generate")
    public ApiResponse<JsonNode> didGenerate() {
        return forwardWithTrace(goClient::didGenerate);
    }

    @PostMapping("/did/register")
    public ApiResponse<JsonNode> didRegister(@RequestBody(required = false) JsonNode body) {
        JsonNode requestBody = nonNullBody(body);
        return forwardWithTrace(
                () -> goClient.didRegister(requestBody),
                goResp -> goRecordService.saveDidRegistration(requestBody, goResp.path("data"))
        );
    }

    @GetMapping("/did/query")
    public ApiResponse<JsonNode> didQuery(@RequestParam("did") String did) {
        return forwardWithTrace(
                () -> goClient.didQuery(did),
                goResp -> goRecordService.upsertDidFromQuery(did, goResp.path("data"))
        );
    }

    @PostMapping("/vc/issue")
    public ApiResponse<JsonNode> vcIssue(@RequestBody(required = false) JsonNode body) {
        JsonNode requestBody = nonNullBody(body);
        return forwardWithTrace(
                () -> goClient.vcIssue(requestBody),
                goResp -> goRecordService.saveVcIssue(goResp.path("data"))
        );
    }

    @PostMapping("/vc/verify")
    public ApiResponse<JsonNode> vcVerify(@RequestBody(required = false) JsonNode body) {
        JsonNode requestBody = nonNullBody(body);
        return forwardWithTrace(
                () -> goClient.vcVerify(requestBody),
                goResp -> goRecordService.saveVcVerify(requestBody, goResp.path("data"))
        );
    }

    @PostMapping("/vp/generate")
    public ApiResponse<JsonNode> vpGenerate(@RequestBody(required = false) JsonNode body) {
        JsonNode requestBody = nonNullBody(body);
        return forwardWithTrace(() -> goClient.vpGenerate(requestBody));
    }

    @PostMapping("/vp/verify")
    public ApiResponse<JsonNode> vpVerify(@RequestBody(required = false) JsonNode body) {
        JsonNode requestBody = nonNullBody(body);
        return forwardWithTrace(() -> goClient.vpVerify(requestBody));
    }

    @PostMapping("/privacy/group/create")
    public ApiResponse<JsonNode> privacyGroupCreate(@RequestBody(required = false) JsonNode body) {
        JsonNode requestBody = nonNullBody(body);
        return forwardWithTrace(
                () -> goClient.privacyGroupCreate(requestBody),
                goResp -> goRecordService.saveGroupCreate(requestBody, goResp.path("data"))
        );
    }

    @PostMapping("/privacy/group/member")
    public ApiResponse<JsonNode> privacyGroupMember(@RequestBody(required = false) JsonNode body) {
        JsonNode requestBody = nonNullBody(body);
        return forwardWithTrace(
                () -> goClient.privacyGroupMember(requestBody),
                goResp -> goRecordService.saveGroupMember(requestBody, goResp.path("data"))
        );
    }

    @GetMapping("/privacy/group")
    public ApiResponse<JsonNode> privacyGroupQuery(@RequestParam("groupId") String groupId) {
        return forwardWithTrace(
                () -> goClient.privacyGroupQuery(groupId),
                goResp -> goRecordService.upsertGroupFromQuery(goResp.path("data"))
        );
    }

    @PostMapping("/privacy/vp/generate")
    public ApiResponse<JsonNode> privacyVpGenerate(@RequestBody(required = false) JsonNode body) {
        JsonNode requestBody = nonNullBody(body);
        return forwardWithTrace(
                () -> goClient.privacyVPGenerate(requestBody),
                goResp -> goRecordService.savePrivacyVpGenerate(requestBody, goResp.path("data"))
        );
    }

    @PostMapping("/privacy/vp/verify")
    public ApiResponse<JsonNode> privacyVpVerify(@RequestBody(required = false) JsonNode body) {
        JsonNode requestBody = nonNullBody(body);
        return forwardWithTrace(
                () -> goClient.privacyVPVerify(requestBody),
                goResp -> goRecordService.savePrivacyVpVerify(requestBody, goResp.path("data"), false)
        );
    }

    @PostMapping("/privacy/vp/verify-local")
    public ApiResponse<JsonNode> privacyVpVerifyLocal(@RequestBody(required = false) JsonNode body) {
        JsonNode requestBody = nonNullBody(body);
        return forwardWithTrace(
                () -> goClient.privacyVPVerifyLocal(requestBody),
                goResp -> goRecordService.savePrivacyVpVerify(requestBody, goResp.path("data"), true)
        );
    }

    @PostMapping("/privacy/claims/build")
    public ApiResponse<JsonNode> privacyClaimsBuild(@RequestBody(required = false) JsonNode body) {
        JsonNode requestBody = nonNullBody(body);
        return forwardWithTrace(() -> goClient.privacyClaimsBuild(requestBody));
    }

    @PostMapping("/privacy/keyimage")
    public ApiResponse<JsonNode> privacyKeyImage(@RequestBody(required = false) JsonNode body) {
        JsonNode requestBody = nonNullBody(body);
        return forwardWithTrace(() -> goClient.privacyKeyImage(requestBody));
    }

    @PostMapping("/privacy/verify-request/create")
    public ApiResponse<JsonNode> privacyVerifyRequestCreate(@RequestBody(required = false) JsonNode body) {
        JsonNode requestBody = nonNullBody(body);
        return forwardWithTrace(() -> goClient.privacyVerifyRequestCreate(requestBody));
    }

    private ApiResponse<JsonNode> forwardWithTrace(Supplier<JsonNode> request) {
        return forwardWithTrace(request, null);
    }

    private ApiResponse<JsonNode> forwardWithTrace(Supplier<JsonNode> request, Consumer<JsonNode> onGoSuccess) {
        String traceId = UUID.randomUUID().toString();
        try {
            JsonNode goResp = request.get();
            if (onGoSuccess != null && isGoSuccess(goResp)) {
                try {
                    onGoSuccess.accept(goResp);
                } catch (Exception e) {
                    logger.warn("persist go record failed: {}", e.getMessage());
                }
            }
            return enrichGoResponseWithJavaMeta(goResp, traceId);
        } catch (GoClientException e) {
            return ApiResponse.fail(30001, e.getMessage());
        }
    }

    private JsonNode nonNullBody(JsonNode body) {
        return body == null ? objectMapper.createObjectNode() : body;
    }

    private boolean isGoSuccess(JsonNode goResp) {
        return goResp != null && goResp.isObject() && goResp.path("code").asInt(-1) == 0;
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
