package DavexCenter.module.operationHistory;

import DavexBase.body.Body;
import DavexCenter.entity.OperationHistory;
import DavexCenter.mapper.OperationHistoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class OperationHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(OperationHistoryService.class);

    @Autowired
    private OperationHistoryMapper operationHistoryMapper;

    /**
     * 获取所有操作历史记录（按时间倒序）
     */
    public Body<List<OperationHistory>> getAllOperationHistory() {
        try {
            LambdaQueryWrapper<OperationHistory> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(OperationHistory::getTime);
            List<OperationHistory> historyList = operationHistoryMapper.selectList(wrapper);
            return Body.success(historyList, "获取操作历史成功");
        } catch (Exception e) {
            logger.error("获取操作历史失败", e);
            return Body.error("获取操作历史失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取操作历史
     */
    public Body<OperationHistory> getOperationHistoryById(Long uid) {
        try {
            OperationHistory history = operationHistoryMapper.selectById(uid);
            if (history != null) {
                return Body.success(history, "获取操作历史成功");
            } else {
                return Body.error("操作历史不存在");
            }
        } catch (Exception e) {
            logger.error("获取操作历史失败，ID：{}", uid, e);
            return Body.error("获取操作历史失败：" + e.getMessage());
        }
    }

    /**
     * 添加操作历史记录
     */
    public Body<OperationHistory> addOperationHistory(String agentId, String applicationId, 
            String operationType, String operationObject, String result, String remark) {
        try {
            OperationHistory history = new OperationHistory();
            history.setAgentId(agentId);
            history.setApplicationId(applicationId);
            history.setTime(new Timestamp(System.currentTimeMillis()));
            history.setOperationType(operationType);
            history.setOperationObject(operationObject);
            history.setResult(result);
            history.setRemark(remark);

            operationHistoryMapper.insert(history);
            logger.info("添加操作历史成功，类型：{}，对象：{}", operationType, operationObject);
            return Body.success(history, "添加操作历史成功");
        } catch (Exception e) {
            logger.error("添加操作历史失败", e);
            return Body.error("添加操作历史失败：" + e.getMessage());
        }
    }

    /**
     * 记录操作历史（内部方法，供其他Service调用）
     */
    public void recordOperation(String agentId, String applicationId, String operationType, 
            String operationObject, String result, String remark) {
        try {
            OperationHistory history = new OperationHistory();
            history.setAgentId(agentId);
            history.setApplicationId(applicationId);
            history.setTime(new Timestamp(System.currentTimeMillis()));
            history.setOperationType(operationType);
            history.setOperationObject(operationObject);
            history.setResult(result);
            history.setRemark(remark);

            operationHistoryMapper.insert(history);
            logger.info("记录操作历史：类型={}，对象={}，结果={}", operationType, operationObject, result);
        } catch (Exception e) {
            logger.error("记录操作历史失败", e);
        }
    }

    /**
     * 根据代理ID获取操作历史
     */
    public Body<List<OperationHistory>> getOperationHistoryByAgentId(String agentId) {
        try {
            LambdaQueryWrapper<OperationHistory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OperationHistory::getAgentId, agentId)
                   .orderByDesc(OperationHistory::getTime);
            List<OperationHistory> historyList = operationHistoryMapper.selectList(wrapper);
            return Body.success(historyList, "获取操作历史成功");
        } catch (Exception e) {
            logger.error("根据代理ID获取操作历史失败，agentId：{}", agentId, e);
            return Body.error("获取操作历史失败：" + e.getMessage());
        }
    }

    /**
     * 根据操作类型获取操作历史
     */
    public Body<List<OperationHistory>> getOperationHistoryByType(String operationType) {
        try {
            LambdaQueryWrapper<OperationHistory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OperationHistory::getOperationType, operationType)
                   .orderByDesc(OperationHistory::getTime);
            List<OperationHistory> historyList = operationHistoryMapper.selectList(wrapper);
            return Body.success(historyList, "获取操作历史成功");
        } catch (Exception e) {
            logger.error("根据操作类型获取操作历史失败，operationType：{}", operationType, e);
            return Body.error("获取操作历史失败：" + e.getMessage());
        }
    }

    /**
     * 删除操作历史记录
     */
    public Body<String> deleteOperationHistory(Long uid) {
        try {
            int result = operationHistoryMapper.deleteById(uid);
            if (result > 0) {
                return Body.success("删除成功", "删除操作历史成功");
            } else {
                return Body.error("操作历史不存在");
            }
        } catch (Exception e) {
            logger.error("删除操作历史失败，ID：{}", uid, e);
            return Body.error("删除操作历史失败：" + e.getMessage());
        }
    }

    /**
     * 清空所有操作历史记录
     */
    public Body<String> clearAllOperationHistory() {
        try {
            operationHistoryMapper.delete(null);
            return Body.success("清空成功", "清空所有操作历史成功");
        } catch (Exception e) {
            logger.error("清空操作历史失败", e);
            return Body.error("清空操作历史失败：" + e.getMessage());
        }
    }
}
