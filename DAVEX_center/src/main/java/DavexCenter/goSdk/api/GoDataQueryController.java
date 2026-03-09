package DavexCenter.goSdk.api;

import DavexCenter.goSdk.entity.GoDidRecord;
import DavexCenter.goSdk.entity.GoPrivacyGroupRecord;
import DavexCenter.goSdk.entity.GoPrivacyVpRecord;
import DavexCenter.goSdk.entity.GoVcRecord;
import DavexCenter.goSdk.service.GoRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/control/data")
public class GoDataQueryController {

    private final GoRecordService goRecordService;

    public GoDataQueryController(GoRecordService goRecordService) {
        this.goRecordService = goRecordService;
    }

    @GetMapping("/dids")
    public ApiResponse<Map<String, Object>> listDidRecords() {
        List<GoDidRecord> items = goRecordService.listDidRecords();
        return ApiResponse.ok(buildListPayload(items));
    }

    @GetMapping("/vcs")
    public ApiResponse<Map<String, Object>> listVcRecords(
            @RequestParam(value = "holderDid", required = false) String holderDid,
            @RequestParam(value = "issuerDid", required = false) String issuerDid) {
        List<GoVcRecord> items = goRecordService.listVcRecords(holderDid, issuerDid);
        return ApiResponse.ok(buildListPayload(items));
    }

    @GetMapping("/groups")
    public ApiResponse<Map<String, Object>> listGroupRecords(
            @RequestParam(value = "groupId", required = false) String groupId) {
        List<GoPrivacyGroupRecord> items = goRecordService.listGroupRecords(groupId);
        return ApiResponse.ok(buildListPayload(items));
    }

    @GetMapping("/privacy-vps")
    public ApiResponse<Map<String, Object>> listPrivacyVpRecords(
            @RequestParam(value = "groupId", required = false) String groupId,
            @RequestParam(value = "holderKeyImage", required = false) String holderKeyImage) {
        List<GoPrivacyVpRecord> items = goRecordService.listPrivacyVpRecords(groupId, holderKeyImage);
        return ApiResponse.ok(buildListPayload(items));
    }

    private Map<String, Object> buildListPayload(List<?> items) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("total", items == null ? 0 : items.size());
        payload.put("items", items);
        return payload;
    }
}
