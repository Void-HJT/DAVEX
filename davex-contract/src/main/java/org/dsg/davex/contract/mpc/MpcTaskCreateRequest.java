package org.dsg.davex.contract.mpc;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Center、Agent 之间创建 MPC 任务时使用的 JSON 请求协议。
 *
 * 该对象只描述网络传输字段，不包含数据库注解、业务处理和文件操作。
 * 使用 record 保证协议对象创建后不可被意外修改。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MpcTaskCreateRequest(
        String uid,
        String applicationId,
        String centerId,
        String mpcId,
        Map<String, Object> compileParameters,
        Map<String, Object> runtimeParameters,
        Integer n,
        Long part,
        String host,
        Integer port,
        String dataId,
        TaskType taskType,
        Status status,
        List<PartInfo> partInfo) {

    /**
     * 当前支持的 MPC 任务类型。
     *
     * 枚举名称已经用于 JSON 协议，修改名称会造成接口不兼容。
     */
    public enum TaskType {
        GARNET_MPC,
        GARNET_PSI,
        GARNET_INFERENCE
    }

    /**
     * 当前 MPC 任务状态。
     *
     * 本阶段只迁移现有状态，不在这里设计新的任务状态机。
     */
    public enum Status {
        INIT,
        COMPILING,
        READY,
        RUNNING,
        FINISHED,
        FAILED
    }

    /**
     * 一个远端参与方及其输入文件信息。
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PartInfo(
            String agentID,
            Long part,
            String fileID) {
    }
}
