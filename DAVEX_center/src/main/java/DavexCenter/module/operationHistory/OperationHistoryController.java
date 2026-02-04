package DavexCenter.module.operationHistory;

import DavexBase.common.Body;
import DavexCenter.entity.OperationHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/operationHistory")
public class OperationHistoryController {

    @Autowired
    private OperationHistoryService operationHistoryService;

    /**
     * 获取所有操作历史记录
     */
    @GetMapping("/list")
    public Body<List<OperationHistory>> getAllOperationHistory() {
        return operationHistoryService.getAllOperationHistory();
    }

    /**
     * 根据ID获取操作历史
     */
    @GetMapping("/{uid}")
    public Body<OperationHistory> getOperationHistoryById(@PathVariable Long uid) {
        return operationHistoryService.getOperationHistoryById(uid);
    }

    /**
     * 添加操作历史记录
     */
    @PostMapping("/add")
    public Body<OperationHistory> addOperationHistory(
            @RequestParam(required = false) String agentId,
            @RequestParam(required = false) String applicationId,
            @RequestParam String operationType,
            @RequestParam(required = false) String operationObject,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String remark) {
        return operationHistoryService.addOperationHistory(agentId, applicationId, operationType, operationObject, result, remark);
    }

    /**
     * 根据代理ID获取操作历史
     */
    @GetMapping("/byAgent/{agentId}")
    public Body<List<OperationHistory>> getOperationHistoryByAgentId(@PathVariable String agentId) {
        return operationHistoryService.getOperationHistoryByAgentId(agentId);
    }

    /**
     * 根据操作类型获取操作历史
     */
    @GetMapping("/byType/{operationType}")
    public Body<List<OperationHistory>> getOperationHistoryByType(@PathVariable String operationType) {
        return operationHistoryService.getOperationHistoryByType(operationType);
    }

    /**
     * 删除操作历史记录
     */
    @DeleteMapping("/{uid}")
    public Body<String> deleteOperationHistory(@PathVariable Long uid) {
        return operationHistoryService.deleteOperationHistory(uid);
    }

    /**
     * 清空所有操作历史记录
     */
    @DeleteMapping("/clear")
    public Body<String> clearAllOperationHistory() {
        return operationHistoryService.clearAllOperationHistory();
    }
}
